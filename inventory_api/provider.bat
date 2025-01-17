@echo off
SET "JAVA_HOME=C:\Program Files\Java\jdk-23"
echo JAVA_HOME: %JAVA_HOME %
title Test contract on the Inventory API
cd C:\Users\97156\Desktop\emirates\inventory_api
call & .\mvnw clean compile verify -D pact.verifier.publishResults=true -f pom.xml & call  copy .\target\surefire-reports\TEST-com.api.springboot.PactVerificationTest.xml C:\ProgramData\Jenkins\.jenkins\workspace\Producer\report.xml