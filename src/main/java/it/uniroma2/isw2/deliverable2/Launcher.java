package it.uniroma2.isw2.deliverable2;

import it.uniroma2.isw2.deliverable2.exceptions.MaximumRequestToGithubAPIException;
import it.uniroma2.isw2.deliverable2.exceptions.MissingGithubTokenException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {
	
	private static final String PROJECT_NAME = "BOOKKEEPER";
	private static final String RESULTS_FOLDER = "results/";
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2(MAIN)");


	public static void main(String[] args) {

		final String errorMsgFmt = "An error occurred invoking {0} function: {1}";

		/* Needed for SMOTE in presence of NaN */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		try {
			setupResultsFolder();
			new MetricsExtractor(PROJECT_NAME, RESULTS_FOLDER).extract();
			new MachineLearningAnalyser(PROJECT_NAME, RESULTS_FOLDER).finalizeAnalysis();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, errorMsgFmt, new Object [] {"setupResultsFolder", e.getMessage()});
		} catch (MissingGithubTokenException | MaximumRequestToGithubAPIException e) {
			LOGGER.log(Level.SEVERE, errorMsgFmt, new Object [] {"MetricsExtractor#extract", e.getMessage()});
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, errorMsgFmt, new Object [] {"MachineLearningAnalyser#finalizeAnalysis", e.getMessage()});
		}
	}
	
	private static void setupResultsFolder() throws IOException {
		Path resultPath = Paths.get(RESULTS_FOLDER);
		if (!Files.exists(resultPath))
			Files.createDirectory(resultPath);
		
		Path []datasets = {
				Paths.get(String.format("%s%s_all_metrics.csv", RESULTS_FOLDER, PROJECT_NAME)),
				Paths.get(String.format("%s%s_all_metrics.arff", RESULTS_FOLDER, PROJECT_NAME)),
				Paths.get(String.format("%s%s_final_results.csv", RESULTS_FOLDER, PROJECT_NAME))
		};
		
		for (Path ds : datasets) {
			if (Files.exists(ds))
				Files.delete(ds);
			
			Files.createFile(ds);
		}
	}
}
