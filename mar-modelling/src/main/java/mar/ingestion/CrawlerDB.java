package mar.ingestion;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Our standarized SQLite3 database for the GitHub crawlers.
 * 
 * Schema for GitHub:
 * <pre>
 *  CREATE TABLE data (path text PRIMARY KEY, name text, download_url text, size integer, license text, repo_id integer);
 *  CREATE TABLE repo_info (id integer PRIMARY KEY, name text, full_name text, html_url, git_url text, stargazers_count integer, forks_count integer, topics text);
 * </pre>
 * 
 * @author jesus
 */
public class CrawlerDB implements IngestionDB {

	private final List<IngestedModel> models = new ArrayList<IngestedModel>();
	
	@Nonnull
	public CrawlerDB(@Nonnull String modelType, @Nonnull String origin, @Nonnull String rootFolder, @Nonnull File file) {
		String url = getConnectionString(file);
		try (Connection connection = DriverManager.getConnection(url)){

			// TODO: Join with repo and create a complete model
			PreparedStatement allModels = connection.prepareStatement("SELECT "
					+ "model_id,"					// 1
					+ "filename,"					// 2
					+ "d.name, "					// 3
					+ "download_url, "				// 4
					+ "size, "						// 5
					+ "stargazers_count as stars, " // 6
					+ "forks_count as forks, "		// 7
					+ "coalesce(d.topics || ',', '') || r.topics "// 8
					+ "FROM data d LEFT JOIN repo_info r ON d.repo_id = r.id");
			allModels.execute();
			ResultSet rs = allModels.getResultSet();
			while (rs.next()) {
				String model_id = rs.getString(1);
				String fname = rs.getString(2);
				String name = rs.getString(3);
				String download_url = rs.getString(4);
				int size = rs.getInt(5);
				int stars = rs.getInt(6);
				int forks = rs.getInt(7);
				String topicsStr = rs.getString(8);
				String[] topics;
				if (topicsStr != null) {
					topics = topicsStr.split(",");
				} else {
					topics = new String[0];
				}
				
				String id = IngestedModel.newId(origin, modelType, model_id);
				File path = new File(rootFolder + File.separator + fname); 
				models.add(new IngestedModel(id, new File(fname), path, download_url).
						withSizeBytes(size).
						withStars(stars).
						withForks(forks).
						withTopics(topics));
			}
			allModels.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

	@Override
	public List<? extends IngestedModel> getModels() {
		return models;
	}

}
