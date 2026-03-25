#!/bin/bash

# a) Exit with error if mandatory parameters are missing
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Error: Missing arguments."
    echo "Usage: $0 <organizedb_name.db> <megamodel_output.db>"
    exit 1
fi

ORGANIZEDB=$1
MEGAMODELDB=$2
DB_PATH="/data3/supergraph/$ORGANIZEDB"
SKIP_STEP="n"

# b) If the organizedb file exists, ask the user to skip (Yes by default)
if [ -f "$DB_PATH" ]; then
    read -p "File $ORGANIZEDB already exists. Skip the organize step? [Y/n]: " SKIP_STEP
    SKIP_STEP=${SKIP_STEP:-"y"}
fi

# Step 1: Python Processing
if [[ "$SKIP_STEP" =~ ^[Yy]$ ]]; then
    echo ">>> Skipping the organize.py step..."
else
    echo ">>> Running organization step..."
    cd mining || { echo "Failure: Could not enter mining directory"; exit 1; }
#    python3 organize.py -d /data3/supergraph/repos:/data3/supergraph/repos-mps:/data3/supergraph/repos-spoofax \
    python3 organize.py -d /data3/supergraph/repos2 \
	    -o "$DB_PATH" \
	    -c configuration.yaml
    cd ..
fi

# Step 2: Java Transformations
echo ">>> Starting Java transformations..."
# Note: Using the second parameter $MEGAMODELDB for the output
time java --add-opens java.base/java.lang=ALL-UNNAMED -jar mar-modelling-transformations/target/mar-modelling-transformations-1.0-SNAPSHOT-all.jar \
     --repository /data3/supergraph/repos2 \
     --repoDB "$DB_PATH" \
     --cache /data3/supergraph/ \
     --output "$MEGAMODELDB" \
     --analysis-ecore | tee /tmp/megamodel.log
