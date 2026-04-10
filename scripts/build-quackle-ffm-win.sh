#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
QUACKLE_ROOT="${QUACKLE_ROOT:-${PROJECT_ROOT}/../../quackle-master}"
BUILD_DIR="${QUACKLE_ROOT}/build/windows-x86_64"
TOOLCHAIN_FILE="${QUACKLE_ROOT}/cmake/toolchains/mingw-w64-x86_64.cmake"
OUTPUT_DIR="${PROJECT_ROOT}/native"
TARGET_DLL="${OUTPUT_DIR}/quackle_ffm.dll"
MINGW_BIN_DIR="$(brew --prefix mingw-w64)/toolchain-x86_64/x86_64-w64-mingw32/bin"

cmake \
  -S "${QUACKLE_ROOT}" \
  -B "${BUILD_DIR}" \
  -G "Unix Makefiles" \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_TOOLCHAIN_FILE="${TOOLCHAIN_FILE}"

cmake --build "${BUILD_DIR}" --target quackle_ffm -j

mkdir -p "${OUTPUT_DIR}" "${PROJECT_ROOT}/target/native"
cp "${BUILD_DIR}/quackle_ffm.dll" "${TARGET_DLL}"
cp "${BUILD_DIR}/quackle_ffm.dll" "${PROJECT_ROOT}/target/native/quackle_ffm.dll"

if [[ -f "${MINGW_BIN_DIR}/libwinpthread-1.dll" ]]; then
  cp "${MINGW_BIN_DIR}/libwinpthread-1.dll" "${OUTPUT_DIR}/libwinpthread-1.dll"
  cp "${MINGW_BIN_DIR}/libwinpthread-1.dll" "${PROJECT_ROOT}/target/native/libwinpthread-1.dll"
fi

echo "Built Windows DLL at ${TARGET_DLL}"
