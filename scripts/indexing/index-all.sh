#!/bin/bash

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

CONFIG=$REPO_MAR/configuration/dist/config.json

# Ecore
echo "Analysing Ecore..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG ecore

echo "Indexing Ecore..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG ecore


# BPMN
echo "Analysing BPMN..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG bpmn2

echo "Indexing BPMN..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG bpmn2

# UML
echo "Analysing UML..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG uml 

echo "Indexing UML..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG uml

# PNML
echo "Analysing PNML..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG pnml

echo "Indexing PNML..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG pnml

# sculptor
echo "Analysing SCULPTOR..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG sculptor

echo "Indexing SCULPTOR..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG sculptor
