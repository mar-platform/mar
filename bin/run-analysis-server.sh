MEGAMODEL=$1
ORGANIZE=$2
PORT=9090

java -Dserver.port=$PORT -jar mar-spring-webserver/target/mar-spring-webserver-1.0-SNAPSHOT.jar $MEGAMODEL $ORGANIZE  mar-spring-webserver/src/main/resources/configuration.yaml
#--server.servlet.context-path=/modelgraph
