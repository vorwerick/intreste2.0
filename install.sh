#!/bin/bash

sudo rm -rf /etc/xdg/autostart/Intreste.desktop
sudo rm -rf /home/pi/Desktop/Intreste.desktop

sudo cp /home/pi/intreste2.0/Intreste.desktop /etc/xdg/autostart
sudo cp /home/pi/intreste2.0/Intreste.desktop /home/pi/Desktop