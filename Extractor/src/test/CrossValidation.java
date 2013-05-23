/** K-fold Cross Validation
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package test;

import java.text.NumberFormat;
import java.util.ArrayList;

import util.PerformanceCounter;

import baseline.ModelSelector;

public class CrossValidation {

	private static int _KFold = 10; // The fold number of cross validation

	public static String _trainingDir = "data/Cross_Validation/train"; // Directory
																		// of
																		// training
																		// data
	public static String _testingDir = "data/Cross_Validation/test"; // Directory
																		// of
																		// testing
																		// data

	public static void main(String[] args) throws Exception {

		double[] precisionOfKFold = new double[_KFold];
		double[] recallOfKFold = new double[_KFold];
		double[] accuracyOfKFold = new double[_KFold];
		double[] fscoreOfKFold = new double[_KFold];

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

		// Run K-fold cross validation
		for (int k = 0; k < _KFold; k++) {
			// Sepecify training data and testing data directories
			String trainingDataDir = _trainingDir.concat("/" + (k + 1));
			System.out.println("[" + _KFold
					+ "-fold cross validation] Training data directory: "
					+ trainingDataDir);
			String testingDataDir = _testingDir.concat("/" + (k + 1));
			System.out.println("[" + _KFold
					+ "-fold cross validation] Testing data directory: "
					+ testingDataDir);

			TestAll dl = new TestAll(trainingDataDir, testingDataDir);
			ArrayList<ModelSelector> dl_tests = dl.addTestCase();
			dl.doEvaluation(dl_tests);

			// Obtain the performance of current fold validation
			double[] tempResults = PerformanceCounter.avg_stat;
			precisionOfKFold[k] = tempResults[0];
			recallOfKFold[k] = tempResults[1];
			accuracyOfKFold[k] = tempResults[2];
			fscoreOfKFold[k] = tempResults[3];

			// Sum the statistics of each fold validation
			tempPrec += precisionOfKFold[k];
			tempRec += recallOfKFold[k];
			tempAcc += accuracyOfKFold[k];
			tempFscore += fscoreOfKFold[k];

			System.out.println();
		}

		// Calculate average performance
		avgPrec = tempPrec / _KFold;
		avgRec = tempRec / _KFold;
		avgAcc = tempAcc / _KFold;
		avgFscore = tempFscore / _KFold;

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
		System.out.println("  " + nf.format(avgPrec) + "   "
				+ nf.format(avgRec) + "   " + nf.format(avgAcc) + "   "
				+ nf.format(avgFscore));
		System.out.println("\nStability of results:");
		System.out.println("           Avg Err   Std Err   Std Dev");
		System.out.println("Precision:  " + nf.format(avgErrPrec) + "    "
				+ nf.format(stdErrPrec) + "    " + nf.format(sdPrecision));
		System.out.println("Recall:     " + nf.format(avgErrRec) + "    "
				+ nf.format(stdErrRec) + "    " + nf.format(sdRecall));
		System.out.println("Accuracy:   " + nf.format(avgErrAcc) + "    "
				+ nf.format(stdErrAcc) + "    " + nf.format(sdAccuracy));
		System.out.println("Fscore:     " + nf.format(avgErrFscore) + "    "
				+ nf.format(stdErrFscore) + "    " + nf.format(sdFscore));

		System.out.println("==================== END =====================");

	}
}
