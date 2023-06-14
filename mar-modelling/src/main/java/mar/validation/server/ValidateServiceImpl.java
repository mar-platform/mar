package mar.validation.server;

import java.util.Map;

import org.apache.thrift.TException;

import mar.analysis.thrift.InvalidOperation;
import mar.analysis.thrift.Result;
import mar.analysis.thrift.ValidationJob;
import mar.validation.AnalyserRegistry;
import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser.Factory;
import mar.validation.ResourceAnalyser.OptionMap;

public class ValidateServiceImpl implements mar.analysis.thrift.ValidateService.Iface {

	@Override
	public Result validate(ValidationJob job) throws InvalidOperation, TException {
		String type = job.getType();
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(type);
		
		if (factory == null) {
			throw new InvalidOperation("Unknown validation type: " + type);
		}
				
		// This can be problematic if done by different factories...
		factory.configureEnvironment();

		Map<String, String> options = job.getOptions();
		OptionMap map = new OptionMap();
		if (options != null)
			map.putAll(options);
		
		ISingleFileAnalyser analyser = factory.newAnalyser(map);
		AnalysisResult r = analyser.analyse(new IFileInfo.FileInfoById(job.modelId, job.relative_path, job.full_path));		
		return new Result(r.getStatus().name(), r.getStats(), r.getMetadata()).
				setMetadata_json(r.getJsonMetadata());
	}
	
}
