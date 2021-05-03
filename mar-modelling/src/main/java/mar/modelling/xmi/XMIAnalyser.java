package mar.modelling.xmi;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mar.analysis.ecore.EcoreRepository;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Status;
import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.server.AnalysisClient;

public class XMIAnalyser implements ISingleFileAnalyser {

	public static final String ID = "xmi";
	
	public static final String ECORE_DATABASE_FILE = "ECORE_DATABASE_FILE";
	public static final String ECORE_ROOT_FOLDER   = "ECORE_ROOT_FOLDER";
	
	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		}
		
		@Override
		public XMIAnalyser newAnalyser(@CheckForNull OptionMap options) {
			File ecoreDatabaseFile = new File(options.getOption("ECORE_DATABASE_FILE"));
			File ecoreRootFolder = new File(options.getOption("ECORE_ROOT_FOLDER"));
			return new XMIAnalyser(ecoreDatabaseFile, ecoreRootFolder);
		}				
		
		@Override
		public ISingleFileAnalyser newRemoteAnalyser(@CheckForNull OptionMap options) {
			return new AnalysisClient(ID, options);
		}
	}
	
	private final EcoreRepository repo;

	public XMIAnalyser(@Nonnull File ecoreDatabaseFile, File ecoreRootFolder) {
		this.repo = new EcoreRepository(new AnalysisDB(ecoreDatabaseFile), ecoreRootFolder);
	}
	
	@Override
	public AnalysisResult analyse(IFileInfo f) {
		try {
			URIExtractor data = URIExtractor.fromXMI(f.getRelativeFile());
			boolean loaded = data.tryLoad(repo);
			return new AnalysisResult(f.getModelId(), loaded ? Status.VALID : Status.NOT_HANDLED);
		} catch (IOException e) {
			return new AnalysisResult(f.getModelId(), Status.CRASHED);
		}
	}

}
