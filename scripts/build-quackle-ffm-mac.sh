#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
QUACKLE_ROOT="${QUACKLE_ROOT:-${PROJECT_ROOT}/quackle-master}"
BUILD_DIR="${QUACKLE_ROOT}/build/macos"
OUTPUT_DIR="${PROJECT_ROOT}/native"
TARGET_DYLIB="${OUTPUT_DIR}/libquackle_ffm.dylib"

cmake \
  -S "${QUACKLE_ROOT}" \
  -B "${BUILD_DIR}" \
  -DCMAKE_BUILD_TYPE=Release

cmake --build "${BUILD_DIR}" --target quackle_ffm -j

mkdir -p "${OUTPUT_DIR}" "${PROJECT_ROOT}/target/native"
cp "${BUILD_DIR}/libquackle_ffm.dylib" "${TARGET_DYLIB}"
cp "${BUILD_DIR}/libquackle_ffm.dylib" "${PROJECT_ROOT}/target/native/libquackle_ffm.dylib"

echo "Built macOS dylib at ${TARGET_DYLIB}"
