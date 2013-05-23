/** 
 * Utilities of NLP tools
 *   
 * @author Oulin Yang (oulin.yang@gmail.com)
 * @date   25 Oct, 2011
 */

package nlp;

import nlp.opennlp.POSTagger;

public class NLPUtil {

	// Initialise POSTagger in a static manner
	public static POSTagger tagger = null;
	static {
		try {

			tagger = new POSTagger();

		} catch (Exception e) {
			System.err.println("[NLPUtil] Could not initialize POSTagger.");
		}
	}
}
