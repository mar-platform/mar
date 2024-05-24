RUN_COMMAND="Exec with $0 <json-config> <configuration-name> "

JSON=$1
NAME=$2

if [ -z "$REPO_MAR" ]
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

java -cp $REPO_MAR/mar-indexer-spark/target/mar-indexer-spark-1.0-SNAPSHOT-jar-with-dependencies.jar mar.sqlite.SqliteIndexJob $JSON $INDEX_TARGET/sqlite/$NAME.db -t $NAME
