if [ -z "$REPO_MAR" ]
  then
      echo "Please define REPO_MAR"
      exit
fi

cd $REPO_MAR
./bin/test/run-docker.sh
sudo ./scripts/update-hosts.sh
# mvn test -Phbase,no-hbase
# It seems that this tests all
mvn test -Phbase
./bin/test/kill-docker.sh
