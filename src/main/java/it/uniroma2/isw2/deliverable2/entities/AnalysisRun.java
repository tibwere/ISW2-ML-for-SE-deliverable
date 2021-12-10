package it.uniroma2.isw2.deliverable2.entities;

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

public class AnalysisRun {
	
	private static final double CFP = 1.0;
	private static final double CFN = 10.0;
		
 	private Instances trainingSet;
	private Instances testingSet;
	private EvaluationResults results;
	
	public AnalysisRun(Instances fullDataset, AnalysisProfile profile, String project, int testingIdx) {
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
		if (selectionType.equals(AnalysisProfile.FEATURE_SELECTION_BEST_FIRST)) {
			Remove removeFilter = this.getRemoveFilter(getSelectedIndexes());	
			this.trainingSet = Filter.useFilter(this.trainingSet, removeFilter);
			this.testingSet = Filter.useFilter(this.testingSet, removeFilter);			
		}
	}
	
	private String getYValueForOversampling() {
		int countYes = 0;
		int countNo = 0;
		
		for (Instance row : this.trainingSet) {
			if (row.stringValue(row.classIndex()).equals("Y"))
				countYes++;
			else
				countNo++;
		}
		
		double y;
		if (countYes > countNo)
			y = 2 * ((double)countYes/this.trainingSet.size()) * 100;
		else
			y = 2 * ((double)countNo/this.trainingSet.size()) * 100;
		
		return String.valueOf(y);
	}
	
	private void applySampling(String samplingType) throws Exception {
		
		if (samplingType.equals(AnalysisProfile.SAMPLING_UNDERSAMPLING)) {
			SpreadSubsample  spreadSubsample = new SpreadSubsample();
			String[] opts = new String[]{ "-M", "1.0"};
			spreadSubsample.setOptions(opts);
			spreadSubsample.setInputFormat(this.trainingSet);
			
			this.trainingSet = Filter.useFilter(this.trainingSet, spreadSubsample);
		} else if (samplingType.equals(AnalysisProfile.SAMPLING_OVERSAMPLING)) {	
			Resample resample = new Resample();
			String[] opts = new String[]{ "-B", "1.0", "-Z", this.getYValueForOversampling()};
			resample.setOptions(opts);
			resample.setInputFormat(this.trainingSet);
			
			this.trainingSet = Filter.useFilter(this.trainingSet, resample);
		} else if (samplingType.equals(AnalysisProfile.SAMPLING_SMOTE)) {
			SMOTE smote = new SMOTE();
			smote.setInputFormat(this.trainingSet);
			this.trainingSet = Filter.useFilter(this.trainingSet, smote);
		}
	}
	
	private CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
		CostMatrix costMatrix = new CostMatrix(2);
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(1, 0, weightFalsePositive);
		costMatrix.setCell(0, 1, weightFalseNegative);
		costMatrix.setCell(1, 1, 0.0);
		return costMatrix;
	}
	
	public void evaluate(AnalysisProfile profile) throws Exception {
		
		AbstractClassifier basicClassifier = null;
		if (profile.getClassifier().equals(AnalysisProfile.CLASSIFIER_RANDOM_FOREST))
			basicClassifier = new RandomForest();
		else if (profile.getClassifier().equals(AnalysisProfile.CLASSIFIER_NAIVE_BAYES))
			basicClassifier = new NaiveBayes();
		else
			basicClassifier = new IBk();
		
		this.applyFeatureSelection(profile.getFeatureSelectionTechnique());
		this.applySampling(profile.getSamplingTechnique());
		
		Evaluation eval = null;	
		CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
		costSensitiveClassifier.setClassifier(basicClassifier);
		costSensitiveClassifier.setCostMatrix(createCostMatrix(CFP, CFN));
		
		if (profile.getCostSensitiveTechnique().equals(AnalysisProfile.COST_SENSITIVE_CLASSIFIER_NO)) {
			basicClassifier.buildClassifier(trainingSet);
			eval = new Evaluation(this.testingSet);
			eval.evaluateModel(basicClassifier, testingSet);
		} else {
			costSensitiveClassifier.setMinimizeExpectedCost(profile.getCostSensitiveTechnique().equals(AnalysisProfile.COST_SENSITIVE_CLASSIFIER_SENSITIVE_THRESHOLD));
			costSensitiveClassifier.buildClassifier(trainingSet);
			eval = new Evaluation(testingSet, costSensitiveClassifier.getCostMatrix());
			eval.evaluateModel(costSensitiveClassifier, testingSet);
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
}
