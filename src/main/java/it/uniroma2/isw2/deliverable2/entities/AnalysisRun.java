package it.uniroma2.isw2.deliverable2.entities;

import it.uniroma2.isw2.deliverable2.MachineLearningAnalyser;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnalysisRun {
	
	private static final double CFP = 1.0;
	private static final double CFN = 10.0;
		
 	private Instances trainingSet;
	private Instances testingSet;
	private EvaluationResults results;

	private static final String UNSUPPORTED_ERR_MSG = "Unsupported %s type (%s)";
	
	public AnalysisRun(Instances fullDataset, AnalysisProfile profile, String project) {
		this.trainingSet = new Instances(fullDataset, 0);
		this.testingSet = new Instances(fullDataset, 0);
		this.results = new EvaluationResults(project, profile);
	}

	public void addToTesting(Instance row) {
		this.testingSet.add(row);		
	}
	
	public void addToTraining(Instance row) {
		this.trainingSet.add(row);		
	}
	
	public void setupClassIndexes() {
		int numberOfAttributes = this.trainingSet.numAttributes();

		this.trainingSet.setClassIndex(numberOfAttributes - 1);
		this.testingSet.setClassIndex(numberOfAttributes - 1);
	}
	
	public void initializeResults(int fullSize) {
		this.results.setNumberOfTrainingReleases(this.trainingSet.size());
		this.results.setPercentageOfTrainingReleases(((double)trainingSet.size())/fullSize);
		
		int trainingDefects = this.getDefectsInTraining();
		int testingDefects = this.getDefectsInTesting();
		int totalDefects = trainingDefects + testingDefects;

		if (totalDefects > 0) {
			this.results.setPercentageOfDefectiveInTraining(((double)trainingDefects)/(totalDefects));
			this.results.setPercentageOfDefectiveInTesting(((double)testingDefects)/(totalDefects));
		} else {
			this.results.setPercentageOfDefectiveInTraining(0);
			this.results.setPercentageOfDefectiveInTesting(0);
		}
	}
	
	private void applyFeatureSelection(String selectionType) throws Exception {

		switch (selectionType) {
			case AnalysisProfile.FEATURE_SELECTION_NO:
				break;
			case AnalysisProfile.FEATURE_SELECTION_BEST_FIRST:
				Remove removeFilter = this.getRemoveFilter(getSelectedIndexes());
				this.trainingSet = Filter.useFilter(this.trainingSet, removeFilter);
				this.testingSet = Filter.useFilter(this.testingSet, removeFilter);

				setupClassIndexes();
				break;
			default:
				throw new IllegalArgumentException(String.format(UNSUPPORTED_ERR_MSG, "feature selection",
						selectionType));
		}
	}
	
	private void applySampling(String samplingType) throws Exception {

		List<Integer> countYN = getNumberOfYN();
		int majoritySize = Collections.max(countYN);
		int minoritySize = Collections.min(countYN);
		String []opts;

		switch (samplingType) {
			case AnalysisProfile.SAMPLING_NO:
				break;
			case AnalysisProfile.SAMPLING_UNDERSAMPLING:
				SpreadSubsample spreadSubsample = new SpreadSubsample();

				// Choose uniform distribution for spread
				// (see: https://weka.sourceforge.io/doc.dev/weka/filters/supervised/instance/SpreadSubsample.html)
				opts = new String[]{"-M", "1.0"};

				spreadSubsample.setOptions(opts);
				spreadSubsample.setInputFormat(this.trainingSet);
				this.trainingSet = Filter.useFilter(this.trainingSet, spreadSubsample);

				break;
			case AnalysisProfile.SAMPLING_OVERSAMPLING:
				Resample resample = new Resample();

				// -B -> Choose uniform distribution
				// (see: https://weka.sourceforge.io/doc.dev/weka/filters/supervised/instance/Resample.html)
				// -Z -> From https://waikato.github.io/weka-blog/posts/2019-01-30-sampling/
				// "where Y/2 is (approximately) the percentage of data that belongs to the majority class"
				String z = Double.toString(2 * ((double) majoritySize / this.trainingSet.size()) * 100);
				opts = new String[]{"-B", "1.0", "-Z", z};

				resample.setOptions(opts);
				resample.setInputFormat(this.trainingSet);
				this.trainingSet = Filter.useFilter(this.trainingSet, resample);

				break;
			case AnalysisProfile.SAMPLING_SMOTE:
				SMOTE smote = new SMOTE();

				// Percentage of SMOTE instances to create
				// (see: https://weka.sourceforge.io/doc.packages/SMOTE/weka/filters/supervised/instance/SMOTE.html)
				String p = (minoritySize > 0) ?
						Double.toString(100.0 * (majoritySize - minoritySize) / minoritySize) : "100.0";
				opts = new String[]{"-P", p};

				smote.setOptions(opts);
				smote.setInputFormat(this.trainingSet);
				this.trainingSet = Filter.useFilter(this.trainingSet, smote);
				break;

			default:
				throw new IllegalArgumentException(String.format(UNSUPPORTED_ERR_MSG, "sampling", samplingType));
		}
	}

	private List<Integer> getNumberOfYN() {
		int countYes = 0;
		int countNo = 0;

		for (Instance row : this.trainingSet) {
			if (row.stringValue(row.classIndex()).equals("Y"))
				countYes++;
			else
				countNo++;
		}

		return Arrays.asList(countNo, countYes);
	}

	private CostMatrix getCostMatrix() {
		CostMatrix costMatrix = new CostMatrix(2);
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(0, 1, AnalysisRun.CFP);
		costMatrix.setCell(1, 0, AnalysisRun.CFN);
		costMatrix.setCell(1, 1, 0.0);
		return costMatrix;
	}

	private AbstractClassifier getBasicClassifier(String type) {
		AbstractClassifier basicClassifier;
		switch (type) {
			case AnalysisProfile.CLASSIFIER_RANDOM_FOREST:
				basicClassifier = new RandomForest();
				break;

			case AnalysisProfile.CLASSIFIER_NAIVE_BAYES:
				basicClassifier = new NaiveBayes();
				break;

			case AnalysisProfile.CLASSIFIER_IBK:
				basicClassifier = new IBk();
				break;

			default:
				throw new IllegalArgumentException(String.format(UNSUPPORTED_ERR_MSG, "classifier", type));
		}

		return basicClassifier;
	}
	
	public void evaluate(AnalysisProfile profile) throws Exception {
		
		AbstractClassifier basicClassifier = getBasicClassifier(profile.getClassifier());

		this.applyFeatureSelection(profile.getFeatureSelectionTechnique());
		this.applySampling(profile.getSamplingTechnique());
		
		Evaluation eval;
		CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
		costSensitiveClassifier.setClassifier(basicClassifier);
		costSensitiveClassifier.setCostMatrix(getCostMatrix());

		switch (profile.getCostSensitiveTechnique()) {
			case AnalysisProfile.COST_SENSITIVE_CLASSIFIER_NO:
				basicClassifier.buildClassifier(trainingSet);
				eval = new Evaluation(this.testingSet);
				eval.evaluateModel(basicClassifier, testingSet);
				break;
			case AnalysisProfile.COST_SENSITIVE_CLASSIFIER_SENSITIVE_THRESHOLD:
			case AnalysisProfile.COST_SENSITIVE_CLASSIFIER_SENSITIVE_LEARNING:
				costSensitiveClassifier.setMinimizeExpectedCost(profile.getCostSensitiveTechnique().equals(
						AnalysisProfile.COST_SENSITIVE_CLASSIFIER_SENSITIVE_THRESHOLD));
				costSensitiveClassifier.buildClassifier(trainingSet);
				eval = new Evaluation(testingSet, costSensitiveClassifier.getCostMatrix());
				eval.evaluateModel(costSensitiveClassifier, testingSet);
				break;
			default:
				throw new IllegalArgumentException(String.format(UNSUPPORTED_ERR_MSG, "cost-sensitive classifier",
						profile.getCostSensitiveTechnique()));
		}
		
		results.setStatistics(eval);	
		
	}
	
	public EvaluationResults getResults() {
		return this.results;
	}
	
	private int getDefectsInTraining() {
		int counter = 0;
		
		for (Instance row : this.trainingSet)
			if (row.stringValue(row.classIndex()).equals("Y"))
				counter++;
		
		return counter;
	}
	
	private int getDefectsInTesting() {
		int counter = 0;
		
		for (Instance row : this.testingSet)
			if (row.stringValue(row.classIndex()).equals("Y"))
				counter++;
		
		return counter;
	} 
	
	private Remove getRemoveFilter(int []attributesSelected) throws Exception {
		
		Remove filter = new Remove();
		filter.setAttributeIndicesArray(attributesSelected);
		filter.setInvertSelection(true);
		filter.setInputFormat(trainingSet);
		
		return filter;
	}
	
	private int[] getSelectedIndexes() throws Exception {
		AttributeSelection filter = new AttributeSelection();
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();

		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.SelectAttributes(this.trainingSet);
		
		return filter.selectedAttributes();
	}

	public void removeUnwantedAttributes() throws Exception {
		int []indices = new int[]{
				MachineLearningAnalyser.VERSION_IDX,
				MachineLearningAnalyser.NAME_IDX
		};

		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(indices);
		removeFilter.setInvertSelection(false);

		/* the structure of training and testing set is the same */
		removeFilter.setInputFormat(this.trainingSet);

		this.trainingSet = Filter.useFilter(this.trainingSet, removeFilter);
		this.testingSet = Filter.useFilter(this.testingSet, removeFilter);
	}
}
