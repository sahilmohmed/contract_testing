@echo off
SET "JAVA_HOME=C:\Program Files\Java\jdk-23"
echo JAVA_HOME: %JAVA_HOME %
title Run docker
cd C:\Users\97156\Desktop\emirates
call docker compose -f "docker-compose.yaml" up -d