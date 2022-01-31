#!/bin/bash

if [ -z "$REPO_MAR" ]
  then
      echo "Please define REPO_MAR"
      exit
fi


TEST_FOLDER=$REPO_MAR/test-files/target
mkdir -p $TEST_FOLDER
rm $TEST_FOLDER/crawler.db $TEST_FOLDER/analysis.db

python3 $REPO_MAR/mar-crawlers/test/mockcrawler.py -d $REPO_MAR/external-resources/data -e ecore -o $TEST_FOLDER

$REPO_MAR/scripts/indexing/analyse.sh $REPO_MAR/configuration/test/config.json ecore

$REPO_MAR/scripts/indexing/index-lucene.sh $REPO_MAR/configuration/test/config.json ecore

sudo docker system prune -f

$REPO_MAR/bin/test/run-docker.sh
$REPO_MAR/scripts/indexing/index-spark.sh $REPO_MAR/configuration/test/config.json ecore
$REPO_MAR/bin/test/kill-docker.sh
