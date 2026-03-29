cd mar-analysis-frontend-kit
npm install
npm run build
cd ..
rm mar-spring-webserver/src/main/resources/static -rf
cp -r mar-analysis-frontend-kit/build/ mar-spring-webserver/src/main/resources/static
mvn package -Dmaven.test.skip -pl mar-spring-webserver -am
