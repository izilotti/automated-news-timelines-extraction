/** 
 * A Co-Training algorithm for CRF++, training on documents.
 * 
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package baseline.learning.semisupervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import baseline.learning.supervised.CRFpp_documents;

import nlp.opennlp.POSTagger.POSTagging;

import parsers.TimeMLParser;
import util.DocUtil;
import util.FileFinder;
import util.PerformanceCounter;

public class CoTraining_CRF_Documents {

	// Flags
	public static boolean DEBUG = false;				// Turn on/off debug modes
	public static boolean _hasModel1 = true;			// If has model 1
	public static boolean _hasModel2 = true;			// If has model 2
	public static boolean _hasCombinedModel = true;		// If has combined models
	public static boolean _hasToken; 					// If includes tokens
	public static boolean _hasPOS; 						// If includes POS tags

	// Parameters
	public static int _windowSize = 1;					// Window Size
	public static int _iteration = 15;					// Iteration numbers
	public static int _maxSelectionLimit = 3;			// Selection limit
	public static double _threshold_EVENT = 0.0936;		// threshold of EVENT ratio
	public static double _threshold_TIMEX3 = 0.0153;	// threshold of TIMEX3 ratio
	public static double _minSum = 0.1089;				// threshold of sum of EVENT and TIMEX3 ratio
//	public static double _threshold_EVENT = 0.1211;		
//	public static double _threshold_TIMEX3 = 0.0458;
//	public static double _minSum = 0.1668;  
	
	// Directories
	public static String _CRFExecutable = "/usr/local/bin/";						 // Path of CRF++ executable
	public static String _workingDir = System.getProperty("user.dir"); 				 // Get the path of working directory
	public static String _crfFolder = _workingDir.concat("/data/CoTraining/CRFpp");  // Path of CRF folder of co-training

	public static String _labeledDocsDir = _crfFolder.concat("/labeled"); 			// Path of labeled documents
	public static String _unlabeledDocsDir = _crfFolder.concat("/unlabeled/toy"); 	// Path of unlabeled documents
	public static String _testDocsDir = _crfFolder.concat("/test");					// Path of testing data

	public static String _trainingDataDir = _crfFolder.concat("/train.data");		// Train data to CRF++
	public static String _unlabeledDataDir = _crfFolder.concat("/unlabel.data");	// Unlabeled data to CRF++
	public static String _testingDataDir = _crfFolder.concat("/test.data");			// Test data to CRF++

	// Variables
	public static int _testFileCounter;				// Counter of testing files
	public static int _numOflabeledDocs;			// Counter of labeled documents
	public static int _numOfUnlabeledDocs;			// Counter of unlabeled documents
	
	public static ArrayList<String> _labeledDocsList = new ArrayList<String>();
	public static ArrayList<String> _unlabeledDocsList = new ArrayList<String>();
	public static ArrayList<String> _testingFilesList = new ArrayList<String>();

	public static ArrayList<String> _testTrueLabelList = new ArrayList<String>();
	public static ArrayList<String> _testPredLabelList = new ArrayList<String>(); 
	public static ArrayList<String> _crfPredLabel = new ArrayList<String>(); 
	public static ArrayList<Integer> _selectedKeys = new ArrayList<Integer>();		
	public static ArrayList<ArrayList<String>> _crfPredLabelList = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> _ulTokensList = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> _ulPOSsList = new ArrayList<ArrayList<String>>();
	
	public static ArrayList<Double> _avgPrecisionList = new ArrayList<Double>();
	public static ArrayList<Double> _avgRecallList = new ArrayList<Double>();
	public static ArrayList<Double> _avgAccuracyList = new ArrayList<Double>();
	public static ArrayList<Double> _avgFscoreList = new ArrayList<Double>();

	public static ArrayList<TimeMLParser> _trainingData = new ArrayList<TimeMLParser>();
	public static ArrayList<TimeMLParser> _testingData = new ArrayList<TimeMLParser>();
	
	/**
	 * constructor
	 * 
	 * @param 
	 * @return <none>
	 */
	public CoTraining_CRF_Documents() {
		/**  (Can add parameters to constructor) **/
	}
	

	/**
	 * Create a list of labeled documents
	 * 
	 * @param type: file type
	 * @return <none>
	 */
	private void getlabeledDocs(String type) {

		// Find all labeled documents and put them in an ArrayList
		FileFinder ffinder = new FileFinder(type, _labeledDocsDir);
		_labeledDocsList = ffinder.fileList;
		
		// Get the total numbers
		_numOflabeledDocs = _labeledDocsList.size();
	}

	
	/**
	 * Create a list for unlabeled documents
	 * 
	 * @param type: file type (unlabeled data shall be xml format)
	 * @return <none>
	 */
	private void getUnlabeledDocs(String type) {

		// Find all labeled documents and put them in a list
		FileFinder ffinder = new FileFinder(type, _unlabeledDocsDir);
		
		_unlabeledDocsList = ffinder.fileList;
			
		// Get the total numbers
		_numOfUnlabeledDocs = _unlabeledDocsList.size();
	}


	/**
	 * Parse XML documents, assign POS tags to each token, and create
	 * unlabeled.data file for CRF++
	 * 
	 * @param docsList: a list of documents to be parsed
	 * @return <none>
	 */
	private void parseXMLFiles(ArrayList<String> docsList) throws Exception {

		String fileName;

		// Parse (unlabeled) documents and add them to corresponding lists
		for (int i = 0; i < docsList.size(); i++) {
			ArrayList<String> tokenList = new ArrayList<String>();
			ArrayList<String> POSList = new ArrayList<String>();
			// Get the directory of each document
			fileName = docsList.get(i);
			// Instantiate a parser instance
			TimeMLParser timemlInstance = new TimeMLParser(fileName);
			timemlInstance.parse();
			// Get token list and POS list via parsing the node list
			POSTagger(timemlInstance.nodeList, tokenList, POSList);
			if (tokenList.size() == 0 && POSList.size() == 0) {
				System.err.println("\nThis file is empty: " + fileName);
				break;
			}
			// Add them to an another ArrayList
			_ulTokensList.add(tokenList);
			_ulPOSsList.add(POSList);
		}
	}

	
	/**
	 * Create unlabeled.data file for CRF++
	 * 
	 * @param docsList: a list of documents to be parsed
	 * @return <none>
	 */
	private void createUnlabeledData(ArrayList<String> docsList) throws Exception {

		// Create a file
		FileWriter fstream = new FileWriter(_unlabeledDataDir);
		if (DEBUG) {
			System.out.println("\n The unlabeled.data is created by "
					+ docsList.size() + " unlabeled files:");
			for (int i = 0; i < docsList.size(); i++)
				System.out.println(docsList.get(i));
		}
		BufferedWriter out = new BufferedWriter(fstream);
		for (int i = 0; i < docsList.size(); i++) {
			// Loop on all tokens of each file
			for (int j = 0; j < _ulTokensList.get(i).size(); j++) {
				// Get current token
				String token = _ulTokensList.get(i).get(j);
				// Get POS tag of current token
				String POStag = _ulPOSsList.get(i).get(j);
				// Put them in one line
				String line = token + " " + POStag + "\n";
				// Write to file
				out.write(line);
			}
			// Add an empty line between each document
			out.write("\n");
		}
		// Close the text stream
		out.close();
		if (DEBUG == true)
			System.out.println("[CRFpp] unlabel.data created.\n ");
	}

	/**
	 * POS Tagging
	 * 
	 * @param nodeList: list of text nodes tokenList: list of tokens POSList: list
	 *            of POS tags
	 * @return <none>
	 */
	public void POSTagger(ArrayList<String> nodeList,
			ArrayList<String> tokenList, ArrayList<String> POSList) {
		int sum = 0, temp = 0;
		try {
			for (int i = 0; i < nodeList.size(); i++) {
				// Process each node in node list using POSTagging technique
				POSTagging tags = nlp.NLPUtil.tagger
						.process(nodeList.get(i), 1);
				// Loop on sentences
				for (int si = 0; si < tags._taggings.length; si++) {
					// Loop on taggings
					for (int ti = 0; ti < tags._taggings[si].length; ti++) {
						temp = tags._taggings[si][ti].length;
						// Loop on words
						for (int wi = 0; wi < tags._taggings[si][ti].length; wi++) {
							tokenList.add(tags._tokens[si][wi]); // Set up token list
							POSList.add(tags._taggings[si][ti][wi]); // Set up POS tags list
						}
					}
					sum += temp; // Increment length of each node
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Select top results from co-training prediction
	 * 
	 * @param model: the directory of model file 
	 * @return a list of selected files
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String> selectTopResults(String model) throws Exception {

		// Call CRF++ to test on those unlabeled documents
		String command = _CRFExecutable + "crf_test -v1 -m " + model + " " + _unlabeledDataDir;	
		
		// Variables
		int docCounter = 0, tokenCounter = 0, eventCounter = 0, timex3Counter = 0;
		String str = null;
		
		double[] eventRatio = new double[_unlabeledDocsList.size()];
		double[] timex3Ratio = new double[_unlabeledDocsList.size()];
		int[] segLength = new int[_unlabeledDocsList.size()];
		double[] condLikelihood = new double[_unlabeledDocsList.size()];
		
		ArrayList<String> selectedFile = new ArrayList<String>();
		HashMap<Integer, Double> hmProb = new HashMap<Integer, Double>();
		
		// Execute command by running a sub-process
		Process p = Runtime.getRuntime().exec(command);
		
		// Read in CRF++ prediction results
		BufferedReader buffIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		// Loop on each line of input data stream
		while ((str = buffIn.readLine()) != null) {
			if (!str.isEmpty()) {
				// Print CRF++ prediction results
				if (DEBUG)
					System.out.println(str);
				// Read conditional likelihood values (starts with '# 0.')
				if (str.startsWith("# 0.")) {
					// Skip some operations this is the first time see result
					// "#0.x"
					if (docCounter > 0) {
						// Make a copy of predicted labels of the previous
						// segment
						_crfPredLabelList.add((ArrayList<String>) _crfPredLabel
								.clone());
						// Get the length of last text segment
						segLength[docCounter - 1] = _crfPredLabel.size();
						// Calculate the percentages of <EVENT> and <TIMEX3>
						// labels of the previous segment
						eventRatio[docCounter - 1] = (double) eventCounter
								/ (double) tokenCounter;
						timex3Ratio[docCounter - 1] = (double) timex3Counter
								/ (double) tokenCounter;
						if (DEBUG)
							System.out.println("TOTAL = " + tokenCounter
									+ " EVENT = " + eventCounter + " TIMEX3 = "
									+ timex3Counter + "; Event ratio = "
									+ eventRatio[docCounter - 1]
									+ ", TIMEX3 Ratio = "
									+ timex3Ratio[docCounter - 1]);
						// Reset counters to 0
						tokenCounter = 0;
						eventCounter = 0;
						timex3Counter = 0;
						// Clear previous labels
						_crfPredLabel.clear();
					}
					// Obtain the conditional probability value of current
					// document
					condLikelihood[docCounter] = Double.parseDouble(str
							.substring(2));
					// Increment the counter for documents
					docCounter++;
				} 
				else {
					// Increment the counter for tokens
					tokenCounter++;
					// Get and store the predicted label of each token
					int true_left = str.lastIndexOf("<");
					int true_right = str.lastIndexOf(">");
					String predLabel = str.substring(true_left, true_right + 1);
					_crfPredLabel.add(predLabel);
					// If predicted label is EVENT, increment the EVENT counter
					if (predLabel.equalsIgnoreCase("<EVENT>")) {
						eventCounter++;
					}
					// If predicted label is TIMEX3, increment the TIMEX3
					// counter
					else if (predLabel.equalsIgnoreCase("<TIMEX3>")) {
						timex3Counter++;
					}
				}
			}
		}
		// Calculate for the last segment
		_crfPredLabelList.add((ArrayList<String>) _crfPredLabel.clone());
		segLength[docCounter-1] = _crfPredLabel.size();
		eventRatio[docCounter-1] =  (double) eventCounter / (double) tokenCounter;
		timex3Ratio[docCounter-1] = (double) timex3Counter / (double) tokenCounter;
		_crfPredLabel.clear();
		
		if (DEBUG)
			System.out.println("TOTAL = " + tokenCounter + " EVENT = " + eventCounter + " TIMEX3 = " + timex3Counter
					+ "; Event ratio = " + eventRatio[docCounter-1] + ", TIMEX3 Ratio = " + timex3Ratio[docCounter-1]);
		
		// Close the input stream
		buffIn.close();
		
		/********** Finish data collection **********/
		
		
		// Put document indexes and probability values to hash map
		for (int k = 0; k < condLikelihood.length; k++) {
			// Normalize conditional likelihoods with the lengths of segments
			double normalizedCL = Math.pow(condLikelihood[k],
					1.0 / (double) segLength[k]);
			hmProb.put(k, normalizedCL);
		}

		if (DEBUG == true)
			// Display the conditional probability of each document
			for (int m = 0; m < hmProb.size(); m++) {
				System.out.println("hmProb " + m + ":  " + hmProb.get(m));
			}

		if (DEBUG == true)
			// Display EVENT and TIMEX3 labels ratios of each document
			for (int m = 0; m < eventRatio.length; m++)
				System.out.println(eventRatio[m] + "   " + timex3Ratio[m]);
		
		int selectCounter = 0;
		// Sort the HashMap of conditional probabilities by values in descending
		// order
		for (Iterator<Integer> i = sortByValue(hmProb).iterator(); i.hasNext();) {
			// Get the key value
			Integer key = (Integer) i.next();
			// If meet thresholds then add this document to labeled Document Set
			/**************************/
			// Several attempts of combining thresholds
			// if (hmProb.get(key)>0){
			// if ((eventRatio[key]+timex3Ratio[key])>=_minSum){
			if ((eventRatio[key] + timex3Ratio[key]) >= _minSum
					&& hmProb.get(key) > 0) {
				// if (eventRatio[key]>= _threshold_EVENT && timex3Ratio[key] >=
				// _threshold_TIMEX3 && hmProb.get(key)>0){
				// if ((eventRatio[key]>= _threshold_EVENT && hmProb.get(key)>0)
				// || ( timex3Ratio[key] >= _threshold_TIMEX3 &&
				// hmProb.get(key)>0)){
				// if (hmProb.get(key)>0 && eventRatio[key]>0 &&
				// timex3Ratio[key]>0){

			/**************************/
				// if (DEBUG)
				System.out
						.printf("\n*** Satisfy thresholds! key: %s, value: %s, EVENT Ratio: %s, TIMEX3 Ratio: %s, "
								+ "\ndir: %s\n", key, hmProb.get(key),
								eventRatio[key], timex3Ratio[key],
								_unlabeledDocsList.get(key));
				// Add selected document to Selected File Set
				if (!selectedFile.contains(_unlabeledDocsList.get(key))) {
					selectedFile.add(_unlabeledDocsList.get(key));
					_selectedKeys.add(key);
				}
				// Increment selected files counter
				selectCounter++;
				// If achieve maximum selected files number, exit loop
				if (selectCounter == _maxSelectionLimit)
					break;
			}
		}

		// Return the selected files
		return selectedFile;
	}

	
	/**
	 *  Update Train.data of CRF++
	 *  
	 *  @param selectedFiles: the list of selected files
	 *  		iteration: the iteration number
	 *  return <none>
	 */
	private void updateTrainData(ArrayList<String> selectedFiles, int iteration)
			throws IOException {

		// Open train.data, and append new text to the bottom
		FileWriter fstream = new FileWriter(_trainingDataDir, true);
		BufferedWriter out = new BufferedWriter(fstream);

		for (int i = 0; i < selectedFiles.size(); i++) {
			int key = _selectedKeys.get(i);
			for (int j = 0; j < _ulTokensList.get(key).size(); j++) {
				String token = _ulTokensList.get(key).get(j);
				String POStag = _ulPOSsList.get(key).get(j);
				String tmlTag = _crfPredLabelList.get(key).get(j);
				// Put them in one line
				String line = token + " " + POStag + " " + tmlTag + "\n";
				// Write this line to train_file
				out.write(line);
			}
			// Add an empty line between each document
			out.write("\n");
		}
		// Close data stream
		out.close();
		// Copy this train.data file
		String dtPath = _workingDir
				.concat("/data/CoTraining/CRFpp/TrainDataCopies/train.data"
						+ iteration);
		DocUtil.copyFile(_trainingDataDir, dtPath);
	}

	/**
	 * Sort HashMap by its values
	 * 
	 * @param Map: the HashMap to be sorted
	 * @return the sorted sequence of keys
	 */
	public static List<Integer> sortByValue(final Map<Integer, Double> m) {

		// Copy all the keys from the map
		List<Integer> keys = new ArrayList<Integer>();
		keys.addAll(m.keySet());

		// Sort by comparing values
		Collections.sort(keys, new Comparator<Object>() {
			@SuppressWarnings("unchecked")
			public int compare(Object o1, Object o2) {
				Object v1 = m.get(o1);
				Object v2 = m.get(o2);
				if (v1 == null) {
					return (v2 == null) ? 0 : 1;
				} else if (v1 instanceof Comparable) {
					// In descending order
					return ((Comparable<Object>) v2).compareTo(v1);
				} else {
					return 0;
				}
			}
		});

		return keys;
	}

	/**
	 * Evaluate the performance of a specific model
	 * 
	 * @param crf
	 *            : a CRFpp model
	 * @return <none>
	 */
	private void evaluateModel(CRFpp_documents crf) throws Exception {

		double all_precision = 0, all_recall = 0, all_accuracy = 0, all_fscore = 0;
		_testFileCounter = 0;

		// Loop on all testing data
		for (TimeMLParser timeMLInstance : _testingData) {
			// Increment test file counter
			_testFileCounter++;
			if (DEBUG == true)
				System.out.println("[Evaluator] Testing: "
						+ _testingFilesList.get(_testFileCounter - 1));
			// Predict new documents
			crf.computeResults(timeMLInstance);
			// Get necessary testing results
			_testTrueLabelList = crf.getTrueLabelList();
			_testPredLabelList = crf.getPredLabelList();

			// Calculate model's performance (precision, recall, accuracy and
			// F-score)
			double[] pc = PerformanceCounter.countPerformance(_windowSize,
					_testTrueLabelList, _testPredLabelList);
			// Sum all performance metrics
			all_precision += pc[0];
			all_recall += pc[1];
			all_accuracy += pc[2];
			all_fscore += pc[3];
			// Clear lists
			_testTrueLabelList.clear();
			_testPredLabelList.clear();
		}

		// Get the size of testing files
		int testFileSize = crf._testingFilesList.size();
		// Calculate the average performance
		double[] results = PerformanceCounter.countAveragePerformance(
				all_precision, all_recall, all_accuracy, all_fscore,
				testFileSize);
		// Save results to lists and print out at the end of program
		_avgPrecisionList.add(results[0]);
		_avgRecallList.add(results[1]);
		_avgAccuracyList.add(results[2]);
		_avgFscoreList.add(results[3]);

		// Print out performance statistics
		PerformanceCounter.printAveragePerformance();
	}
	

	/**
	 * Main entrance to the program
	 * 
	 * @param
	 * @return
	 */
	public static void main(String[] args) throws Exception {
		
		// Start a timer
		long start = System.currentTimeMillis();

		// Paths of templates
		String template1 = _crfFolder.concat("/template1");
		String template2 = _crfFolder.concat("/template2");
		String template_combined = _crfFolder.concat("/template_combined");
		
		// Paths of models
		String model1 = _crfFolder.concat("/model1");
		String model2 = _crfFolder.concat("/model2");
		String model_combined = _crfFolder.concat("/model_combined");
		
		ArrayList<String> selectedFiles = new ArrayList<String>();	// Selected files
		ArrayList<CRFpp_documents> modelList = new ArrayList<CRFpp_documents>();		// A list of models for co-training

		// Instantiate CRF models with various parameter sets
		CRFpp_documents crf1 = new CRFpp_documents(".tml", 	/* Document type */
								_labeledDocsDir,			/* Directory of training data */
								_testDocsDir, 				/* Directory of testing data */
								_trainingDataDir, 			/* Directory of train file for CRF++ */
								_testingDataDir, 			/* Directory of test file for CRF++ */
								template1, 					/* Directory of template file for CRF++ */
								model1, 					/* Directory of model file for CRF++ */
								1, 							/* Window Size */
								true, 						/* If includes tokens */
								false 						/* If includes POS tags */);

		 CRFpp_documents crf2 = new CRFpp_documents(".tml", /* Document type */
								 _labeledDocsDir, 			/* Directory of training data */
								 _testDocsDir, 				/* Directory of testing data */
								 _trainingDataDir, 			/* Directory of train file for CRF++ */
								 _testingDataDir, 			/* Directory of test file for CRF++ */
								 template2, 				/* Directory of template file for CRF++ */
								 model2, 					/* Directory of model file for CRF++ */
								 1, 						/* Window Size */
								 false, 					/* If includes tokens */
								 true 						/* If includes POS tags */);
		 
		 CRFpp_documents crf_combined = new CRFpp_documents(".tml", /* Document type */
								 _labeledDocsDir, 			/* Directory of training data */
								 _testDocsDir, 				/* Directory of testing data */
								 _trainingDataDir, 			/* Directory of train file for CRF++ */
								 _testingDataDir, 			/* Directory of test file for CRF++ */
								 template_combined, 		/* Directory of template file for CRF++ */
								 model_combined, 			/* Directory of model file for CRF++ */
								 1, 						/* Window Size */
								 true, 						/* If includes tokens */
								 true 						/* If includes POS tags */);
		
		 
		// Create a new instance of the co-training algorithm
		CoTraining_CRF_Documents ct = new CoTraining_CRF_Documents();

		// Obtain a small set of labeled documents L 
		// (create _labeledDocsList, _numOflabeledDocs)
		ct.getlabeledDocs(".tml");

		// Obtain a large set of unlabeled documents U (have to be XML format files)
		// (create _unlabeledDocsList, _numOfUnlabeledDocs)
		ct.getUnlabeledDocs(".xml");
		// Parse the unlabeled documents
		// (create _ulTokensList, _unPOSList)
		ct.parseXMLFiles(_unlabeledDocsList);

		// Parse labeled documents and get training data
		for (int i = 0; i < _labeledDocsList.size(); i++) {
			String fileName = _labeledDocsList.get(i);
			// Using TimeML parser
			TimeMLParser timeMLInstance = new TimeMLParser(fileName);
			timeMLInstance.parse();
			_trainingData.add(timeMLInstance);
		}
		crf1.createTrainFile(_trainingData);

		// Add instances of models of co-training to a list
		if (_hasModel1) {
			modelList.add(crf1);
			crf1.createTemplateFile();
		}
		if (_hasModel2) {
			modelList.add(crf2);
			crf2.createTemplateFile();
		}
		
		// Parse testing files and get testing data (same for all models)
		crf1.parseTestingData(".tml");
		crf2.parseTestingData(".tml");
		if (_hasCombinedModel) crf_combined.parseTestingData(".tml");
		_testingFilesList = crf1._testingFilesList;
		_testingData = crf1.getTestingData();
		
		// Create unlabled.data
		ct.createUnlabeledData(_unlabeledDocsList);

		/******* Iteration Begins ******/
		int N = _iteration, iter_count = 1;
		for (int iter = 1; iter <= N; iter++) {		
			System.out.println("\n\n** Iteration: " + iter);
			/** Loop on each model **/
			for (int k=0; k<modelList.size(); k++){
				System.out.println("\nNow training on model" + (k+1) + ": " + _crfFolder.concat("/model" + (k+1)));
				System.out.println("labeled Documents: " + _labeledDocsList.size());
				System.out.println("Unlabeled Documents: " + _unlabeledDocsList.size());
				System.out.println();
				
				// Build Model k from labeled document set L***
				modelList.get(k).trainModel();
					
				// Select the most confident predictions with model k
				selectedFiles = ct.selectTopResults(_crfFolder.concat("/model" + (k+1)));
				
				// Update train.data
				ct.updateTrainData(selectedFiles, iter);
				
				// Add new data to labeled document set L
				for (int j = 0; j < selectedFiles.size(); j++) {
					if (_unlabeledDocsList.contains(selectedFiles.get(j))) {
						_unlabeledDocsList.remove(selectedFiles.get(j));
						// Set the selected lists to empty
						_ulTokensList.get(_selectedKeys.get(j)).clear();
						_ulPOSsList.get(_selectedKeys.get(j)).clear();
					}
					// And add selected files to labeled document set L
					_labeledDocsList.add(selectedFiles.get(j));
				}
				
				// Remove selected files from unlabeled document set U
				for (int jj=_ulTokensList.size()-1; jj>0; jj--){
					if (_ulTokensList.get(jj).size()==0) {
						_ulTokensList.remove(jj);
					}
					if (_ulPOSsList.get(jj).size()==0) {
						_ulPOSsList.remove(jj);
					}
				}
				
				// Update unlabel.data file
				ct.createUnlabeledData(_unlabeledDocsList);
				
				// Evaluate this model
				System.out.println("\nTesting results of model" + (k+1) + ": ");
				// TODO test.data are created redundantly
				ct.evaluateModel(modelList.get(k));		
				
				// Clear lists for next iteration
				selectedFiles.clear();
				_selectedKeys.clear();
				_crfPredLabelList.clear();
			
			} /* End of loop on two models*/
			
			if (_hasCombinedModel){
				// Evaluate combined model
				System.out.println("\nTesting results of combined model: ");
				crf_combined.createTemplateFile();
				crf_combined.trainModel();
				ct.evaluateModel(crf_combined);	
			}
			
			System.out.println("\n============= Iteration " + iter_count + " done ============== \n\n\n");
			iter_count++;
		} /* End of loop on iterations */
		
		// Stop time counter and get total running time
		long time = System.currentTimeMillis() - start;
		// Convert million seconds to seconds
		int seconds = (int)(time / 1000);
		
		
		/************* Print test results *************/
		int span = 0;
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		System.out.println("\n\n\n******** Co-training Test Report *********");
		System.out.println("Total time: " + seconds + "s");
		System.out.println("The initial number of labeled documents: " + _numOflabeledDocs);
		System.out.println("The initial number of unlabeled documents: " + _numOfUnlabeledDocs);
		System.out.println("Max number of selected documents : " + _maxSelectionLimit);
		System.out.println("EVENT labels threshold: " + _threshold_EVENT);
		System.out.println("TIMEX3 labels threshold: " + _threshold_TIMEX3);
		
		if (_hasModel1 && _hasModel2 && _hasCombinedModel)
			span = 3;
		else if (_hasModel1 && !_hasModel2 && !_hasCombinedModel)
			span = 1;
		else if (!_hasModel1 && _hasModel2 && !_hasCombinedModel)
			span = 1;
		else if (!_hasModel1 && !_hasModel2 && _hasCombinedModel)
			span = 1;
		else 
			span = 2;
		
		if (_hasModel1){
			System.out.println("\nModel1:");
			System.out.println("Precision    Recall    Accuracy    Fscore");
			for (int m=0; m<_avgPrecisionList.size(); m+=span){
				System.out.print("  " + nf.format(_avgPrecisionList.get(m)) + "     ");
				System.out.print(nf.format(_avgRecallList.get(m)) + "     ");
				System.out.print(nf.format(_avgAccuracyList.get(m)) + "     ");
				System.out.print(nf.format(_avgFscoreList.get(m)) + "\n");
			}
		}

		if (_hasModel2){
			System.out.println("\nModel2:");
			System.out.println("Precision    Recall    Accuracy    Fscore");
			for (int m=1; m<_avgPrecisionList.size(); m+=span){
				System.out.print("  " + nf.format(_avgPrecisionList.get(m)) + "     ");
				System.out.print(nf.format(_avgRecallList.get(m)) + "     ");
				System.out.print(nf.format(_avgAccuracyList.get(m)) + "     ");
				System.out.print(nf.format(_avgFscoreList.get(m)) + "\n");
			}
		}

		if (_hasCombinedModel){
			System.out.println("\nCombined model:");
			System.out.println("Precision    Recall    Accuracy    Fscore");
			for (int m=2; m<_avgPrecisionList.size(); m+=span){
				System.out.print("  " + nf.format(_avgPrecisionList.get(m)) + "     ");
				System.out.print(nf.format(_avgRecallList.get(m)) + "     ");
				System.out.print(nf.format(_avgAccuracyList.get(m)) + "     ");
				System.out.print(nf.format(_avgFscoreList.get(m)) + "\n");
			}
		}
	}
}
