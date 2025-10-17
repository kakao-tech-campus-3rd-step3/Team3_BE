#!/bin/bash
set -euo pipefail

APP_DIR="/home/ubuntu/app"
JAR="$APP_DIR/app.jar"
LOG="$APP_DIR/deploy.log"
ERR="$APP_DIR/deploy_err.log"


PORT="${PORT:-8080}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:${PORT}/actuator/health}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-120}"
HEALTH_INTERVAL="${HEALTH_INTERVAL:-3}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
JAVA_OPTS="${JAVA_OPTS:-} -Dspring.output.ansi.enabled=always"


LOG_MAX_SIZE="${LOG_MAX_SIZE:-100M}"
LOG_MAX_FILES="${LOG_MAX_FILES:-5}"


NC="\033[0m"; BLUE="\033[1;34m"; YELLOW="\033[1;33m"; RED="\033[1;31m"; GREEN="\033[1;32m"
_ts() { date '+%F %T'; }
_log_plain() { echo "[$(_ts)] [$1] $2" >> "$LOG"; }   # 파일엔 평문
logi() { echo -e "${BLUE}[$(_ts)] [INFO]${NC} $1";  _log_plain INFO "$1"; }
logw() { echo -e "${YELLOW}[$(_ts)] [WARN]${NC} $1"; _log_plain WARN "$1"; }
loge() { echo -e "${RED}[$(_ts)] [ERROR]${NC} $1";  _log_plain ERROR "$1"; }
logs() { echo -e "${GREEN}[$(_ts)] [OK]${NC} $1";   _log_plain OK "$1"; }

if [ -n "${GITHUB_ACTIONS:-}" ]; then
  logi "detected GitHub Actions environment"
  logi "using PORT=${PORT}, HEALTH_URL=${HEALTH_URL}"
fi

mkdir -p "$APP_DIR"
logi "===== START DEPLOY ====="


setup_logrotate() {
    local logrotate_conf="/etc/logrotate.d/app-deploy"
    if [ ! -f "$logrotate_conf" ]; then
        logi "setup logrotate for deploy logs"
        sudo tee "$logrotate_conf" > /dev/null << EOF
$LOG $ERR {
    daily
    size $LOG_MAX_SIZE
    rotate $LOG_MAX_FILES
    missingok
    notifempty
    compress
    delaycompress
    dateext
    copytruncate
    create 0644 ubuntu ubuntu
}
EOF
        logs "logrotate configuration created"
    else
        logi "logrotate already configured"
    fi
}


setup_logrotate

logi "stop running application if exists"
CURRENT_PID="$(pgrep -f 'java .*app\.jar' || true)"
if [ -n "$CURRENT_PID" ]; then
  logi "kill -15 ${CURRENT_PID}"
  kill -15 "${CURRENT_PID}" || true
  
  logi "waiting for graceful shutdown..."
  for i in {1..20}; do
    if ps -p "${CURRENT_PID}" >/dev/null 2>&1; then 
      sleep 1
    else 
      logs "graceful shutdown completed"
      break
    fi
  done
  
  if ps -p "${CURRENT_PID}" >/dev/null 2>&1; then
    logw "graceful stop timeout. sending SIGKILL"
    kill -9 "${CURRENT_PID}" || true
  fi
fi

if [ -f "$APP_DIR/.env" ]; then
  logi "load .env"
  sed -i 's/\r$//' "$APP_DIR/.env"
  set -o allexport
  . "$APP_DIR/.env"
  set +o allexport
fi

logi "run: $JAR (profile=$SPRING_PROFILES_ACTIVE, port=$PORT)"
nohup java ${JAVA_OPTS} \
  -Dserver.port="${PORT}" \
  -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" \
  -jar "$JAR" >> "$LOG" 2>> "$ERR" &
NEW_PID=$!
logs "started pid=$NEW_PID"


if command -v curl >/dev/null 2>&1; then
  logi "health check: ${HEALTH_URL} (timeout=${HEALTH_TIMEOUT}s)"
  SECONDS=0
  HEALTH_OK=0
  
  while [ "$SECONDS" -lt "$HEALTH_TIMEOUT" ]; do
    logi "health check attempt at ${SECONDS}s"
    
    if curl -fsS --connect-timeout 2 "${HEALTH_URL}" | grep -q '"status"\s*:\s*"UP"'; then
      HEALTH_OK=1
      logs "health check passed in ${SECONDS}s"
      break
    fi
    
    sleep "$HEALTH_INTERVAL"
  done

  if [ "$HEALTH_OK" -eq 1 ]; then
    logs "application is healthy and ready"
  else
    loge "health check failed after ${HEALTH_TIMEOUT}s"
    loge "last 50 lines of error log:"
    tail -n 50 "$ERR" | sed 's/^/ERR> /' >> "$LOG"
    exit 1
  fi
else
  logw "curl not found. skipping health check"
fi

logs "===== DEPLOY DONE ====="
