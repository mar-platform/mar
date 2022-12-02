package mar.analysis.backend.megamodel;

public enum ArtefactType {
	ANT("ant", false),
	LAUNCH("launch", false),
	QVTO("qvto"),
	OCL("ocl"),
	XTEXT("xtext"),
	EMFATIC("emfatic"),
	EPSILON("epsilon"),
	ACCELEO("acceleo"),
	ATL("atl"),
	SIRIUS("sirius"),
	HENSHIN("henshin");
	
	public final String id;
	public final boolean isArtefactFile;
	
	private ArtefactType(String id, boolean isArtefactFile) {
		this.id = id;
		this.isArtefactFile = isArtefactFile;
	}
	
	private ArtefactType(String id) {
		this(id, true);
	}
}
