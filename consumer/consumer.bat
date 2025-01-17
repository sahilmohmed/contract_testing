@echo off
SET "JAVA_HOME=C:\Program Files\Java\jdk-23"
echo JAVA_HOME: %JAVA_HOME %
title Generate contract with mock consumer
cd C:\Users\97156\Desktop\emirates\consumer
call & .\mvnw clean compile verify -f pom.xml & call ./mvnw pact:publish & call  copy .\target\surefire-reports\TEST-com.consumer_mock.consumer.ItemServiceClientPactTest.xml C:\ProgramData\Jenkins\.jenkins\workspace\Consumer\report.xml