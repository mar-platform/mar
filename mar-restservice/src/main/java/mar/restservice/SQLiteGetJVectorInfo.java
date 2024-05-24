package mar.restservice;

import mar.embeddings.IndexedDB;
import mar.embeddings.IndexedDB.IndexedModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import mar.restservice.model.IModelResult;
import mar.utils.Utils;
import mar.validation.AnalysisMetadataDocument;

public class SQLiteGetJVectorInfo implements ModelDataAccessor {

	private IndexedDB db;

	public SQLiteGetJVectorInfo(Path dbFile) {
		db = new IndexedDB(dbFile.toFile(), IndexedDB.Mode.READ);
	}

	@Override
	public void close() throws IOException {
		db.close();
	}

	@Override
	public void updateInformation(List<? extends IModelResult> models) throws IOException {
		for (IModelResult m : models) {
			IndexedModel storedModel = db.getByModelId(m.getId());
			AnalysisMetadataDocument analysisDoc;
			if (storedModel == null || Utils.isNullOrEmpty(storedModel.getMetadata())) {
				analysisDoc = new AnalysisMetadataDocument();
			} else {
				// System.out.println("Metadata: " + storedModel.getMetadata());
				analysisDoc = AnalysisMetadataDocument.loadFromJSON(storedModel.getMetadata());
			}
			m.setMetadata(analysisDoc);
		}
	}

	@Override
	public String getMetadata(String id) throws IOException {
		IndexedModel storedModel = db.getByModelId(id);
		if (storedModel == null)
			return null;
		return storedModel.getMetadata();
	}

}
