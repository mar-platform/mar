#!/bin/bash

# Initialize default values
THREADS=1
PARAMS=()

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
	--parallel)
	    THREADS="$2"
	    shift 2
	    ;;
	*)
	    PARAMS+=("$1") # Save positional arguments to an array
	    shift
	    ;;
    esac
done

# Assign positional arguments from the array
ORGANIZEDB="${PARAMS[0]}"
MEGAMODELDB="${PARAMS[1]}"
PREVIOUS_MEGAMODEL="${PARAMS[2]}"

# a) Exit with error if mandatory parameters are missing
if [ -z "$ORGANIZEDB" ] || [ -z "$MEGAMODELDB" ]; then
    echo "Error: Missing arguments."
    echo "Usage: $0 [--parallel <n>] <organizedb_name.db> <megamodel_output.db> [previous_megamodel]"
    exit 1
fi

DB_PATH="/data3/supergraph/$ORGANIZEDB"
SKIP_STEP="n"

echo "Running with"
echo "  - OrganizeDB: $ORGANIZEDB"
echo "  - MegamodelDB: $MEGAMODELDB"
echo "  - PreviousMegamodel: $PREVIOUS_MEGAMODEL"
echo "  - Parallelism: $THREADS"


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
    python3 organize.py -d /data3/supergraph/repos2 \
	    -o "$DB_PATH" \
	    -c configuration.yaml
    cd ..
fi

PREV_ARG=""
if [ -n "$PREVIOUS_MEGAMODEL" ]; then
    PREV_ARG="--prev-version \"$PREVIOUS_MEGAMODEL\""
fi

# Step 2: Java Transformations
echo ">>> Starting Java transformations with $THREADS thread(s)..."
time java --add-opens java.base/java.lang=ALL-UNNAMED -jar mar-modelling-transformations/target/mar-modelling-transformations-1.0-SNAPSHOT-all.jar \
     --repository /data3/supergraph/repos2 \
     --repoDB "$DB_PATH" \
     --cache /data3/supergraph/ \
     --output "$MEGAMODELDB" \
     --analysis-ecore \
     --parallel "$THREADS" \
     $PREV_ARG | tee /tmp/megamodel.log

