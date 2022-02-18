package mar.analysis.backend;

import java.io.File;

import io.micronaut.runtime.Micronaut;
import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.megamodel.model.Relationship;

public class Application {

    public static void main(String[] args) {
    	String fileName = args[0];
    	MegamodelDB db = new MegamodelDB(new File(fileName));
    	
//    	MegamodelDB db = new MegamodelDB(new File("/tmp/test.db"));
//    	db.addArtefact("m1.ecore", "ecore");
//    	db.addArtefact("t1.qvto", "qvto");
//    	db.addArtefact("t2.qvto", "qvto");
//
//    	db.addRelationship("t1.qvto", "m1.ecore", Relationship.TYPED_BY);
//    	db.addRelationship("t2.qvto", "m1.ecore", Relationship.TYPED_BY);
    	
    	TransformationRelationshipsAnalysis analysis = new TransformationRelationshipsAnalysis(db);
    	
    	
    	Micronaut mm = Micronaut.build(args);    	
    	mm.classes(Application.class).
    		singletons(analysis).
    		start();   
    	System.out.println(mm);
    }
}
