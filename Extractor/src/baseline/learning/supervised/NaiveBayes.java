/** 
 * Naive Bayes Classifier 
 * 
 * Naive Bayes model assumes there is no dependence between
 * each pair of tokens. But selections of features are of 
 * great importance to NB's performances.
 * 
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package baseline.learning.supervised;

import parsers.TimeMLParser;

import util.DocUtil;

import java.util.ArrayList;
import java.util.HashMap;

import baseline.ModelSelector;


public class NaiveBayes extends ModelSelector{
	
	public static boolean _addLabelledData = true;
	
	public int _windowSize;				// Set the window size (size 0 means on current position)
	public boolean _has_POS;			// If feature 'POS' is included in this model
	public boolean _has_prevLabel;		// If feature 'previous label' is included in this model
	public double _smoothing_const;		// To eliminate zero probabilities
	
	double total = 0, numEVENT = 0, numTIMEX3 = 0, numNONE = 0;
	
	public ArrayList<String> _tokenList = new ArrayList<String>();		// List of tokens
	public ArrayList<String> _predLabelList = new ArrayList<String>();	// List of predicted TimeML tags
	public ArrayList<String> _trueLabelList = new ArrayList<String>();	// List of true TimeML tags
	
	// Initialize/Declare HashMaps/Array of HashMaps for feature counts according to Window Size
	// When Window Size = 0:
	HashMap<String,Integer> hmThisToken = new HashMap<String,Integer>();	// token on position i
	HashMap<String,Integer> hmThisPOS = new HashMap<String,Integer>();		// POS on position i
	// When Window Size > 0:
	HashMap<String, Integer>[] hmPrevToken;		// token on position i-x
	HashMap<String, Integer>[] hmNextToken;		// token on position i+x
	HashMap<String, Integer>[] hmPrevPOS;		// POS on position i-x	
	HashMap<String, Integer>[] hmNextPOS;		// POS on position i+x
	HashMap<String, Integer>[] hmPrevLabel;		// TimeML tag on position i-x
	
	// Constructor
	@SuppressWarnings("unchecked")
	public NaiveBayes(String prefix, String trainingDir, String testingDir, int windowSize, 
			boolean has_POS, boolean has_prevLabel, double smoothing_const){
		
		// Call SuperClass constructor
		super(prefix, trainingDir, testingDir);
		
		// Make sure Window Size is within a boundary to limit computation complexity
		if (windowSize>5) {
			System.err.println("[Naive Bayes] Warning: The Window Size shall not exceed 5. Stop.");
			System.exit(1);
		}
		
		_windowSize = windowSize;
		_has_POS = has_POS;
		_has_prevLabel = has_prevLabel;
		_smoothing_const = smoothing_const;
		
		hmPrevToken = new HashMap[_windowSize];		// token on position i-x
		hmNextToken = new HashMap[_windowSize];		// token on position i+x
		hmPrevPOS = new HashMap[_windowSize];		// POS on position i-x	
		hmNextPOS = new HashMap[_windowSize];		// POS on position i+x
		hmPrevLabel = new HashMap[_windowSize];		// TimeML label on position i-x
		
		// Initialize Array of HashMaps
		for (int index=0; index<_windowSize; index++){
			hmPrevToken[index] = new HashMap<String, Integer>();
			hmNextToken[index] = new HashMap<String, Integer>();
			hmPrevPOS[index] = new HashMap<String, Integer>();
			hmNextPOS[index] = new HashMap<String, Integer>();
			hmPrevLabel[index] = new HashMap<String, Integer>();
		}
	}
	
	
	/** Data pre-processing
	 * 
	 * @param type: the file type (.tml or .xml) of data
	 * @return <none>
	 */
	@Override
	public void preprocessing(String type) throws Exception {
		
		/** This is intended to be empty **/
		
	}
	
	
	/** Training Naive Bayes model
	 * 
	 * @param <none>
	 * @return <none>
	 */
	@Override
	public void trainModel() throws Exception{
		
		System.out.println("\n[Naive Bayes]: NB, Training...");
		int FileCounter = 0;
		
		// Iterate all parsed training data files
		for (TimeMLParser timeMLInstance : _trainingData) {
			FileCounter++;
			System.out.println("[Naive Bayes] Training:  " + _trainingFilesList.get(FileCounter-1));
			
			/* 
			 * Adding Beginning and Ending tags to each ArrayLists of parsed files,
			 * in order to process the first and last token correctly with various Window
			 * Sizes. Note the number of inserted tags for each ArrayList shall be 
			 * equivalent to the value of Window Size .
			 * 
			 */
			for (int i=0; i<_windowSize; i++){
				timeMLInstance.tokenList.add(0, "[BEGIN_Token]");
				timeMLInstance.tokenList.add(timeMLInstance.tokenList.size(), "[END_Token]");
				timeMLInstance.POSList.add(0, "[BEGIN_POS]");
				timeMLInstance.POSList.add(timeMLInstance.POSList.size(), "[END_POS]");
				timeMLInstance.tmlList.add(0, "[BEGIN_TimeML]");
				timeMLInstance.tmlList.add(timeMLInstance.tmlList.size(), "[END_TimeML]");
			}
			
			// Iterate all tokens of training data except for new inserted tags
			for (int i=_windowSize; i<timeMLInstance.tokenList.size()-_windowSize; i++){
				
				if (DEBUG == true){
					// Display lists
					System.out.println("Iterate all tokens of training data except for above new inserted tags:");
					System.out.println("tokenList("+i+"): " + timeMLInstance.tokenList.get(i) 
							+ " \\" + timeMLInstance.POSList.get(i) + ", "
							+ timeMLInstance.tmlList.get(i));
				}
				
				// Extract text elements
				String thisToken = timeMLInstance.tokenList.get(i);
				String thisPOS = timeMLInstance.POSList.get(i);
				
				// Skip sentence boundary
				if (thisToken.isEmpty() && thisPOS.isEmpty())
					continue;
				
				String[] prevToken = new String[_windowSize];
				String[] nextToken = new String[_windowSize];
				String[] prevPOS = new String[_windowSize];
				String[] nextPOS = new String[_windowSize];
				String[] prevLabel = new String[_windowSize];
				for (int index=0; index<_windowSize; index++){
					prevToken[index] = timeMLInstance.tokenList.get(i-index-1);
					nextToken[index] = timeMLInstance.tokenList.get(i+index+1);
					prevPOS[index] = timeMLInstance.POSList.get(i-index-1);
					nextPOS[index] = timeMLInstance.POSList.get(i+index+1);	
					prevLabel[index] = timeMLInstance.tmlList.get(i-index-1);
				}
				
				if (DEBUG == true){
					// Display elements in sequence
					for (int index=_windowSize-1; index>=0; index--){
						System.out.println("prevToken["+index+"]: "+prevToken[index]);
						System.out.println("prevPOS["+index+"]: "+prevPOS[index]);
						System.out.println("prevLabel["+index+"]: "+prevLabel[index]);
					}
					System.out.println("thisToken: "+thisToken);
					System.out.println("thisPOS: "+thisPOS);
					for (int index=0; index<_windowSize; index++){
						System.out.println("nextToken["+index+"]: "+nextToken[index]);
						System.out.println("nextPOS["+index+"]: "+nextPOS[index]);
					}
					System.out.println("------------------------------------");
				}
				
				// Call feature counting function according to the TimeML tag
				String label = timeMLInstance.tmlList.get(i);
				if (label.equalsIgnoreCase("<EVENT>")){
					// If the label is <EVENT>, then pass an argument "E" to countFeatures function
					countFeatures("E", thisToken, thisPOS, prevToken, nextToken, prevPOS, 
							nextPOS, prevLabel);
					// Increment Event tag counter
					numEVENT++;
				}
				else if (label.equalsIgnoreCase("<TIMEX3>")){
					// If the label is <TIMEX3>, then pass an argument "T" to countFeatures function
					countFeatures("T", thisToken, thisPOS, prevToken, nextToken, prevPOS, 
							nextPOS, prevLabel);
					// Increment TIMEX3 tag counter
					numTIMEX3++;
				}
				else {
					// Otherwise pass an argument "N" to countFeatures function
					countFeatures("N", thisToken, thisPOS, prevToken, nextToken, prevPOS, 
							nextPOS, prevLabel);
					// Increment NONE tag counter
					numNONE++;
				}
				
				// Increment counter of all tokens
				total++;
				
			}  /* End of loop on tokens */	
			
		}  /* End of loop on files */
		
		
		/** 
		 *  line 205 - 318:
		 * 	This part is a temporary process to have Naive Bayes model
		 *  integrate new labeled data from Co-training and evaluate 
		 *  the performance. It has nothing to do with the normal functions
		 *  of Naive Bayes model.
		 *  
		 *  If tested with new labeled data, turn _addLabelledData to true. 
		 *  **/
		if(_addLabelledData){
			// Read new labeled data set from file
			ArrayList<ArrayList<String>> newLabeledDocSet = DocUtil.readNewLabelledData();
			// Variables
			ArrayList<String> tokens = new ArrayList<String>();
			ArrayList<String> POSs = new ArrayList<String>();
			ArrayList<String> labels = new ArrayList<String>();
			ArrayList<ArrayList<String>> tokensList = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> POSsList = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> labelsList = new ArrayList<ArrayList<String>>();
			for (int i=0; i<newLabeledDocSet.size(); i++) {
				for (int j=0; j<newLabeledDocSet.get(i).size(); j++) {
					String line = newLabeledDocSet.get(i).get(j);
					String[] elements = line.split(" ");
					String token = elements[0];
					String POS = elements[1];
					String label = elements[2];
					tokens.add(token);
					POSs.add(POS);
					labels.add(label);
				}
				@SuppressWarnings("unchecked")
				ArrayList<String> tokens_copy = (ArrayList<String>) tokens.clone();
				@SuppressWarnings("unchecked")
				ArrayList<String> POSs_copy = (ArrayList<String>) POSs.clone();
				@SuppressWarnings("unchecked")
				ArrayList<String> labels_copy = (ArrayList<String>) labels.clone();
				tokensList.add(tokens_copy);
				POSsList.add(POSs_copy);
				labelsList.add(labels_copy);
				tokens.clear();
				POSs.clear();
				labels.clear();
			}
			
			for (int m=0; m<tokensList.size(); m++) {
				FileCounter++;
				System.out.println("[Naive Bayes] Training on new added data:  " + m);
				
				/* 
				 * Adding Beginning and Ending tags to each ArrayLists of parsed files,
				 * in order to process the first and last token correctly with various Window
				 * Sizes. Note the number of inserted tags for each ArrayList shall be 
				 * equivalent to the value of Window Size .
				 * 
				 */
				for (int i=0; i<_windowSize; i++){
					tokensList.get(m).add(0, "[BEGIN_Token]");
					tokensList.get(m).add(tokensList.get(m).size(), "[END_Token]");
					POSsList.get(m).add(0, "[BEGIN_POS]");
					POSsList.get(m).add(POSsList.get(m).size(), "[END_POS]");
					labelsList.get(m).add(0, "[BEGIN_TimeML]");
					labelsList.get(m).add(labelsList.get(m).size(), "[END_TimeML]");
				}
				
				// Iterate all tokens of training data except for new inserted tags
				for (int i=_windowSize; i<tokensList.get(m).size()-_windowSize; i++){
					
					// Extract text elements
					String thisToken = tokensList.get(m).get(i);
					String thisPOS = POSsList.get(m).get(i);
					String[] prevToken = new String[_windowSize];
					String[] nextToken = new String[_windowSize];
					String[] prevPOS = new String[_windowSize];
					String[] nextPOS = new String[_windowSize];
					String[] prevLabel = new String[_windowSize];
					for (int index=0; index<_windowSize; index++){
						prevToken[index] = tokensList.get(m).get(i-index-1);
						nextToken[index] = tokensList.get(m).get(i+index+1);
						prevPOS[index] = POSsList.get(m).get(i-index-1);
						nextPOS[index] = POSsList.get(m).get(i+index+1);	
						prevLabel[index] = labelsList.get(m).get(i-index-1);
					}
					
					// Call feature counting function according to the TimeML tag
					String label = labelsList.get(m).get(i);
					if (label.equalsIgnoreCase("<EVENT>")){
						// If the label is <EVENT>, then pass an argument "E" to countFeatures function
						countFeatures("E", thisToken, thisPOS, prevToken, nextToken, prevPOS, 
								nextPOS, prevLabel);
						// Increment Event tag counter
						numEVENT++;
					}
					else if (label.equalsIgnoreCase("<TIMEX3>")){
						// If the label is <TIMEX3>, then pass an argument "T" to countFeatures function
						countFeatures("T", thisToken, thisPOS, prevToken, nextToken, prevPOS, 
								nextPOS, prevLabel);
						// Increment TIMEX3 tag counter
						numTIMEX3++;
					}
					else {
						// Otherwise pass an argument "N" to countFeatures function
						countFeatures("N", thisToken, thisPOS, prevToken, nextToken, prevPOS, 
								nextPOS, prevLabel);
						// Increment NONE tag counter
						numNONE++;
					}
					
					// Increment counter of all tokens
					total++;
					
				}  /* End of loop on tokens */	
				
			}  /* End of loop on files */		
		}
		/** End of temporary part codes **/
		
		System.out.println("\n[Naive Bayes] Training Ends.\n\n");
	}
	

	/** Counting features from training data
	 * 
	 * @param str: label of the token
	 * @param tToken: the token of current position
	 * @param tPOS: the POS tag of the token of current position
	 * @param pToken: tokens in previous positions
	 * @param nToken: tokens in following positions
	 * @param pPOS: POS tags in previous positions
	 * @param nPOS: POS tags in following positions
	 * @param pLabel: TimeML label of previous tokens
	 * @return
	 */
	private void countFeatures(String str, String tToken, String tPOS, String[] pToken, 
			String[] nToken, String[] pPOS,  String[] nPOS, String[] pLabel) throws Exception{
		
		String label = str;
		String thisToken = tToken;
		String thisPOS = tPOS;
		String[] prevToken = pToken;
		String[] nextToken = nToken;
		String[] prevPOS = pPOS;
		String[] nextPOS = nPOS;
		String[] prevLabel = pLabel;
		
		// Concatenate each element with corresponding label and then store in HashMaps
		String thisToken_label = thisToken.concat("_").concat(label);
		String thisPOS_label = thisPOS.concat("_").concat(label);
		String[] prevToken_label = new String[_windowSize];
		String[] nextToken_label = new String[_windowSize];
		String[] prevPOS_label = new String[_windowSize];
		String[] nextPOS_label = new String[_windowSize];
		String[] prevLabel_label = new String[_windowSize];
		for (int index=0; index<_windowSize; index++){
			prevToken_label[index] = prevToken[index].concat("_").concat(label);;
			nextToken_label[index] = nextToken[index].concat("_").concat(label);
			prevPOS_label[index] = prevPOS[index].concat("_").concat(label);
			nextPOS_label[index] = nextPOS[index].concat("_").concat(label);
			prevLabel_label[index] = prevLabel[index].concat("_").concat(label);
		}
		
		if (DEBUG == true){
			// Display elements with labels in sequence
			for (int index=_windowSize-1; index>=0; index--){
				System.out.println("prevToken_label["+index+"]: "+prevToken_label[index]);
				System.out.println("prevPOS_label["+index+"]: "+prevPOS_label[index]);
				System.out.println("prevLabel_label["+index+"]: "+prevLabel_label[index]);
			}
			System.out.println("thisToken_label: "+thisToken_label);
			System.out.println("thisToken_label: "+thisPOS_label);
			for (int index=0; index<_windowSize; index++){
				System.out.println("nextToken_label["+index+"]: "+nextToken_label[index]);
				System.out.println("nextPOS_label["+index+"]: "+nextPOS_label[index]);
			}
			System.out.println("------------------------------------");
		}
		
		/*** HashMaps set up ***/
		// Check whether they are contained in corresponding HashMaps
		if (hmThisToken.containsKey(thisToken_label)) {
			// Get the count(HashMap value) of occurrences of this String, 
            // increment it and put it back.
			hmThisToken.put(thisToken_label, hmThisToken.get(thisToken_label) + 1);
		} else {
			// Or if this is the first time see it then set value to 1
			hmThisToken.put(thisToken_label, 1);
        }
		
		if (hmThisPOS.containsKey(thisPOS_label)) {
			hmThisPOS.put(thisPOS_label, hmThisPOS.get(thisPOS_label) + 1);
		} else {
			hmThisPOS.put(thisPOS_label, 1);
        }
		
		for (int index=0; index<_windowSize; index++){
			if (hmPrevToken[index].containsKey(prevToken_label[index])) {
				hmPrevToken[index].put(prevToken_label[index], 
						hmPrevToken[index].get(prevToken_label[index])+1);
			} else {
				hmPrevToken[index].put(prevToken_label[index], 1);
            }
			
			if (hmNextToken[index].containsKey(nextToken_label[index])) {
				hmNextToken[index].put(nextToken_label[index], 
						hmNextToken[index].get(nextToken_label[index])+1);
			} else {
				hmNextToken[index].put(nextToken_label[index], 1);
            }
			
			if (hmPrevPOS[index].containsKey(prevPOS_label[index])) {
				hmPrevPOS[index].put(prevPOS_label[index], 
						hmPrevPOS[index].get(prevPOS_label[index])+1);
			} else {
				hmPrevPOS[index].put(prevPOS_label[index], 1);
            }
			
			if (hmNextPOS[index].containsKey(nextPOS_label[index])) {
				hmNextPOS[index].put(nextPOS_label[index], 
						hmNextPOS[index].get(nextPOS_label[index]) + 1);
			} else {
				hmNextPOS[index].put(nextPOS_label[index], 1);
            }
			
			if (hmPrevLabel[index].containsKey(prevLabel_label[index])) {
				hmPrevLabel[index].put(prevLabel_label[index], 
						hmPrevLabel[index].get(prevLabel_label[index]) + 1);
			} else {
				hmPrevLabel[index].put(prevLabel_label[index], 1);
            }
		}
	}
	
	
	/** Search feature counts from trained HashMaps
	 * 
	 * @param hm: the HashMap which feature counts are stored
	 * @param str: the objective token
	 * @param label: TimeML label of the token
	 * @return counts
	 */
	private int searchHashMap(HashMap<String, Integer> hm, String str, 
			String label) throws Exception{
		
		Integer cur_count = 0;
		String key = str.concat("_").concat(label);
		
		cur_count = hm.get(key);
		if (cur_count == null) cur_count = 0;
		
		return cur_count;
	}
	
	
	/** Compute results with Naive Bayes model
	 * 
	 * @param timeMLInstance: the Instance of the file parsed by TimeMLParser
	 * @return <none>
	 */
	@Override
	public void computeResults(TimeMLParser timeMLInstance) throws Exception{
	
		double P_EVENT = 0, P_TIMEX3 = 0, P_NONE = 0, P_MAX = 0;
		double P_E = 0, P_T = 0, P_N = 0;
		double P_thisToken_E = 0, P_thisToken_T = 0, P_thisToken_N = 0;
		double P_thisPOS_E = 0, P_thisPOS_T = 0, P_thisPOS_N = 0;
		
		double[] P_prevToken_E = new double[_windowSize];
		double[] P_prevToken_T = new double[_windowSize];
		double[] P_prevToken_N = new double[_windowSize];
		double[] P_nextToken_E = new double[_windowSize];
		double[] P_nextToken_T = new double[_windowSize];
		double[] P_nextToken_N = new double[_windowSize];
		
		double[] P_prevPOS_E = new double[_windowSize];
		double[] P_prevPOS_T = new double[_windowSize];
		double[] P_prevPOS_N = new double[_windowSize];
		double[] P_nextPOS_E = new double[_windowSize];
		double[] P_nextPOS_T = new double[_windowSize];
		double[] P_nextPOS_N = new double[_windowSize];
		
		double[] P_prevLabel_E = new double[_windowSize];
		double[] P_prevLabel_T = new double[_windowSize];
		double[] P_prevLabel_N = new double[_windowSize];
		
		// Probabilities computation; Part A
		P_E = numEVENT / total;
		P_T = numTIMEX3 / total;
		P_N = numNONE / total;
		if (DEBUG == true) {
			System.out.println("P_E: " + P_E);
			System.out.println("P_T: " + P_T);
			System.out.println("P_N: " + P_N);
		}
		
		// Initialize a new label list with <NONE>
		for (int i=0; i<timeMLInstance.tokenList.size(); i++){
			if (timeMLInstance.tokenList.get(i).isEmpty() && timeMLInstance.POSList.get(i).isEmpty())
				_predLabelList.add("");
			else
				_predLabelList.add("<NONE>");
		}
		
		
		// Add Beginning and Ending tags for each testing file (Similar to before)
		for (int j=0; j<_windowSize; j++){
			timeMLInstance.tokenList.add(0, "[BEGIN_Token]");
			timeMLInstance.tokenList.add(timeMLInstance.tokenList.size(), "[END_Token]");
			timeMLInstance.POSList.add(0, "[BEGIN_POS]");
			timeMLInstance.POSList.add(timeMLInstance.POSList.size(), "[END_POS]");
			timeMLInstance.tmlList.add(0, "[BEGIN_TimeML]");
			timeMLInstance.tmlList.add(timeMLInstance.tmlList.size(), "[END_TimeML]");
			_predLabelList.add(0, "[BEGIN_TimeML]");
			_predLabelList.add(_predLabelList.size(), "[END_TimeML]");
		}
		
		
		// Iterate all tokens of testing data except for Beginning and Ending tags
		for (int i=_windowSize; i<timeMLInstance.tokenList.size()-_windowSize; i++){
			
			// Extract all elements but not all of them will be used in NB algorithm
			String thisToken = timeMLInstance.tokenList.get(i);
			String thisPOS = timeMLInstance.POSList.get(i);
			
			// Skip sentence boundary
			if (thisToken.isEmpty() && thisPOS.isEmpty())
				continue;
			
			String[] prevToken = new String[_windowSize];
			String[] nextToken = new String[_windowSize];
			String[] prevPOS = new String[_windowSize];
			String[] nextPOS = new String[_windowSize];
			String[] prevLabel = new String[_windowSize];
			for (int index=0; index<_windowSize; index++){
				prevToken[index] = timeMLInstance.tokenList.get(i-index-1);
				nextToken[index] = timeMLInstance.tokenList.get(i+index+1);
				prevPOS[index] = timeMLInstance.POSList.get(i-index-1);
				nextPOS[index] = timeMLInstance.POSList.get(i+index+1);	
				prevLabel[index] = timeMLInstance.tmlList.get(i-index-1);
			}
			
			if (DEBUG == true){
				// Display elements in sequence
				for (int index=_windowSize-1; index>=0; index--){
					System.out.println("prevToken["+index+"]: "+prevToken[index]);
					System.out.println("prevPOS["+index+"]: "+prevPOS[index]);
					System.out.println("prevLabel["+index+"]: "+prevLabel[index]);
				}
				System.out.println("thisToken: "+thisToken);
				System.out.println("thisToken: "+thisPOS);
				for (int index=0; index<_windowSize; index++){
					System.out.println("nextToken["+index+"]: "+nextToken[index]);
					System.out.println("nextPOS["+index+"]: "+nextPOS[index]);
				}
				System.out.println("------------------------------------");
			}		
			
			// Probabilities computation; Part B
			P_thisToken_E = (searchHashMap(hmThisToken, thisToken, "E") + _smoothing_const) 
							/ (numEVENT + _smoothing_const);	
			P_thisToken_T = (searchHashMap(hmThisToken, thisToken, "T") + _smoothing_const) 
							/ (numTIMEX3 + _smoothing_const);
			P_thisToken_N = (searchHashMap(hmThisToken, thisToken, "N") + _smoothing_const) 
							/ (numNONE + _smoothing_const);
			if (DEBUG == true){
				System.out.println("thisToken, E = "+
						searchHashMap(hmThisToken, thisToken, "E") + ", P = " + P_thisToken_E + " " + numEVENT);
				System.out.println("thisToken, T = "+
						searchHashMap(hmThisToken, thisToken, "T") + ", P = " + P_thisToken_T + " " + numTIMEX3);
				System.out.println("thisToken, N = "+
						searchHashMap(hmThisToken, thisToken, "N") + ", P = " + P_thisToken_N + " " + numNONE);
			}

			P_thisPOS_E = (searchHashMap(hmThisPOS, thisPOS, "E") + _smoothing_const) 
							/ (numEVENT + _smoothing_const);
			P_thisPOS_T = (searchHashMap(hmThisPOS, thisPOS, "T") + _smoothing_const) 
							/ (numTIMEX3 + _smoothing_const);
			P_thisPOS_N = (searchHashMap(hmThisPOS, thisPOS, "N") + _smoothing_const) 
							/ (numNONE + _smoothing_const);
			if (DEBUG == true){
				System.out.println("thisPOS, E = "+
						searchHashMap(hmThisPOS, thisPOS, "E") + ", P = " + P_thisPOS_E);
				System.out.println("thisPOS, T = "+
						searchHashMap(hmThisPOS, thisPOS, "T") + ", P = " + P_thisPOS_T);
				System.out.println("thisPOS, N = "+
						searchHashMap(hmThisPOS, thisPOS, "N") + ", P = " + P_thisPOS_N);
			}		
			
			// Compute probabilities within the window size
			for (int index=0; index<_windowSize; index++){
				P_prevToken_E[index] = (searchHashMap(hmPrevToken[index], prevToken[index], "E") 
										+ _smoothing_const) / (numEVENT + _smoothing_const);
				P_prevToken_T[index] = (searchHashMap(hmPrevToken[index], prevToken[index], "T") 
										+ _smoothing_const) / (numTIMEX3 + _smoothing_const);
				P_prevToken_N[index] = (searchHashMap(hmPrevToken[index], prevToken[index], "N") 
										+ _smoothing_const) / (numNONE + _smoothing_const);
				P_nextToken_E[index] = (searchHashMap(hmNextToken[index], nextToken[index], "E") 
										+ _smoothing_const) / (numEVENT + _smoothing_const);
				P_nextToken_T[index] = (searchHashMap(hmNextToken[index], nextToken[index], "T") 
										+ _smoothing_const) / (numTIMEX3 + _smoothing_const);
				P_nextToken_N[index] = (searchHashMap(hmNextToken[index], nextToken[index], "N") 
										+ _smoothing_const) / (numNONE + _smoothing_const);
				
				P_prevPOS_E[index] = (searchHashMap(hmPrevPOS[index], prevPOS[index], "E") 
										+ _smoothing_const) / (numEVENT + _smoothing_const);
				P_prevPOS_T[index] = (searchHashMap(hmPrevPOS[index], prevPOS[index], "T") 
										+ _smoothing_const) / (numTIMEX3 + _smoothing_const);
				P_prevPOS_N[index] = (searchHashMap(hmPrevPOS[index], prevPOS[index], "N") 
										+ _smoothing_const) / (numNONE + _smoothing_const);
				P_nextPOS_E[index] = (searchHashMap(hmNextPOS[index], nextPOS[index], "E") 
										+ _smoothing_const) / (numEVENT + _smoothing_const);
				P_nextPOS_T[index] = (searchHashMap(hmNextPOS[index], nextPOS[index], "T") 
										+ _smoothing_const) / (numTIMEX3 + _smoothing_const);
				P_nextPOS_N[index] = (searchHashMap(hmNextPOS[index], nextPOS[index], "N") 
										+ _smoothing_const) / (numNONE + _smoothing_const);
				
				P_prevLabel_E[index] = (searchHashMap(hmPrevLabel[index], prevLabel[index], "E") 
										+ _smoothing_const) / (numEVENT + _smoothing_const);
				P_prevLabel_T[index] = (searchHashMap(hmPrevLabel[index], prevLabel[index], "T") 
										+ _smoothing_const) / (numTIMEX3 + _smoothing_const);
				P_prevLabel_N[index] = (searchHashMap(hmPrevLabel[index], prevLabel[index], "N") 
										+ _smoothing_const) / (numNONE + _smoothing_const);
			}
			
			// Probabilities computation; Part C
			double P_conditional_E = 0, P_conditional_T = 0, P_conditional_N = 0;
			
			// When Window Size = 0 and no POS
			P_conditional_E = P_thisToken_E;
			P_conditional_T = P_thisToken_T;
			P_conditional_N = P_thisToken_N;
			
			// When Window Size = 0 and with POS
			if (_has_POS == true) {
				P_conditional_E *= P_thisPOS_E;
				P_conditional_T *= P_thisPOS_T;
				P_conditional_N *= P_thisPOS_N;
			}
			
			// When Window Size > 0
			for (int index=0; index<_windowSize; index++){
				P_conditional_E *= P_prevToken_E[index]*P_nextToken_E[index];
				P_conditional_T *= P_prevToken_T[index]*P_nextToken_T[index];
				P_conditional_N *= P_prevToken_N[index]*P_nextToken_N[index];
				if (_has_POS == true) {
					P_conditional_E *= P_prevPOS_E[index]*P_nextPOS_E[index];
					P_conditional_T *= P_prevPOS_T[index]*P_nextPOS_T[index];
					P_conditional_N *= P_prevPOS_N[index]*P_nextPOS_N[index];
				}
				if (_has_prevLabel == true){
					P_conditional_E *= P_prevLabel_E[index];
					P_conditional_T *= P_prevLabel_T[index];
					P_conditional_N *= P_prevLabel_N[index];
				}
			}
			
			P_EVENT =  P_E * P_conditional_E;
			P_TIMEX3 = P_T * P_conditional_T;
			P_NONE =   P_N * P_conditional_N;
			if (DEBUG == true){
				System.out.println("P_EVENT: " + P_EVENT);
				System.out.println("P_TIMEX3: " + P_TIMEX3);
				System.out.println("P_NONE: " + P_NONE);
			}
			
			// Update TimeML tags according to calculated probabilities
			P_MAX = Math.max(P_EVENT, Math.max(P_TIMEX3, P_NONE));
			if (P_EVENT == P_MAX){
				_predLabelList.set(i, "<EVENT>");
			} else if (P_TIMEX3 == P_MAX) {
				_predLabelList.set(i, "<TIMEX3>");
			}
			
			// Display testing results
			if (DEBUG == true){
				System.out.println("tokenList("+i+"): " + timeMLInstance.tokenList.get(i) 
						+ " \\" + timeMLInstance.POSList.get(i) + ", "
						+ timeMLInstance.tmlList.get(i) + " : " + _predLabelList.get(i));
				if (!(timeMLInstance.tmlList.get(i).equalsIgnoreCase(_predLabelList.get(i)))){
					System.out.println(" ! different !");
				}
				System.out.println();
			}
			
		}  /* End of loop on tokens*/
		
		for (int i=0; i<timeMLInstance.tmlList.size(); i++){	
			// Get tokens
			_tokenList.add(timeMLInstance.tokenList.get(i));		
			// Get true TimeML labels
			_trueLabelList.add(timeMLInstance.tmlList.get(i));
		}
		
		// Remove dummy tags
		while (_tokenList.contains("[BEGIN_Token]"))
			_tokenList.remove("[BEGIN_Token]");
		while (_tokenList.contains("[END_Token]"))
			_tokenList.remove("[END_Token]");
		while (_trueLabelList.contains("[BEGIN_TimeML]"))
			_trueLabelList.remove("[BEGIN_TimeML]");
		while (_trueLabelList.contains("[END_TimeML]"))
			_trueLabelList.remove("[END_TimeML]");
		while (_predLabelList.contains("[BEGIN_TimeML]"))
			_predLabelList.remove("[BEGIN_TimeML]");
		while (_predLabelList.contains("[END_TimeML]"))
			_predLabelList.remove("[END_TimeML]");
		
	}
	
	@Override
	public ArrayList<String> getTokenList(){
		return _tokenList;
	}
	
	@Override
	public ArrayList<String> getTrueLabelList(){
		return _trueLabelList;
	}

	@Override
	public ArrayList<String> getPredLabelList(){
		return _predLabelList;
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

