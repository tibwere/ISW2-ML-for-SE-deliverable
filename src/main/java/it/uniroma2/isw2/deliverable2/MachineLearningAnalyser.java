package it.uniroma2.isw2.deliverable2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import it.uniroma2.isw2.deliverable2.entities.AnalysisProfile;
import it.uniroma2.isw2.deliverable2.entities.EvaluationResults;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class MachineLearningAnalyser {
	
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2(ML)");
 	private static final int VERSION_IDX = 0;
 	private static final int BUGGYNESS_IDX = 13;
	
	private String projectName;
	private String resultsFolder;
	private List<String> versionNames;
	private Instances fullDataset;
	
	public MachineLearningAnalyser(String projectName, String resultsFolder) throws IOException {
		this.projectName = projectName;
		this.resultsFolder = resultsFolder;
		this.fullDataset = this.loadCSV(new File(String.format("%s%s_all_metrics.csv", this.resultsFolder, this.projectName)));
		this.versionNames = this.getVersionNames();
	}
	
	public void finalizeAnalysis() throws Exception {
				
		File csvDataset = new File(String.format("%s%s_final_results.csv", this.resultsFolder, this.projectName));
		try (FileWriter writer = new FileWriter(csvDataset, false)) {
			writer.append(EvaluationResults.CSV_HEADER);
			for (AnalysisProfile profile : AnalysisProfile.generateAllProfiles())
				this.walkForward(profile, writer);
		}
	}
	
	private void walkForward(AnalysisProfile profile, FileWriter writer) throws Exception {
		
		for (int testingIdx = 1; testingIdx < this.versionNames.size(); ++testingIdx) {
			Instances trainingSet = new Instances(this.fullDataset, 0);
			Instances testingSet = new Instances(this.fullDataset, 0);
			EvaluationResults res = new EvaluationResults(this.projectName, profile);
			int defectsInTraining = 0;
			int defectsInTesting = 0;

			for (Instance row : fullDataset) {
				if (this.versionNames.indexOf(row.stringValue(VERSION_IDX)) == testingIdx) {
					testingSet.add(row);
					if (row.stringValue(BUGGYNESS_IDX).equals("Y"))
						defectsInTesting++;
					
				} else if (this.versionNames.indexOf(row.stringValue(VERSION_IDX)) < testingIdx) {
					trainingSet.add(row);
					if (row.stringValue(BUGGYNESS_IDX).equals("Y"))
						defectsInTraining++;
				}
			}

			res.setNumberOfTrainingReleases(trainingSet.size());
			res.setPercentageOfTrainingReleases(((double)trainingSet.size())/fullDataset.size());
			res.setPercentageOfDefectiveInTraining(((double)defectsInTraining)/(defectsInTesting+defectsInTraining));
			res.setPercentageOfDefectiveInTesting(((double)defectsInTesting)/(defectsInTesting+defectsInTraining));
			
			this.evaluation(trainingSet, testingSet, profile, res);
			writer.append(String.format("%s%n", res));
		}
	}
	
	private void evaluation(Instances trainingSet, Instances testingSet, AnalysisProfile profile, EvaluationResults results) throws Exception {
		int numberOfAttributes = trainingSet.numAttributes();
		trainingSet.setClassIndex(numberOfAttributes - 1);
		testingSet.setClassIndex(numberOfAttributes - 1);
		
		AbstractClassifier classifier = null;
		if (profile.getClassifier().equals(AnalysisProfile.CLASSIFIER_RANDOM_FOREST))
			classifier = new RandomForest();
		else if (profile.getClassifier().equals(AnalysisProfile.CLASSIFIER_NAIVE_BAYES))
			classifier = new NaiveBayes();
		else
			classifier = new IBk();
		
		classifier.buildClassifier(trainingSet);
		
		Evaluation eval = new Evaluation(testingSet);
		eval.evaluateModel(classifier, testingSet);
		
		results.setStatistics(eval);
	}
	
	private Instances loadCSV(File csvFile) throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(csvFile);
		return loader.getDataSet();
	}
	
	private List<String> getVersionNames() {
		List<String> versions = new ArrayList<>();
		
		for (Instance row : this.fullDataset)
			if (!versions.contains(row.stringValue(VERSION_IDX)))
				versions.add(row.stringValue(VERSION_IDX));
		
		return versions;
	}
}
