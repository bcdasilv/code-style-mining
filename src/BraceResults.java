/**
 * This class is to count the number of different style occurrences that occur for one method.
 * Results will be analyzed in a different area.
 * 
 * @author jananisridhar
 *
 */

public class BraceResults {

	// different styles and their counts
	protected int SINGLE_GOOGLE;
	protected int SINGLE_EX_KR;
	protected int SINGLE_ALLMAN;
	protected int SINGLE_WEIRD;
	protected int MUL_GOOGLE;
	protected int MUL_EX_KR;
	protected int MUL_ALLMAN;
	protected int MUL_WEIRD;
	
	public void updateStyles(boolean single, int style) {
	
		if (single) {
			switch (style) {
			case BraceParser.ALLMAN_STYLE:
				SINGLE_ALLMAN++;
				break;
			case BraceParser.EX_KR_STYLE:
				SINGLE_EX_KR++;
				break;
			case BraceParser.GOOGLE_STYLE:
				SINGLE_GOOGLE++;
				break;
			default:
				SINGLE_WEIRD++;
				break;
			}
		}
		
		else {			
			switch (style) {
			case BraceParser.ALLMAN_STYLE:
				MUL_ALLMAN++;
				break;
			case BraceParser.EX_KR_STYLE:
				MUL_EX_KR++;
				break;
			case BraceParser.GOOGLE_STYLE:
				MUL_GOOGLE++;
				break;
			default:
				MUL_WEIRD++;
				break;
			}
		}
		
	}
	
}
