package mar.analysis.inference;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "infer", mixinStandardHelpOptions = true, description = "Infer a meta-model from a set of files")
public class InferMetamodelMain implements Callable<Integer> {

	@Parameters(index = "0", description = "Model folder")
	private File modelFolder;
	@Parameters(index = "1", description = "Output meta-model")
	private File outputMetamodel;

	@Option(required = false, names = { "-e", "--extension" }, description = "Filter files by extension")
	private String extension = "";

	@Option(required = false, names = { "-t", "--test" }, description = "Instead of generating a meta-model it uses the output metamodel as source for the test")
	private boolean test = false;

	@Override
	public Integer call() throws Exception {
		List<File> files = Files.walk(modelFolder.toPath()).
				filter(p -> p.toString().endsWith(extension)).
				map(Path::toFile).collect(Collectors.toList());

		EPackage root;
		if (! test) {
			root = new MetamodelInference().infer(files);
	
			XMIResourceImpl m = new XMIResourceImpl(URI.createFileURI(outputMetamodel.getAbsolutePath()));
			m.getContents().add(root);
			m.save(null);
	
			System.out.println("Written to: " + outputMetamodel);
		} else {
			ResourceSet rs = new ResourceSetImpl();
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			Resource r = rs.getResource(URI.createFileURI(outputMetamodel.getAbsolutePath()), true);
			root = (EPackage) r.getContents().get(0);
		}
		
		int ok  = 0;
		int bad = 0;
		for(File f : files) {
			ResourceSet rs = new ResourceSetImpl();
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
			rs.getPackageRegistry().put(root.getNsURI(), root);
			try {
				Resource r = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);
				r.unload();
				ok++;
			} catch (Exception e) {
				if (e.getMessage().contains("Content is not allowed in prolog")) {
					System.out.println("Invalid model: " + f);
					continue;
				} else {
					System.out.println("Not inferred correctly: " + f);
					e.printStackTrace();
				}
				bad++;
			}
			System.out.println("Ok: " + ok + ". Bad: " + bad + ". " + f);
		}
		
		return 0;
	}
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new InferMetamodelMain()).execute(args);
		System.exit(exitCode);
	}
}
