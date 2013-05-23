/** 
 * Conditional Random Fields Classifier, training on documents.
 * 
 * 	This class integrates toolkit CRF++ (http://crfpp.sourceforge.net/) 
 * 	to this program and evaluates performance.
 * 
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package baseline.learning.supervised;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import java.io.BufferedWriter;

import java.io.File;

import java.io.FileWriter;

import java.io.IOException;

import java.io.InputStreamReader;

import java.util.ArrayList;

import parsers.TimeMLParser;

import baseline.ModelSelector;


public class CRFpp_documents extends ModelSelector{
	
	/** 
	 * Important: Set up paths below correctly before running
	 * (Make sure the directories are exist)
	 */	
	// The path where CRF++ shell is installed
	public String _CRFppDir = "/usr/local/bin/";
	
	
	public int _windowSize;													// Window size
	
	public String _crf_train;												// Train file for CRF++
	public String _crf_test;												// Test file for CRF++
	public String _crf_template;											// Template file for CRF++
	public String _crf_model;												// Model for CRF++

	public boolean has_Token;												// Flag: if include tokens
	public boolean has_POS;													// Flag: if include POS tags
	
	public ArrayList<String> _tokenList = new ArrayList<String>();			// List of tokens
	public ArrayList<String> _predLabelList = new ArrayList<String>();		// List of predicted TimeML tags
	public ArrayList<String> _trueLabelList = new ArrayList<String>();		// List of true TimeML tags


	// Constructor
	public CRFpp_documents(String type, String trainingDir, String testingDir, String crf_train, String crf_test,
			String crf_template, String crf_model, int windowSize, boolean flag1, boolean flag2) {
		
		super(type, trainingDir, testingDir);
		
		_crf_train = crf_train;
		_crf_test = crf_test;
		_crf_template = crf_template;
		_crf_model = crf_model;
		_windowSize = windowSize;
		has_Token = flag1;
		has_POS = flag2;
		
	}
	
	
	/** 
	 * Here are two tasks in data preparation:
	 * 	1. Create train_file
	 * 	2. Create template_file
	 * When those files are generated, save them in designated directory.
	 * 
	 * @param type: the type of data file (.tml, .xml, etc.)
	 * @return <none>
	 */
	@Override
	public void preprocessing(String type) throws Exception {
		// Create training file for CRF++
		createTrainFile(_trainingData);
		// Create template file for CRF++
		createTemplateFile();
	}
	
	
	/** 
	 * Create training data for CRF++.
	 * There are three columns: tokens, POS tags, and true TimeML tag.
	 * 
	 * @param trainingData: list of TimeMLParser instances
	 * @return <none>
	 */
	public void createTrainFile(ArrayList<TimeMLParser> trainingData) throws IOException{

		int FileCounter = 0;
		
		// Create a file
		FileWriter fstream = new FileWriter(_crf_train);
	    BufferedWriter out = new BufferedWriter(fstream);
	      
	    // Loop on all parsed training data
		for (TimeMLParser timeMLInstance : trainingData) {
			
			FileCounter++;	// Increment file counter	
			if (DEBUG == true)
				System.out.println("[CRFpp] Training:  " + trainingData.get(FileCounter-1));
    		
			// Loop on all tokens of each file
			for (int i=0; i<timeMLInstance.tokenList.size(); i++) {
				// Get current token
				String token = timeMLInstance.tokenList.get(i);
				// Get POS tag of current token
				String POStag = timeMLInstance.POSList.get(i);
				// Get TimeML tag of current token
				String tmlTag = timeMLInstance.tmlList.get(i);	
				// Put them in one line
				String line =  token + " " + POStag + " " + tmlTag + "\n";
				// Write this line to train_file
				out.write(line);
			}
			// Add an empty line between each document
			out.write("\n");
		}
		out.close();
		if (DEBUG == true)
			System.out.println("[CRFpp] Train file " + _crf_train
					+ " is created successfully.");
	}
	
	
	/** 
	 * Create testing data for CRF++.
	 * (Using the same format as training data)
	 * 
	 * @param timeMLInstance: TimeMLParser instance
	 * @return <none>
	 */
	protected void createSingleTestFile(TimeMLParser timeMLInstance) throws IOException{
		
		int FileCounter = 0;
		
		FileWriter fstream = new FileWriter(_crf_test);
	    BufferedWriter out = new BufferedWriter(fstream);
			
		FileCounter++;	// Increment file counter
		
		// Loop on all tokens of each file
		for (int i=0; i<timeMLInstance.tokenList.size(); i++) {
			
			String token = timeMLInstance.tokenList.get(i);
			String POStag = timeMLInstance.POSList.get(i);
			String tmlTag = timeMLInstance.tmlList.get(i);
			String line =  token + " " + POStag + " " + tmlTag + "\n";
			// Write each line to test_file
			out.write(line);
		}
	    out.close();
	    if (DEBUG == true)
	    	System.out.println("[CRFpp] Test file " + _crf_test + " is created successfully.");
	}
	
	
	
	/** 
	 *  Create feature template file for CRF++.
	 * 
	 *  This file describes which features are used in training
	 *  and testing files.
	 * 
	 * @param <none>
	 * @return <none>
	 */
	public void createTemplateFile() throws IOException{
		
		FileWriter fstream = new FileWriter(_crf_template);
	    BufferedWriter out = new BufferedWriter(fstream);
	
		out.write("# Unigram\n");
		// Feature token (column 0), start from index U00
		if (has_Token){		
			for (int i=0; i<2*_windowSize+1; i++){
				out.write("U0" + i + ":%x[" + (i-_windowSize) + "," + 0 + "]\n");
			}
			for(int j=0; j<2*_windowSize; j++){
				out.write("U0" + (2*_windowSize+1+j) + ":%x[" + (j-_windowSize) + 
						"," + 0 + "]/%x[" + (j-_windowSize+1) + "," + 0 + "]\n");
			}
			out.write("\n");	
		}
		// Feature POS (column 1), start from index U10
		if (has_POS){
			for (int m=0; m<2*_windowSize+1; m++){
				out.write("U1" + m + ":%x[" + (m-_windowSize) + "," + 1 + "]\n");
			}
			for(int n=0; n<2*_windowSize; n++){
				out.write("U1" + (2*_windowSize+1+n) + ":%x[" + (n-_windowSize) + 
						"," + 1 + "]/%x[" + (n-_windowSize+1) + "," + 1 + "]\n");
			} 
			out.write("\n");
		}
		// Bigram
		out.write("# Bigram\n");
		out.write("B\n");
		
		out.close();
		if (DEBUG == true) 
			 System.out.println("[CRFpp] Template file " + _crf_template + " is created successfully.\n");
	}
	
	
	/** 
	 * Call CRF++ toolkit to generate the model_file (i.e. train the model)
	 * The command is: "crf_learn template_file train_file model_file"
	 * 
	 * @param <none>
	 * @return <none>
	 */
	@Override
	public void trainModel() throws Exception {
		
		// Use 4 threads to run CRF++
		String command = _CRFppDir + "crf_learn -p 4 " + _crf_template + " "
				+ _crf_train + " " + _crf_model;
		if (DEBUG == true)
			System.out.println("The train command is: " + command);

		try {
			File f = new File(_crf_model);

			// If an old model file exists, delete it before run CRF++
			if (f.exists())
				f.delete();

			System.out.println("[CRFpp] CRF++ is training, please wait...");
			// Execute CRF++ command in run time
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader process_out = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			PrintWriter process_in = new PrintWriter(p.getOutputStream(), true);

			// Provide input to process (could come from any stream)
			process_in.close(); // Need to close input stream so process exits!
			process_out.close();
			p.waitFor();

			// Check if model file is created, otherwise wait
			while (!f.exists()) {
				Thread.sleep(1000);
			}
			if (DEBUG == true)
				System.out.println("[CRFpp] Done. Model file " + _crf_model
						+ " generated.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/** 
	 * Call CRF++ toolkit to run testing
	 * The command is: "crf_test -m model_file test_file"
	 * 
	 * @param model: id of model
	 * 		testFile: the path of testing file
	 * @return <none>
	 */
	public void labelDocuments(String model, String testFile) {

		String command = _CRFppDir + "crf_test -m " + model + " " + testFile;
		if (DEBUG == true)
			System.out.println("The test command is: " + command);

		try {
			// Execute the command by running a subprocess
			Process p = Runtime.getRuntime().exec(command);

			// Read in input stream
			BufferedReader buffIn = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String str = null;
			while ((str = buffIn.readLine()) != null) {
				// Display stdout
				if (DEBUG == true)
					System.out.println(str);
				if (!str.isEmpty()) {
					if (!str.startsWith("# 0.")) {
						// Get the last column (the estimated tag)
						_predLabelList.add(str.substring(str.lastIndexOf('<')));
						// Get the second to last column (the true answer)
						int true_left = str.indexOf("<");
						int true_right = str.indexOf(">");
						_trueLabelList.add(str.substring(true_left,
								true_right + 1));
					}
				}
			}
			buffIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Display true tags
		if (DEBUG == true) {
			System.out.println("\n_trueLabelList.size() = "
					+ _trueLabelList.size());
			for (int i = 0; i < _trueLabelList.size(); i++) {
				System.out.println(_trueLabelList.get(i));
			}
		}

		// Display estimated tags
		if (DEBUG == true) {
			System.out.println("\n_predLabelList.size() = "
					+ _predLabelList.size());
			for (int i = 0; i < _predLabelList.size(); i++) {
				System.out.println(_predLabelList.get(i));
			}
		}
	}
	
	
	/**
	 * Count CRF++ performance
	 * 
	 * @param timeMLInstance: the test file that parsed by TimeML parser
	 * @return <none>
	 */
	@Override
	public void computeResults(TimeMLParser timeMLInstance) throws Exception {

		// Create test_file for CRF++
		createSingleTestFile(timeMLInstance);

		// Use CRF++ to label new documents
		labelDocuments(_crf_model, _crf_test);

	}

	@Override
	public ArrayList<String> getTokenList() {

		return _tokenList;
	}

	@Override
	public ArrayList<String> getPredLabelList() {

		return _predLabelList;
	}

	@Override
	public ArrayList<String> getTrueLabelList() {

		return _trueLabelList;
	}

	@Override
	public int getWindowSize() {

		return _windowSize;
	}

	@Override
	public void clearLists() {

		_tokenList.clear();
		_trueLabelList.clear();
		_predLabelList.clear();

	}
}
