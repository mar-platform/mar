MEGAMODEL=$1
ORGANIZE=$2
PORT=9090

java -Dreactor.netty.http.server.accessLogEnabled=true -Dserver.port=$PORT -jar mar-spring-webserver/target/mar-spring-webserver-1.0-SNAPSHOT.jar $MEGAMODEL $ORGANIZE  mar-spring-webserver/src/main/resources/configuration.yaml --logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG 
