/** 
 * Recursively searching files and record their names
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package util;

import java.io.File;
import java.util.ArrayList;

public class FileFinder {

	public ArrayList<String> fileList = new ArrayList<String>();

	/**
	 * File finder Constructor
	 * 
	 * @param type
	 *            : file type (.tml, or .xml)
	 * @param src
	 *            : source directory
	 * @return ArrayList of files name depends on type
	 */
	public FileFinder(String type, String src) {

		if (type.equals(".tml"))
			fileList = CollectTMLFiles(src);
		else if (type.equals(".xml"))
			fileList = CollectXMLFiles(src);
		else if (type.equals(""))
			fileList = CollectFiles(src);
		else
			System.err.println("[FileFinder: Unknown source type.]");
	}

	/**
	 * Collect All TimeML files
	 * 
	 * @param src
	 *            : source directory
	 * @return ArrayList of .tml files names
	 */
	public static ArrayList<String> CollectTMLFiles(String src) {
		ArrayList<String> allTMLFiles = new ArrayList<String>();

		File fdir = new File(src);
		if (fdir.isDirectory()) {
			// Generate a list of file names of that directory
			String[] dirList = fdir.list();

			// Recursive searching on the list
			for (int i = 0; i < dirList.length; i++) {
				// Collect all TimeML file names
				if (dirList[i].endsWith(".tml")) {
					String tmlFileName = src.concat("/").concat(dirList[i]);
					allTMLFiles.add(tmlFileName);
				}
			}
		}
		return allTMLFiles;
	}

	/**
	 * Collect All XML files
	 * 
	 * @param src
	 *            : source directory
	 * @return ArrayList of .xml files names
	 */
	public static ArrayList<String> CollectXMLFiles(String src) {
		ArrayList<String> allXMLFiles = new ArrayList<String>();

		File fdir = new File(src);
		if (fdir.isDirectory()) {
			// Generate a list of file names of that directory
			String[] dirList = fdir.list();

			// Recursive searching on the list
			for (int i = 0; i < dirList.length; i++) {
				// Collect all XML file names
				if (dirList[i].endsWith(".xml")) {
					String xmlFileName = src.concat("/").concat(dirList[i]);
					allXMLFiles.add(xmlFileName);
				}
			}
		}
		return allXMLFiles;
	}

	/**
	 * Collect All TimeML files
	 * 
	 * @param src
	 *            : source directory
	 * @return ArrayList of .tml files names
	 */
	public static ArrayList<String> CollectFiles(String src) {
		ArrayList<String> allFiles = new ArrayList<String>();

		File fdir = new File(src);
		if (fdir.isDirectory()) {
			// Generate a list of file names of that directory
			String[] dirList = fdir.list();

			// Recursive searching on the list
			for (int i = 0; i < dirList.length; i++) {
				// Collect all file names
				String fileName = src.concat("/").concat(dirList[i]);
				allFiles.add(fileName);
			}
		}
		return allFiles;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("\nTest1 (CollectTMLFiles): ");
		try {
			FileFinder ffinder1 = new FileFinder(".tml",
					"/Users/oulin_yang/Documents/workspace/"
							+ "Automated_Timeline_Extraction/src/data/train");
			System.out.println(ffinder1.fileList.size());
			for (int i = 0; i < ffinder1.fileList.size(); i++) {
				System.out.println(ffinder1.fileList.get(i));
			}
		} catch (Exception e) {
			System.out.println("Failed with test 1.");
		}

		System.out.println("\nTest2 (CollectXMLFiles): ");
		try {
			FileFinder ffinder2 = new FileFinder(".xml", "/Users/oulin_yang/"
					+ "code/ttk-1.0/code/data/tmp");
			System.out.println(ffinder2.fileList.size());
			for (int i = 0; i < ffinder2.fileList.size(); i++) {
				System.err.println(ffinder2.fileList.get(i));
			}
		} catch (Exception e) {
			System.err.println("Failed with test 2.");
		}

		System.out.println("\nTest3 (CollectFiles): ");
		try {
			FileFinder ffinder3 = new FileFinder(
					"",
					"/Users/oulin_yang/Documents/workspace/Automated_Timeline_Extraction/data/CoTraining/CRFpp/unlabelled/Australian/nuclear");
			System.out.println(ffinder3.fileList.size());
			for (int i = 0; i < ffinder3.fileList.size(); i++) {
				System.err.println(ffinder3.fileList.get(i));
			}
		} catch (Exception e) {
			System.err.println("Failed with test 3.");
		}
	}
}