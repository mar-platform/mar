RUN_COMMAND="Exec with $0 <configuration-file> <model-type>"

CONFIG=$1
KIND=$2

if [ -z "$CONFIG" ]
  then
      echo $RUN_COMMAND
      exit
fi

if [ -z "$KIND" ]
  then
      echo $RUN_COMMAND
      exit
fi

echo "Running analysis on: $CONFIG"
echo "     for model type: $KIND"
echo
echo "Executing with  $CONFIG $KIND"

java -jar ./mar-modelling/target/mar-modelling-1.0-SNAPSHOT-jar-with-dependencies.jar -parallel=4 -t=$KIND $CONFIG 
