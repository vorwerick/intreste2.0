#!/bin/bash

echo "Building application"
./gradlew --stop
./gradlew packageUberJarForCurrentOS
