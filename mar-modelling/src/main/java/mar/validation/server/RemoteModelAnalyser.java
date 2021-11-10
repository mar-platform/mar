package mar.validation.server;

import javax.annotation.Nonnull;

import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;

/**
 * A remote analyser for single type of models.
 * @author jesus
 *
 */
public class RemoteModelAnalyser extends AnalysisClient implements ISingleFileAnalyser.Remote   {

	@Nonnull
	private final String type;

	public RemoteModelAnalyser(String type, OptionMap options) {
		super(options);
		this.type = type;
	}
	
	@Override
	public AnalysisResult analyse(IFileInfo f) {
		return analyse(f, type);
	}

}
