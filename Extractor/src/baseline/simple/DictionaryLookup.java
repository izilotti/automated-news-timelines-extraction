/** Dictionary Lookup Classifier 
 * 
 *  Dictionary Lookup method is a type of rote memorization.
 *  From training data it simply stores feature (EVENT, TIMEX 
 *  NONE etc.) counts using HashMaps in memory as 'dictionaries'.
 *  And when testing on new data, it looks up its dictionaries 
 *  and assign the most likely TimeML tags to new data.
 *  
 *  It works as a very baseline for other models.
 *   
 *  @author Oulin Yang (oulin.yang@gmail.com)
 *  @date   25 Oct, 2011
 */

package baseline.simple;

import java.util.ArrayList;

import java.util.HashMap;

import parsers.TimeMLParser;

import baseline.ModelSelector;


public class DictionaryLookup extends ModelSelector{
	
	// Use HashMaps to store text labeled with desired TimeML tags
	private HashMap<String,Integer> dictEVENT = new HashMap<String,Integer>();
	private HashMap<String,Integer> dictTIMEX3 = new HashMap<String,Integer>();
	private HashMap<String,Integer> dictNONE = new HashMap<String,Integer>();
	
	// List of tokens
	public ArrayList<String> _tokenList = new ArrayList<String>();
	// List of true TimeML tags
	public ArrayList<String> _trueLabelList = new ArrayList<String>();
	// List of predicted TimeML labels
	public ArrayList<String> _predLabelList = new ArrayList<String>();
	
	
	// Constructor
	public DictionaryLookup(String prefix, String trainingDir, String testingDir){
		// Call SuperClass constructor
		super(prefix, trainingDir, testingDir);
	}
	
	
	/** Data pre-processing
	 * 
	 * @param type: the file type (.tml or .xml) of data
	 * @return <none>
	 */
	@Override
	public void preprocessing(String type) throws Exception {
		
		/** This is intended to be empty**/
	}
	
	
	/** Training the 'Dictionary Look Up' model
	 * 
	 * @param <none>
	 * @return <none>
	 */
	@Override
	public void trainModel() throws Exception{
		
		System.out.println("\n[DictionaryLookup]: DictionaryLookup, Training...");
		
		// This is the only step of training in current model
		buildDictionary();
		
		System.out.println("\n[DictionaryLookup]: DictionaryLookup, Training Ends.\n\n");
		
	}
	
	
	/** Computing results of Dictionary Lookup model
	 * 
	 * @param timeMLInstance: the Instance of the (testing) file parsed by TimeMLParser
	 * @return <none>
	 */
	@Override
	public void computeResults(TimeMLParser timeMLInstance) throws Exception{
		
		String token = null;
		String newLabel = null;
		
		// Initialise
		for (int i=0; i<timeMLInstance.tmlList.size(); i++){		
			// Generate token list
			_tokenList.add(timeMLInstance.tokenList.get(i));
			// Generate true TimeML tag List
			_trueLabelList.add(timeMLInstance.tmlList.get(i));
			// Initialize predicted TimeML tag list with <NONE>
			if (!timeMLInstance.tokenList.get(i).isEmpty()) 
				_predLabelList.add("<NONE>");
			else
				_predLabelList.add("");
		}
		
		// Loop on all tokens
		for (int i=0; i<timeMLInstance.tokenList.size(); i++){
			
			// Get a token
			token = timeMLInstance.tokenList.get(i);
			
			// Skip boundary
			if (token.isEmpty()) continue;
			
			// Look up this token in dictionary and return the most
			// likely TimeML tag
			newLabel = lookUpDictionary(token);
			
			// Also save this label to predicted label list
			_predLabelList.set(i, newLabel);
			
			if (DEBUG == true)
				System.out.println("token("+i+"): " + timeMLInstance.tokenList.get(i) 
						+ " \\" + timeMLInstance.POSList.get(i) + ", "
						+ timeMLInstance.tmlList.get(i) + " : " + _predLabelList.get(i));
		
		}	/* End of loop on tokens */
		
	}
	
	
	/** Dictionary build-up process
	 * 
	 * @param <none>
	 * @return <none>
	 */
	private void buildDictionary() throws Exception {
		
		String token = null;
		String label = null;
		int FileCounter = 0;
		
		// Loop on all parsed training files
		for (TimeMLParser timeMLInstance : _trainingData) {
			// Increment File counter
			FileCounter++;
    		System.out.println("[DictionaryLookUp] Training:  " + _trainingFilesList.get(FileCounter-1));
    		
			// Loop on all tokens of each file
			for (int i=0; i<timeMLInstance.tokenList.size(); i++) {	
				
				token = timeMLInstance.tokenList.get(i);	// get token		
				label = timeMLInstance.tmlList.get(i);	// get true TimeML tag
				
				// Skip sentence boundary
				if (token.isEmpty() && label.isEmpty()) continue;
				
				// Increment feature counts of each HashMap against labels
				if (label.equalsIgnoreCase("<EVENT>")){
					// Check if this token has been written into dictionary
					if (dictEVENT.containsKey(token)) {
						// Return feature counts of this token, increment it and then put it back
						dictEVENT.put(token, dictEVENT.get(token) + 1);
					} else { 
		                // If this is the first time 'see' this token, set its count to 1 
						dictEVENT.put(token, 1);
		            }
				}
				// (Logically following above comments)
				else if (label.equalsIgnoreCase("<TIMEX3>")){
					if (dictTIMEX3.containsKey(token)) {
						dictTIMEX3.put(token, dictTIMEX3.get(token) + 1);
					} else {
						dictTIMEX3.put(token, 1);
		            }
				} 
				else {
					if (dictNONE.containsKey(token)) {
						dictNONE.put(token, dictNONE.get(token) + 1);
					} else {
						dictNONE.put(token, 1);
		            }
				}
			}	/* End of loop on tokens */
			
		}	/* End of loop on files */
		
		if (DEBUG == true){
			System.out.println("dictEVENT.size() = " + dictEVENT.size());
			System.out.println("dictTIMEX3.size() = " + dictTIMEX3.size());
			System.out.println("dictNONE.size() = " + dictNONE.size());
		}
	}
	
	
	/** Look up new token in dictionary
	 * 
	 * @param token: the token to be predicted
	 * @return the predicted TimeML tag
	 */
	private String lookUpDictionary(String token) throws Exception {
		
		String predLabel = null;
		Integer cur_countE = 0;
		Integer cur_countT = 0;
		Integer cur_countN = 0;
		Integer cur_max = 0;
		
		// Search this token in HashMaps
		cur_countE = dictEVENT.get(token);
		// If it doesn't find the token set the count to 0
		if (cur_countE == null) cur_countE = 0;
		
		cur_countT = dictTIMEX3.get(token);
		if (cur_countT == null) cur_countT = 0;
		
		cur_countN = dictNONE.get(token);
		if (cur_countN == null) cur_countN = 0;
		
		if (DEBUG == true){
			System.out.println(token + " (cur_countE): " + cur_countE);
			System.out.println(token + " (cur_countT): " + cur_countT);
			System.out.println(token + " (cur_countN): " + cur_countN);
		}
		
		// Get the most frequent feature counts
		cur_max = Math.max(cur_countE, Math.max(cur_countT, cur_countN));
		
		// If cannot find the token in dictionary, then return label <NONE>
		if (cur_max == 0) predLabel = "<NONE>";
		// Otherwise return the label of highest frequency in this order
		else if (cur_countE == cur_max) predLabel = "<EVENT>";
		else if (cur_countT == cur_max) predLabel = "<TIMEX3>";
		else predLabel = "<NONE>";

		return predLabel;
	}
	
	
	@Override
	public ArrayList<String> getTokenList(){
		return _tokenList;
	}
	
	
	@Override
	public ArrayList<String> getPredLabelList(){
		return _predLabelList;
	}
	
	
	@Override
	public ArrayList<String> getTrueLabelList(){
		return _trueLabelList;
	}

	@Override
	public int getWindowSize() {
		return 0;
	}

	@Override
	public void clearLists() {
		_tokenList.clear();
		_trueLabelList.clear();
		_predLabelList.clear();
	}	
}