#!/bin/bash

if [ -z "$REPO_MAR" ]
  then
      echo "Please define REPO_MAR"
      exit
fi

pushd .
mkdir -p $REPO_MAR/external-resources
cd $REPO_MAR/external-resources
wget http://models-lab.inf.um.es/tools/worde4mde/worde4mde-embeddings.tar.gz
tar xzf worde4mde-embeddings.tar.gz
rm worde4mde-embeddings.tar.gz
popd
