package mar.restservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.MarConfiguration;
import mar.model2graph.PathComputation;

public class HbasePerformanceTest extends MarTest {

	//private final PathComputation path = new Model2GraphAllpaths(4);
	//private final IScorer scorer = new HbaseScorer(path);
	
	private final MarConfiguration configuration = MarConfiguration.getHbaseConfiguration("ecore");
	private final PathComputation path = configuration.getPathComputation();
	private final IScorer scorer = configuration.getScorer();
	
	private boolean random = true; 
	
	public static void main(String[] args) throws IOException {
		String repo = args[0];
		String listFilename = args[1];
		String nameHint = args[2];
		nameHint = nameHint.replaceAll("/", "_").replaceAll(".txt", "").replace(" ", "_");
		
		System.out.println("Running...");		
		System.out.println(repo);
		System.out.println(listFilename);
		System.out.println(nameHint);
		new HbasePerformanceTest().test(repo, listFilename, nameHint + "_performance.csv");
	}

	public void test(String prefix, String filename, String resultFileName) throws IOException {
		FileWriter fw = new FileWriter(resultFileName);
		fw.write("File");
		fw.write(",");
		fw.write("Paths");
		fw.write(",");
		fw.write("Prepare");
		fw.write(",");
		fw.write("Get");
		fw.write(",");
		fw.write("Score");
		fw.write(",");
		fw.write("ResultSize");
		fw.write("\n");

		List<Path> paths = new ArrayList<>();
		List<String> lines = FileUtils.readLines(new File(filename));
		for (String f : lines) {
			paths.add(Paths.get(f));
		}
		
		if (random) {
			Collections.shuffle(paths);
		}
		
		for (Path original : paths) {
			try {
				//Path original = mutantsFolder.toPath().resolveSibling(t);
				// Path original = mutantsFolder.toPath().relativize(path);
				Path path = Paths.get(prefix, original.toString());
				evaluate(original, path, fw);
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		fw.close();
	}
	
	public void evaluate(@NonNull Path original, @NonNull Path path, @NonNull FileWriter fw) throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		File f = path.toFile();
		//File f = new File("src/test/resources/relational-query.ecore");
		Resource r = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);
		
		int TIMES = 1;
		for(int i = 0; i < TIMES; i++) {
			System.out.println("Testing " + original);
			StopWatch watch = new StopWatch();
			watch.start();
			{
				Profiler profiler = new Profiler();
				Map<String, Double> result = scorer.score(r, profiler);
				Map<? extends String, Double> times = profiler.getTimes();

				fw.write(original.toString());
				fw.write(",");
				fw.write(timeToString(times.get("paths")));
				fw.write(",");
				fw.write(timeToString(times.get("prepare-gets")));
				fw.write(",");
				fw.write(timeToString(times.get("get")));
				fw.write(",");
				fw.write(timeToString(times.get("score")));
				fw.write(",");
				fw.write(String.valueOf(result.size()));
				fw.write("\n");
			}
			watch.stop();
			System.out.println("Total: " + String.format("%.2f", watch.getTime() / 1000.0));
		}

	}

	private String timeToString(double d) {
		return String.format(Locale.US, "%.2f", d);
	}
	
	
	public boolean isEcore(Path path, BasicFileAttributes attrs) {
		return path.toString().endsWith(".ecore") && attrs.isRegularFile();
	}
}
