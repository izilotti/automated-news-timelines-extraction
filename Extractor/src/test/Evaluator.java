/**
 * Main Evaluator
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package test;

import java.util.ArrayList;

import java.util.List;

import baseline.ModelSelector;

import parsers.TimeMLParser;

import util.PerformanceCounter;

public class Evaluator {

	public static final boolean DEBUG = false; // Turn on/off debug function
	public static boolean DISPLAY_COLUMNS = false;
	public static int testInstanceCounter; // Counter for instances of tested
											// models
	public static int testFileCounter; // Counter for testing files
	public static int testSentCounter; // Counter for sentences in testing files

	/**
	 * Model Evaluation
	 * 
	 * @param tests
	 *            : Instance of tests
	 * @return <none>
	 */
	public static void doEval(List<ModelSelector> tests) throws Exception {

		ArrayList<String> _tokenList = new ArrayList<String>(); // List of
																// tokens
		ArrayList<String> _trueLabelList = new ArrayList<String>(); // List of
																	// true
																	// TimeML
																	// tags
		ArrayList<String> _predLabelList = new ArrayList<String>(); // List of
																	// predicted
																	// TimeML
																	// tags

		int _windowSize = 0;

		double all_precision = 0, all_recall = 0, all_accuracy = 0, all_fscore = 0;

		testInstanceCounter = 0;
		testFileCounter = 0;
		testSentCounter = 0;
		// Loop on each model instance
		for (ModelSelector t : tests) {

			// Increment model instance counts
			testInstanceCounter++;

			// Get the window size
			_windowSize = t.getWindowSize();

			// Parse training and testing data
			t.parseTrainingData(".tml");
			t.parseTestingData(".tml");

			// Pre-process data
			t.preprocessing(t._prefix);

			// Training this model
			t.trainModel();

			// Loop on each testing file
			for (TimeMLParser timeMLInstance : t.getTestingData()) {

				// Increment testing files counter
				testFileCounter++;
				System.out.println("[Evaluator] Testing: "
						+ t._testingFilesList.get(testFileCounter - 1));

				// Run selected model (t) with testing data
				t.computeResults(timeMLInstance);

				// Get necessary testing results
				_tokenList = t.getTokenList();
				_trueLabelList = t.getTrueLabelList();
				_predLabelList = t.getPredLabelList();

				if (DISPLAY_COLUMNS) {
					// Display a 2-column testing results
					if (_tokenList.size() == _predLabelList.size()) {
						System.out.println("[Evaluator] Final results:");
						for (int j = 0; j < _tokenList.size(); j++) {
							System.out.print(j + ": " + _tokenList.get(j) + " "
									+ _predLabelList.get(j) + " "
									+ _trueLabelList.get(j));
							if (!_predLabelList.get(j).equals(
									_trueLabelList.get(j)))
								System.out.print(" **");
							System.out.println();
						}
					} else {
						System.err
								.println("Evaluator: The size of tokenList is not equal to size of predLabelList");
						System.exit(0);
					}
				}

				/** Evaluate individual sentences **/
				int sentLength = 0;
				ArrayList<String> tokenList = new ArrayList<String>();
				ArrayList<String> predLabelList = new ArrayList<String>();
				ArrayList<String> trueLabelList = new ArrayList<String>();
				for (int i = 0; i < _tokenList.size(); i++) {

					String token = _tokenList.get(i);
					String trueLabel = _trueLabelList.get(i);
					String predLabel = _predLabelList.get(i);
					
					// Test
//					if (DEBUG)
						System.out.println(i + ": " + token + "   " + predLabel + "  "
								+ trueLabel);

					// Detect sentence boundary
					if (!token.isEmpty() && !trueLabel.isEmpty()) {
						tokenList.add(token);
						predLabelList.add(predLabel);
						trueLabelList.add(trueLabel);
						sentLength++;
					}
					else {
												
//						if (DEBUG) {
							System.out.println("sentLength = " + sentLength);
							for (int m = 0; m < tokenList.size(); m++) {
								System.out.println("- S - " + m + ": "
										+ tokenList.get(m) + "   "
										+ predLabelList.get(m) + "  "
										+ trueLabelList.get(m));
							}
//						}
						
						sentLength = 0;
						
						// Increment sentence counter
						testSentCounter++;
						
						// Calculate model's performance
						// (precision, recall, accuracy and F-score)
						double[] pc = PerformanceCounter.countPerformance(
								_windowSize, trueLabelList, predLabelList);
						// Sum all performance metrics
						all_precision += pc[0];
						all_recall += pc[1];
						all_accuracy += pc[2];
						all_fscore += pc[3];
						// Clear lists
						tokenList.clear();
						predLabelList.clear();
						trueLabelList.clear();
					}
				}

				// Clear lists for next run
				t.clearLists();

			} /* End of loop on files */

			// Get the number of testing files
//			int testFileSize = t.getTestingData().size();

			// Calculate the average performance
			PerformanceCounter.countAveragePerformance(all_precision,
					all_recall, all_accuracy, all_fscore, testSentCounter);

			// Print out performance
			System.out.println("\n\n[Evauator] Test Instance "
					+ testInstanceCounter + ": ");
			PerformanceCounter.printAveragePerformance();

			// Reset values
			all_precision = 0;
			all_recall = 0;
			all_accuracy = 0;
			all_fscore = 0;
//			testFileSize = 0;
			testFileCounter = 0;

		} /* End of loop on test instance */
	}
}
