import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;

public class WhiteSpaceParser {

	private static ParsingHelper ph = new ParsingHelper();
	
	public void parseMethodWhiteSpace(String[] linesOfFile, int methodBodyLength,
			int i, List<MethodDeclaration> methods) {
		
		int startLine = 0;
		int endLine = 0;
		int j = 0;
		String nextMethodName = methods.get(i+1).getDeclarationAsString(true, true);
		String thisMethodName = methods.get(i).getDeclarationAsString(true, true);
		boolean googleStyle;
		
		// getting the "absolute" line number of the method
		for (; j < linesOfFile.length; j++) {
			String line = linesOfFile[j];

			if (line.contains(thisMethodName)) {
				startLine = j;
				break;
			}
		}
		
		endLine = startLine + methodBodyLength - 1;	
		googleStyle = linesOfFile[endLine + 2].contains(nextMethodName);
		System.out.printf("Follows google style for lines between methods? %b\n" , googleStyle);
		
	}
	
	public void parseIndents(String[] methodLines) {
		int i, cnt = 0;
		char c;
		int minIndent = Integer.MAX_VALUE;
		int maxIndent = Integer.MIN_VALUE;
		boolean spaces = false, tabs = false;
		int numTabs = 0;
		int numSpaces = 0;
		String line;
		int indent;

		for (i = 0; i < methodLines.length; i++) {
			cnt = 0;
			line = methodLines[i];

			if (!line.trim().equals("")) {
//				System.out.printf("Line is %s\n", line);
				while (Character.isWhitespace(c = line.charAt(cnt))) {
					if (c == ' ') {
						spaces = true;
						numSpaces++;
					} else if (c == '\t') {
						tabs = true;
						numTabs++;
					}
					cnt++;
				}

				// update space count
				if ((line.length() >= 2) && (line.charAt(line.length() - 1) == '{')) {

					indent = updateSpaceCount(methodLines, i);
					
					if (indent > maxIndent) {
						maxIndent = indent;
					}

					if (indent < minIndent) {
						minIndent = indent;
					}

				}
			}

		}
		
		System.out.printf("Uses tabs? %b\t spaces? %b\t\nmaxIndent = %d\t minIndent = %d\n", tabs, spaces, maxIndent, minIndent);

	}

	public int updateSpaceCount(String[] methodLines, int i) {
		int indent;
		int line1Spaces = 0;
		int line2Spaces = 0;
		String line1 = methodLines[i];
		String line2;
		int j = i + 1;
		
		while (methodLines[j].trim().equals("")) {
			j++;
		}
		
		line2 = methodLines[j];
		
		if (line2.contains("\t") || line1.contains("\t")) {
			return 0;
		}
		
		while (line1.charAt(line1Spaces) == ' ') {
			line1Spaces++;
		}

		while (line2.charAt(line2Spaces) == ' ') {
			line2Spaces++;
		}

		indent = line2Spaces - line1Spaces;
		
		if (indent <= 0) {
			return 0;
		}
		
		System.out.printf("Indent is : %d\n", indent);
		return indent;
		
	}
	
	public void parseWhiteSpace(String[] methodLines) {
		parseIndents(methodLines);	
		parseExtraBlankLines(methodLines);
	}
	

	public void parseExtraBlankLines(String[] methodLines) {
		int extraBlankLines = 0;
		
		for (int i = 0; i < methodLines.length; i++) {
			String line = methodLines[i];
			if (ph.onlyContains(line, '{')) {
				if (methodLines[i+1].trim().equals("")) {
					extraBlankLines++;
				}
			}
		}
		
		System.out.printf("Extra blank lines after control statements: %d\n", extraBlankLines);
	}
	
	
}
