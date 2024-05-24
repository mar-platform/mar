RUN_COMMAND="Exec with $0 <json-config> <configuration-name> <embedding-strategy>"

JSON=$1
NAME=$2
EMBEDDING_STRATEGY=$3

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

if [ -z "$EMBEDDING_STRATEGY" ]
  then
      echo $RUN_COMMAND
      exit
fi


#java -jar $REPO_MAR/mar-indexer-embeddings/target/mar-indexer-embeddings-1.0-SNAPSHOT-jar-with-dependencies.jar $JSON -t $NAME -m full


java -cp $REPO_MAR/mar-indexer-embeddings/target/mar-indexer-embeddings-1.0-SNAPSHOT-jar-with-dependencies.jar mar.indexer.embeddings.CreateIndex $JSON $INDEX_TARGET/jvector/$NAME.jvector $INDEX_TARGET/jvector/$NAME.info -t $NAME -e $EMBEDDING_STRATEGY
