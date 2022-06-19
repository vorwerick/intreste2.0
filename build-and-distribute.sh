#!/bin/bash

rm -rf /etc/intreste
mkdir /etc/intreste
mkdir /etc/intreste/distribution
cd /etc/intreste/distribution
echo "Building application"
git clone https://vorwerick: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@github.com/vorwerick/intreste2.0.git
./gradlew --stop
./gradlew packageUberJarForCurrentOS
sudo chmod +x /etc/intreste/distribution/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar
curl -X POST https://dzubera.cz/pushfile.php?key=intreste -F key=intreste -F fileToUpload=@/etc/intreste/build/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar
sudo cp /etc/intreste/distribution/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar /etc/intreste
sudo cp /etc/intreste/distribution/intreste2.0/run.sh /etc/intreste
sudo cp /etc/intreste/distribution/intreste2.0/update-jar-executable.sh /etc/intreste
sudo rm -rf /etc/xdg/autostart/Intreste.desktop
sudo rm -rf /home/pi/Desktop/Intreste.desktop
sudo rm -rf /home/pi/Desktop/Update.desktop
sudo cp /etc/intreste/distribution/intreste2.0/Intreste.desktop /etc/xdg/autostart
sudo cp /etc/intreste/distribution/intreste2.0/Intreste.desktop /home/pi/Desktop
sudo cp /etc/intreste/distribution/intreste2.0/Update.desktop /home/pi/Desktop
echo "Done, you can exit the window"
read
