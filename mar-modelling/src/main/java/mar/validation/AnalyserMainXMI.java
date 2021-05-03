package mar.validation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import mar.modelling.xmi.XMIAnalyser;
import mar.modelling.xmi.XMIAnalyser.Factory;
import mar.validation.ISingleFileAnalyser.Remote;
import mar.validation.ResourceAnalyser.OptionMap;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "analyser-xmi", mixinStandardHelpOptions = true,
	description = "Analyses XMI files")
public class AnalyserMainXMI implements Callable<Integer> {

    @Parameters(index = "0", description = "Root folder.")
    private File root;

    @Parameters(index = "1", description = "Output database.")
    private File outputDatabase;

    @Parameters(index = "2", description = "Ecore database file")
    private File ecoreDatabaseFile;

    @Parameters(index = "3", description = "Ecore root folder")
    private File ecoreRootFolder;

    @Override
    public Integer call() throws Exception {    	    	
    	System.out.println("Collecting files...");
    	
    	Factory factory = new XMIAnalyser.Factory();  
    	OptionMap options = new OptionMap();
    	options.put(XMIAnalyser.ECORE_DATABASE_FILE, ecoreDatabaseFile.getAbsolutePath());
    	options.put(XMIAnalyser.ECORE_ROOT_FOLDER, ecoreRootFolder.getAbsolutePath());
    	
		factory.configureEnvironment();
		IFileProvider provider = new IFileProvider() {			
			@Override
			public List<? extends IFileInfo> getLocalFiles() {
				try {
					return Files.walk(root.toPath()).
						map(p -> p.toFile()).
						filter(f -> f.isFile()).
						filter(f -> f.getPath().endsWith(".xmi")).
						map(f -> new IFileInfo.FileInfo(root, f)).
						collect(Collectors.toList());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	    	}
		};
		
		//ISingleFileAnalyser singleAnalyser = factory.newAnalyser()) {
		try(ISingleFileAnalyser.Remote singleAnalyser = (Remote) factory.newRemoteAnalyser(options)) {			
			try(ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, provider, outputDatabase)) {
				analyser.check();
			}
		}
    	
    	return 0;
    }
    
	public static void main(String[] args) {
        int exitCode = new CommandLine(new AnalyserMainXMI()).execute(args);
        System.exit(exitCode);
	}
	
}
