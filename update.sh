#!/bin/bash

echo "Start updating Intrste 2.0..."
cd /home/pi/intreste2.0/
echo "Pulling from repository"
git pull
echo "Building application"
./gradlew packageUberJarForCurrentOS
java -jar /home/pi/build/compose/jars/KotlinJvmComposeDesktopAppliaction
