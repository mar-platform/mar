#!/bin/bash

RUN_COMMAND="Exec with $0 <json-config>"

JSON=$1

if [ -z "$REPO_ROOT" ]
  then
      echo "Please define REPO_ROOT"
      exit
fi

if [ -z "$REPO_MAR" ]
  then
      echo "Please define REPO_MAR"
      exit
fi

if [ -z "$JSON" ]
  then
      echo $RUN_COMMAND
      exit
fi

if [ -z "$INDEX_TARGET" ]
  then
      echo "Please define INDEX_TARGET"
      exit
fi

java -jar $REPO_MAR/mar-indexer-lucene/target/mar-indexer-lucene-1.0-SNAPSHOT-jar-with-dependencies.jar $JSON $INDEX_TARGET/lucene -all
