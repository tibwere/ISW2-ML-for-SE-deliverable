package it.uniroma2.isw2.deliverable2.entities;

import weka.classifiers.Evaluation;

public class EvaluationResults {
	
	public static final String CSV_HEADER = "Dataset,#TrainingRelease,%Training,%Defective in training,%Defective in testing,"
			+ "Classifier,Balancing,Feature Selection,Sensitivity,"
			+ "TP,FP,TN,FN,Precision,Recall,AUC,Kappa\n";
	
	
	private String dataset;
	private int numberOfTrainingReleases;
	private double percentageOfTrainingReleases;
	private double percentageOfDefectiveInTraining;
	private double percentageOfDefectiveInTesting;
	private String classifier;
	private String balancing; 
	private String featureSelection;
	private String sensitivity;
	private double truePositive; 
	private double falsePositive;
	private double trueNegative;
	private double falseNegative;
	private double precision;
	private double recall;
	private double auc;
	private double kappa;
	
	public EvaluationResults(String dataset, AnalysisProfile profile) {
		this.dataset = dataset;
		this.classifier = profile.getClassifier();
		this.balancing = profile.getSamplingTechnique();
		this.featureSelection = profile.getFeatureSelectionTechnique();
		this.sensitivity = profile.getCostSensitiveTechnique();
	}
	
	public void setStatistics(Evaluation eval) {
		final int CLASS_INDEX = 1;
		
		this.truePositive = eval.truePositiveRate(CLASS_INDEX);
		this.falsePositive = eval.falsePositiveRate(CLASS_INDEX);
		this.trueNegative = eval.trueNegativeRate(CLASS_INDEX);
		this.falseNegative = eval.falseNegativeRate(CLASS_INDEX);
		this.precision = eval.precision(CLASS_INDEX);
		this.recall = eval.recall(CLASS_INDEX);
		this.auc = eval.areaUnderROC(CLASS_INDEX);
		this.kappa = eval.kappa();
	}
	
	public void setNumberOfTrainingReleases(int numberOfTrainingReleases) {
		this.numberOfTrainingReleases = numberOfTrainingReleases;
	}

	public void setPercentageOfTrainingReleases(double percentageOfTrainingReleases) {
		this.percentageOfTrainingReleases = percentageOfTrainingReleases;
	}

	public void setPercentageOfDefectiveInTraining(double percentageOfDefectiveInTraining) {
		this.percentageOfDefectiveInTraining = percentageOfDefectiveInTraining;
	}

	public void setPercentageOfDefectiveInTesting(double percentageOfDefectiveInTesting) {
		this.percentageOfDefectiveInTesting = percentageOfDefectiveInTesting;
	}

	public void setSensitivity(String sensitivity) {
		this.sensitivity = sensitivity;
	}
	
	public String toString() {
		return String.format("%s,%d,%.7f,%.7f,%.7f,%s,%s,%s,%s,%.7f,%.7f,%.7f,%.7f,%.7f,%.7f,%.7f,%.7f", this.dataset,
				this.numberOfTrainingReleases, this.percentageOfTrainingReleases, this.percentageOfDefectiveInTraining,
				this.percentageOfDefectiveInTesting, this.classifier, this.balancing, this.featureSelection,
				this.sensitivity, this.truePositive, this.falsePositive, this.trueNegative, this.falseNegative,
				this.precision, this.recall, this.auc, this.kappa);
	}
}
