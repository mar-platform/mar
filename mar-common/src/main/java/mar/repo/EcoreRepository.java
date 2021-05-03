package mar.repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import edu.umd.cs.findbugs.annotations.NonNull;

public class EcoreRepository {

	private final List<Item> items;

	public EcoreRepository(@NonNull File repo) throws IOException {
		this.items = new ArrayList<Item>(1024);
		readRepositoryData(repo);
	}

	private void readRepositoryData(File repo) throws IOException {
		// Files.readAllLines(Paths.get(analysisFolder.getAbsolutePath(), "valids.txt"));
		// For simplicity, assume that stats.txt contains all the valid files
		
		File stats = Paths.get(repo.getAbsolutePath(), "analysis", "stats.txt").toFile();
		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader(new FileInputStream(stats)));
		for (CSVRecord record : csvParser) {
		    String localFile = record.get(0);
		    int total = Integer.parseInt(record.get(1));
		    int packages = Integer.parseInt(record.get(2));
		    int classes = Integer.parseInt(record.get(3));
		    int references = Integer.parseInt(record.get(4));
		    int attributes = Integer.parseInt(record.get(5));		    
		
		    this.items.add(new Item(localFile, total, packages, classes, references, attributes));
		}
	
		csvParser.close();
	}

	private List<? extends Item> getItems() {
		return items;
	}
	
	private List<Item> getItems(@NonNull Predicate<Item> predicate) {
		return items.stream().filter(predicate).collect(Collectors.toList());
	}
	
	private List<Item> getNormalItems() {
		return getItems(i -> i.classes * (i.attributes + i.references) <= 5000 && i.total < 6000);
	}
	
	public static class Item {
		private String localFile;
		private int packages;
		private int classes;
		private int references;
		private int attributes;
		private int total;
		
		public Item(String localFile, int total, int packages, int classes, int references, int attributes) {
			this.total = total;
			this.localFile = localFile;
			this.packages = packages;
			this.classes = classes;
			this.references = references;
			this.attributes = attributes;
		}
		
		public String getLocalFile() {
			return localFile;
		}
		
		public int getTotal() {
			return total;
		}
		
		public int getNumPackages() {
			return packages;
		}
		
		public int getNumClasses() {
			return classes;
		}
		
		public int getNumReferences() {
			return references;
		}
		
		public int getNumAttributes() {
			return attributes;
		}
	}
}
