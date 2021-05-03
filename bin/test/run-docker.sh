#!/bin/bash

cd $REPO_MAR
sudo docker-compose -f docker-compose-test.yml up -d
sudo scripts/update-hosts.sh

echo "Docker started. Waiting for HBase to be ready..."
status=1
while [ $status -gt 0 ]
do
    $REPO_MAR/scripts/is-hbase-running.sh
    status=$?
    if [ $status -gt 0 ]
    then
      echo "Sleeping for 3 seconds..."
      sleep 3
    fi
done

touch $REPO_MAR/.test_docker_running
