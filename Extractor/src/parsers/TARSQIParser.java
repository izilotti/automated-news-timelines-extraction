/** Parsing TARSQI XML files
 *  
 *  It is designed to parse TARSQI files. 
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package parsers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.ArrayList;

public class TARSQIParser {

	protected final static boolean DEBUG = false;

	public String _fileName;
	public ArrayList<String> nodeList = new ArrayList<String>(); // List of
																	// nodes
	public ArrayList<String> predLabelList = new ArrayList<String>(); // List of
																		// true
																		// TimeML
																		// tags
	private static boolean flag_TIMEX3;
	private static int count;

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            : the name of TARSQI XML file
	 * 
	 */
	public TARSQIParser(String fileName) {
		_fileName = fileName;
	}

	/**
	 * Parsing TARSQI files
	 * 
	 * @param _fileName
	 *            : the name of TARSQI output xml file
	 * @return
	 */
	public void parse() throws Exception {

		// Parse TARSQI XML files
		util.DocUtil.parser.parse(_fileName);

		// Print nodes of TARSQI XML file and generate the nodeList
		printNode(util.DocUtil.parser.getDocument(), "", 0, nodeList,
				predLabelList);

		// More process on two lists
		furtherProcess(nodeList, predLabelList);

		if (DEBUG == true) {
			System.out
					.println("[TARSQIParser] Print out node list with TimeML tags:");
			System.out.println("The size of nodeList = " + nodeList.size());
			System.out.println("The size of predLabelList = "
					+ predLabelList.size());
			// Print out node list content
			for (int i = 0; i < nodeList.size(); i++) {
				System.out.println(i + ": " + nodeList.get(i) + "	"
						+ predLabelList.get(i));
			}
		}
	}

	/**
	 * Traverse nodes of XML file and generate nodeList and tmlList
	 * 
	 * @param n
	 *            : the nodes of XML
	 * @param prefix
	 *            : the prefix of nodes
	 * @param depth
	 *            : the depth of XML tree
	 * @param al
	 *            : the node list
	 * @return <none>
	 */
	public void printNode(Node n, String prefix, int depth,
			ArrayList<String> nodeList, ArrayList<String> predLabelList) {

		if (n.getNodeName().equalsIgnoreCase("TIMEX3")) {
			flag_TIMEX3 = true;
			count = 1; // at least there is one token of TIMEX3
			NodeList child = n.getChildNodes();
			if (child.getLength() > 1) {
				for (int j = 0; child != null && j < child.getLength(); j++) {
					Node node = child.item(j);
					String str = node.getNodeValue();
					if (node.getNodeType() == Node.TEXT_NODE) {
						if (str.length() > 0)
							count++;
					}
				}
			}
		}

		try {

			// print out node names and attributes
			if (DEBUG == true) {
				System.out.print("\n" + util.DocUtil.pad(depth) + "["
						+ n.getNodeName());
				NamedNodeMap m = n.getAttributes();
				for (int j = 0; m != null && j < m.getLength(); j++) {
					Node item = m.item(j);
					System.out.print(" " + item.getNodeName() + "="
							+ item.getNodeValue());
				}
				System.out.print("] ");
			}

			// Get the child nodes of current node
			NodeList cn = n.getChildNodes();

			// Turn off flag when there is no TIMEX3 tag
			if (count == 0)
				flag_TIMEX3 = false;

			// Print out Contents of the node and recursively extend the tree
			for (int i = 0; cn != null && i < cn.getLength(); i++) {
				Node item = cn.item(i);

				if (item.getNodeType() == Node.TEXT_NODE) {
					String val = item.getNodeValue().trim();
					if (val.length() > 0) {

						if (DEBUG == true)
							// print node values (text)
							System.out.print(" \"" + item.getNodeValue().trim()
									+ "\"");

						// Eliminate the space before 'n't', e.g. in the case of
						// 'do n't'
						if (val.contains("n't")) {
							val = val.replaceAll(" n't", "n't");
						}

						// Generate node list
						nodeList.add(val);

						// Generate predicted TimeML tag list
						if (n.getNodeName().equalsIgnoreCase("EVENT"))
							predLabelList.add("<EVENT>");
						else if (flag_TIMEX3 == true) {
							predLabelList.add("<TIMEX3>");
							count--;
						} else
							predLabelList.add("<NONE>");
					}
				} else {
					printNode(item, prefix, depth + 1, nodeList, predLabelList);

				}
			}

		} catch (Exception e) {
			System.out.println(util.DocUtil.pad(depth) + "Exception e: ");
			e.printStackTrace();
		}
	}

	/**
	 * To deal with some special cases
	 * 
	 * @param nodeList
	 *            : the list of text node
	 * @param predLabelList
	 *            : the list of predicted labels
	 * @return <none>
	 */
	private void furtherProcess(ArrayList<String> nodeList,
			ArrayList<String> predLabelList) {

		// Delete the first node if it is just a name
		if (nodeList.get(0).endsWith(".xml")) {
			nodeList.remove(0);
			predLabelList.remove(0);
		}

		for (int i = 0; i < nodeList.size(); i++) {

			// "&amp;" never appear in TimeML file but it is found
			// in TARSQI output
			if (nodeList.get(i).equalsIgnoreCase("&amp;")) {
				nodeList.set(i, "\"");

				nodeList.remove(i + 1);
				predLabelList.remove(i + 1);

				nodeList.remove(i + 1);
				predLabelList.remove(i + 1);
			}

			// TARSQI omits the last dot of "non-U.S."
			if (nodeList.get(i).equalsIgnoreCase("non-U.S")) {
				nodeList.set(i, "non-U.S.");
				nodeList.remove(i + 1);
				predLabelList.remove(i + 1);
			}

			// If there is a ellipsis, divide it
			if (nodeList.get(i).equals("...")) {
				nodeList.set(i, ".");

				nodeList.add(i + 1, ".");
				predLabelList.add(i + 1, predLabelList.get(i));

				nodeList.add(i + 2, ".");
				predLabelList.add(i + 2, predLabelList.get(i));
			}

			// No clue why 'replaced-dns' is produced (it is not in
			// TimeML text at all)
			if (nodeList.get(i).equals("replaced-dns")) {
				nodeList.set(i, "Conn");
				predLabelList.set(i, "<NONE>");
				nodeList.set(i + 1, ".");
				predLabelList.set(i + 1, "<NONE>");
				nodeList.add(i + 2, "based");
				predLabelList.add(i + 2, "<NONE>");
			}
		}
	}

	public static void main(String[] args) throws Exception {

		String file = "/Users/oulin_yang/Documents/workspace/Automated_Timeline_Extraction/"
				+ "data/CoTraining/CRFpp/unlabelled/test/" + "wsj_0073.xml";
		TARSQIParser tar = new TARSQIParser(file);
		tar.parse();
	}
}