package mar.analysis.ecore;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.types.TypesPackage;
import org.eclipse.uml2.uml.UMLPackage;

import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public class EcoreRepository {

	@Nonnull
	private final AnalysisDB db;
	@Nonnull
	private final File rootFolder;
	private Map<String, EcorePredefinedModel> knownMetamodels;
	/** Metamodels which doesn't exist physically because they are derived from other files (e.g., Xtext generated clause) */ 
	private Map<String, List<EcoreModel>> derivedMetamodels = new HashMap<String, List<EcoreModel>>();

	public EcoreRepository(@Nonnull AnalysisDB db, @Nonnull File rootFolder) {
		this.db = db;
		this.rootFolder = rootFolder;
		this.knownMetamodels = getKnownMetamodels();
	}

	private Map<String, EcorePredefinedModel> getKnownMetamodels() {
		Map<String, EcorePredefinedModel> models = new HashMap<>();
		models.put(UMLPackage.eINSTANCE.getNsURI(), new EcorePredefinedModel("uml", UMLPackage.eINSTANCE));
		models.put(TypesPackage.eINSTANCE.getNsURI(), new EcorePredefinedModel("UMLTypes", TypesPackage.eINSTANCE));		
		models.put(EcorePackage.eINSTANCE.getNsURI(), new EcorePredefinedModel("ecore", EcorePackage.eINSTANCE));		
		return models;
	}

	public void addDerived(@Nonnull String uri, @Nonnull EcoreDerivedModel derived) {
		List<EcoreModel> list = derivedMetamodels.computeIfAbsent(uri, (k) -> new ArrayList<>());
		list.add(derived);
	}

	
	@Nonnull
	public List<EcoreModel> findEcoreByURI(String uri) {
		if (knownMetamodels.containsKey(uri))
			return Collections.singletonList(knownMetamodels.get(uri));
		
		if (derivedMetamodels.containsKey(uri))
			return derivedMetamodels.get(uri);		
		
		List<Model> models = db.findByMetadata("nsURI", uri, (relative) -> rootFolder + File.separator + relative);
		ArrayList<EcoreModel> result = new ArrayList<>(models.size());
		for (Model model : models) {
			result.add(new EcoreFileModel(model));
		}
		return result;
	}

	// FIXME: Perhaps return EcoreModel
	public List<Model> getModels() {
		try {
			List<Model> models = db.getValidModels((relative) -> rootFolder + File.separator + relative);
			return models;
		} catch (SQLException e) {
			throw new RuntimeException();
		}
	}
	
	public interface EcoreModel {

		String getId();
		
		String getName();		
		
		List<EPackage> getPackages(ResourceSet rs);
	}
	
	public static interface EcoreDerivedModel extends EcoreModel {

		@Override
		public default List<EPackage> getPackages(ResourceSet rs) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static class EcorePredefinedModel implements EcoreModel {
		private final String name;
		private final EPackage pkg;

		public EcorePredefinedModel(String name, EPackage pkg) {
			this.name = name;
			this.pkg = pkg;;
		}		
		
		@Override
		public String getId() {
			return getUri();
		}
		
		public String getUri() {
			return pkg.getNsURI();
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public List<EPackage> getPackages(ResourceSet rs) {
			return Collections.singletonList(pkg);
		}
	}
	
	public static class EcoreFileModel implements EcoreModel {
		@Nonnull
		private final Model model;

		public EcoreFileModel(Model m) {
			this.model = m;
		}

		@Nonnull
		public String getId() {
			return model.getId();
		}
		
		@Nonnull
		public Model getModel() {
			return model;
		}
		
		@Nonnull
		public Resource load(ResourceSet rs) {
			String path = model.getFile().getAbsolutePath();
			return rs.getResource(URI.createFileURI(path), true);
		}
		
		@Override
		public List<EPackage> getPackages(ResourceSet rs) {
			List<EPackage> packages = new ArrayList<EPackage>();
			Resource r = this.load(rs);
			r.getAllContents().forEachRemaining(o -> {
				if (o instanceof EPackage) {
					EPackage pkg = (EPackage) o;
					packages.add(pkg);						
				}
			});
			return packages;
		}

		@Override
		public String getName() {
			return model.getFile().getName();
		}
	}
	
}
