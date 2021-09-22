package mar.renderers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.Nonnegative;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class PlantUmlText {

	private StringBuilder builder;

	public PlantUmlText() {
		builder = new StringBuilder();
	}
	
	public PlantUmlText line(String string) {
		builder.append(string);
		builder.append("\n");
		return this;
	}
	
	public PlantUmlText append(String string) {
		builder.append(string);
		return this;
	}

	public void start() {
		line("@startuml");
	}
	
	public void end() {
		line("\n@enduml");
	}
	
	public void toImage(@Nonnegative File file) throws IOException {
		SourceStringReader reader = new SourceStringReader(builder.toString());
		reader.generateImage(file);
		// toText(System.out);
	}
	
	public void toImage(OutputStream os) throws IOException {
		SourceStringReader reader = new SourceStringReader(builder.toString());
		reader.generateImage(os, new FileFormatOption(FileFormat.PNG));		
	}
	
	public void toText(PrintStream out) {
		out.println(builder.toString());
	}
	
}
