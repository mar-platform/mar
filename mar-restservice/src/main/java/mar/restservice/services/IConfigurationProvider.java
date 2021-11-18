package mar.restservice.services;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.MarChatBotConfiguration;
import mar.MarConfiguration;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.lucene.core.ITextSearcher;

public interface IConfigurationProvider {

	@Nonnull
	public MarConfiguration getConfiguration(@NonNull String modelType);

	@Nonnull
	public ITextSearcher newSearcher();

	@Nonnull
	public IndexJobConfigurationData getIndexJobConfiguration();

	public String getModelFile(String id);
	
	public MarChatBotConfiguration getChatBotConfiguration(@NonNull String modelType);
}
