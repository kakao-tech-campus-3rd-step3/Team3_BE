#!/bin/bash

DEPLOY_PATH=/home/ubuntu/app/
DEPLOY_JAR=$DEPLOY_PATH/app.jar

echo ">>> 배포 JAR: $DEPLOY_JAR" >> /home/ubuntu/deploy.log

echo ">>> 현재 실행중인 애플리케이션 pid 확인 후 일괄 종료" >> /home/ubuntu/deploy.log
CURRENT_PID=$(pgrep -f app.jar)
if [ -n "$CURRENT_PID" ]; then
    echo ">>> 종료할 PID: $CURRENT_PID" >> /home/ubuntu/deploy.log
    kill -15 $CURRENT_PID
    sleep 5
fi

# 환경변수 로드
if [ -f /home/ubuntu/app/.env ]; then
    echo ">>> .env 파일 로드" >> /home/ubuntu/deploy.log
    export $(cat /home/ubuntu/app/.env | xargs)
fi

echo ">>> $DEPLOY_JAR 실행합니다" >> /home/ubuntu/deploy.log
nohup java -jar $DEPLOY_JAR >> /home/ubuntu/deploy.log 2> /home/ubuntu/deploy_err.log &
echo ">>> 배포 완료" >> /home/ubuntu/deploy.log
