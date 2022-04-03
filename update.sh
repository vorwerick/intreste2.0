#!/bin/bash

echo "Start updating Intrste 2.0..."
cd /home/pi
rm intreste2.0
git clone git@github.com:vorwerick/intreste2.0.git
echo "Building application"
./gradlew --stop
./gradlew packageUberJarForCurrentOS
java -jar /home/pi/intreste2.0/build/compose/jars/Intreste-linux.arm64-2.0.0.jar
