package mar.analysis.xtext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.xtext.Grammar;

import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.modelling.loader.ILoader;
import mar.models.xtext.XtextLoader;
import mar.models.xtext.XtextLoader.XtextAnalysisResult;
import mar.validation.AnalysisDB.Status;
import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;

public class XtextAnalyser implements ISingleFileAnalyser {

	public static final String ID = "xtext";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public XtextAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new XtextAnalyser();
		}				
		
		@Override
		public String getId() {
			return ID;
		}

		@Override
		public ILoader newLoader() {
			return new XtextLoader();
		}
		
	}
	
	@Override
	public AnalysisResult analyse(IFileInfo f) {
		XtextLoader loader = new XtextLoader();		
		try {
			XtextAnalysisResult r = loader.doAnalysis(f.getFullFile());
			
			AnalysisResult res = new AnalysisResult(f.getModelId(), Status.VALID)
					.withStats(computeStats(r.getGrammar()))
					.withMetadata(computeMetadata(r));
			
			return res;
		} catch (IOException e) {
			return new AnalysisResult(f.getModelId(), Status.CRASHED);
		}
	}

	private Map<String, List<String>> computeMetadata(XtextAnalysisResult r) {
		Map<String, List<String>> metadata = new HashMap<String, List<String>>();
		
		if (! r.getImportedURIs().isEmpty())		
			metadata.put("importedURIs", new ArrayList<>(r.getImportedURIs()));
		
		if (! r.getGeneratedURIs().isEmpty())		
			metadata.put("generatedURIs", new ArrayList<>(r.getGeneratedURIs()));

		return metadata;
	}

	private Map<String, Integer> computeStats(Grammar grammar) {
		Map<String, Integer> stats = new HashMap<String, Integer>();
		stats.put("numRules", grammar.getRules().size());
		return stats;
	}

	private static class XtextMetamodelAnalyser extends SingleEcoreFileAnalyser {
				
		private Collection<? extends EPackage> packages;

		public XtextMetamodelAnalyser(XtextAnalysisResult r) {
			this.packages = r.getPackages();
		}

		@Override
		protected Resource loadModel(IFileInfo f) throws IOException {
			try {				
				Resource res = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath() + ".ecore"));
			    for (EPackage pkg : packages) {
					res.getContents().add(pkg);
				}
			    return res;
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException(e);
			}
		}
		
	}
	


}
