package it.uniroma2.isw2.deliverable2;

public class Launcher {
	
	private static final String PROJECT_NAME = "STORM";
	private static final String RESULTS_FOLDER = "results/";
	
	public static void main(String[] args) throws Exception {
		new MetricsExtractor(PROJECT_NAME, RESULTS_FOLDER).extract();		
		new MachineLearningAnalyser(PROJECT_NAME, RESULTS_FOLDER).finalizeAnalysis();
	}
}
