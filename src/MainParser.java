import java.io.*;
import java.util.List;
import java.util.ArrayList;

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

public class MainParser {

	public static final String gitURL = "https://github.com/janani-sridhar/CaesarCipher";
	public static final String directory = "/Users/jananisridhar/Desktop/CC";
	public static final String localFile = "./RandomFile.java";

	// Parser objects
	private static ParsingHelper ph = new ParsingHelper();
	private static BraceParser bp = new BraceParser();
	private static NameParser np = new NameParser();
	private static WhiteSpaceParser wp = new WhiteSpaceParser();

	// result objects -- usually for each method
	private static ArrayList<BraceResults> braces = new ArrayList<BraceResults>();
	private static ArrayList<Integer> linesExceeding = new ArrayList<Integer>();
	
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
					wp.parseMethodWhiteSpace(linesOfFile, methodBody.length, i, methods);
				}
				
				System.out.printf("\nParsing method %s ", name);
				System.out.printf("-----------------------------\n");
				parseLineLengths(methodBody);
				braces.add(bp.parseMethodBraces(methodBody));	
				wp.parseWhiteSpace(methodBody);
				np.parseVariables(methodBody);
			}

		}

	}

	public static void parseLineLengths(String[] methodLines) {
		int transgressions = 0;
		for (String line: methodLines) {
			if (line.length() > 100) {
				transgressions++;
			}
		}
		
		linesExceeding.add(transgressions);
		
	}
	
	public static void analyzeResults() {
		
	}

}
