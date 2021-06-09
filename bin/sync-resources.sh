if [ -z "$REPO_MAR" ]
  then
      echo "Please define REPO_MAR"
      exit
fi

pushd .
mkdir -p $REPO_MAR/external-resources
cd $REPO_MAR/external-resources
wget http://mar-search.org/external-resources/tests.tar.gz
tar xzf tests.tar.gz
rm tests.tar.gz
popd

pushd .
cd $REPO_MAR
wget http://mar-search.org/external-resources/libs.tar.gz
tar xzf libs.tar.gz
rm libs.tar.gz
popd

ln -s $REPO_MAR/external-resources/ml-models/ mar-ml/src/main/resources/ml-models
