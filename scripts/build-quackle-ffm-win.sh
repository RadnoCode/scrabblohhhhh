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

real_path() {
  local path="$1"
  if command -v realpath >/dev/null 2>&1; then
    realpath "${path}"
    return 0
  fi

  local dir base target
  dir="$(cd "$(dirname "${path}")" && pwd -P)"
  base="$(basename "${path}")"
  while [[ -L "${dir}/${base}" ]]; do
    target="$(readlink "${dir}/${base}")"
    if [[ "${target}" = /* ]]; then
      path="${target}"
    else
      path="${dir}/${target}"
    fi
    dir="$(cd "$(dirname "${path}")" && pwd -P)"
    base="$(basename "${path}")"
  done
  printf '%s/%s\n' "${dir}" "${base}"
}

is_windows_cmake() {
  [[ "$(basename "${CMAKE_BIN}")" == *.exe ]]
}

to_cmake_path() {
  local value="$1"
  if is_windows_cmake && command -v wslpath >/dev/null 2>&1; then
    wslpath -m "${value}"
    return 0
  fi
  printf '%s\n' "${value}"
}

fail_missing_tool() {
  local message="$1"
  echo "${message}" >&2
  if [[ "$(uname -s)" == "Darwin" ]]; then
    echo "On macOS, install the cross toolchain with: brew install cmake mingw-w64 ninja" >&2
  fi
  exit 1
}

find_runtime_dll() {
  local dll_name="$1"
  local tool_bin_dir
  local real_tool_bin_dir
  local root
  local candidate
  local found

  tool_bin_dir="$(dirname "${MINGW_CXX}")"
  real_tool_bin_dir="$(dirname "$(real_path "${MINGW_CXX}")")"

  local search_roots=(
    "${MINGW_BIN_DIR}"
    "${MINGW_REAL_BIN_DIR}"
    "${tool_bin_dir}"
    "${real_tool_bin_dir}"
    "${MINGW_REAL_BIN_DIR}/.."
    "${real_tool_bin_dir}/.."
    "${MINGW_REAL_BIN_DIR}/../${MINGW_TRIPLE}"
    "${real_tool_bin_dir}/../${MINGW_TRIPLE}"
  )

  for root in "${search_roots[@]}"; do
    [[ -n "${root}" && -d "${root}" ]] || continue

    for candidate in \
      "${root}/${dll_name}" \
      "${root}/bin/${dll_name}" \
      "${root}/lib/${dll_name}" \
      "${root}/${MINGW_TRIPLE}/bin/${dll_name}" \
      "${root}/${MINGW_TRIPLE}/lib/${dll_name}"; do
      if [[ -f "${candidate}" ]]; then
        printf '%s\n' "${candidate}"
        return 0
      fi
    done

    found="$(find "${root}" -maxdepth 6 -type f -name "${dll_name}" -print -quit 2>/dev/null || true)"
    if [[ -n "${found}" ]]; then
      printf '%s\n' "${found}"
      return 0
    fi
  done

  return 1
}

copy_runtime_dll() {
  local dll_name="$1"
  local dll_path

  if ! runtime_dll_imported "${dll_name}"; then
    return 0
  fi

  dll_path="$(find_runtime_dll "${dll_name}" || true)"
  if [[ -n "${dll_path}" ]]; then
    cp "${dll_path}" "${OUTPUT_DIR}/${dll_name}"
    cp "${dll_path}" "${PROJECT_ROOT}/target/native/${dll_name}"
  fi
}

collect_dll_imports() {
  local objdump_bin

  objdump_bin="${OBJDUMP_BIN:-}"
  if [[ -z "${objdump_bin}" ]]; then
    objdump_bin="$(resolve_tool "${MINGW_TRIPLE}-objdump" "${MINGW_TRIPLE}-objdump.exe" objdump objdump.exe llvm-objdump llvm-objdump.exe || true)"
  fi

  if [[ -z "${objdump_bin}" ]]; then
    return 1
  fi

  "${objdump_bin}" -p "${BUILT_DLL}" 2>/dev/null \
    | sed -n 's/.*DLL Name: //p' \
    | tr '[:upper:]' '[:lower:]'
}

runtime_dll_imported() {
  local dll_name="$1"
  local lower_name

  if [[ -z "${DLL_IMPORTS:-}" ]]; then
    return 0
  fi

  lower_name="$(printf '%s' "${dll_name}" | tr '[:upper:]' '[:lower:]')"
  printf '%s\n' "${DLL_IMPORTS}" | grep -Fxq "${lower_name}"
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
QUACKLE_ROOT="${QUACKLE_ROOT:-${PROJECT_ROOT}/quackle-master}"
BUILD_DIR="${QUACKLE_ROOT}/build/windows-x86_64"
OUTPUT_DIR="${PROJECT_ROOT}/native"
TARGET_DLL="${OUTPUT_DIR}/quackle_ffm.dll"
MINGW_TRIPLE="${MINGW_TRIPLE:-x86_64-w64-mingw32}"

CMAKE_BIN="${CMAKE_BIN:-}"
CMAKE_GENERATOR="${CMAKE_GENERATOR:-}"
MINGW_CC="${MINGW_CC:-${LLVM_CC:-${CC:-}}}"
MINGW_CXX="${MINGW_CXX:-${LLVM_CXX:-${CXX:-}}}"
BUILD_TOOL="${BUILD_TOOL:-${MINGW_MAKE:-}}"
MINGW_RC="${MINGW_RC:-}"
MINGW_BIN_DIR="${MINGW_BIN_DIR:-}"

if [[ -z "${CMAKE_BIN}" ]]; then
  CMAKE_BIN="$(resolve_tool cmake cmake.exe || true)"
fi
if [[ -z "${CMAKE_BIN}" ]]; then
  fail_missing_tool "Missing CMake. Set CMAKE_BIN or install cmake."
fi

if [[ -z "${CMAKE_GENERATOR}" ]]; then
  if is_windows_cmake; then
    CMAKE_GENERATOR="MinGW Makefiles"
  elif resolve_tool ninja >/dev/null 2>&1; then
    CMAKE_GENERATOR="Ninja"
  else
    CMAKE_GENERATOR="Unix Makefiles"
  fi
fi

if [[ -z "${MINGW_CC}" ]]; then
  if is_windows_cmake; then
    MINGW_CC="$(resolve_tool clang clang.exe "${MINGW_TRIPLE}-clang" "${MINGW_TRIPLE}-clang.exe" "${MINGW_TRIPLE}-gcc" "${MINGW_TRIPLE}-gcc.exe" || true)"
  else
    MINGW_CC="$(resolve_tool "${MINGW_TRIPLE}-gcc" "${MINGW_TRIPLE}-clang" clang || true)"
  fi
fi

if [[ -z "${MINGW_CXX}" ]]; then
  if is_windows_cmake; then
    MINGW_CXX="$(resolve_tool clang++ clang++.exe "${MINGW_TRIPLE}-clang++" "${MINGW_TRIPLE}-clang++.exe" "${MINGW_TRIPLE}-g++" "${MINGW_TRIPLE}-g++.exe" || true)"
  else
    MINGW_CXX="$(resolve_tool "${MINGW_TRIPLE}-g++" "${MINGW_TRIPLE}-clang++" clang++ || true)"
  fi
fi

if [[ -z "${MINGW_CC}" || -z "${MINGW_CXX}" ]]; then
  fail_missing_tool "Missing Windows cross compiler. Set MINGW_CC and MINGW_CXX, or install MinGW-w64."
fi

if [[ -z "${BUILD_TOOL}" ]]; then
  case "${CMAKE_GENERATOR}" in
    "MinGW Makefiles")
      BUILD_TOOL="$(resolve_tool mingw32-make mingw32-make.exe || true)"
      ;;
    *Ninja*)
      BUILD_TOOL="$(resolve_tool ninja ninja.exe || true)"
      ;;
    *)
      BUILD_TOOL="$(resolve_tool make gmake || true)"
      ;;
  esac
fi

if [[ -z "${BUILD_TOOL}" ]]; then
  fail_missing_tool "Missing build tool for CMake generator '${CMAKE_GENERATOR}'. Set BUILD_TOOL or MINGW_MAKE."
fi

if [[ -z "${MINGW_RC}" ]]; then
  MINGW_RC="$(resolve_tool "${MINGW_TRIPLE}-windres" "${MINGW_TRIPLE}-windres.exe" windres windres.exe || true)"
fi

if [[ -z "${MINGW_RC}" ]] && ! is_windows_cmake; then
  fail_missing_tool "Missing MinGW resource compiler. Set MINGW_RC or install windres from MinGW-w64."
fi

if [[ -z "${MINGW_BIN_DIR}" ]]; then
  if [[ -n "${MINGW_RC}" ]]; then
    MINGW_BIN_DIR="$(dirname "${MINGW_RC}")"
  else
    MINGW_BIN_DIR="$(dirname "${MINGW_CXX}")"
  fi
fi

MINGW_REAL_BIN_DIR="$(cd "${MINGW_BIN_DIR}" && pwd -P)"
HOST_QUACKLE_ROOT="$(to_cmake_path "${QUACKLE_ROOT}")"
HOST_BUILD_DIR="$(to_cmake_path "${BUILD_DIR}")"
HOST_BUILD_TOOL="$(to_cmake_path "${BUILD_TOOL}")"
HOST_MINGW_CC="$(to_cmake_path "${MINGW_CC}")"
HOST_MINGW_CXX="$(to_cmake_path "${MINGW_CXX}")"

CMAKE_ARGS=(
  --fresh
  -G "${CMAKE_GENERATOR}"
  -S "${HOST_QUACKLE_ROOT}"
  -B "${HOST_BUILD_DIR}"
  -DCMAKE_MAKE_PROGRAM="${HOST_BUILD_TOOL}"
  -DCMAKE_SYSTEM_NAME=Windows
  -DCMAKE_SYSTEM_PROCESSOR=x86_64
  -DCMAKE_BUILD_TYPE=Release
  -DCMAKE_C_COMPILER="${HOST_MINGW_CC}"
  -DCMAKE_CXX_COMPILER="${HOST_MINGW_CXX}"
)

if [[ -n "${MINGW_RC}" ]]; then
  CMAKE_ARGS+=(-DCMAKE_RC_COMPILER="$(to_cmake_path "${MINGW_RC}")")
fi

if ! is_windows_cmake && [[ "$(basename "${MINGW_CC}")" == clang* ]]; then
  CMAKE_ARGS+=(
    -DCMAKE_C_COMPILER_TARGET="${MINGW_TRIPLE}"
    -DCMAKE_CXX_COMPILER_TARGET="${MINGW_TRIPLE}"
  )
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

DLL_IMPORTS="$(collect_dll_imports || true)"
copy_runtime_dll "libwinpthread-1.dll"
copy_runtime_dll "libstdc++-6.dll"
copy_runtime_dll "libgcc_s_seh-1.dll"
copy_runtime_dll "libgcc_s_dw2-1.dll"

echo "Configured Windows native build with CMake (${CMAKE_BIN}), generator '${CMAKE_GENERATOR}', and compiler (${MINGW_CXX})"
echo "Built Windows DLL at ${TARGET_DLL}"
