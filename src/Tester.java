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
 *  The overall "method style" will be computed in a different function.
 *  
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

public class Tester {

	public static final String gitURL = "https://github.com/janani-sridhar/CaesarCipher";
	public static final String directory = "/Users/jananisridhar/Desktop/CC";
	public static final String localFile = "./RandomFile.java";

	// brace styles
	public static final int ALLMAN_STYLE = 0;
	public static final int GOOGLE_STYLE = 1;
	public static final int EX_KR_STYLE = 2;
	public static final int WEIRD_STYLE = 3;

	private static ParsingHelper ph = new ParsingHelper();

	public static void main(String[] args) {

		//		getGitFiles(gitURL, directory);
		parseFile(localFile);

	}

	public static void getGitFiles(String url, String directory) {
		File gitFolder = new File(directory);
		Git git = null;

		if (!gitFolder.exists()) {
			try {
				git = Git.cloneRepository()
						.setURI( url )
						.setDirectory(gitFolder)
						.call();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}   

		}

		else {
			try {
				git = Git.open(gitFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Repository repo = git.getRepository();		

		for (File content : repo.getWorkTree().listFiles())
			classifyFiles(content);
	}

	public static void classifyFiles(File f) {

		if (f.isFile()) {
			if (f.getName().contains("java"))  
				parseFile(f.getAbsolutePath());
		}

		else {
			for (File k: f.listFiles())
				classifyFiles(k);
		}

	}

	public static boolean wildCardPresent(CompilationUnit cu) {
		boolean result = true;
		NodeList<ImportDeclaration> imports = cu.getImports();
		for (ImportDeclaration each : imports) {
			result &= each.isAsterisk();
		}
		return !result;
	}

	public static void parseFile(String f) {

		FileInputStream in = null;
		String[] linesOfFile = null;
		CompilationUnit cu;
		boolean wildCard = false;

		try {
			in = new FileInputStream(new File(f));
			linesOfFile = ph.readLines(f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		cu = JavaParser.parse(in);

		// look for asterisks
		wildCard = wildCardPresent(cu);
		System.out.printf("Wildcards present? %b\n\n", wildCard);

		// Iterate through each method in each class.
		List <ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
		for (ClassOrInterfaceDeclaration item: classes) {

			List<MethodDeclaration> methods = item.getMethods();
			List<FieldDeclaration> fields = item.getFields();			
			
			for (int i = 0; i < methods.size(); i++) {

				MethodDeclaration member = methods.get(i);
				String name = member.getDeclarationAsString(true, true);
				String[] methodBody = ph.getMethodBody(name, linesOfFile, 0);

				if (i + 1 != methods.size()) {
					parseMethodWhiteSpace(linesOfFile, methodBody.length, i, methods);
				}
				
				System.out.printf("\nParsing method %s ", name);
				System.out.printf("-----------------------------\n");
				parseLineLengths(methodBody);
				parseMethodBraces(methodBody);	
				parseWhiteSpace(methodBody);
				parseVariables(methodBody);
			}

		}

	}

	public static void parseMethodWhiteSpace(String[] linesOfFile, int methodBodyLength,
			int i, List<MethodDeclaration> methods) {
		
		int startLine = 0;
		int endLine = 0;
		int j = 0;
		String nextMethodName = methods.get(i+1).getDeclarationAsString(true, true);
		String thisMethodName = methods.get(i).getDeclarationAsString(true, true);
		boolean googleStyle;
		
//		System.out.printf("\nThis methodName = %s\n", thisMethodName);
//		System.out.printf("Next methodName = %s\n", nextMethodName);
		
		// getting the "absolute" line number of the method
		for (; j < linesOfFile.length; j++) {
			String line = linesOfFile[j];

			if (line.contains(thisMethodName)) {
//				System.out.printf("current line = %s\n", line);
				startLine = j;
				break;
			}
		}
		
//		System.out.printf("j = %d\n", j);
		endLine = startLine + methodBodyLength - 1;
		
//		System.out.printf("Startline = %d\t Endline = %d\n", startLine, endLine);
//		System.out.printf("Endline is: %s\n", linesOfFile[endLine]);

		
		googleStyle = linesOfFile[endLine + 2].contains(nextMethodName);
		System.out.printf("Follows google style for lines between methods? %b\n" , googleStyle);
		
	}
	
	public static void parseFieldNames(List<FieldDeclaration> fields) {
		
		String field;
		int camelCase = 0;
		int underScores = 0;
		String[] c = {"String", "int", "boolean", "char", "long", "float", "double"};
		ArrayList<String> primitives = new ArrayList<>(Arrays.asList(c));
		String[] words;
		ArrayList<String> wordList;
		int indx = 0;
		
		for (int i = 0; i < fields.size(); i++) {
			field = fields.get(i).toString();
			words = field.split("\\s+");
			wordList = new ArrayList<>(Arrays.asList(words));
			
			// go through each word in the field declaration and find the index of the primitive type
			for (int j = 0; j < wordList.size(); j++) {
				if (primitives.contains(wordList.get(i))) {
					indx = j;
					break;
				}
			}
			
			if (parseName(wordList.get(indx+1))) {
				underScores++;
			}
			
			else {
				camelCase++;
			}
			
		}
		
	}
	
	public static void parseWhiteSpace(String[] methodLines) {
		parseIndents(methodLines);	
		parseExtraBlankLines(methodLines);
	}
	
	
	public static void parseExtraBlankLines(String[] methodLines) {
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
	
	public static void parseLineLengths(String[] methodLines) {
		int transgressions = 0;
		for (String line: methodLines) {
			if (line.length() > 100) {
				transgressions++;
			}
		}
		
		System.out.printf("# of lines with length over 100 characters: %d\n", transgressions);
	}
	
	public static void parseVariables(String[] methodLines) {
		
		String[] c = {"String", "int", "boolean", "char", "long", "float", "double"};
		ArrayList<String> primitives = new ArrayList<>(Arrays.asList(c));
		int underscores = 0;
		int camelCase = 0;
		
		for (int i = 0; i < methodLines.length; i++) {
			String name = methodLines[i].split("\\s+")[0];
			if (primitives.contains(name)) {
				String var = methodLines[i].split("\\s+")[1]; 
				if (parseName(var)) {
					underscores++;
				}
				else {
					camelCase++;
				}
			}
		}
		
		System.out.printf("# of variables with underscores: %d\t, # of camelCase variables: %d\n", underscores, camelCase);
	}
	
	// returns true if it contains underscores, false if not (camelcase)
	public static boolean parseName(String var) {
		boolean underscores = false;
		
		if (var.contains("_")) {
			underscores = true;
		}
		
		return underscores;
	}
	
	public static void parseIndents(String[] methodLines) {
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

	public static int updateSpaceCount(String[] methodLines, int i) {
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
		
//		System.out.printf("%s\n%s\n", line1, line2);
		
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
	

	/**
	 * This method evaluates the style for a "single block". If it doesn't match a known style,
	 * WEIRD_STYLE is returned.
	 * @param lines lines of the method body
	 * @param control type of control statement, should be one of: "for", "while", "do"
	 * @param currLine starting line to search from in the method body
	 * @return the style associated with the block
	 */
	public static int evalSingleBlockStyle(String[] lines, String control, int currLine) {
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
	public static boolean checkOneLiner(String[] lines, int i) {
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
	public static boolean checkGoogleSingle(String[] lines, int i) {
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
	public static boolean checkAllmanSingle(String[] lines, int i) {

		if (!lines[i].contains("{") && ph.onlyContains(lines[i+1], '{')) {
			if (ph.onlyContains(lines[lines.length - 1], '}'))
				return true;
		}
		return false;

	}

	/**
	 * Looks through each method and analyzes brace styles
	 * @param methodLines the lines of the method
	 */
	public static void parseMethodBraces(String[] methodLines) {

		int blockStyle;
		String line;
		String control;
		ArrayList<Integer> singleBlockStyles = new ArrayList<Integer>();
		ArrayList<Integer> multipleBlockStyles = new ArrayList<Integer>();

		for (int i = 0; i < methodLines.length; i++) {

			blockStyle = WEIRD_STYLE;
			line = methodLines[i];

			// "single" block: for, while, do.
			if (ph.startsWith(line, "for") || ph.startsWith(line, "while") || ph.startsWith(line, "do")) {
				control = line.split("\\s+")[0];
				blockStyle = evalSingleBlockStyle(methodLines, control, i);
//				System.out.printf("For single block starting at line %d, style is %d\n", i, blockStyle);
				singleBlockStyles.add(blockStyle);
			}

			// "multiple" block: if, try
			else if (ph.startsWith(line, "try") || ph.startsWith(line, "if")) {
				control = line.trim().split("\\s+")[0];
				//				System.out.printf("Control is: %s\n", control);
				blockStyle = evalMultipleBlockStyle(methodLines, control, i);
//				System.out.printf("For multiple block starting at line %d, style is %d\n", i, blockStyle);
				multipleBlockStyles.add(blockStyle);				
			}

		}

	}

	/**
	 * This method checks if a multiple block follows the Allman Style.
	 * @param lines the lines of method
	 * @param control the type of control statement
	 * @param startLine the current line
	 * @return 
	 */
	public static boolean checkAllmanMultiple(String[] lines, String control, int startLine) {

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
	public static boolean checkGoogleMultiple(String[] lines, String control, int startLine) {

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
	public static boolean checkOneLinerMultiple(String[] lines, int startLine) {
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
	public static int evalMultipleBlockStyle(String[] methodLines, String control, int currLine) {

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
