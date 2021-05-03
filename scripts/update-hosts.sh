#!/bin/bash


# first try to remove old items 
sudo sed -i -E 's/^.+(zoo|datanode|hbase-master|hbase-region)$//' /etc/hosts

# delete blank lines
sudo sed -i '/^$/d' /etc/hosts

#We want to generate something like:
# 172.20.0.10 zoo zoo
# 172.20.0.5 datanode datanode
# 172.20.0.7 hbase-master hbase-master
# 172.20.0.3 hbase-regionserver hbase-region

IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' zoo` &&  echo $IP  zoo zoo | sudo tee -a /etc/hosts
IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' datanode` &&  echo $IP  datanode datanode | sudo tee -a /etc/hosts
IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' hbase-master` &&  echo $IP  hbase-master hbase-master | sudo tee -a /etc/hosts
IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' hbase-regionserver` &&  echo $IP  hbase-regionserver hbase-region | sudo tee -a /etc/hosts

