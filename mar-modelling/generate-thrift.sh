 #!/bin/sh
 
BASEDIR=$(dirname $0)
rm -rf $BASEDIR/src/main/java-gen
mkdir -p $BASEDIR/src/main/java-gen 
thrift -r -out $BASEDIR/src/main/java-gen --gen java $BASEDIR/src/main/thrift/validator.thrift 