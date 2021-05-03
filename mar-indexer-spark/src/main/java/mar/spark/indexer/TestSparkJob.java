package mar.spark.indexer;

import java.io.IOException;

import org.apache.spark.launcher.SparkLauncher;

public class TestSparkJob {

	public static void main(String[] args) throws IOException, InterruptedException {
       Process spark = new SparkLauncher()
    	         //.setAppResource("/my/app.jar")
    	         .setMainClass("my.spark.app.Main")
    	         .setMaster("local")
    	         .setConf(SparkLauncher.DRIVER_MEMORY, "2g")
    	         .launch();
       spark.waitFor();
	}
	
}
