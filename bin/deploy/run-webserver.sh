
java -cp \
     $REPO_MAR/mar-restservice/lib/emfatic/org.eclipse.emf.emfatic.core_0.8.0.202003241508.jar:$REPO_MAR/mar-restservice/lib/emfatic/org.eclipse.gymnast.runtime.core_0.8.0.202003241508.jar:$REPO_MAR/mar-restservice/target/mar.restservice-1.0-SNAPSHOT-jar-with-dependencies.jar \
     -Djava.awt.headless=true \
     mar.restservice.Main -c $REPO_MAR/configuration/dist/config.json -p 80


