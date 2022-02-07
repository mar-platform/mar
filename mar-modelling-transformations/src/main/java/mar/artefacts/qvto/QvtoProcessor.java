package mar.artefacts.qvto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.m2m.internal.qvt.oml.InternalTransformationExecutor;
import org.eclipse.m2m.internal.qvt.oml.QvtMessage;
import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;

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
		Collection<String> missingMetamodels = getMissingMetamodels(qvtoFile);
		
		Set<EcoreModel> metamodels = new HashSet<EcoreRepository.EcoreModel>();
		Set<EPackage> packages = new HashSet<EPackage>();
		ResourceSet rs = new ResourceSetImpl();
		for (String uri : missingMetamodels) {
			List<EcoreModel> m = this.repo.findEcoreByURI(uri);
			if (! m.isEmpty()) {
				EcoreModel ecore = m.get(0);
				metamodels.add(ecore);
				
				// TODO: Factorise URIExtractor combinator generator to try all possible combinations
				Resource r = ecore.load(rs);
				r.getAllContents().forEachRemaining(o -> {
					if (o instanceof EPackage) {
						EPackage pkg = (EPackage) o;
						packages.add(pkg);						
					}
				});
			}
		}
		
		Qvto trafo = new QvtoLoader().load(qvtoFile, packages);
		trafo.addMetamodels(metamodels);
		
//		System.out.println("Loaded " + qvtoFile + " with " + unit.getErrors().size() + " errors");
//		int i = 0;
//		for (QvtMessage qvtMessage : unit.getErrors()) {
//			System.out.println("  - " + qvtMessage);
//			if (i++ > 5)
//				break;
//		}
		
		return trafo;
	}
	
	@Nonnull
	private Collection<String> getMissingMetamodels(@Nonnull String qvtoFile) {
		Qvto trafo = new QvtoLoader().load(qvtoFile, Collections.emptyList());
		return trafo.getMissingMetamodels();
	}
	
}
