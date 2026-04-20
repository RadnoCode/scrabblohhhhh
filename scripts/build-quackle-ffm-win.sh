#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
QUACKLE_ROOT="${QUACKLE_ROOT:-${PROJECT_ROOT}/quackle-master}"
BUILD_DIR="${QUACKLE_ROOT}/build/windows-x86_64"
OUTPUT_DIR="${PROJECT_ROOT}/native"
TARGET_DLL="${OUTPUT_DIR}/quackle_ffm.dll"
MINGW_TRIPLE="${MINGW_TRIPLE:-x86_64-w64-mingw32}"
MINGW_CC="${MINGW_CC:-$(command -v "${MINGW_TRIPLE}-gcc" 2>/dev/null || true)}"
MINGW_CXX="${MINGW_CXX:-$(command -v "${MINGW_TRIPLE}-g++" 2>/dev/null || true)}"
MINGW_RC="${MINGW_RC:-$(command -v "${MINGW_TRIPLE}-windres" 2>/dev/null || true)}"

if [[ -z "${MINGW_CC}" || -z "${MINGW_CXX}" ]]; then
  echo "Missing mingw-w64 cross compiler. Set MINGW_CC and MINGW_CXX, or install ${MINGW_TRIPLE}-gcc/${MINGW_TRIPLE}-g++." >&2
  exit 1
fi

MINGW_BIN_DIR="$(dirname "${MINGW_CXX}")"
MINGW_REAL_BIN_DIR="$(dirname "$(realpath "${MINGW_CXX}")")"

CMAKE_ARGS=(
  --fresh
  -S "${QUACKLE_ROOT}"
  -B "${BUILD_DIR}"
  -DCMAKE_SYSTEM_NAME=Windows
  -DCMAKE_BUILD_TYPE=Release
  -DCMAKE_C_COMPILER="${MINGW_CC}"
  -DCMAKE_CXX_COMPILER="${MINGW_CXX}"
)

if [[ -n "${MINGW_RC}" ]]; then
  CMAKE_ARGS+=(-DCMAKE_RC_COMPILER="${MINGW_RC}")
fi

cmake "${CMAKE_ARGS[@]}"

cmake --build "${BUILD_DIR}" --target quackle_ffm -j

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
  WINPTHREAD_DLL="${MINGW_REAL_BIN_DIR}/../${MINGW_TRIPLE}/bin/libwinpthread-1.dll"
fi

if [[ -f "${WINPTHREAD_DLL}" ]]; then
  cp "${WINPTHREAD_DLL}" "${OUTPUT_DIR}/libwinpthread-1.dll"
  cp "${WINPTHREAD_DLL}" "${PROJECT_ROOT}/target/native/libwinpthread-1.dll"
fi

echo "Built Windows DLL at ${TARGET_DLL}"
