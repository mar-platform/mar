#!/bin/bash

# Default values
port=80
storage="jvector"
config_file="$REPO_MAR/configuration/dist/config.json"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -p|--port)
            port="$2"
            shift
            shift
            ;;
        -s|--storage)
            storage="$2"
            shift
            shift
            ;;
        -c|--config)
            config_file="$2"
            shift
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate port is a positive integer
if ! [[ "$port" =~ ^[0-9]+$ ]]; then
    echo "Port must be a positive integer"
    exit 1
fi

# Validate storage value
if [[ "$storage" != "hbase" && "$storage" != "sqlite" && "$storage" != "jvector" ]]; then
    echo "Invalid storage option. Options are: hbase, sqlite, jvector"
    exit 1
fi

# Check if config file exists
if [ ! -f "$config_file" ]; then
    echo "Config file not found: $config_file"
    exit 1
fi

echo "Using port: $port"
echo "Using storage: $storage"
echo "Using config file: $config_file"

java -cp \
     $REPO_MAR/mar-restservice/lib/emfatic/org.eclipse.emf.emfatic.core_0.8.0.202003241508.jar:$REPO_MAR/mar-restservice/lib/emfatic/org.eclipse.gymnast.runtime.core_0.8.0.202003241508.jar:$REPO_MAR/mar-restservice/target/mar.restservice-1.0-SNAPSHOT-jar-with-dependencies.jar \
     -Djava.awt.headless=true \
     mar.restservice.Main -c $config_file -p $port -s $storage


