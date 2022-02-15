package mar.artefacts.qvto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import mar.analysis.ecore.EcoreRepository;
import mar.analysis.ecore.EcoreRepository.EcoreModel;
import mar.artefacts.Transformation;
import mar.artefacts.Transformation.Qvto;

public class QvtoProcessor {

	private final EcoreRepository repo;

	// TODO: EcoreRepository could be something more generic, able to connect to MAR or to use the analysis db
	public QvtoProcessor(@Nonnull EcoreRepository repo) {
		this.repo = repo;
	}
	
	public Transformation load(@Nonnull String qvtoFile) {
		Qvto tmp = new QvtoLoader().load(qvtoFile, Collections.emptyList());
		Collection<String> expectedMetamodels = tmp.getMetamodelURIs(); 
		
		// Collection<String> missingMetamodels = getMissingMetamodels(qvtoFile);
		
		Set<EcoreModel> metamodels = new HashSet<EcoreRepository.EcoreModel>();
		Set<EPackage> packages = new HashSet<EPackage>();
		ResourceSet rs = new ResourceSetImpl();
		for (String uri : expectedMetamodels) {
			List<EcoreModel> m = this.repo.findEcoreByURI(uri);
			if (! m.isEmpty()) {
				EcoreModel ecore = m.get(0);
				metamodels.add(ecore);
				packages.addAll(ecore.getPackages(rs));
			}
		}
		
		Qvto trafo = new QvtoLoader().load(qvtoFile, packages);
		
		trafo.addMetamodels(metamodels);
	
		return trafo;
	}
	
}
