package mar.artefacts;

import java.util.EnumSet;

import mar.artefacts.RecoveredPath.HeuristicPath;

public class MetamodelReference {

	public static Kind[] EMPTY_KIND = new Kind[0];
	
	public static enum Kind {
		IMPORT,
		GENERATE, 
		TYPED_BY,
		INPUT_OF,
		OUTPUT_OF
	}

	public static enum RecoveryMethod {
		DEFAULT,
		FOOTPRINT
	}
	
	private final Metamodel metamodel;
	private final EnumSet<Kind> kind;
	private RecoveryMethod recoveryMethod = RecoveryMethod.DEFAULT;

	public MetamodelReference(Metamodel metamodel, Kind... kind) {
		this.metamodel = metamodel;
		this.kind = EnumSet.noneOf(Kind.class);
		for(int i = 0; i < kind.length; i++)
			this.kind.add(kind[i]);	
	}
	
	public MetamodelReference withRecoveryMethod(RecoveryMethod method) {
		this.recoveryMethod = method;
		return this;
	}
	
	public Metamodel getMetamodel() {
		return metamodel;
	}
	
	public RecoveryMethod getRecoveryMethod() {
		return recoveryMethod;
	}
	
	public Kind[] getKind() {
		return kind.toArray(EMPTY_KIND);
	}

	public boolean is(Kind kind) {
		return this.kind.contains(kind);
	}

	public boolean isHeuristicRecovery() {
		return metamodel.getPath() != null && metamodel.getPath() instanceof HeuristicPath;
	}

}
