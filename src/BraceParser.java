/*	Google style often incorporates a lot of the general K & R style. However, there are some attributes
 * 	exclusive to K & R, which will be identified as "EX_KR_style"
 * 
 *  for "single" blocks, that don't have an immediate block after, there are 3 styles: 
 *  	- exclusive K & R --
 *  		- when there is one line in the block, and no braces
 *  	- Google --
 *  		- when there is one line in the block, and braces.
 *  		- when there are multiple lines in the block, and opening brace is same-line
 *  	- Allman --
 *  		- brace always present, on new line.
 *  
 *  for "multiple" blocks, that have an immediate block after:
 *  	- exclusive K & R --
 *  		- when each small block is a one-liner and has no braces
 *  	- Google --
 *  		- despite whether each small block is one or more lines, there are always braces
 *  		- the new block statement will not use a separating line break after the closing brace from
 *  			previous block
 *  	- Allman --
 *  		- separate lines, braces always.
 *  
 */

public class BraceParser {

	// brace styles
	public static final int ALLMAN_STYLE = 0;
	public static final int GOOGLE_STYLE = 1;
	public static final int EX_KR_STYLE = 2;
	public static final int WEIRD_STYLE = 3;

	private static ParsingHelper ph = new ParsingHelper();

	/**
	 * This method evaluates the style for a "single block". If it doesn't match a known style,
	 * WEIRD_STYLE is returned.
	 * @param lines lines of the method body
	 * @param control type of control statement, should be one of: "for", "while", "do"
	 * @param currLine starting line to search from in the method body
	 * @return the style associated with the block
	 */
	public int evalSingleBlockStyle(String[] lines, String control, int currLine) {

		String[] blockLines = ph.getMethodBody(control, lines, currLine);

		if (checkOneLiner(lines, currLine)) {
			return EX_KR_STYLE;
		} else if (checkAllmanSingle(blockLines, 0)) {
			return ALLMAN_STYLE;
		} else if (checkGoogleSingle(blockLines, 0)) {
			return GOOGLE_STYLE;
		}

		return WEIRD_STYLE;
	}

	/**
	 * This method checks if a one-line control statement uses braces.
	 * @param lines the lines of the method
	 * @param i the current line 
	 * @return whether it is a one-line control statement that does not use braces
	 */
	public boolean checkOneLiner(String[] lines, int i) {
		if (!lines[i].contains("{") && !lines[i+1].contains("{"))
			return true;
		return false;
	}

	/**
	 * This method checks if a single block follows the Google Style.
	 * @param lines lines of the block
	 * @param i the starting line in the block
	 * @return whether it follows Google Style.
	 */
	public boolean checkGoogleSingle(String[] lines, int i) {
		if (lines[i].contains("{") && ph.onlyContains(lines[lines.length - 1], '}'))
			return true;
		return false;
	}

	/**
	 * This method checks if a single block follows the Allman Style.
	 * @param lines lines of the block
	 * @param i the starting line in the block
	 * @return whether it follows Allman Style.
	 */
	public boolean checkAllmanSingle(String[] lines, int i) {

		if (!lines[i].contains("{") && ph.onlyContains(lines[i+1], '{')) {
			if (ph.onlyContains(lines[lines.length - 1], '}'))
				return true;
		}
		return false;

	}

	/**
	 * Looks through each method and analyzes brace styles -- "Master" function
	 * @param methodLines the lines of the method
	 */
	public BraceResults parseMethodBraces(String[] methodLines) {

		int blockStyle;
		String line;
		String control;
		BraceResults br = new BraceResults();

		for (int i = 0; i < methodLines.length; i++) {

			blockStyle = WEIRD_STYLE;
			line = methodLines[i];

			// "single" block: for, while, do.
			if (ph.startsWith(line, "for") || ph.startsWith(line, "while") || ph.startsWith(line, "do")) {
				control = line.split("\\s+")[0];
				blockStyle = evalSingleBlockStyle(methodLines, control, i);
				br.updateStyles(true, blockStyle);
			}

			// "multiple" block: if, try
			else if (ph.startsWith(line, "try") || ph.startsWith(line, "if")) {
				control = line.trim().split("\\s+")[0];
				blockStyle = evalMultipleBlockStyle(methodLines, control, i);
				br.updateStyles(false, blockStyle);				
			}

		}
		
		return br;
	}

	/**
	 * This method checks if a multiple block follows the Allman Style.
	 * @param lines the lines of method
	 * @param control the type of control statement
	 * @param startLine the current line
	 * @return 
	 */
	public boolean checkAllmanMultiple(String[] lines, String control, int startLine) {

		String[] blockLines = ph.getMethodBody(control, lines, startLine);
		boolean result = true;
		int currLine = startLine;

		if (control.equals("try")) {
			result = checkAllmanSingle(blockLines, 0);

			while (result) {
				blockLines = ph.getNextBlock("catch", lines, currLine);
				if (blockLines == null) {
					blockLines = ph.getNextBlock("finally", lines, currLine);
					if (blockLines == null) {
						break;
					}
				}

				result &= checkAllmanSingle(blockLines, 0);
				currLine += blockLines.length;		
			}	
		}

		else if (control.equals("if")) {
			result = checkAllmanSingle(blockLines, 0);

			while (result) {
				blockLines = ph.getNextBlock("else if", lines, currLine);

				if (blockLines == null) {
					blockLines = ph.getNextBlock("else", lines, currLine);
					if (blockLines == null) {
						break;
					}
				}

				result &= checkAllmanSingle(blockLines, 0);
				currLine += blockLines.length;					
			}

		}

		return result;
	}

	/**
	 * This method checks if a multiple block follows the Google Style.
	 * @param lines the lines of method
	 * @param control the type of control statement
	 * @param startLine the current line
	 * @return
	 */
	public boolean checkGoogleMultiple(String[] lines, String control, int startLine) {

		String[] blockLines = ph.getMethodBody(control, lines, startLine);
		boolean result = true;
		int currLine = startLine;

		if (control.equals("try")) {

			result &= blockLines[0].contains("{");

			while (result) {
				blockLines = ph.getNextBlock("catch", lines, currLine);

				if (blockLines == null) {
					blockLines = ph.getNextBlock("finally", lines, currLine);
					if (blockLines == null) {
						break;
					}
				}

				result &= blockLines[0].contains("}") && (blockLines[0].contains("catch") ||
						blockLines[0].contains("finally"));
				currLine += blockLines.length;

			}
		}

		else if (control.equals("if")) {

			result &= blockLines[0].contains("{");

			while (result) {
				blockLines = ph.getNextBlock("else", lines, currLine);

				if (blockLines == null) {
					blockLines = ph.getNextBlock("else if", lines, currLine);
					if (blockLines == null) {
						break;
					}
				}

				result &= blockLines[0].contains("}") && (blockLines[0].contains("else") ||
						blockLines[0].contains("else if"));
				currLine += blockLines.length;

			}
		}

		return result;
	}

	/**
	 * This method checks whether a one-line control statement uses braces.
	 * @param lines the lines of method
	 * @param startLine the current line
	 * @return
	 */
	public boolean checkOneLinerMultiple(String[] lines, int startLine) {
		boolean result = true;
		int currLine = startLine;

		result = checkOneLiner(lines, currLine);
		while (result) {
			if (!lines[currLine].contains("else")) {
				break;
			}
			result &= checkOneLiner(lines, currLine);
			currLine += 2;
		}

		return result;

	}

	/**
	 * 
	 * @param methodLines
	 * @param control
	 * @param currLine
	 * @return
	 */
	public int evalMultipleBlockStyle(String[] methodLines, String control, int currLine) {

		if (control.equals("if")) {
			if (checkOneLinerMultiple(methodLines, currLine)) {
				return EX_KR_STYLE;
			}
		}

		if (checkAllmanMultiple(methodLines, control, currLine)) {
			return ALLMAN_STYLE;
		} else if (checkGoogleMultiple(methodLines, control, currLine)) {
			return GOOGLE_STYLE;
		}

		return WEIRD_STYLE;
	}
}
