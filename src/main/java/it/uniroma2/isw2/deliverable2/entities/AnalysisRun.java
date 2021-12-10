package it.uniroma2.isw2.deliverable2.entities;

import java.util.HashMap;
import java.util.Map;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;

public class AnalysisRun {
	
	private static final String MAJORITY_KEY = "MAJORITY";
	private static final String MINORITY_KEY = "MINORITY";
		
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
	
	private Map<String, Integer> getMajorityAndMinorityFromTrainingSet() {
		int countYes = 0;
		int countNo = 0;
		
		for (Instance row : this.trainingSet) {
			if (row.stringValue(row.classIndex()).equals("Y"))
				countYes++;
			else
				countNo++;
		}
		
		Map<String, Integer> majmin = new HashMap<>();
		if (countYes > countNo) {
			majmin.put(MAJORITY_KEY, countYes);
			majmin.put(MINORITY_KEY, countNo);
		} else {
			majmin.put(MAJORITY_KEY, countNo);
			majmin.put(MINORITY_KEY, countYes);
		}
		
		return majmin;
	}
	
	private void applySampling(FilteredClassifier fc, String samplingType) throws Exception {

		if (samplingType.equals(AnalysisProfile.SAMPLING_UNDERSAMPLING)) {
			SpreadSubsample  spreadSubsample = new SpreadSubsample();
			String[] opts = new String[]{ "-M", "1.0"};
			spreadSubsample.setOptions(opts);
			fc.setFilter(spreadSubsample);
		} else if (samplingType.equals(AnalysisProfile.SAMPLING_OVERSAMPLING)) {
			Map<String, Integer> majmin = this.getMajorityAndMinorityFromTrainingSet();
			double y = 100 * ((majmin.get(MAJORITY_KEY) - majmin.get(MINORITY_KEY))/((double)majmin.get(MINORITY_KEY)));		
			Resample resample = new Resample();
			resample.setNoReplacement(false);
			resample.setBiasToUniformClass(1.0);
			resample.setSampleSizePercent(y);
			resample.setInputFormat(this.trainingSet);
			fc.setFilter(resample);
		} else if (samplingType.equals(AnalysisProfile.SAMPLING_SMOTE)) {
			SMOTE smote = new SMOTE();
			smote.setInputFormat(this.trainingSet);
			fc.setFilter(smote);
		}
	}
	
	public void evaluate(AnalysisProfile profile) throws Exception {
		
		AbstractClassifier classifier = null;
		if (profile.getClassifier().equals(AnalysisProfile.CLASSIFIER_RANDOM_FOREST))
			classifier = new RandomForest();
		else if (profile.getClassifier().equals(AnalysisProfile.CLASSIFIER_NAIVE_BAYES))
			classifier = new NaiveBayes();
		else
			classifier = new IBk();
		
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(classifier);
		
		this.applySampling(fc, profile.getSamplingTechnique());
		
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
