package mar.analysis.backend.megamodel;

public enum ArtefactType {
	ANT("ant"),
	LAUNCH("launch"),
	QVTO("qvto"),
	OCL("ocl"),
	XTEXT("xtext"),
	EMFATIC("emfatic"),
	ACCELEO("acceleo"),
	ATL("atl"),
	SIRIUS("sirius"); 
	
	public final String id;
	
	private ArtefactType(String id) {
		this.id = id;
	}
}
