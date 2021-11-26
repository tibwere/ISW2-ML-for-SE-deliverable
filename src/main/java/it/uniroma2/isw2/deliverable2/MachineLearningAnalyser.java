package it.uniroma2.isw2.deliverable2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static final String CSV_HEADER = "Dataset,#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa\n";
 	private static final int VERSION_IDX = 0;	
	
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
		AbstractClassifier []classifiers = {
				new RandomForest(),
				new NaiveBayes(),
				new IBk()
		};
		
		for (AbstractClassifier c : classifiers)
			this.walkForward(c);
	}
	
	private void walkForward(AbstractClassifier classifier) throws Exception {

		File outfile = new File(String.format("%s%s_milestone2.csv", this.resultsFolder, this.projectName));
		try (FileWriter writer = new FileWriter(outfile, true)) {
			writer.append(CSV_HEADER);
			for (int testingIdx = 1; testingIdx < this.versionNames.size(); ++testingIdx) {
				Instances trainingSet = new Instances(this.fullDataset, 0);
				Instances testingSet = new Instances(this.fullDataset, 0);

				for (Instance row : fullDataset) {
					if (this.versionNames.indexOf(row.stringValue(VERSION_IDX)) == testingIdx)
						testingSet.add(row);
					if (this.versionNames.indexOf(row.stringValue(VERSION_IDX)) < testingIdx)
						trainingSet.add(row);
				}

				LOGGER.log(Level.INFO,
						"Training set size: {0} [Releases up to {1}]- Testing set size: {2} [Release {3}]",
						new Object[] { trainingSet.size(), this.versionNames.get(testingIdx - 1), testingSet.size(),
								this.versionNames.get(testingIdx) });

				writer.append(this.evaluation(trainingSet, testingSet, classifier, testingIdx));
			}
		}
	}
	
	private String evaluation(Instances trainingSet, Instances testingSet, AbstractClassifier classifier, int trainingSize) throws Exception {
		int numberOfAttributes = trainingSet.numAttributes();
		trainingSet.setClassIndex(numberOfAttributes - 1);
		testingSet.setClassIndex(numberOfAttributes - 1);
		classifier.buildClassifier(trainingSet);
		Evaluation eval = new Evaluation(testingSet);
		eval.evaluateModel(classifier, testingSet);
		
		return String.format("%s,%s,%s,%.3f,%.3f,%.3f,%.3f%n",
				this.projectName,trainingSize,classifier.getClass().getSimpleName(),
				eval.precision(1),eval.recall(1),eval.areaUnderPRC(1),eval.kappa());
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
