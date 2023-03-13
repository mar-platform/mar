# https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html
mvn install:install-file -Dfile="thirdparty/archimate/com.archimatetool.model.jar" -DgroupId=archimate -DartifactId=archimate-model -Dversion="4.2.0" -Dpackaging=jar

mvn install:install-file -Dfile="thirdparty/archimate/com.archimatetool.jdom.jar" -DgroupId=archimate -DartifactId=archimate-jdom -Dversion="4.2.0" -Dpackaging=jar

mvn install:install-file -Dfile="thirdparty/lilypond/org.elysium_0.6.0.201805301416.jar" -DgroupId=elysium -DartifactId=elysium -Dversion="0.6.0" -Dpackaging=jar

mvn install:install-file -Dfile="thirdparty/anatlyzer/all.jar" -DgroupId=anatlyzer -DartifactId=anatlyzer-bundle -Dversion="0.8.0" -Dpackaging=jar

#    jaxen-1.1.3.jar  jdom-2.0.5.jar

# cp thirdparty/simulink/simulink.ecore mar-modelling-eclipse/src/main/java/mar/models/simulink


pushd .
cd thirdparty
git clone https://github.com/mrcalvin/qvto-cli.git
cd qvto-cli/qvt-bundle
# This is an old project and we need to update the versions
sed -i 's/http:\/\/download.eclipse.org\/releases\/luna/http:\/\/download.eclipse.org\/releases\/2021-09/' pom.xml
sed -i 's/0\.22\.0/2\.6\.0/g' pom.xml

mvn clean install
popd
