package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launcher {
	
	private static final String PROJECT_NAME = "STORM";
	private static final String RESULTS_FOLDER = "results/";
	
	public static void main(String[] args) throws Exception {
		
		/* Needed for SMOTE in presence of NaN */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		setupResultsFolder();
		
		new MetricsExtractor(PROJECT_NAME, RESULTS_FOLDER).extract();		
		new MachineLearningAnalyser(PROJECT_NAME, RESULTS_FOLDER).finalizeAnalysis();
	}
	
	private static void setupResultsFolder() throws IOException {
		Path resultPath = Paths.get(RESULTS_FOLDER);
		if (!Files.exists(resultPath))
			Files.createDirectory(resultPath);
		
		Path []datasets = {
				Paths.get(String.format("%s%s_all_metrics.csv", RESULTS_FOLDER, PROJECT_NAME)),
				Paths.get(String.format("%s%s_final_results.csv", RESULTS_FOLDER, PROJECT_NAME))
		};
		
		for (Path ds : datasets) {
			if (Files.exists(ds))
				Files.delete(ds);
			
			Files.createFile(ds);
		}
	}
}
