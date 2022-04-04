#!/bin/bash

cd /home/pi/intreste2.0
git stash
git pull
sudo chmod +x install.sh
sudo chmod +x build.sh
sudo chmod +x run.sh
