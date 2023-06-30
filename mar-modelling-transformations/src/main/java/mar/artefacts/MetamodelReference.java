package mar.artefacts;

import java.util.EnumSet;

public class MetamodelReference {

	public static Kind[] EMPTY_KIND = new Kind[0];
	
	public static enum Kind {
		IMPORT,
		GENERATE, 
		TYPED_BY,
		INPUT_OF,
		OUTPUT_OF
	}

	private final Metamodel metamodel;
	private final EnumSet<Kind> kind;

	public MetamodelReference(Metamodel metamodel, Kind... kind) {
		this.metamodel = metamodel;
		this.kind = EnumSet.noneOf(Kind.class);
		for(int i = 0; i < kind.length; i++)
			this.kind.add(kind[i]);	
	}
	
	public Metamodel getMetamodel() {
		return metamodel;
	}
	
	public Kind[] getKind() {
		return kind.toArray(EMPTY_KIND);
	}

	public boolean is(Kind kind) {
		return this.kind.contains(kind);
	}

}
