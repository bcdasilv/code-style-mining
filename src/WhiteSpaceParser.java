import java.util.List;
import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Checks if next line is next method
 * Returns true if google style
 */
public class WhiteSpaceParser {
	public boolean parseWhiteSpaceBetweenMethods(String[] linesOfFile, int methodBodyLength, int i,
												 List<MethodDeclaration> methods) {
		String nextMethodName = methods.get(i+1).getDeclarationAsString(true, true);
		String thisMethodName = methods.get(i).getDeclarationAsString(true, true);
		
		// getting the "absolute" line number of the method
		int startLine = 0;
		for(int j = 0; j < linesOfFile.length; j++) {
			String line = linesOfFile[j];
			if(line.contains(thisMethodName)) {
				startLine = j;
				break;
			}
		}
		int endLine = startLine + methodBodyLength - 1;
		return linesOfFile[endLine].contains(nextMethodName);
	}
	
	/**
	 *  operates under a few assumptions: lines in the same block are indented the same way
	 *  the field "numSpaceOccurrences" in MethodWhiteSpaceParser will be computed as follows:
	 *  	1) obtain # of spaces used in each line, store in arraylist 
	 *  	2) obtain average indent value
	 *  	3) for each line, divide # spaces by avg indent value, then sum the quotients up.
	 * @param methodLines
	 * @param mwp
	 */
	public void parseIndents(String[] methodLines, MethodWhiteSpaceResults mwp) {
		int i, cnt = 0;
		char c;
		int minIndent = Integer.MAX_VALUE;
		int maxIndent = Integer.MIN_VALUE;
		ArrayList<Integer> indents = new ArrayList<Integer>();
		ArrayList<Integer> spaces = new ArrayList<Integer>(); // # spaces used in each line
		int numTabs = 0;
		int numLineSpaces = 0;
		String line;
		int indent;

		for(i = 0; i < methodLines.length; i++) {
			cnt = 0;
			line = methodLines[i];

			if(!line.trim().equals("")) {
				numLineSpaces = 0;
				while(Character.isWhitespace(c = line.charAt(cnt))) {
					if(c == ' ') {
						numLineSpaces++;
					} else if(c == '\t') {
						numTabs++;
					}
					cnt++;
				}

				spaces.add(numLineSpaces);
				
				if((line.length() >= 2) && (line.charAt(line.length() - 1) == '{')) {
					indent = updateSpaceCount(methodLines, i);
					if(indent > 0) {
						indents.add(indent);					
						maxIndent = Math.max(indent, maxIndent);
						minIndent = Math.min(indent, minIndent);
					}
				}
			}

		}
		
		// Computing Results
		mwp.minIndent = minIndent;
		mwp.maxIndent = maxIndent;
		mwp.numTabOccurrences = numTabs;
		mwp.averageIndent = computeAverageIndent(indents);
		mwp.numSpaceOccurrences = computeSpaceOccurrences(spaces, mwp.averageIndent);
	}

	// -1 denotes that only tabs were used. a non-negative number should be returned otherwise.
	public int computeAverageIndent(ArrayList<Integer> indents) {
		int sum = 0;
		for (int i = 0; i < indents.size(); i++) {
			sum += indents.get(i);
		}
		
		if (indents.size() > 0) {
			int avg = sum/indents.size();
			return avg;
		}
		return -1;
	}
	
	public int computeSpaceOccurrences(ArrayList<Integer> lineSpaces, int avg) {
		if (avg > 0) {
			int numOccurrences = 0;
			for (int i = 0; i < lineSpaces.size(); i++) {
				numOccurrences += (lineSpaces.get(i)/avg);
			}

			return numOccurrences;
		}
		return 0;
	}
	
	public int updateSpaceCount(String[] methodLines, int i) {
		int indent;
		int line1Spaces = 0;
		int line2Spaces = 0;
		String line1 = methodLines[i];
		String line2;
		int j = i + 1;

		// This prevents from going out of bounds
		if(j == methodLines.length) {
			return 0;
		}

		while(methodLines[j].trim().equals("")) {
			j++;
		}
		
		line2 = methodLines[j];
		
		if(line2.contains("\t") || line1.contains("\t")) {
			return -1;
		}
		while(line1.charAt(line1Spaces) == ' ') {
			line1Spaces++;
		}
		while(line2.charAt(line2Spaces) == ' ') {
			line2Spaces++;
		}

		indent = line2Spaces - line1Spaces;
		
		if(indent <= 0) {
			return 0;
		}
		return indent;
	}
	
	public MethodWhiteSpaceResults parseWhiteSpace(String[] methodLines) {
		MethodWhiteSpaceResults mwp = new MethodWhiteSpaceResults();
		parseIndents(methodLines, mwp);	
		return mwp;
	}
	
}
