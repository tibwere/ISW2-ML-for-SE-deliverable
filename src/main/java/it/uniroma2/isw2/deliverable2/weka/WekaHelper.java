package it.uniroma2.isw2.deliverable2.weka;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class WekaHelper {
	
	public static void createArffFile(File csvSourceFile, File arffDestinationFile) throws IOException {
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(csvSourceFile);
	    Instances data = loader.getDataSet();
	    
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(arffDestinationFile);
	    saver.writeBatch();
	    
	    fixHeader(arffDestinationFile);
	}
	
	private static void fixHeader(File arff) throws IOException {
		final int VERSION_INDEX_IN_ARFF = 2;
		final int NAME_INDEX_IN_ARFF = 3;
		final int BUGGYNESS_INDEX_IN_ARFF = 15;
		
		final String CORRECT_VERSION_HEADER = "@attribute Version string";
		final String CORRECT_NAME_HEADER = "@attribute Name string";
		final String CORRECT_BUGGYNESS_HEADER = "@attribute Buggyness class {Y, N}";
		
		
		Path filePath = Paths.get(arff.getAbsolutePath());
		List<String> fileContent = new ArrayList<>(Files.readAllLines(filePath, StandardCharsets.UTF_8));
		fileContent.set(VERSION_INDEX_IN_ARFF, CORRECT_VERSION_HEADER);
		fileContent.set(NAME_INDEX_IN_ARFF, CORRECT_NAME_HEADER);
		fileContent.set(BUGGYNESS_INDEX_IN_ARFF, CORRECT_BUGGYNESS_HEADER);
		
		Files.write(filePath, fileContent, StandardCharsets.UTF_8);		
	}
	
	

}
