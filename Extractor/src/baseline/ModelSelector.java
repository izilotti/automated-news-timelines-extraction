/** 
 * ModelSelector provides an abstract class for specific models.
 * All model classes shall extend from this abstract class.
 * 
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date 25 Oct, 2011
 */
package baseline;

import java.util.ArrayList;
import parsers.TimeMLParser;
import util.FileFinder;

public abstract class ModelSelector {

	public static boolean DEBUG = false;

	public String prefix;
	public String trainingDir;
	public String testingDir;

	// The lists of file names of training and testing data
	public ArrayList<String> _trainingFilesList = new ArrayList<String>();
	public ArrayList<String> _testingFilesList = new ArrayList<String>();

	// Training and testing data (in TimeMLParser format)
	public ArrayList<TimeMLParser> _trainingData = new ArrayList<TimeMLParser>();
	public ArrayList<TimeMLParser> _testingData = new ArrayList<TimeMLParser>();

	// Constructor
	public ModelSelector(String prefix, String trainingDir, String testingDir) {
		this.prefix = prefix;
		this.trainingDir = trainingDir;
		this.testingDir = testingDir;
	}

	/**
	 * Get file names of training data and parse by TimeML Parser
	 * 
	 * @param type
	 *            : the prefix of file name
	 * @return <none>
	 */
	public void parseTrainingData(String type) throws Exception {

		_trainingFilesList.clear();
		_trainingData.clear();

		FileFinder ffinder = new FileFinder(type, trainingDir);
		_trainingFilesList = ffinder.fileList;
		String fileName1;

		for (int i = 0; i < _trainingFilesList.size(); i++) {
			fileName1 = _trainingFilesList.get(i);
			TimeMLParser timeMLInstance = new TimeMLParser(fileName1);
			timeMLInstance.parse();
			_trainingData.add(timeMLInstance);
		}
	}

	/**
	 * Get file names of testing data and parse by TimeML Parser
	 * 
	 * @param type
	 *            : the prefix of file name
	 * @return <none>
	 */
	public void parseTestingData(String type) throws Exception {

		_testingFilesList.clear();
		_testingData.clear();

		FileFinder ffinder = new FileFinder(type, testingDir);
		_testingFilesList = ffinder.fileList;
		String fileName;

		for (int i = 0; i < _testingFilesList.size(); i++) {
			fileName = _testingFilesList.get(i);
			TimeMLParser timeMLInstance = new TimeMLParser(fileName);
			timeMLInstance.parse();
			_testingData.add(timeMLInstance);
		}
	}

	public abstract void preprocessing(String type) throws Exception;

	public abstract void trainModel() throws Exception;

	public abstract void computeResults(TimeMLParser timeMLInstance)
			throws Exception;

	public abstract ArrayList<String> getTokenList();

	public abstract ArrayList<String> getPredLabelList();

	public abstract ArrayList<String> getTrueLabelList();

	public abstract int getWindowSize();

	public ArrayList<TimeMLParser> getTrainingData() {
		return _trainingData;
	}

	public ArrayList<TimeMLParser> getTestingData() {
		return _testingData;
	}

	public abstract void clearLists();

}
