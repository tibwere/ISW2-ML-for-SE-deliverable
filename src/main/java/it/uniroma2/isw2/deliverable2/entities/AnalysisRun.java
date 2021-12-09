package it.uniroma2.isw2.deliverable2.entities;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class AnalysisRun {
	
	private static final int BUGGYNESS_IDX = 13;
		
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
	
	public void applyFeatureSelection(String selectionType) throws Exception {
		if (selectionType.equals(AnalysisProfile.FEATURE_SELECTION_BEST_FIRST)) {
			Remove removeFilter = this.getRemoveFilter(getSelectedIndexes());	
			this.trainingSet = Filter.useFilter(this.trainingSet, removeFilter);
			this.testingSet = Filter.useFilter(this.testingSet, removeFilter);			
		}
	}
	
	public void evaluate(String classifierType) throws Exception {
		
		AbstractClassifier classifier = null;
		if (classifierType.equals(AnalysisProfile.CLASSIFIER_RANDOM_FOREST))
			classifier = new RandomForest();
		else if (classifierType.equals(AnalysisProfile.CLASSIFIER_NAIVE_BAYES))
			classifier = new NaiveBayes();
		else
			classifier = new IBk();
		
		classifier.buildClassifier(trainingSet);
		Evaluation eval = new Evaluation(testingSet);
		eval.evaluateModel(classifier, testingSet);
		
		results.setStatistics(eval);		
	}
	
	public EvaluationResults getResults() {
		return this.results;
	}
	
	private int getDefectsInTraining() {
		int counter = 0;
		
		for (Instance row : this.trainingSet)
			if (row.stringValue(BUGGYNESS_IDX).equals("Y"))
				counter++;
		
		return counter;
	}
	
	private int getDefectsInTesting() {
		int counter = 0;
		
		for (Instance row : this.testingSet)
			if (row.stringValue(BUGGYNESS_IDX).equals("Y"))
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
