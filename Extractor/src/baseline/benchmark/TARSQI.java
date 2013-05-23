/** 
 *  The TARSQI Toolkit (TTK) is a Time mark-up tool developed
 *  by timeml.org. Published on Nov, 2007.
 *  (http://www.timeml.org/site/tarsqi/toolkit/index.html)
 *  To install TARSQI toolkit, please refer to its manual.
 *  
 *  It integrates extraction of events and time expressions 
 *  with creation of temporal links, using a set of mostly 
 *  independent modules, while ensuring consistency with a 
 *  constraint propagation component
 *  
 *  Here it functions as a benchmark to other models - by
 *  comparing with true labels from TimeML file, the TARSQI
 *  toolkit shall be able to produce a more reliable tagging 
 *  accuracy than most of other models.
 *   
 *  @author Oulin Yang (oulin.yang@gmail.com)
 *  @date   25 Oct, 2011
 */

package baseline.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import baseline.ModelSelector;

import parsers.TARSQIParser;
import parsers.TimeMLParser;
import util.FileFinder;

public class TARSQI extends ModelSelector{
	
	/** 
	 * Important: Set up paths below correctly before running
	 * (Make sure those directories are exist)
	 */	
	// Get current working directory
	String curWorkingDir = System.getProperty("user.dir");
	// Paths where input and output files to TARSQI are placed	
	public String inDir = curWorkingDir.concat("/data/TARSQI_IN/10");
	public String outDir = curWorkingDir.concat("/data/TARSQI_OUT_sentences/10");
	
	// The path where Python shell is installed
	public String pythonDir = "/usr/bin/python";
	
	// ArrayLists for tokens and true TimeML labels parsed by TimeML Parser
	public ArrayList<String> _tmlTokenList = new ArrayList<String>();
	public ArrayList<String> _trueLabelList = new ArrayList<String>();
	
	// ArrayList for tokens and predicted TimeML tags produced by TARSQI Toolkit
	public ArrayList<String> _tarsqiTokenList = new ArrayList<String>();
	public ArrayList<String> _predLabelList = new ArrayList<String>();
	
	
	// Constructor
	public TARSQI(String prefix, String trainingDir, String testingDir) {
		
		super(prefix, trainingDir, testingDir);
		
		if (!trainingDir.isEmpty()){
			System.err.println("[TARSQI]: Error. The training directory shall be left blank.");
			System.exit(0);
		}
	}	
	
	
	/** 
	 * Parse testing data and take off TimeML tags from those files. 
	 * When new XML files (without TimeML tags) are produced, call 
	 * TARSQI toolkit to process the XML files.
	 * 
	 * @param type: file type of testing data
	 * @return <none>
	 */
	@Override
	public void preprocessing(String type) throws Exception {
		
		// Get testing data of certain file types (.tml here)
		parseTestingData(type);
		
		// Loop on testing files
//		for (int i=0; i<_testingData.size(); i++){
//			System.out.println("[TARSQI]: Now working on file: " + _testingFilesList.get(i));
//			
//			// Convert TimeML file to plain XML file
//			String fileName = labelsOff(_testingFilesList.get(i));
//			System.out.println("	  Tags off Done, a TARSQI input files generated.");
//			
//			// If the this file has not been processed by TARSQI, then call
//			// TARSQI toolkit to process this XML file by giving the file name
//			File f = new File(outDir.concat("/" + fileName));
//			if (!f.exists()) callTARSQI(fileName);
//			System.out.println("	  TARSQI Toolkit is running ... please wait ...");
//			// Check if TARSQI has finished processing, otherwise sleep
//			// current thread for 500ms to avoid busy-waiting
//			while (!f.exists()) {
//				Thread.sleep(500);
//			}
//			System.out.println("	  TARSQI process done!\n" );
//		}
	}
	
	
	/** The training function is intentionally left blank here**/
	@Override
	public void trainModel() throws Exception {
		/** keep this function empty**/
	}
	
	
	/** 
	 * Taking off the TimeML and other labels from testing data,
	 * and generate 'plain' XML files, i.e. TimeML files but 
	 * without tags.
	 * 
	 * The function reads in each file, process, and write/store
	 * output (XML) files in a specified directory (inDir).
	 * 
	 * @param fileDir: the directory of a single testing file
	 * @return fileName: the name of new plain XML file
	 */
	private String labelsOff (String fileDir){
		
		// ArrayList of text nodes
		ArrayList<String> nodeList = new ArrayList<String>();
			
		try {
			// Parse by DOMParser
			util.DocUtil.parser.parse(fileDir);
			
			// Print out nodes of XML file and generate a nodeList
			TimeMLParser tmlInstance = new TimeMLParser(fileDir);
			tmlInstance.printNode(util.DocUtil.parser.getDocument(), 0, nodeList);
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		// Get the file name from its path value
		String fileName = fileDir.substring(fileDir.lastIndexOf("/")+1,
							fileDir.lastIndexOf(".")).concat(".xml");
		
		// Write text nodes to a XML file
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			/** Use 'simple XML' format defined by TARSQI toolkit **/
			// Root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("DOC");
			doc.appendChild(rootElement);
	 
			// DOCID elements
			Element docID = doc.createElement("DOCID");
			rootElement.appendChild(docID);
			docID.appendChild(doc.createTextNode(fileName));
			
			// TEXT elements
			Element text = doc.createElement("TEXT");
			rootElement.appendChild(text);
	 
			// Append text content
			for (int i=0; i<nodeList.size(); i++){
				text.appendChild(doc.createTextNode(nodeList.get(i) + " "));
			}
	 
			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File(inDir + "/" + fileName));
			transformer.transform(source, result);
		 
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}catch(TransformerException tfe){
			tfe.printStackTrace();
		}
		return fileName;
	}
	
	
	/** 
	 * Call the TARSQI tool to read in data from 'inDir'
	 * and to save output files to 'outDir'.
	 * 
	 * @param <none>
	 * @return <none>
	 */
	private void callTARSQI(String fileName) {
		
		try {
			// Set the directory where tarsqi.py exists
			String tarsqiDotPyDir = curWorkingDir.concat("/ttk-1.0/code/tarsqi.py");
			String input_type = "simple-xml";
			String infile = inDir.concat("/" + fileName);
			String outfile = outDir.concat("/" + fileName);
			
			/** 
			 * Command format: 
			 * python tarsqi.py <input_type> [flags] <infile> <outfile>
			 * 
			 **/
			String command = pythonDir.concat(" ").
							 concat(tarsqiDotPyDir).concat(" ").
							 concat(input_type).concat(" ").
							 concat(infile).concat(" ").
							 concat(outfile);
			
			if (DEBUG == true)
				System.out.println("The exec command is: " + command);
			
			// Execute the command in run time
			Runtime.getRuntime().exec(command);
			
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	/** 
	 * Compare TARSQI performance with ground truth
	 * 
	 * @param timeMLInstance: TimeMLParser instance
	 * @return <none>
	 */
	@Override
	public void computeResults(TimeMLParser timeMLInstance) throws Exception {
		
		// Get the ground truth (original TimeML tags) from TimeML data
		getGroundTruth(timeMLInstance);
		
		// Parse corresponding TARSQI output file
		String fileName = timeMLInstance._fileDir.substring(
				timeMLInstance._fileDir.lastIndexOf("/")+1,
				timeMLInstance._fileDir.lastIndexOf("."));
//		String fileDir = outDir.concat("/" + fileName).concat(".xml");
//
//		TARSQIParser tasqiInstance = new TARSQIParser(fileDir);
//		tasqiInstance.parse();
//		getTARSQIResults(tasqiInstance);
//		
//		// Compare the TARSQI mark-up outputs with ground truth
//		// If two token lists are of different length, then manually
//		// meddle could be required
//		compareAndRevise(_tmlTokenList, _trueLabelList, _tarsqiTokenList, _predLabelList);
			
		
//		FileWriter outFile1 = new FileWriter(curWorkingDir.concat("/data/tml/" + fileName + ".txt"));
//		PrintWriter out1 = new PrintWriter(outFile1);
//		for (int i=0; i<timeMLInstance.tmlList.size(); i++){
//			// Write text to file
//			out1.println(timeMLInstance.tokenList.get(i) + "  " + 
//					timeMLInstance.tmlList.get(i));
//			
//		}
//		out1.close();
		
//		FileWriter outFile2 = new FileWriter(curWorkingDir.concat("/data/xml/" + fileName + ".txt"));
//		PrintWriter out2 = new PrintWriter(outFile2);
		String tarsqiResults = outDir;
		FileFinder ffinder = new FileFinder(".xml", tarsqiResults);
		for (int i=0; i<ffinder.fileList.size(); i++){
			String xmlFileName = ffinder.fileList.get(i);
			if (xmlFileName.contains(fileName)){
				System.out.println(ffinder.fileList.get(i));
				TARSQIParser tasqiInstance = new TARSQIParser(ffinder.fileList.get(i));
				tasqiInstance.parse();
				for (int j=0; j<tasqiInstance.nodeList.size(); j++){		
//					out2.println(tasqiInstance.nodeList.get(j) + "  " + 
//							tasqiInstance.predLabelList.get(j));
					// Get tokens
					_tarsqiTokenList.add(tasqiInstance.nodeList.get(j));		
					// Get predicted labels
					_predLabelList.add(tasqiInstance.predLabelList.get(j));
				}
//				out2.println("");
				_tarsqiTokenList.add("");
				_predLabelList.add("");
			}	
		}
//		out2.close();
		System.out.println("\n\n\n");
	}
	
	
	/** 
	 * Collecting ground truth set (the original TimeML tags) from 
	 * testing data.
	 * 
	 * @param timeMLInstance: TimeMLParser instance
	 * @return <none>
	 */
	private void getGroundTruth(TimeMLParser timeMLInstance) throws Exception {
		
		for (int i=0; i<timeMLInstance.tmlList.size(); i++){		
			// Get tokens
			_tmlTokenList.add(timeMLInstance.tokenList.get(i));
			// Get labels
			_trueLabelList.add(timeMLInstance.tmlList.get(i));
		}
	}
	
	
	/** 
	 * Get TARSQI outputs
	 * 
	 * @param tarsqiInstance: TARSQIParser instance
	 * @return <none>
	 */
	private void getTARSQIResults(TARSQIParser tarsqiInstance) throws Exception {
		
		for (int i=0; i<tarsqiInstance.nodeList.size(); i++){		
			// Get tokens
			_tarsqiTokenList.add(tarsqiInstance.nodeList.get(i));		
			// Get predicted labels
			_predLabelList.add(tarsqiInstance.predLabelList.get(i));
		}
	}
	
	/** 
	 * Verify TARSQI results
	 * 
	 * @param _tmlTokenList: the list of TimeML tokens
	 * 		_trueLabelList: the list of true labels
	 * 		_tarsqiTokenList: the list of TARSQI output tokens
	 * 		_predLabelList: the list of TARSQI predicted labels
	 * @return <none>
	 */
	private void compareAndRevise (ArrayList<String> _tmlTokenList, 
			ArrayList<String> _trueLabelList, ArrayList<String> _tarsqiTokenList, 
			ArrayList<String> _predLabelList) throws Exception {
		
		// Check equalities between token lists and label lists
		if (!(_tmlTokenList.size() == _trueLabelList.size())){
			System.err.println("[TARSQI]: The lengths of TimeML token list and label" +
			"are not equal.");
			System.exit(0);
		}
			
		if (!(_tarsqiTokenList.size() == _predLabelList.size())){
			System.err.println("[TARSQI]: The lengths of TARSQI token list and label" +
			"are not equal.");
			System.exit(0);
		}	
		
		// Check if the length of token list generated by TimeML parser is equal to
		// the length of token list generated by TARSQI parser
		// TODO if not, then require manually modify TARSQI token list on command line
		if (!(_tmlTokenList.size() == _tarsqiTokenList.size())){
			System.err.println("\n[TARSQI]: The lengths of TARSQI token list and " +
					"TimeML token list are not equal.");
			
			// Compare each token until find the difference
			// (first prevent run out of ArrayList size)
			int size = Math.min(_tmlTokenList.size(), _tarsqiTokenList.size());
			String p_str1 = "";
			String p_str2 = "";
			String n_str1 = "";
			String n_str2 = "";
			String nn_str1 = "";
			String nn_str2 = "";
			for (int i=2; i<size+2; i++) {
				try{
					p_str1 = _tmlTokenList.get(i-1);
					p_str2 = _tarsqiTokenList.get(i-1);
					String str1 = _tmlTokenList.get(i);
					String str2 = _tarsqiTokenList.get(i);
					try{
						n_str1 = _tmlTokenList.get(i+1);
						n_str2 = _tarsqiTokenList.get(i+1);
						nn_str1 = _tmlTokenList.get(i+2);
						nn_str2 = _tarsqiTokenList.get(i+2);
					}
					catch (Exception e){
						e.printStackTrace();
						System.err.println("Catch Exception in compareAndRevise");
					}				
					
					if (!(str1.equalsIgnoreCase(str2))){
						
						// Display where the difference starts
						System.err.println("The comparison differen starts between" +
								" TimeML token (" + i + "): " + str1 + " " +
										"and TARSQI token (" + i + "): " + str2);
						System.err.println("TimeML: " + p_str1 + "   " + "TARSQI: " + p_str2 + "");
						System.err.println("TimeML: " + str1 + "   " + "TARSQI: " + str2 + "");
						System.err.println("TimeML: " + n_str1 + "   " + "TARSQI: " + n_str2 + "");
						System.err.println("TimeML: " + nn_str1 + "   " +"TARSQI: " + nn_str2 + "");
						System.exit(0);
					}				
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}	
		// If pass comparisons then print success message
		System.out.println("[TARSQI]: The comparison test PASS.\n");
	}
	

	@Override
	public ArrayList<String> getTokenList() {
		return _tarsqiTokenList;
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
		return 0;
	}

	
	@Override
	public void clearLists() {
		_tmlTokenList.clear();
		_trueLabelList.clear();
		_tarsqiTokenList.clear();
		_predLabelList.clear();
	}
	
	
	
//	public static void main(){
//		
//	}
}
