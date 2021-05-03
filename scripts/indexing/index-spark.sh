RUN_COMMAND="Exec with $0 <json-config> <configuration-name> "

JSON=$1
NAME=$2

if [ -z "$SPARK_BIN" ]
  then
      echo "Please define REPO_MAR"
      exit
fi

if [ -z "$JSON" ]
  then
      echo $RUN_COMMAND
      exit
fi


if [ -z "$NAME" ]
  then
      echo $RUN_COMMAND
      exit
fi

$SPARK_BIN/spark-submit --master local[6] --driver-memory 12G $REPO_MAR/mar-indexer-spark/target/mar-indexer-spark-1.0-SNAPSHOT-jar-with-dependencies.jar $JSON -t $NAME -m full

