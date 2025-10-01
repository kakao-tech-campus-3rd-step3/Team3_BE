#!/bin/bash
set -euo pipefail

APP_DIR="/home/ubuntu/app"
JAR="$APP_DIR/app.jar"
LOG="$APP_DIR/deploy.log"
ERR="$APP_DIR/deploy_err.log"

mkdir -p "$APP_DIR"
echo "===== $(date '+%F %T') START DEPLOY =====" >> "$LOG"

echo ">>> stop running application if exists" >> "$LOG"
CURRENT_PID="$(pgrep -f 'java .*app\.jar' || true)"
if [ -n "$CURRENT_PID" ]; then
  echo ">>> kill -15 ${CURRENT_PID}" >> "$LOG"
  kill -15 "${CURRENT_PID}" || true
  sleep 5
fi

if [ -f "$APP_DIR/.env" ]; then
  echo ">>> load .env" >> "$LOG"
  sed -i 's/\r$//' "$APP_DIR/.env"
  set -o allexport
  . "$APP_DIR/.env"
  set +o allexport
fi

SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

echo ">>> run: $JAR (profile=$SPRING_PROFILES_ACTIVE)" >> "$LOG"
nohup java ${JAVA_OPTS:-} -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" -jar "$JAR" >> "$LOG" 2>> "$ERR" &
NEW_PID=$!
echo ">>> started pid=$NEW_PID" >> "$LOG"

echo "===== $(date '+%F %T') DEPLOY DONE =====" >> "$LOG"
