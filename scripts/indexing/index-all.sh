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
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG repo-ecore-all


# BPMN
echo "Analysing BPMN..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG genmymodel-bpmn

echo "Indexing BPMN..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG repo-genmymodel-bpmn2

# UML
echo "Analysing UML..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG genmymodel-uml 

echo "Indexing UML..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG repo-genmymodel-uml

# PNML
echo "Analysing PNML..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG pnml

echo "Indexing PNML..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG repo-github-pnml

# sculptor
echo "Analysing SCULPTOR..."
$REPO_MAR/scripts/indexing/analyse.sh $CONFIG sculptor

echo "Indexing SCULPTOR..."
$REPO_MAR/scripts/indexing/index-spark.sh $CONFIG repo-github-sculptor
