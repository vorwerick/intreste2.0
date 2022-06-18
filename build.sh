#!/bin/bash

cd /home/pi/intreste2.0
echo "Building application"
./gradlew --stop
./gradlew packageUberJarForCurrentOS
sudo chmod +x /home/pi/intreste2.0/run.sh
sudo chmod +x /home/pi/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar
