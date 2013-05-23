/** 
 * TimeML files Parser
 *   
 *   It is designed to parse TimeBank Corpus data (.tml formats).
 *   And to ensure this parser can produce results which are in
 *   consistent with TARSQI outputs, some extra limitations are
 *   added, like stop-word list, abbreviations processing etc.
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */
package parsers;

import nlp.opennlp.POSTagger.POSTagging;
import nlp.opennlp.SentenceExtractor;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.*;

import java.util.*;


public class TimeMLParser implements ErrorHandler {
	
	protected static boolean DEBUG = false;
	
	public String _fileDir;
	
	public ArrayList<String> tokenList = new ArrayList<String>();	// ArrayList of tokens
	public ArrayList<String> POSList = new ArrayList<String>();		// ArrayList of POS
	public ArrayList<String> tmlList = new ArrayList<String>();		// ArrayList of true TimeML labels
	public ArrayList<String> nodeList = new ArrayList<String>();	// ArrayList of text nodes	
	public String[] sentences;
	
	private ArrayList<String> target = new ArrayList<String>(); 	// Chars which require further process
	private ArrayList<String> specialCases = new ArrayList<String>();// Where do not parse target chars
	private ArrayList<String> tagCollect = new ArrayList<String>();
	
	private ArrayList<Integer> textNodeCount = new ArrayList<Integer>();
	private ArrayList<Integer> ncList = new ArrayList<Integer>();	// ArrayList of node length counts
	
	
	/** Constructor
	 * 
	 * @param fileName: the name (with directory) of TimeML file
	 * 
	 */
	public TimeMLParser (String fileDir) throws IOException {	
		
		_fileDir = fileDir;
		
	/** Set up targets for further process **/
		target.add("/");
		target.add("(");
		target.add(")");
		target.add("'");
		target.add(":");
		target.add(",");
		target.add("`");
		target.add(".");
		target.add(";");
		target.add("[");
		target.add("]");
		target.add("-");
		target.add("\"");
		target.add("{");
		target.add("}");
		target.add("$");
		target.add("%");
		target.add("#");
		target.add("@");
	/***************************************/
		
		
	/** Set up a repository for special cases **/

		// Phrase 'coup d'etat'
		specialCases.add("d'etat");		
		
	/*****************************************/
	}
	
	
	/** Calling specific functions to parse .tml files by using NLP techniques
	 * 
	 * @param <none>
	 * @return <none>
	 * 
	 */
	public void parse() {
		if (DEBUG == true) {
			// Turn Validation on
			try {
				util.DocUtil.parser.setFeature(
						"http://xml.org/sax/features/validation", true);
				util.DocUtil.parser.setFeature(
						"http://apache.org/xml/features/validation/schema",
						true);
				util.DocUtil.parser.setFeature(
						"http://apache.org/xml/features/validation/"
								+ "schema-full-checking", true);
			} catch (SAXNotRecognizedException e) {
				System.err.println(e);
			} catch (SAXNotSupportedException e) {
				System.err.println(e);
			}

			// Register Error Handler
			util.DocUtil.parser.setErrorHandler(this);
		}

		try {
			// Parse the TimeML file using DOM parser
			util.DocUtil.parser.parse(_fileDir);
			Document document = util.DocUtil.parser.getDocument();

			// Print nodes in XML format and generate nodeList
			printNode(document, 0, nodeList);

			// Call POSTagger to process nodeList to get tokenList and POSList
			loadPOSTagger(tokenList, POSList, tmlList, nodeList, ncList);

			// Traverse the file and map TIMEML tags to correspondent positions
			// in tmlList
			mapTags(document, tmlList, ncList);
			
			// Break sentences to documents
			breakSentences();
			
			// Extend TimeML parsing results to be the same as TARSQI Toolkit
			// does
			furtherProcess();
			
			if (DEBUG == true) {
				for (int i=0; i<tokenList.size(); i++)
					System.out.println("tokenList " + i + ": " + tokenList.get(i));
				for (int i=0; i<POSList.size(); i++)
					System.out.println("POSList " + i + ": " + POSList.get(i));				
				for (int i=0; i<tmlList.size(); i++)
					System.out.println("tmlList " + i + ": " + tmlList.get(i));				
				for (int i=0; i<nodeList.size(); i++)
					System.out.println("nodeList " + i + ": " + nodeList.get(i));				
				for (int i=0; i<ncList.size(); i++)
					System.out.println("ncList " + i + ": " + ncList.get(i));				
				for (int i=0; i<tagCollect.size(); i++)
					System.out.println("tagCollect " + i + ": " + tagCollect.get(i));				
				for (int i=0; i<textNodeCount.size(); i++)
					System.out.println("textNodeCount " + i + ": " + textNodeCount.get(i));
			}

			// Display parsing results
			if (DEBUG == true) {
				System.out
						.println("\n\n========= Sample outputs in TimeMLParsing Main ==========\n\n");
				for (int j = 0; j < tokenList.size(); j++) {
					System.out.println("tokenList(" + j + "): "
							+ tokenList.get(j) + " \\" + POSList.get(j) + ", "
							+ tmlList.get(j));
				}
				System.out
						.println("\n================= TimeMLParsing End ======================\n\n");
			}

		} catch (SAXException e) {
			System.err.println (e);
		} catch (IOException e) {
			System.err.println (e);
		} catch (Exception e) {
			System.err.println (e);
		}
	}

	
	/** Print out TimeML file in structured style, and generate the nodeList
	 * 
	 * @param n: 	 nodes of TimeML files (starting from root node)
	 * @param depth: the depth of XML file tree
	 * @param al: 	 the nodeList
	 * @return <none>
	 */
	public void printNode(Node n, int depth, ArrayList<String> nodeList) {
		
		try {

			// Print out node names and attributes
			if (DEBUG == true) {
				System.out.print("\n" + util.DocUtil.pad(depth) + "["
						+ n.getNodeName());
				NamedNodeMap m = n.getAttributes();
				for (int i = 0; m != null && i < m.getLength(); i++) {
					Node item = m.item(i);
					System.out.print(" " + item.getNodeName() + "="
							+ item.getNodeValue());
				}
				System.out.print("] ");
			}

			// Get the child nodes of current node
			NodeList cn = n.getChildNodes();

			// Display the contents of nodes and recursively extend the tree
			for (int i = 0; cn != null && i < cn.getLength(); i++) {
				Node item = cn.item(i);
				if (item.getNodeType() == Node.TEXT_NODE) {
					String val = item.getNodeValue().trim();
					if (val.length() > 0) {
						// Print out node values (text)
						if (DEBUG == true)
							System.out.print(" \"" + val + "\"");
						// Eliminate the space before 'n't', e.g. in the case of
						// 'do n't'
						if (val.contains("n't")) {
							val = val.replaceAll(" n't", "n't");
						}

						// Remove multiple white spaces using regular expression
						val = val.replaceAll("\\s+", " ");

						// Generate the node list
						nodeList.add(val);

						int countNode = 0;
						if (n.getNodeName().equalsIgnoreCase("EVENT")) {
							tagCollect.add("<EVENT>");
							for (int iv = 0; iv < val.length(); iv++) {
								char c = val.charAt(iv);
								if (c == ' ' || c == '$')
									countNode++;
							}
							textNodeCount.add(1 + countNode);
						} else if (n.getNodeName().equalsIgnoreCase("TIMEX3")) {
							tagCollect.add("<TIMEX3>");
							for (int iv = 0; iv < val.length(); iv++) {
								char c = val.charAt(iv);
								if (c == ' ' || c == '.')
									countNode++;
							}
							textNodeCount.add(1 + countNode);
						} else {
							tagCollect.add("<NONE>");
							textNodeCount.add(1);
						}
					}
				} else
					printNode(item, depth + 2, nodeList);
			}
				
		} catch (Exception e) {
			System.err.println("[TimeMLParser] Error: a problem occured in printNode function.");
			System.out.println(util.DocUtil.pad(depth) + "Exception e: ");
		}
	}

	
	/** POSTagging each node and generate token, POSTagger and TimeML tag lists
	 * 
	 * @param al1: the token list
	 * @param al2: the POS tags list
	 * @param al3: the TimeML label list
	 * @param al4: the node list
	 * @param al5: the node counting list
	 * @return <none>
	 */
	private void loadPOSTagger(ArrayList<String> tokenList, ArrayList<String> POSList, ArrayList<String> tmlList,
			ArrayList<String> nodeList, ArrayList<Integer> ncList) throws IOException{

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
							// Set up token list
							tokenList.add(tags._tokens[si][wi]);	
							// Set up POS tagger list
							POSList.add(tags._taggings[si][ti][wi]);	
							// Initialize TimeML list with tag <NONE>
							tmlList.add("<NONE>");			
						}
					}
					sum += temp; // Increment length of each node
				}
				ncList.add(i, sum); // Node counting list records the length of
									// each node
			}

		} catch (Exception e) {
			System.err
					.println("[TimeMLParser] Error: a problem occured in loadPOSTagger function.");
		}
	}
	

	/**
	 * Traverse TimeML file and mapping TimeML tags to correspondent positions
	 * in tmlList
	 * 
	 * @param al1
	 *            : the TimeML label list
	 * @param al2
	 *            : the node counting list
	 * @return <none>
	 */
	private void mapTags(Node node, ArrayList<String> tmlList,
			ArrayList<Integer> ncList) throws Exception {

		if (!(ncList.size() == tagCollect.size() && tagCollect.size() 
				== textNodeCount.size())) {
			System.err.println("[TimeMLParser]: The lengths of ncList, textNodeCount and tagCollect are not equal.");
			System.err.println("ncList.size() = " + ncList.size());
			System.err.println("tagCollect.size() = " + tagCollect.size());
			System.err.println("textNodeCount.size() = " + textNodeCount.size());
			System.exit(0);
		}

		try {
			for (int i = 0; i < ncList.size(); i++) {

				// If the tag is <NONE>, skip to next tag
				if (tagCollect.get(i).equalsIgnoreCase("<NONE>")) {
					continue;
				} else {
					// Check textNodeCount
					if (textNodeCount.get(i) == 1) {
						tmlList.set(ncList.get(i) - 1, tagCollect.get(i));
					}
					// If the <EVENT> or <TIMEX3> tag shall be labeled on
					// more than one tokens (e.g. the third quarter)
					else {
						for (int d = 0; d < textNodeCount.get(i); d++) {
							tmlList.set(ncList.get(i) - 1 - d,
									tagCollect.get(i));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/** Break documents into sentences
	 * 
	 * @param <none>
	 * @return <none>
	 */
	private void breakSentences() throws IOException{
		
		// Reorganise text nodes to documents
		String doc = "";
		for (int i = 0; i < nodeList.size(); i++) {
			String node = nodeList.get(i);
			doc = doc.concat(node).concat(" ");
		}
		if (DEBUG) 
			System.out.println("\n\n ^^ " + doc);

		// Break into sentences
		SentenceExtractor extractor = new SentenceExtractor();
		sentences = extractor.process(doc);
		if (DEBUG)
			for (int k = 0; k < sentences.length; k++) {
				System.out.println("      " + sentences[k]);
		}
		
		// Add a space (to those lists) at the end of each sentence
		int index = 0;
		for (int i = 0; i < sentences.length; i++) {
			POSTagging tags = nlp.NLPUtil.tagger.process(sentences[i], 1);
			for (int si = 0; si < tags._taggings.length; si++) {
				index += tags._tokens[si].length;
				tokenList.add(index, "");
				POSList.add(index, "");
				tmlList.add(index, "");
				index++;
			}
		}
		
		// Test
		if (DEBUG) {
			for (int i = 0; i < tokenList.size(); i++) {
				System.out.println("&& " + tokenList.get(i) + "   " + POSList.get(i)
						+ "   " + tmlList.get(i));		
			}
			System.out.println("\n\n\n\n");
		}
	}
	
	
	/** Further process and extend three main lists to
	 *  the a unified format (TARSQI format)
	 * 
	 * @param tokenList: the list of tokens
	 * @param POSList: the list of POS tags
	 * @param tmlList: the list of TimeML tags
	 * @return <none>
	 */
	private void furtherProcess() throws Exception {	

		ArrayList<String> processedTokenList = new ArrayList<String>();
		ArrayList<String> processedPOSList = new ArrayList<String>();	
		ArrayList<String> processedtmlList = new ArrayList<String>();	
		StringBuffer sb = new StringBuffer();
		
		String prevToken = "";
		String nextToken = "";
		
		try {
			// Loop on the token list
			for (int i=0; i<tokenList.size(); i++){
				// Get token, POS tag and TimeML tag
				String token = tokenList.get(i);
				String POSTag = POSList.get(i);
				String TMLTag = tmlList.get(i);

				// Sentence boundary
				if (token.length() == 0) {
					processedTokenList.add(token);
					processedPOSList.add(POSTag);
					processedtmlList.add(TMLTag);
					continue;
				}
				
				if (i > 0)
					prevToken = tokenList.get(i - 1);
				if (i < tokenList.size() - 1)
					nextToken = tokenList.get(i + 1);

				// Case: as what TARSQI does, parsing "wasn't" as "wasn", "'", and "t"
				if (token.equalsIgnoreCase("n't")) {

					processedTokenList.set((processedTokenList.size() - 1),
							tokenList.get(i - 1).concat("n"));

					processedTokenList.add("'");
					processedPOSList.add(POSTag);
					processedtmlList.add(TMLTag);

					processedTokenList.add("t");
					processedPOSList.add(POSTag);
					processedtmlList.add(TMLTag);

					continue;
				}

				// Case: do not parse "p-", "a-", "of-"
				// TODO too specific to be generic
				if (token.equals("-")
						&& ((prevToken.equalsIgnoreCase("a")
								|| (prevToken.equalsIgnoreCase("p"))
								|| (prevToken.equalsIgnoreCase("of"))
								|| (prevToken.equalsIgnoreCase("Do")) || (prevToken
								.equalsIgnoreCase("tra"))))) {

					processedTokenList.set((processedTokenList.size() - 1),
							tokenList.get(i - 1).concat("-"));

					continue;
				}

				// Case: if an abbreviation is at the end of string, it tends to
				// parse the second dot as a period. This is to eliminate this
				// error.
				if (token.equals(".")) {
					int len = prevToken.length();
					if (len >= 3) {
						if (Character.isLetter(prevToken.charAt(len - 1))
								&& prevToken.charAt(len - 2) == '.') {
							processedTokenList.set(
									(processedTokenList.size() - 1), tokenList
											.get(i - 1).concat("."));
							continue;
						}
					}
				}
				
				// Go through each char in that token
				for (int j = 0; j < token.length(); j++) {
					// If this token is just one char, then
					// directly put it into the list and jump
					// to the next token
					if (token.length() == 1) {
						processedTokenList.add(token);
						processedPOSList.add(POSTag);
						processedtmlList.add(TMLTag);
						break;
					}

					// Get a char
					char ch = token.charAt(j);
					
					// If this char is included in target but is not a
					// special case, then store its previous chars (if there
					// are)
					// as a single token, and then store this char as another
					// token.
					if (target.contains(Character.toString(ch))
							&& !constraints(prevToken, token, nextToken, ch, j)) {
						if (!(sb.length() == 0)) {
							// Save previous chars as a token
							processedTokenList.add(sb.toString());
							// And repeat tags
							processedPOSList.add(POSTag);
							processedtmlList.add(TMLTag);
						}
						// Clear the string buffer
						sb.setLength(0);
						// Save this char as a token
						processedTokenList.add(Character.toString(ch));
						processedPOSList.add(POSTag);
						processedtmlList.add(TMLTag);
					}
					// Otherwise append this char to string buffer,
					// and continue the loop
					else {
						sb.append(ch);
					}
				} /* end of loop on tokens */

				// Store the rest of token or entire token
				if (!(sb.length() == 0)) {
					processedTokenList.add(sb.toString());
					processedPOSList.add(POSTag);
					processedtmlList.add(TMLTag);
				}
				sb.setLength(0);

			} /* end of loop on token list */

			// Refresh token, POS Tag, and TimeML tag lists with processed ones
			tokenList.clear();
			POSList.clear();
			tmlList.clear();
			for (int k = 0; k < processedTokenList.size(); k++) {
				tokenList.add(processedTokenList.get(k));
				POSList.add(processedPOSList.get(k));
				tmlList.add(processedtmlList.get(k));
			}

		} catch (Exception e) {
			System.err
					.println("[TimeMLParser] Error: a problem occured in furtherProcess function.");
			e.printStackTrace();
		}

	}
	
	/**
	 * There is no need to further process those special cases
	 * 
	 * @param token
	 *            : the single token
	 * @param ch
	 *            : the char in that token
	 * @param index
	 *            : where the objective char sits in the token
	 * @return <none>
	 */
	private boolean constraints(String prevToken, String token,
			String nextToken, char ch, int index) {

		boolean flag = false;

		try {
			// If this token is a special case, then skip it and
			// do not parse this token
			if (specialCases.contains(token))
				flag = true;
			else {
				/** Char ch is in the middle of the token **/
				if (index > 0 && index < token.length() - 1) {

					// Do not parse "a-b"
					if (ch == '-')
						flag = true;
					// Do not parse "--" in the middle
					else if (ch == '-' && token.charAt(index + 1) == '-')
						flag = true;

					// Do not parse floating point numbers
					if (ch == '.' && Character.isDigit(token.charAt(index - 1))
							&& Character.isDigit(token.charAt(index + 1)))
						flag = true;
					// Do not parse phrases like "U.N.-ordered"
					else if (ch == '.' && token.charAt(index + 1) == '-')
						flag = true;
					// Do not parse "c.1998"
					else if (ch == '.'
							&& Character.isLetter(token.charAt(index - 1))
							&& Character.isDigit(token.charAt(index + 1)))
						flag = true;
					// Do not parse "A.B"
					else if (ch == '.'
							&& Character.isUpperCase(token.charAt(index - 1))
							&& Character.isUpperCase(token.charAt(index + 1)))
						flag = true;

					// Do not parse A'B
					if (ch == '\''
							&& Character.isUpperCase(token.charAt(index - 1))
							&& Character.isUpperCase(token.charAt(index + 1)))
						flag = true;
				}
				/** Char ch at the beginning of the string **/
				else if (index == 0) {
					// Don't parse "-old"
					if (ch == '-')
						flag = true;
					// Do not parse "--" of the head
					else if (ch == '-' && token.charAt(index + 1) == '-')
						flag = true;

				}
				/** Char ch at the end of the string **/
				else if (index == token.length() - 1) {
					// Do not parse "a-"
					if (ch == '-'
							&& Character.isLetter(token.charAt(index - 1)))
						flag = true;
					// Do not parse "--" at the tail
					else if (ch == '-' && token.charAt(index - 1) == '-')
						flag = true;
				}

				// Do not parse abbreviations like 'U.S.'
				if (ch == '.' && ((index + 2) <= (token.length() - 1))
						&& token.charAt(index + 2) == '.')
					flag = true;
				else if (ch == '.' && index == token.length() - 1
						&& token.length() >= 4
						&& token.charAt(index - 2) == '.')
					flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return flag;
	}
	
	// Warning Event Handler
	public void warning(SAXParseException e) throws SAXException {
		System.err.println("Warning: " + e);
	}

	// Error Event Handler
	public void error(SAXParseException e) throws SAXException {
		System.err.println("Error: " + e);
	}

	// Fatal Error Event Handler
	public void fatalError(SAXParseException e) throws SAXException {
		System.err.println("Fatal Error: " + e);
	}

}