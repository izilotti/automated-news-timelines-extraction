/** 
 *  Convert text to XML formats, and sentence breaking applied
 *   
 *  @author Oulin Yang (oulin.yang@gmail.com)
 *  @date   25 Oct, 2011
 */

package nlp;

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

import nlp.opennlp.SentenceExtractor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.DocUtil;

public class SentenceBreaking {

	public static String _curWorkingDir = System.getProperty("user.dir");
	public static String _crfFolder = _curWorkingDir
			.concat("/data/CoTraining/CRFpp");
	public static String _text_dir = "data/CoTraining/CRFpp/unlabelled/TextData";
	public static String _xml_dir = _crfFolder
			.concat("/unlabelled/TextToXML/XML_Sentences");

	public static void main(String arg[]) throws IOException {

		int count = 0;
		SentenceBreaking instance = new SentenceBreaking();

		ArrayList<String> fileList = DocUtil.visitAllFiles(new File(_text_dir));
		for (int i = 0; i < fileList.size(); i++) {
			String TXTFilePath = fileList.get(i);
			String TXTFilename = TXTFilePath.substring(TXTFilePath
					.lastIndexOf("/") + 1);
			// Skip
			if (TXTFilename.equalsIgnoreCase(".DS_Store")) {
				continue;
			}

			ArrayList<String> lineList = new ArrayList<String>();
			// Read in text files
			lineList = instance.readTextFile(TXTFilePath, TXTFilename);
			// Build XML files
			instance.breakSentence(TXTFilename, lineList);
			// Increment counter
			count++;
		}

		System.out.println(" \n\n *** Done. " + count
				+ " text files are successfully converted to XML_sentences.");
	}

	/**
	 * Read in text files
	 * 
	 * @param TXTFilename
	 *            : file name
	 * @return the list of text file names
	 * @throws IOException
	 */
	private ArrayList<String> readTextFile(String TXTFilePath,
			String TXTFilename) throws IOException {

		ArrayList<String> lineList = new ArrayList<String>();

		// Read in text file into ArrayList
		File inFile = new File(TXTFilePath);
		if (!inFile.exists()) {
			System.out.println("Cannot find " + TXTFilePath);
			System.exit(0);
		}

		Scanner myFile = new Scanner(inFile);

		for (int i = 0; myFile.hasNext(); i++) {
			lineList.add(myFile.nextLine());
		}
		myFile.close();

		return lineList;
	}

	/**
	 * Convert text files to XML files, and sentences breaking applied
	 * 
	 * @param TXTFilename
	 *            : file name nodeList: the list of text file names
	 * @return <none>
	 * @throws IOException
	 */
	private void breakSentence(String TXTFilename, ArrayList<String> lineList)
			throws IOException {

		ArrayList<String> sentenceList = new ArrayList<String>();

		SentenceExtractor extractor = new SentenceExtractor();
		for (int i = 0; i < lineList.size(); i++) {
			if (!lineList.get(i).isEmpty()) {
				String[] sentences = extractor.process(lineList.get(i));
				for (int j = 0; j < sentences.length; j++) {
					sentenceList.add(sentences[j]);
				}
			}

		}

		System.out.println(sentenceList.size());
		for (int i = 0; i < sentenceList.size(); i++) {
			buildXML(TXTFilename, sentenceList.get(i), i + 1);
		}
	}

	/**
	 * Build XML files
	 * 
	 * @param TXTFilename
	 *            : file name of text file sentence: text content
	 *            sentenceCounter: sentence counter
	 * @return
	 */
	private void buildXML(String TXTFilename, String sentence,
			int sentenceCounter) {

		String xmlFilename;

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			/** Use 'simple-xml' format defined by TARSQI toolkit **/
			// Root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("DOC");
			doc.appendChild(rootElement);

			// TEXT elements
			Element text = doc.createElement("TEXT");
			rootElement.appendChild(text);

			// Append text content
			text.appendChild(doc.createTextNode(sentence));

			// Write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			xmlFilename = _xml_dir + "/" + TXTFilename + "_" + sentenceCounter
					+ ".xml";
			// System.out.println("--" + xmlFilename);
			StreamResult result = new StreamResult(new File(xmlFilename));
			transformer.transform(source, result);
			System.out.println("XML file generated.");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

}