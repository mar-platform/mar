package mar.artefacts;

public class MetamodelReference {

	public static enum Kind {
		IMPORT,
		GENERATE, 
		TYPED_BY
	}

	private final Metamodel metamodel;
	private final Kind[] kind;

	public MetamodelReference(Metamodel metamodel, Kind... kind) {
		this.metamodel = metamodel;
		this.kind = kind;
	}
	
	public Metamodel getMetamodel() {
		return metamodel;
	}
	
	public Kind[] getKind() {
		return kind;
	}

}
