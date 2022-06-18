#!/bin/bash

sudo rm -rf /etc/xdg/autostart/Intreste.desktop
sudo rm -rf ~/Desktop/Intreste.desktop
sudo rm -rf ~/Desktop/Update.desktop


sudo cp ~/intreste2.0/Intreste.desktop /etc/xdg/autostart
sudo cp ~/intreste2.0/Intreste.desktop ~/Desktop
sudo cp ~/intreste2.0/Update.desktop ~/Desktop