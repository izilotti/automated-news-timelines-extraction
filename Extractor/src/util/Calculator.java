package util;

import java.text.NumberFormat;

public class Calculator {
	
	public Calculator(){
		
	}
	
	private void calculate(double[] precisionOfKFold, double[] recallOfKFold,
			double[] accuracyOfKFold, double[] fscoreOfKFold){

		int _KFold = 10;
		
		double tempPrec = 0, tempRec = 0, tempAcc = 0, tempFscore = 0;
		double avgPrec = 0, avgRec = 0, avgAcc = 0, avgFscore = 0;
		double tempDiff_Precision = 0, tempSqrDiff_Precision = 0, sdPrecision = 0;
		double tempDiff_Recall = 0, tempSqrDiff_Recall = 0, sdRecall = 0;
		double tempDiff_Accuracy = 0, tempSqrDiff_Accuracy = 0, sdAccuracy = 0;
		double tempDiff_Fscore = 0, tempSqrDiff_Fscore = 0, sdFscore = 0;
		double avgErrPrec = 0, avgErrRec = 0, avgErrAcc = 0, avgErrFscore = 0;
		double stdErrPrec = 0, stdErrRec = 0, stdErrAcc = 0, stdErrFscore = 0;

		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		
		for (int k = 0; k < _KFold; k++) {
			
			// Sum the statistics of each fold validation
			tempPrec += precisionOfKFold[k];
			tempRec += recallOfKFold[k];
			tempAcc += accuracyOfKFold[k];
			tempFscore += fscoreOfKFold[k];
		}
		
		// Calculate average performance
		avgPrec = tempPrec / _KFold;
		avgRec = tempRec / _KFold;
		avgAcc = tempAcc / _KFold;
		avgFscore = tempFscore / _KFold;
//		System.out.println(avgFscore);
		
		
		// Sum the difference of each value from the average
		for (int i = 0; i < _KFold; i++) {
			tempDiff_Precision += precisionOfKFold[i] - avgPrec;
			tempDiff_Recall += recallOfKFold[i] - avgRec;
			tempDiff_Accuracy += accuracyOfKFold[i] - avgAcc;
			tempDiff_Fscore += fscoreOfKFold[i] - avgFscore;
			// sum of the squares of differences
			tempSqrDiff_Precision += Math.pow(tempDiff_Precision, 2);
			tempSqrDiff_Recall += Math.pow(tempDiff_Recall, 2);
			tempSqrDiff_Accuracy += Math.pow(tempDiff_Accuracy, 2);
			tempSqrDiff_Fscore += Math.pow(tempDiff_Fscore, 2);
		}
		
		
		// Calculate standard deviations
		sdPrecision = Math.sqrt(tempSqrDiff_Precision / _KFold);
		sdRecall = Math.sqrt(tempSqrDiff_Recall / _KFold);
		sdAccuracy = Math.sqrt(tempSqrDiff_Accuracy / _KFold);
		sdFscore = Math.sqrt(tempSqrDiff_Fscore / _KFold);

		
		
		// Average Error
		avgErrPrec = tempDiff_Precision / _KFold;
		avgErrRec = tempDiff_Recall / _KFold;
		avgErrAcc = tempDiff_Accuracy / _KFold;
		avgErrFscore = tempDiff_Fscore / _KFold;
		
		

		// Standard Error
		stdErrPrec = sdPrecision / (Math.sqrt(_KFold));
		stdErrRec = sdRecall / (Math.sqrt(_KFold));
		stdErrAcc = sdAccuracy / (Math.sqrt(_KFold));
		stdErrFscore = sdFscore / (Math.sqrt(_KFold));
	
		/** Display results **/
		System.out
				.println("\n\n=================================================");
		System.out.println("Average performance of " + _KFold
				+ "-fold Cross Validation:");
		System.out.println("=================================================");
		System.out.println("\nPrecision  Recall  Accuracy  F-score");
		System.out.println("  " + avgPrec + "   "
				+ avgRec + "   " + avgAcc + "   "
				+ avgFscore);
		System.out.println("\nStability of results:");
		System.out.println("           Avg Err   Std Err   Std Dev");
		System.out.println("Precision:  " + avgErrPrec + "    "
				+ stdErrPrec + "    " + sdPrecision);
		System.out.println("Recall:     " + avgErrRec + "    "
				+ stdErrRec + "    " + sdRecall);
		System.out.println("Accuracy:   " + avgErrAcc + "    "
				+ stdErrAcc + "    " + sdAccuracy);
		System.out.println("Fscore:     " + avgErrFscore + "    "
				+ stdErrFscore + "    " + sdFscore);

		System.out.println("==================== END =====================");
	}
	
	public static void main(String[] args){
		double[] precisionOfKFold = {69.12, 70.86, 73.16, 77.19, 79.82, 72.62, 68.98, 72.34, 77.47, 80.58};
        double[] recallOfKFold = {64.7, 69.01, 71.07, 70.93, 68.68, 60.93, 62.56, 66.68, 65.23, 69.19};
		double[] accuracyOfKFold = {93.53, 92.63, 93.86, 91.39, 91, 90.13, 91.15, 91.53, 89.83, 91.46};
		double[] fscoreOfKFold = {65.17, 66.64, 69.52, 70.08, 70.08, 63.22, 62.62, 66.14, 67.11, 71.26};
		Calculator cal = new Calculator();
		cal.calculate(precisionOfKFold, recallOfKFold, accuracyOfKFold, fscoreOfKFold);
	}
}
