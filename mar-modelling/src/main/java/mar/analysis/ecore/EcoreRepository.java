package mar.analysis.ecore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public class EcoreRepository {

	@Nonnull
	private final AnalysisDB db;
	@Nonnull
	private final File rootFolder;

	public EcoreRepository(@Nonnull AnalysisDB db, @Nonnull File rootFolder) {
		this.db = db;
		this.rootFolder = rootFolder;
	}

	@Nonnull
	public List<EcoreModel> findEcoreByURI(String uri) {
		List<Model> models = db.findByMetadata("nsURI", uri, (relative) -> rootFolder + File.separator + relative);
		ArrayList<EcoreModel> result = new ArrayList<>(models.size());
		for (Model model : models) {
			result.add(new EcoreModel(model));
		}
		return result;
	}

	public class EcoreModel {
		@Nonnull
		private final Model model;

		public EcoreModel(Model m) {
			this.model = m;
		}

		@Nonnull
		public Resource load(ResourceSet rs) {
			String path = model.getFile().getAbsolutePath();
			return rs.getResource(URI.createFileURI(path), true);
		}
	}
	
}
