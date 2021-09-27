RUN_COMMAND="Exec with $0 <configuration-name>"

NAME=$1

$SPARK_BIN/spark-submit --master local[6] --driver-memory 8G $REPO_MAR/mar-spark-merge/target/mar-spark-merge-1.0-SNAPSHOT-jar-with-dependencies.jar $NAME
