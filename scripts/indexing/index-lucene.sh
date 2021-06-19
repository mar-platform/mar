RUN_COMMAND="Exec with $0 <json-config> <configuration-name> "

JSON=$1
NAME=$2

if [ -z "$INDEX_TARGET" ]
  then
      echo "Please define INDEX_TARGET"
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

java -jar $REPO_MAR/mar-indexer-lucene/target/mar-indexer-lucene-1.0-SNAPSHOT-jar-with-dependencies.jar $JSON $INDEX_TARGET/lucene -repository $NAME 
