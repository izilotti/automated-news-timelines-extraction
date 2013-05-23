/** 
 * Evaluate performance of models
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package util;

import java.text.NumberFormat;

import java.util.ArrayList;

public class PerformanceCounter {

	protected final static boolean DEBUG = false;
	protected final static boolean DISPLAY = true;

	public static final double smooth_const = 0.000000001;

	public static int testFileSize = 0;

	/*
	 * Array index representations:
	 * 
	 * stat[0]: precision stat[1]: recall stat[2]: accuracy stat[3]: F-score
	 */
	public static double[] stat = new double[4];

	/*
	 * Array index representations:
	 * 
	 * avg_stat[0]: average precision avg_stat[1]: average recall avg_stat[2]:
	 * average accuracy avg_stat[3]: average F-score
	 */
	public static double[] avg_stat = new double[4];

	/**
	 * Performance counter, for single file
	 * 
	 * @param windowSize
	 *            : the size of window
	 * @param al1
	 *            : the actual labels given by testing data
	 * @param al2
	 *            : the predicted labels given by algorithms
	 * @return precision, recall, accuracy, F-score
	 */
	public static double[] countPerformance(int windowSize,
			ArrayList<String> al1, ArrayList<String> al2) throws Exception {

		ArrayList<String> trueLabels = al1; // true labels
		ArrayList<String> predLabels = al2; // predicted labels

		int start = 0, end = 0;
		int true_pos = 0, false_pos = 0, true_neg = 0, false_neg = 0;
		String str1 = null;
		String str2 = null;

		if (DEBUG == true) {
			System.out
					.println("\n===========================================================\n");
			System.out.println("!!!!! trueLabels.size = " + trueLabels.size());
			System.out.println("!!!!! predLabels.size = " + predLabels.size());
		}

		start = windowSize;
		if (trueLabels == null) {
			System.out.println("[PC] Not ready to run.");
			System.exit(0);
		} else
			end = trueLabels.size() - windowSize;

		// Count confusion matrix of all predicted labels
		for (int i = start; i < end; i++) {
			str1 = trueLabels.get(i);
			str2 = predLabels.get(i);
			// System.out.println(i + ": " + str1 + " " + str2);

			// If predicted label is <NONE>, and true label is <NONE>,
			// increment the true negative counts
			if (str2.equalsIgnoreCase("<NONE>")) {
				if (str1.equals(str2)) {
					true_neg++;
				} else {
					// Otherwise increment the false negative counts
					false_neg++;
				}
			} else {
				// If predict result is not <NONE> also the true label isn't,
				// increment the true positive counts
				if (str1.equals(str2)) {
					true_pos++;
				} else {
					// Otherwise increment the false positive counts
					false_pos++;
				}
			}
		}

		// Display the confusion matrices
		if (DISPLAY == true) {
			System.out.println("\nPerformance Measurements:");
			System.out.println("true_pos = " + true_pos);
			System.out.println("false_pos = " + false_pos);
			System.out.println("true_neg = " + true_neg);
			System.out.println("false_neg = " + false_neg + "\n");
		}

		// Set digits of floating point numbers
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumFractionDigits(2);

		// Calculate performance parameters:
		// precision
		stat[0] = (double) true_pos
				/ ((double) (true_pos + false_pos) + smooth_const);
		// recall
		stat[1] = (double) true_pos
				/ ((double) (true_pos + false_neg) + smooth_const);
		// accuracy
		stat[2] = (double) (true_pos + true_neg)
				/ ((double) (true_pos + true_neg + false_pos + false_neg) + smooth_const);
		// F-score
		stat[3] = 2 * (double) (stat[0] * stat[1])
				/ ((double) (stat[0] + stat[1]) + smooth_const);

		if (DISPLAY == true) {
			System.out.println("Precision = " + nf.format(stat[0]));
			System.out.println("Recall = " + nf.format(stat[1]));
			System.out.println("Accuracy = " + nf.format(stat[2]));
			System.out.println("F-score = " + nf.format(stat[3]) + "\n");
		}

		return stat;
	}

	/**
	 * Average performance counter, for multiple files
	 * 
	 * @param d1: sum of precision
	 * @param d2: sum of recall
	 * @param d3: sum of accuracy
	 * @param d4: sum of F-score
	 * @param i1: sum of numbers of files
	 * @return average of precision, recall, accuracy, F-score
	 */
	public static double[] countAveragePerformance(double d1, double d2,
			double d3, double d4, int i) {

		double all_precision = d1;
		double all_recall = d2;
		double all_accuracy = d3;
		double all_fscore = d4;
		testFileSize = i;

		avg_stat[0] = all_precision / testFileSize;
		avg_stat[1] = all_recall / testFileSize;
		avg_stat[2] = all_accuracy / testFileSize;
		avg_stat[3] = all_fscore / testFileSize;


		
		return avg_stat;
	}

	/**
	 * Display average performance
	 * 
	 * @param
	 * @return
	 */
	public static void printAveragePerformance() {

		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		System.out.println("************************************");
		System.out.println("Test files: " + testFileSize + "\n");
		System.out.println("Average performance: ");
		System.out.println("Precision  Recall  Accuracy  F-score");
		System.out.println("  " + nf.format(avg_stat[0]) + "   "
				+ nf.format(avg_stat[1]) + "   " + nf.format(avg_stat[2])
				+ "   " + nf.format(avg_stat[3]));
		System.out.println("\n************************************\n");
	}

}
