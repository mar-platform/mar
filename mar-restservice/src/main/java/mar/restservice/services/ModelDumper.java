package mar.restservice.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Nonnull;

import mar.restservice.services.SearchOptions.ModelType;

public class ModelDumper implements AutoCloseable {

	private final File temp;

	public ModelDumper(String contents, ModelType type) throws IOException {
		this.temp = File.createTempFile("uploaded-file", "." + type.name());
		
		try (FileWriter writer = new FileWriter(temp)) {			
			writer.append(contents);
		}
	}
	
	@Nonnull
	public File getFile() {
		return temp;
	}

	@Override
	public void close() throws IOException {
		temp.delete();		
	}

}
