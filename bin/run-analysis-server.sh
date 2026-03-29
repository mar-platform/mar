MEGAMODEL=$1
ORGANIZE=$2

java -jar mar-spring-webserver/target/mar-spring-webserver-1.0-SNAPSHOT.jar $MEGAMODEL $ORGANIZE  mar-spring-webserver/src/main/resources/configuration.yaml
