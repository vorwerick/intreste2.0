#!/bin/bash

#if java is installed

rm -rf /etc/intreste
mkdir /etc/intreste
mkdir /etc/intreste/distribution
cd /etc/intreste/distribution
echo "Building application"
git clone https://vorwerick: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@github.com/vorwerick/intreste2.0.git
cd /etc/intreste/distribution/intreste2.0
./gradlew --stop
./gradlew packageUberJarForCurrentOS
sudo chmod 777 /etc/intreste/distribution/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar
curl -X POST https://dzubera.cz/pushfile.php?key=intreste -F key=intreste -F fileToUpload=@/etc/intreste/distribution/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar
sudo cp /etc/intreste/distribution/intreste2.0/build/compose/jars/Intreste-linux-arm64-2.0.0.jar /etc/intreste
sudo cp /etc/intreste/distribution/intreste2.0/run.sh /etc/intreste
sudo cp /etc/intreste/distribution/intreste2.0/update-jar-executable.sh /etc/intreste
sudo chmod +x /etc/intreste/update-jar-executable.sh
sudo chmod +x /etc/intreste/run.sh
sudo rm -rf /etc/xdg/autostart/Intreste.desktop
sudo rm -rf /home/ales/Desktop/Intreste.desktop
sudo rm -rf /home/ales/Desktop/Update.desktop
sudo cp /etc/intreste/distribution/intreste2.0/Intreste.desktop /etc/xdg/autostart
sudo cp /etc/intreste/distribution/intreste2.0/Intreste.desktop /home/ales/Desktop
sudo cp /etc/intreste/distribution/intreste2.0/Update.desktop /home/ales/Desktop
echo "Done, you can exit the window"
read
