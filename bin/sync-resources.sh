if [ -z "$REPO_MAR" ]
  then
      echo "Please define REPO_MAR"
      exit
fi

echo 
echo "Downloading third-party resources..."
echo "Using MAR source code in $REPO_MAR"
echo 

./bin/sync-tests.sh

pushd .
cd $REPO_MAR
wget http://sanchezcuadrado.es/mar/external-resources/libs.tar.gz
tar xzf libs.tar.gz
rm libs.tar.gz
popd

# This creates a cycle when packaging
# ln -s $REPO_MAR/external-resources/ml-models/ mar-ml/src/main/resources/ml-models
