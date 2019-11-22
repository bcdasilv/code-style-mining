import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import config.Config;
import org.json.JSONObject;

public class FileParser {
	// Parser objects -- for calling methods in other classes
	private ParsingHelper ph = new ParsingHelper();
	private BraceParser bp = new BraceParser();
	private NameParser np = new NameParser();
	private WhiteSpaceParser wp = new WhiteSpaceParser();

	// result objects. If it is a collection, it is for each method, and if it is a boolean, then it is an element pertaining to the file.
	// the one exception is the NameResults object -- it holds name result information for all the classes, members inside classes, and local variables inside methods.
	// there is only one NameResults object for each file.
	
	protected boolean wildCard;
	protected boolean packageDecName;
	protected ArrayList<Boolean> classWhiteSpace = new ArrayList<>();
	protected ArrayList<BraceResults> braces = new ArrayList<>();
	protected ArrayList<Integer> linesExceeding = new ArrayList<>();
	protected ArrayList<MethodWhiteSpaceResults> methodWPs = new ArrayList<>();
	protected NameResults nr = new NameResults();

	private static final Config config = Config.getInstance();
	private static final String tempFilePath = config.getTempJavaFilePath();

	private JSONify jsonify;

	public String repoURL;

	public FileParser() {
		jsonify = new JSONify(this);
	}
	
	public void parseFile(String repoURL, String filePath, JSONifySummary summary) throws ParseProblemException {
		this.repoURL = repoURL;
		FileInputStream in = null;
		String[] linesOfFile = null;
		Map<String, Integer> LOCMetrics = null;
		
		try {
			in = new FileInputStream(new File(tempFilePath));
			linesOfFile = ph.readLines(tempFilePath);
			LOCCounter counter = new LOCCounter(tempFilePath);
			LOCMetrics = counter.getLOC();
			summary.addLOCMetrics(LOCMetrics);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//StaticJavaParser.getConfiguration().setPreprocessUnicodeEscapes(true);

		CompilationUnit cu = StaticJavaParser.parse(in);
		// look for asterisks in import statements and parse package declaration
		wildCardPresent(cu);
		parsePkgDec(cu);
		List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
		for(ClassOrInterfaceDeclaration item: classes) {
			// parse class name
			np.parseClassName(item.getNameAsString(), nr);
			// parse field name and method name (need to implement)
			List<MethodDeclaration> methods = item.getMethods();
			List<FieldDeclaration> fields = item.getFields();
			np.parseFieldNames(fields, nr);
			np.parseMethodNames(methods, nr);
			
			// boolean to determine whether the whitespace inside a class follows google style
			boolean classWPSoFar = true;
			
			// Iterate through each method in each class
			for(int i = 0; i < methods.size(); i++) {
				MethodDeclaration member = methods.get(i);
				String name = member.getDeclarationAsString(true, true);
				String[] methodBody = ph.getMethodBody(name, linesOfFile, 0);

				if(i + 1 != methods.size()) {
					classWPSoFar &= wp.parseWhiteSpaceBetweenMethods(linesOfFile, methodBody.length, i, methods);
				}

				//System.out.println("\nParsing method:\t\t" + name);
				
				parseLineLengths(methodBody);
				braces.add(bp.parseMethodBraces(methodBody));	
				methodWPs.add(wp.parseWhiteSpace(methodBody));
				np.parseVariables(methodBody, nr);
			}
			classWhiteSpace.add(classWPSoFar);
		}
		// add each file to the summary class
		//returns JSONObject with details for files
		JSONObject results = jsonify.JSONify(repoURL, filePath, LOCMetrics);
		summary.addObject(results);
	}
	
	// @TODO: change this to lines of file
	public void parseLineLengths(String[] methodLines) {
		int transgressions = 0;
		for(String line: methodLines) {
			if(line.length() > 100) {
				transgressions++;
			}
		}
		linesExceeding.add(transgressions);
	}
	
	public void parsePkgDec(CompilationUnit cu) {
		Optional<PackageDeclaration> pkgDec = cu.getPackageDeclaration();
		
		try {
			String stringPkgDec = pkgDec.get().getNameAsString();
			packageDecName = (np.parseName(stringPkgDec, false, false, true) == 2) ? true : false;
		} catch(NoSuchElementException e) {
			packageDecName = true;
		}
	
	}
	
	public void wildCardPresent(CompilationUnit cu) {
		boolean result = true;
		NodeList<ImportDeclaration> imports = cu.getImports();
		for(ImportDeclaration each : imports) {
			result &= each.isAsterisk();
		}
		wildCard = !result;
	}
}
