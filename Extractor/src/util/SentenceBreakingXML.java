package util;

import java.io.File;
import java.io.IOException;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SentenceBreakingXML {
	

	// Get current working directory
	static String _curWorkingDir = System.getProperty("user.dir");
	// Paths where input and output files to TARSQI are placed	
	public static String _inDir = _curWorkingDir.concat("/data/TARSQI_IN/10");
	public static String _outDir = _curWorkingDir.concat("/data/TARSQI_IN_sentences/10");
	
	
	public SentenceBreakingXML(){
		
	}
	
	
	private String[] parseXmlFile(String xmlFileName) throws IOException {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		SentenceExtractor extractor = new SentenceExtractor();
		String[] sentences = null;
		
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(xmlFileName);

			//get the root element
			Element docEle = dom.getDocumentElement();
			
			//get a node list of elements
			NodeList nl = docEle.getElementsByTagName("TEXT");
//			System.out.println(nl);
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
//					System.out.println(nl.item(i).getTextContent());
					String content = nl.item(i).getTextContent();
					sentences = extractor.process(content);
//					System.out.println(extractor.getSentenceString(sentences));
					
				}
			}		
			
			System.out.println("----------------------\n");
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return sentences;
	}
	
	
	private void buildXMLFile(String newFileName, String sentence){
		System.out.println(newFileName);
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
			docID.appendChild(doc.createTextNode(newFileName));
			
			// TEXT elements
			Element text = doc.createElement("TEXT");
			rootElement.appendChild(text);
	 
			// Append text content
			text.appendChild(doc.createTextNode(sentence));
	 
			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File(_outDir + "/" + newFileName));
			transformer.transform(source, result);
		 
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}catch(TransformerException tfe){
			tfe.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args){
		FileFinder ffinder = new FileFinder(".xml", _inDir);
		SentenceBreakingXML sbXML = new SentenceBreakingXML();
		for (int i=0; i<ffinder.fileList.size(); i++){
			System.out.println(ffinder.fileList.get(i));
			String xmlFileName = ffinder.fileList.get(i);
			try {
				String[] sentences = sbXML.parseXmlFile(xmlFileName);
				for (int j=0; j<sentences.length; j++){
					String newFileName = null;
					if(j<=9){
						newFileName = xmlFileName.substring(xmlFileName.lastIndexOf("/")+1,
								xmlFileName.lastIndexOf(".")).concat("_0"+Integer.toString(j))
										.concat(".xml");
					}
					else{
						newFileName = xmlFileName.substring(xmlFileName.lastIndexOf("/")+1,
								xmlFileName.lastIndexOf(".")).concat("_"+Integer.toString(j))
										.concat(".xml");
					}
					
					sbXML.buildXMLFile(newFileName, sentences[j]);
				}			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
}
