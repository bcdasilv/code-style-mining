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

public class Tester {

	public static final String gitURL = "https://github.com/janani-sridhar/CaesarCipher";
	public static final String directory = "/Users/jananisridhar/Desktop/CC";
	public static final String localFile = "./RandomFile.java";

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
		System.out.printf("Wildcards present? %b\n", wildCard);

		// Iterate through each method in each class.
		List <ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
		for (ClassOrInterfaceDeclaration item: classes) {

			List<MethodDeclaration> members = item.getMethods();

			for (MethodDeclaration member : members) {
				String name = member.getDeclarationAsString(true, true);
				String[] methodBody = ph.getMethodBody(name, linesOfFile, 0);
				parseMethodBraces(methodBody);	
			}

		}

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
				System.out.printf("For single block starting at line %d, style is %d\n", i, blockStyle);
				singleBlockStyles.add(blockStyle);
			}

			// "multiple" block: if, try
			else if (ph.startsWith(line, "try") || ph.startsWith(line, "if")) {
				control = line.trim().split("\\s+")[0];
//				System.out.printf("Control is: %s\n", control);
				blockStyle = evalMultipleBlockStyle(methodLines, control, i);
				System.out.printf("For multiple block starting at line %d, style is %d\n", i, blockStyle);
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
