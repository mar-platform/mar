package mar.analysis.backend.megamodel;

public enum ArtefactType {
	ANT("ant", false),
	LAUNCH("launch", false),
	QVTO("qvto"),
	OCL("ocl"),
	XTEXT("xtext"),
	XTEND("xtend"),	
	EMFTEXT("emftext"),	
	EMFATIC("emfatic"),
	ECORE("ecore"),
	XCORE("xcore"),
	EPSILON("epsilon"),
	ACCELEO("acceleo"),
	ATL("atl"),
	SIRIUS("sirius"),
	GMF("gmf"),
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
