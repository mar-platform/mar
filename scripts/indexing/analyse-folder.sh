RUN_COMMAND="Exec with $0 <kind> <crawler-repo> <output-folder>"

KIND=$1
CRAWLER_REPO=$2
OUTPUT_FOLDER=$3


if [ -z "$CRAWLER_REPO" ]
  then
      echo $RUN_COMMAND
      exit
fi

if [ -z "$OUTPUT_FOLDER" ]
  then
      echo $RUN_COMMAND
      exit
fi

case $KIND in
  genmymodel-bpmn)
    FILELIST=/tmp/$KIND.txt
    find $CRAWLER_REPO -type f -printf "%P\n" | grep "^data/.\+\.xmi$" > $FILELIST
    ;;
  genmymodel-uml)
    KIND="uml" # AnalyserMain is generic
    FILELIST=/tmp/$KIND.txt
    find $CRAWLER_REPO -type f -printf "%P\n" | grep "^data/.\+\.xmi$" > $FILELIST
    ;;	  
  ecore)
    FILELIST=/tmp/$KIND.txt
    find $CRAWLER_REPO -type f -printf "%P\n" | grep "^data/.\+\.ecore$" > $FILELIST
    ;;
  *)
    echo "Invalid model kind $KIND"
    echo $RUN_COMMAND
    exit
    ;;
esac

TOTAL_MODELS=`wc --lines $FILELIST | cut -f1 -d" "`

mkdir -p $OUTPUT_FOLDER

echo "Running analysis on: $CRAWLER_REPO"
echo "     for model type: $KIND"
echo "     with file list: $FILELIST"
echo "         containing: $TOTAL_MODELS models"
echo "      output folder: $OUTPUT_FOLDER"

echo
echo "Executing with  $KIND $CRAWLER_REPO $FILELIST $OUTPUT_FOLDER"

java -jar ./mar-modelling/target/mar-modelling-1.0-SNAPSHOT-jar-with-dependencies.jar $KIND $CRAWLER_REPO $FILELIST $OUTPUT_FOLDER
