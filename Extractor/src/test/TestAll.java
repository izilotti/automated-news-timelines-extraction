/** 
 * Main entrance to the tool
 * 
 * To use a specific model, remove comments 
 * in corresponding lines.
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package test;

import java.util.ArrayList;

import baseline.ModelSelector;
import baseline.benchmark.TARSQI;
import baseline.learning.supervised.CRFpp_documents;
import baseline.learning.supervised.CRFpp_sentences;
import baseline.learning.supervised.NaiveBayes;
import baseline.simple.DictionaryLookup;

public class TestAll {
	
	// Switch on/off Debug function
	public final static boolean DEBUG = false;
	
	/*****************/
	// Directories
	public static String _curWorkingDir = System.getProperty("user.dir");			// Get the path of working directory
	public static String _trainingData = _curWorkingDir.concat("/data/training");	// Directory of training data
//	public static String _testingData = _curWorkingDir.concat("/data/testing");		// Directory of testing data
//	public static String _trainingData = _curWorkingDir.concat("/data/toy/training");	// Directory of training data
//	public static String _testingData = _curWorkingDir.concat("/data/toy/testing");		// Directory of testing data	
	public static String _testingData = _curWorkingDir.concat("/data/Cross_Validation/test/10");	

	
	// CRF++ Directories
	public static String _crfFolder = _curWorkingDir.concat("/data/CRFpp");	// Directory of data for CRF++ model
	public static String _crf_train = _crfFolder.concat("/train.data");		// Directory of train file for CRF++
	public static String _crf_test = _crfFolder.concat("/test.data");		// Directory of test file for CRF++
	public static String _crf_template = _crfFolder.concat("/template");	// Directory of template file for for CRF++
	public static String _crf_model = _crfFolder.concat("/model");			// Directory of model file for CRF++
	
	/*****************/
	
	// Constructors
	public TestAll(){
		/** an empty constructor **/
	}
	
	public TestAll(String trainingData, String testingData){
		_trainingData = trainingData;
		_testingData = testingData;
	}
	
	
	
	public ArrayList<ModelSelector> addTestCase(){
		
		ArrayList<ModelSelector> tests = new ArrayList<ModelSelector>();
    	
    	/** Test Dictionary Lookup model **/
//    	tests.add(new DictionaryLookup(".tml", 			/* Document type */
//    									_trainingData, 	/* Directory of training data */
//    									_testingData	/* Directory of testing data */ ));
    				
    	
    	/** Test Naive Bayes model **/
//    	tests.add(new NaiveBayes(".tml", 		/* Document type */
//    							_trainingData, 	/* Directory of training data */
//    							_testingData,	/* Directory of testing data */ 
//    							1, 				/* Window Size */
//    							true, 			/* If includes POS tags */
//    							true, 			/* If includes Previous TimeML Tags */
//    							0.005			/* Smoothing constant */ ));
    	
    	
    	/** Test TARSQI toolkit **/
//    	tests.add(new TARSQI(".tml", 			/* Document type */
//							  "", 				/* The dir of training data is intentionally left blank */
//							  _testingData		/* Directory of testing data */ ));
    	
    	
    	
    	/** Test CRF++ on documents **/
//    	tests.add(new CRFpp_documents(".tml", 			/* Document type */
//    						_trainingData, 		/* Directory of training data */
//    						_testingData,		/* Directory of testing data */
//    						_crf_train,			/* Directory of train file for CRF++ */
//    						_crf_test,			/* Directory of test file for CRF++ */
//    						_crf_template,		/* Directory of template file for CRF++ */
//    						_crf_model,			/* Directory of model file for CRF++ */
//							1,					/* Window Size */ 
//							true,				/* If includes tokens */
//							true 				/* If includes POS tags */));
    	
    	
    	/** Test CRF++ on sentences **/
    	tests.add(new CRFpp_sentences(".tml", 			/* Document type */
    						_trainingData, 		/* Directory of training data */
    						_testingData,		/* Directory of testing data */
    						_crf_train,			/* Directory of train file for CRF++ */
    						_crf_test,			/* Directory of test file for CRF++ */
    						_crf_template,		/* Directory of template file for CRF++ */
    						_crf_model,			/* Directory of model file for CRF++ */
							1,					/* Window Size */ 
							true,				/* If including tokens */
							true, 				/* If including POS tags */
							false)				/* If has context only */);	
		
		return tests;
	}

	public void doEvaluation(ArrayList<ModelSelector> tests) throws Exception {

		// Evaluate selected models
		Evaluator.doEval(tests);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// Initialise class instance
		TestAll testall = new TestAll();

		// Add test cases
		ArrayList<ModelSelector> tests = testall.addTestCase();

		// Evaluation
		testall.doEvaluation(tests);
	}
}
