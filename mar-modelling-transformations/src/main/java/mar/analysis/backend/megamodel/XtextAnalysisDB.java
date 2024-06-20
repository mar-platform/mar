package mar.analysis.backend.megamodel;

import java.io.File;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;

import edu.emory.mathcs.backport.java.util.Collections;
import mar.validation.AnalysisDB;

public class XtextAnalysisDB extends AnalysisDB {

	public XtextAnalysisDB(File file) {
		super(file);
	}
	
	@Override
	public List<Model> getValidModels(@Nonnull Function<String, String> relativePathTransformer) throws SQLException {
		List<Model> models = super.getValidModels(relativePathTransformer);
		List<Model> results = new ArrayList<>();
		PreparedStatement statement = connection.prepareStatement("select type, value from models join metadata on models.id = metadata.id where models.id = ?");
		for (Model model : models) {
			XtextModel xtext = new XtextModel(model.getId(), model.getRelativePath(), model.getFile(), model.getMetadata());
			results.add(xtext);
			
			statement.setString(1, model.getId());
			statement.execute();
			
			ResultSet rs = statement.getResultSet();
			while (rs.next()) {
				String type  = rs.getString(1);
				String value = rs.getString(2);
				if (type.equals("generatedURIs")) {
					xtext.addGeneratedMetamodel(value.split("|"));
				} else if (type.equals("importedURIs")) {
					xtext.addImportedMetamodel(value.split("|"));
				}
			}

		}
		
		return results;
	}

	
	public static class XtextModel extends Model {

		private Set<String> generatedMetamodels = new HashSet<>();
		private Set<String> importedMetamodels  = new HashSet<>();
		
		public XtextModel(String id, Path relative, File file, String metadata) {
			super(id, relative, file, metadata);
		}

		public void addImportedMetamodel(String... uris) {
			Collections.addAll(importedMetamodels, uris);
		}

		public void addGeneratedMetamodel(String... uris) {
			Collections.addAll(generatedMetamodels, uris);			
		}
		
		public Set<? extends String> getGeneratedMetamodels() {
			return generatedMetamodels;
		}
		
		public Set<? extends String> getImportedMetamodels() {
			return importedMetamodels;
		}
		
	}

}
