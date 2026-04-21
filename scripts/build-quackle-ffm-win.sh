#!/usr/bin/env bash
set -euo pipefail

resolve_tool() {
  local candidate
  for candidate in "$@"; do
    if [[ -n "${candidate}" ]] && command -v "${candidate}" >/dev/null 2>&1; then
      command -v "${candidate}"
      return 0
    fi
  done
  return 1
}

to_cmake_path() {
  local value="$1"
  if [[ "${CMAKE_BIN}" == *.exe ]] && command -v wslpath >/dev/null 2>&1; then
    wslpath -m "${value}"
    return 0
  fi
  printf '%s\n' "${value}"
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
QUACKLE_ROOT="${QUACKLE_ROOT:-${PROJECT_ROOT}/quackle-master}"
BUILD_DIR="${QUACKLE_ROOT}/build/windows-x86_64"
OUTPUT_DIR="${PROJECT_ROOT}/native"
TARGET_DLL="${OUTPUT_DIR}/quackle_ffm.dll"
MINGW_TRIPLE="${MINGW_TRIPLE:-x86_64-w64-mingw32}"

# Clang handles compilation while MinGW supplies make, windres, and libwinpthread.
CMAKE_BIN="${CMAKE_BIN:-}"
LLVM_CC="${LLVM_CC:-${CC:-}}"
LLVM_CXX="${LLVM_CXX:-${CXX:-}}"
MINGW_MAKE="${MINGW_MAKE:-}"
MINGW_RC="${MINGW_RC:-}"
MINGW_BIN_DIR="${MINGW_BIN_DIR:-}"

if [[ -z "${CMAKE_BIN}" ]]; then
  CMAKE_BIN="$(resolve_tool cmake cmake.exe || true)"
fi

if [[ -z "${LLVM_CC}" ]]; then
  LLVM_CC="$(resolve_tool clang clang.exe "${MINGW_TRIPLE}-clang" "${MINGW_TRIPLE}-clang.exe" || true)"
fi

if [[ -z "${LLVM_CXX}" ]]; then
  LLVM_CXX="$(resolve_tool clang++ clang++.exe "${MINGW_TRIPLE}-clang++" "${MINGW_TRIPLE}-clang++.exe" || true)"
fi

if [[ -z "${MINGW_MAKE}" ]]; then
  MINGW_MAKE="$(resolve_tool mingw32-make mingw32-make.exe || true)"
fi

if [[ -z "${MINGW_RC}" ]]; then
  MINGW_RC="$(resolve_tool "${MINGW_TRIPLE}-windres" "${MINGW_TRIPLE}-windres.exe" windres windres.exe || true)"
fi

if [[ -z "${LLVM_CC}" || -z "${LLVM_CXX}" ]]; then
  echo "Missing Clang toolchain. Set LLVM_CC and LLVM_CXX, or install clang/clang++." >&2
  exit 1
fi

if [[ -z "${CMAKE_BIN}" ]]; then
  echo "Missing CMake. Set CMAKE_BIN or install cmake." >&2
  exit 1
fi

if [[ -z "${MINGW_MAKE}" ]]; then
  echo "Missing MinGW make tool. Set MINGW_MAKE or install mingw32-make." >&2
  exit 1
fi

if [[ -z "${MINGW_BIN_DIR}" ]]; then
  if [[ -n "${MINGW_RC}" ]]; then
    MINGW_BIN_DIR="$(dirname "${MINGW_RC}")"
  else
    MINGW_BIN_DIR="$(dirname "${MINGW_MAKE}")"
  fi
fi

MINGW_REAL_BIN_DIR="$(cd "${MINGW_BIN_DIR}" && pwd -P)"
HOST_QUACKLE_ROOT="$(to_cmake_path "${QUACKLE_ROOT}")"
HOST_BUILD_DIR="$(to_cmake_path "${BUILD_DIR}")"
HOST_MINGW_MAKE="$(to_cmake_path "${MINGW_MAKE}")"
HOST_LLVM_CC="$(to_cmake_path "${LLVM_CC}")"
HOST_LLVM_CXX="$(to_cmake_path "${LLVM_CXX}")"

CMAKE_ARGS=(
  --fresh
  -G "MinGW Makefiles"
  -S "${HOST_QUACKLE_ROOT}"
  -B "${HOST_BUILD_DIR}"
  -DCMAKE_MAKE_PROGRAM="${HOST_MINGW_MAKE}"
  -DCMAKE_SYSTEM_NAME=Windows
  -DCMAKE_BUILD_TYPE=Release
  -DCMAKE_C_COMPILER="${HOST_LLVM_CC}"
  -DCMAKE_CXX_COMPILER="${HOST_LLVM_CXX}"
)

if [[ -n "${MINGW_RC}" ]]; then
  CMAKE_ARGS+=(-DCMAKE_RC_COMPILER="$(to_cmake_path "${MINGW_RC}")")
fi

"${CMAKE_BIN}" "${CMAKE_ARGS[@]}"

"${CMAKE_BIN}" --build "${HOST_BUILD_DIR}" --target quackle_ffm -j

BUILT_DLL="${BUILD_DIR}/quackle_ffm.dll"
if [[ ! -f "${BUILT_DLL}" ]]; then
  BUILT_DLL="${BUILD_DIR}/libquackle_ffm.dll"
fi

if [[ ! -f "${BUILT_DLL}" ]]; then
  echo "Unable to find built Windows DLL in ${BUILD_DIR}." >&2
  exit 1
fi

mkdir -p "${OUTPUT_DIR}" "${PROJECT_ROOT}/target/native"
cp "${BUILT_DLL}" "${TARGET_DLL}"
cp "${BUILT_DLL}" "${PROJECT_ROOT}/target/native/quackle_ffm.dll"

WINPTHREAD_DLL="${MINGW_BIN_DIR}/libwinpthread-1.dll"
if [[ ! -f "${WINPTHREAD_DLL}" ]]; then
  WINPTHREAD_DLL="${MINGW_REAL_BIN_DIR}/libwinpthread-1.dll"
fi

if [[ ! -f "${WINPTHREAD_DLL}" ]]; then
  WINPTHREAD_DLL="${MINGW_REAL_BIN_DIR}/../${MINGW_TRIPLE}/bin/libwinpthread-1.dll"
fi

if [[ -f "${WINPTHREAD_DLL}" ]]; then
  cp "${WINPTHREAD_DLL}" "${OUTPUT_DIR}/libwinpthread-1.dll"
  cp "${WINPTHREAD_DLL}" "${PROJECT_ROOT}/target/native/libwinpthread-1.dll"
fi

echo "Configured Windows native build with CMake (${CMAKE_BIN}), Clang (${LLVM_CXX}), and MinGW make (${MINGW_MAKE})"
echo "Built Windows DLL at ${TARGET_DLL}"
