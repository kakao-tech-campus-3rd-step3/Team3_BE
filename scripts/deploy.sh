#!/bin/bash
set -e

APP_DIR="/home/ubuntu/app"
JAR="$APP_DIR/app.jar"
LOG="$APP_DIR/deploy.log"
ERR="$APP_DIR/deploy_err.log"

echo "===== $(date '+%F %T') START DEPLOY =====" >> "$LOG"

echo ">>> stop running application if exists" >> "$LOG"
CURRENT_PID=$(pgrep -f "app.jar" || true)
if [ -n "$CURRENT_PID" ]; then
  echo ">>> kill -15 $CURRENT_PID" >> "$LOG"
  kill -15 $CURRENT_PID
  sleep 5
fi

# 환경변수 로드 (.env)
if [ -f "$APP_DIR/.env" ]; then
  echo ">>> load .env" >> "$LOG"
  set -o allexport

  export $(grep -v '^#' "$APP_DIR/.env" | xargs)
  set +o allexport
fi

# 기본 프로필 안전장치
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

echo ">>> run: $JAR (profile=$SPRING_PROFILES_ACTIVE)" >> "$LOG"
nohup java $JAVA_OPTS -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" -jar "$JAR" >> "$LOG" 2>> "$ERR" &

echo "===== $(date '+%F %T') DEPLOY DONE =====" >> "$LOG"
