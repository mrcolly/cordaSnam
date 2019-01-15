#!/bin/bash

cd /root/cordaSnam/kotlin-source/build/nodes
cd Notary
nohup java -jar corda.jar &
sleep 2
cd ..


cd Sman
nohup java -jar corda.jar &
sleep 2
nohup java -jar corda-webserver.jar &
sleep 2
cd ..


cd EMI
nohup java -jar corda.jar &
sleep 2
nohup java -jar corda-webserver.jar &
sleep 2
cd ..


cd Songenia
nohup java -jar corda.jar &
sleep 2
nohup java -jar corda-webserver.jar &
sleep 2
cd ..


cd Edifon
nohup java -jar corda.jar &
sleep 2
nohup java -jar corda-webserver.jar &
sleep 2
cd ..
