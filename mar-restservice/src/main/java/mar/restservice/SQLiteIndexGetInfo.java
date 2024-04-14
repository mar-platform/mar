package mar.restservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import mar.restservice.model.IModelResult;
import mar.sqlite.SqliteIndexDatabase;
import mar.utils.Utils;
import mar.validation.AnalysisMetadataDocument;

public class SQLiteIndexGetInfo implements ModelDataAccessor {

	private SqliteIndexDatabase db;

	public SQLiteIndexGetInfo(SqliteIndexDatabase db) {
		this.db = db;
	}

	@Override
	public void close() throws IOException {
	}
		

	@Override
	public void updateInformation(List<? extends IModelResult> models) throws IOException {
		for (IModelResult m : models) {
			String metadata = getMetadata(m.getId());
			AnalysisMetadataDocument analysisDoc;
			if (Utils.isNullOrEmpty(metadata)) {
				analysisDoc = new AnalysisMetadataDocument();
			} else {
				analysisDoc = AnalysisMetadataDocument.loadFromJSON(metadata);
			}
			m.setMetadata(analysisDoc);
		}
	}

	@Override
	public String getMetadata(String id) throws IOException {
		try {
			String metadata = db.getModelMetadataById(id);
			return metadata;
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

}
