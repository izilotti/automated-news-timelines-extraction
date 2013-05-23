/** 
 *  Convert text to XML
 *   
 *  @author Oulin Yang (oulin.yang@gmail.com)
 *  @date   25 Oct, 2011
 */

package util;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

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

public class TextToXML {

	public static String _curWorkingDir = System.getProperty("user.dir");
	public static String _crfFolder = _curWorkingDir
			.concat("/data/CoTraining/CRFpp");
	// public static String _text_dir = _crfFolder
	// .concat("/unlabelled/TextToXML/Text");
	public static String _text_dir = "data/CoTraining/CRFpp/unlabeled/TextToXML/Text";
	public static String _xml_dir = _crfFolder
			.concat("/unlabeled/TextToXML/XML");

	public static void main(String arg[]) throws FileNotFoundException {

		int count = 0;
		TextToXML instance = new TextToXML();

		ArrayList<String> fileList = DocUtil.visitAllFiles(new File(_text_dir));
		for (int i = 0; i < fileList.size(); i++) {
			// System.out.println("Text File: " + fileList.get(i));
			String TXTFilePath = fileList.get(i);
			String TXTFilename = TXTFilePath.substring(TXTFilePath
					.lastIndexOf("/") + 1);
			// System.out.println("@@" + TXTFilename);
			String XMLFilename = TXTFilename.concat(".xml");

			ArrayList<String> nodeList = new ArrayList<String>();
			nodeList = instance.readTextFile(TXTFilePath);
			instance.buildXMLFile(XMLFilename, nodeList);

			count++;
		}

		System.out.println(" \n\n *** Done. " + count
				+ " text files are successfully converted to XML format.");
	}

	public TextToXML() {

	}

	private ArrayList<String> readTextFile(String TXTFilename)
			throws FileNotFoundException {

		ArrayList<String> lineList = new ArrayList<String>();
		ArrayList<String> nodeList = new ArrayList<String>();

		// Read in text file into ArrayList
		File inFile = new File(TXTFilename);

		if (!inFile.exists()) {
			System.out.println("Cannot find "
					+ _text_dir.concat("/" + TXTFilename));
			System.exit(0);
		}

		Scanner myFile = new Scanner(inFile);

		for (int i = 0; myFile.hasNext(); i++) {
			lineList.add(myFile.nextLine());
		}
		myFile.close();

		// System.out.println(lineList.get(2));
		for (int j = 0; j < lineList.size(); j++) {
			String[] s = lineList.get(j).split(" ");
			for (int m = 0; m < s.length; m++) {
				nodeList.add(s[m]);
				// System.out.println(nodeList.get(m));
			}
		}

		// for (int m=0; m<nodeList.size(); m++){
		// System.out.println(nodeList.get(m));
		// }
		System.out.println("Read text file done.");

		return nodeList;
	}

	private void buildXMLFile(String XMLFilename, ArrayList<String> nodeList) {
		// Write text nodes to a XML file
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			/** Use 'simple-xml' format defined by TARSQI toolkit **/
			// Root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("DOC");
			doc.appendChild(rootElement);

			// DOCID elements
			Element docID = doc.createElement("DOCID");
			rootElement.appendChild(docID);
			docID.appendChild(doc.createTextNode(XMLFilename));

			// TEXT elements
			Element text = doc.createElement("TEXT");
			rootElement.appendChild(text);

			// Append text content
			for (int i = 0; i < nodeList.size(); i++) {
				text.appendChild(doc.createTextNode(nodeList.get(i) + " "));
			}

			// Write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(_xml_dir + "/"
					+ XMLFilename));
			transformer.transform(source, result);
			System.out.println("XML file generated.");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

}