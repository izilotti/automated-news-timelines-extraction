/** 
 * Clean and unify file names
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package util;

import java.io.File;

public class CleanFileName {

	public static void rename(String oldName, String newName) {

		// File (or directory) with old name
		File file = new File(oldName);

		// File (or directory) with new name
		File file2 = new File(newName);

		// Rename file (or directory)
		boolean success = file.renameTo(file2);
		if (success) {
			System.out.println("renamed!");
		} else
			System.out.println("Fail");
	}

	public static void main(String[] args) {
		String path = "data/CoTraining/CRFpp/unlabelled/All";
		File dir = new File(path);

		int count = 0;
		for (File file : dir.listFiles()) {

			String fileName = file.getName();

			if (fileName.contains("%")) {
				count++;

				String newName = fileName.replace('%', '_');

				String oldPathName = path.concat("/" + fileName);
				System.out.println("oldPathName = " + oldPathName);
				String newPathName = path.concat("/" + newName);
				System.out.println("newPathName = " + newPathName + "\n");
				rename(oldPathName, newPathName);
			}
		}

		System.out.println(count);
	}
}
