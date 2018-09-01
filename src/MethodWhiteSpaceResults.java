/**
 * This class is intended to store vertical white space results for each method parsed.
 * An ArrayList will be maintained in MainParser.java to store results for all methods.
 * @author jananisridhar
 * 
 * Google style defines that an indent should be two spaces.
 * 
 * Fields:
 * averageIndent = average # of spaces used for one indent
 * minIndent = minimum # of spaces for one indent
 * maxIndent = max # of spaces used for one indent
 * numTabOccurrences = # of times that tabs were used for an indent
 * numSpaceOccurrences = # of times that spaces were used for an indent
 * 
 */
public class MethodWhiteSpaceResults {

	protected int averageIndent;
	protected int minIndent;
	protected int maxIndent;
	protected int numTabOccurrences;
	protected int numSpaceOccurrences;
	
}
