/** 
 * Utilities for documents
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.xerces.parsers.DOMParser;

public class DocUtil {

	public static String _curWorkingDir = System.getProperty("user.dir");

	// ArrayList for all XML files paths
	public static ArrayList<String> _allXMLFiles = new ArrayList<String>();
	public static ArrayList<String> _allTMLFiles = new ArrayList<String>();
	public static ArrayList<String> _allFiles = new ArrayList<String>();

	// StringBuffer
	public static StringBuffer pad(int depth) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < depth; i++)
			sb.append("  ");

		return sb;
	}

	// Initialise a Xerces' DOM Parser in a static manner
	public static DOMParser parser = null;
	static {
		try {

			parser = new DOMParser();

		} catch (Exception e) {
			System.err
					.println("[DocUtil] Could not initialize DOMParser in TimeMLParsing.");
		}
	}

	/**
	 * Count files in a directory (including files in all sub-directories)
	 * 
	 * @param directory
	 *            : the directory to start in
	 * @return the total number of files
	 */
	public static int countFilesInDirectory(File directory) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				count++;
			}
			if (file.isDirectory()) {
				count += countFilesInDirectory(file);
			}
		}
		return count;
	}

	// Process only files under dir
	public static ArrayList<String> visitAllXMLFiles(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllXMLFiles(new File(dir, children[i]));
			}
		} else {
			String name = dir.getName();
			if (name.endsWith(".xml"))
				_allXMLFiles.add(dir.getPath());
		}
		return _allXMLFiles;
	}

	public static ArrayList<String> visitAllTMLFiles(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllTMLFiles(new File(dir, children[i]));
			}
		} else {
			String name = dir.getName();
			if (name.endsWith(".tml"))
				_allTMLFiles.add(dir.getPath());
		}
		return _allTMLFiles;
	}

	public static ArrayList<String> visitAllFiles(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllFiles(new File(dir, children[i]));
			}
		} else {
			String name = dir.getName();
			// System.out.println("111");
			if (!name.startsWith("."))
				_allFiles.add(dir.getPath());
		}

		return _allFiles;
	}

	public static void copyFile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.err
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void createTextFile(String dir, ArrayList<String> al)
			throws IOException {
		// Create a file
		FileWriter fstream = new FileWriter(dir);

		BufferedWriter out = new BufferedWriter(fstream);
		for (int i = 0; i < al.size(); i++) {
			String line = al.get(i);
			out.write(line + "\n");
		}
		// Close the text stream
		out.close();
	}

	public static ArrayList<ArrayList<String>> readNewLabelledData() {

		String filePath = "data/CoTraining/CRFpp/TrainDataCopies/NewData";
		int count = 0;
		ArrayList<String> newLabeledDoc = new ArrayList<String>();
		ArrayList<ArrayList<String>> newLabeledDocSet = new ArrayList<ArrayList<String>>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String str;
			while ((str = in.readLine()) != null) {
				if (!str.isEmpty())
					newLabeledDoc.add(str);
				else {
					ArrayList<String> newLabeledDoc_copy = (ArrayList<String>) newLabeledDoc
							.clone();
					newLabeledDocSet.add(newLabeledDoc_copy);
					newLabeledDoc.clear();
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return newLabeledDocSet;
	}

	public static void cleanLists() {
		_allXMLFiles.clear();
		_allTMLFiles.clear();
		_allFiles.clear();
	}
	

	public static void fileWriter(){
		try {
			FileWriter outFile = new FileWriter(_curWorkingDir.concat("/data/tml.txt"));
			PrintWriter out = new PrintWriter(outFile);
			
			// Also could be written as follows on one line
			// Printwriter out = new PrintWriter(new FileWriter(args[0]));
		
			// Write text to file
			out.println("This is line 1");
			out.println("This is line 2");
			out.print("This is line3 part 1, ");
			out.println("this is line 3 part 2");
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		String _curWorkingDir = System.getProperty("user.dir");
		// String path = _curWorkingDir.concat();
		String path1 = "/Users/oulin_yang/ANU/COMP8800_Research_Project/Backups/TextData";
		String path2 = "data/backup";
		String path3 = "data/CoTraining/CRFpp/unlabelled/TextData";

		// Test function `countFilesInDirectory'
		// int count1 = countFilesInDirectory(new File(path1));
		// System.out.println(count1);

		// Test function `visitAllXMLFiles'
		// System.out.println("\n========= visitAllXMLFiles ==========");
		// ArrayList<String> listOfFiles1 = visitAllXMLFiles(new File(path3));
		// System.out.println(listOfFiles1.size());
		// for (int i = 0; i < listOfFiles1.size(); i++) {
		// System.out.println(listOfFiles1.get(i));
		// }

		// Test function `visitAllTMLFiles'
		// System.out.println("\n========= visitAllTMLFiles ==========");
		// ArrayList<String> listOfFiles2 = visitAllTMLFiles(new File(path3));
		// System.out.println(listOfFiles2.size());
		// for (int i = 0; i < listOfFiles2.size(); i++) {
		// System.out.println(listOfFiles2.get(i));
		// }

		// Test function `visitAllFiles'
		// System.out.println("\n========= visitAllFiles ==========");
		// ArrayList<String> listOfFiles3 = visitAllFiles(new File(path3));
		// System.out.println(listOfFiles3.size());
		// for (int i = 0; i < listOfFiles3.size(); i++) {
		// System.out.println(listOfFiles3.get(i));
		// }

		// Test function copyFile
		// System.out.println("\n========= copyFile ==========");
		// String srFile =
		// _curWorkingDir.concat("/data/CoTraining/CRFpp/train.data");
		// String dtFile =
		// _curWorkingDir.concat("/data/CoTraining/CRFpp/TrainData_copy/train.data1");
		// copyFile(srFile, dtFile);

		// Test Read New Labeled Data
//		ArrayList<ArrayList<String>> al = readNewLabelledData();
//		for (int i=0; i<al.size(); i++){
//			System.out.println(al.get(i));
//		}
		
		fileWriter();
	}
}
