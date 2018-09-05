import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class FileParser {

	// Parser objects -- for calling methods in other classes
	private static ParsingHelper ph = new ParsingHelper();
	private static BraceParser bp = new BraceParser();
	private static NameParser np = new NameParser();
	private static WhiteSpaceParser wp = new WhiteSpaceParser();

	// result objects. If it is a collection, it is for each method, and if it is a boolean, then it is an element pertaining to the file.
	// the one exception is the NameResults object -- it holds name result information for all the classes, members inside classes, and local variables inside methods.
	// there is only one NameResults object for each file.
	
	protected boolean wildCard;
	protected boolean packageDecName;
	protected ArrayList<Boolean> classWhiteSpace = new ArrayList<Boolean>();
	protected ArrayList<BraceResults> braces = new ArrayList<BraceResults>();
	protected ArrayList<Integer> linesExceeding = new ArrayList<Integer>();
	protected ArrayList<MethodWhiteSpaceResults> methodWPs = new ArrayList<MethodWhiteSpaceResults>();
	protected NameResults nr = new NameResults();
	
	public void parseFile(String f) {

		FileInputStream in = null;
		String[] linesOfFile = null;
		
		try {
			in = new FileInputStream(new File(f));
			linesOfFile = ph.readLines(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CompilationUnit cu = JavaParser.parse(in);

		// look for asterisks in import statements and parse package declaration
		wildCardPresent(cu);
		parsePkgDec(cu);
		
		List <ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);

		for (ClassOrInterfaceDeclaration item: classes) {

			// parse class name
			np.parseClassName(item.getNameAsString(), nr);
			
			// parse field name and method name (need to implement)
			List<MethodDeclaration> methods = item.getMethods();
			List<FieldDeclaration> fields = item.getFields();			
			np.parseFieldNames(fields, nr);
			np.parseMethodNames(methods, nr);
			
			// boolean to determine whether the whitespace inside a class follows google style
			boolean classWPSoFar = true;
			
			// Iterate through each method in each class.
			for (int i = 0; i < methods.size(); i++) {

				MethodDeclaration member = methods.get(i);
				String name = member.getDeclarationAsString(true, true);
				String[] methodBody = ph.getMethodBody(name, linesOfFile, 0);

				if (i + 1 != methods.size()) {
					classWPSoFar &= wp.parseWhiteSpaceBetweenMethods(linesOfFile, methodBody.length, i, methods);
				}
				
				System.out.printf("\nParsing method %s -----------------------------\n ", name);
				
				parseLineLengths(methodBody);
				braces.add(bp.parseMethodBraces(methodBody));	
				methodWPs.add(wp.parseWhiteSpace(methodBody));
				
				np.parseVariables(methodBody, nr);
			}
			
			classWhiteSpace.add(classWPSoFar);

		}

	}
	
	// @TODO: change this to lines of file
	public void parseLineLengths(String[] methodLines) {
		int transgressions = 0;
		for (String line: methodLines) {
			if (line.length() > 100) {
				transgressions++;
			}
		}
		
		linesExceeding.add(transgressions);
	}
	
	public JSONObject JSONify() {
		
		JSONObject file = new JSONObject();
		JSONObject classJSON = new JSONObject();
		
		// file-specific attributes
		file.put("import", wildCard);
		file.put("package", packageDecName);
		
		// class attributes
		JSONifyClassNames(classJSON);
		JSONifyBlankLines(classJSON);
		JSONifyConstants(classJSON);
		JSONifyFields(classJSON, false, false);
		JSONifyMethods(classJSON);
		
		// put class into file
		file.put("class", classJSON);
		
		return file;
		
	}
	
	public void JSONifyClassNames(JSONObject classJSON) {
		int google = 0;
		int other = 0;
		JSONObject classNames = new JSONObject();
		
		for (boolean classNameStyle : nr.classes) {
			if (classNameStyle) {
				google++;
			} else {
				other++;
			}
		} 

		classNames.put("google", google);
		classNames.put("other", other);
		classJSON.put("naming", classNames);
	
	}
	
	public void JSONifyBlankLines(JSONObject classObject) {
		boolean result = true;
		for (boolean bool : classWhiteSpace) {
			result &= bool;
		}
		classObject.put("blank_lines", result);
	}
	
	public void JSONifyConstants(JSONObject classObject) {
		int google = 0;
		int other = 0;
		JSONObject constants = new JSONObject();
		JSONObject names = new JSONObject();
		
		for (boolean constant : nr.constants) {
			if (constant) {
				google++;
			} else {
				other++;
			}
		}
		
		names.put("google", google);
		names.put("other", other);
		constants.put("naming", names);
		classObject.put("constants", constants);
		
	}
	
	public void JSONifyFields(JSONObject parentObject, boolean local, boolean method) {
		int google = 0;
		int other = 0;
		int lowerSnake = 0;
		ArrayList<Integer> styles;
		JSONObject attribute = new JSONObject();
		JSONObject names = new JSONObject();
		
		if (local) {
			styles = nr.variables;
		} else if (method) {
			styles = nr.methods;
		} else {
			styles = nr.fields;
		}
		
		for (int field : styles) {
			if (field == 2) {
				google++;
			} else if (field == 1){
				lowerSnake++;
			} else {
				other++;
			}
		}
		
		names.put("google", google);
		names.put("lower_snake_case", lowerSnake);
		names.put("other", other);
		
		if (local) {
			attribute.put("naming", names);
			parentObject.put("variables", attribute);
		} else if (method) {
			parentObject.put("naming", names);
		} else {
			attribute.put("naming", names);
			parentObject.put("fields", attribute);
		}
	}

	public void JSONifyIndents(JSONObject methodObject) {
		JSONObject indentObject = new JSONObject();
		int tabs = 0, spaces = 0, avgIndent = 0;
		int minIndent = Integer.MAX_VALUE;
		int maxIndent = Integer.MIN_VALUE;
		
		for (MethodWhiteSpaceResults mwp : methodWPs) {
			tabs += mwp.numTabOccurrences;
			spaces += mwp.numSpaceOccurrences;
			
			// minIndent and maxIndent
			minIndent = Math.min(minIndent, mwp.minIndent);
			maxIndent = Math.max(maxIndent, mwp.maxIndent);
			avgIndent += mwp.averageIndent;		
		}
		
		if (methodWPs.size() > 0) {
			avgIndent /= methodWPs.size();
		}
		
		// start putting attributes to indentObject
		indentObject.put("tabs", tabs)
			.put("spaces", spaces)
			.put("min_indent", minIndent)
			.put("max_indent", maxIndent)
			.put("avg_indent", avgIndent);
		
		methodObject.put("indents", indentObject);
		
	}
	
	public void JSONifyMethods(JSONObject classObject) {
		
		JSONObject methodObject = new JSONObject();
		
		JSONifyFields(methodObject, false, true);	// add method names
		JSONifyIndents(methodObject); // indents
		
		methodObject.put("line_length", linesExceeding); // # of lines exceeding 
		JSONifyFields(methodObject, true, false); // add local variables
		JSONifyBraces(methodObject);
		
		classObject.put("methods", methodObject);
			
	}
	
	public void JSONifyBraces(JSONObject methodObject) {
		JSONObject braceObject = new JSONObject();
		JSONObject single = new JSONObject();
		JSONObject multiple = new JSONObject();
		
		int sGoogle = 0, sAllman = 0, sEx_kr = 0, sOther = 0;
		int mGoogle = 0, mAllman = 0, mEx_kr = 0, mOther = 0;
		
		for (BraceResults br : braces) {
			sGoogle += br.SINGLE_GOOGLE;
			sAllman += br.SINGLE_ALLMAN;
			sEx_kr += br.SINGLE_EX_KR;
			sOther += br.SINGLE_WEIRD;
			
			mGoogle += br.MUL_GOOGLE;
			mAllman += br.MUL_ALLMAN;
			mEx_kr += br.MUL_EX_KR;
			mOther += br.MUL_WEIRD;
		}
		
		single.put("google", sGoogle)
			.put("allman", sAllman)
			.put("ex_kr", sEx_kr)
			.put("other", sOther);
		
		multiple.put("google", mGoogle)
			.put("allman", mAllman)
			.put("ex_kr", mEx_kr)
			.put("other", mOther);
		
		braceObject.put("single", single)
			.put("multiple", multiple);
	
		methodObject.put("curly_braces", braceObject);
		
	}
	
	public void parsePkgDec(CompilationUnit cu) {
		Optional<PackageDeclaration> pkgDec = cu.getPackageDeclaration();
		
		try {
			String stringPkgDec = pkgDec.get().getNameAsString();
			packageDecName = (np.parseName(stringPkgDec, false, false, true) == 2) ? true : false;
		} catch (NoSuchElementException e) {
			packageDecName = true;
		}
	
	}
	
	public void wildCardPresent(CompilationUnit cu) {
		boolean result = true;
		NodeList<ImportDeclaration> imports = cu.getImports();
		for (ImportDeclaration each : imports) {
			result &= each.isAsterisk();
		}
		wildCard = !result;
	}
	
}
