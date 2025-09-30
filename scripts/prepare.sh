#!/bin/bash
set -e

APP_DIR="/home/ubuntu/app"

mkdir -p "$APP_DIR"
# 이전 번들 찌꺼기 정리(선택)
find "$APP_DIR" -maxdepth 1 -name "*.jar" -type f -delete || true
