# https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html
mvn install:install-file -Dfile=thirdparty/archimate/com.archimatetool.model.jar -DgroupId=archimate -DartifactId=archimate-model -Dversion=4.2.0 -Dpackaging=jar

mvn install:install-file -Dfile=thirdparty/archimate/com.archimatetool.jdom.jar -DgroupId=archimate -DartifactId=archimate-jdom -Dversion=4.2.0 -Dpackaging=jar

mvn install:install-file -Dfile=thirdparty/lilypond/org.elysium_0.6.0.201805301416.jar -DgroupId=elysium -DartifactId=elysium -Dversion=0.6.0 -Dpackaging=jar

mvn install:install-file -Dfile=thirdparty/rasa/rasa-java-client-library-0.2.0.jar  -DpomFile=thirdparty/rasa/rasa-java-client-library-0.2.0.pom -DgroupId=io.github.arturkorb -DartifactId=rasa-java-client-library -Dversion=0.2.0 -Dpackaging=jar

#    jaxen-1.1.3.jar  jdom-2.0.5.jar

# cp thirdparty/simulink/simulink.ecore mar-modelling-eclipse/src/main/java/mar/models/simulink
