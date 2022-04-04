#!/bin/bash

cd /home/pi/intreste2.0
echo "Building application"
./gradlew --stop
./gradlew packageUberJarForCurrentOS
