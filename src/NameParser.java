import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class NameParser {
	
	private ArrayList<String> primitives;
	
	public NameParser() {
		String[] c = {"String", "int", "boolean", "char", "long", "float", "double"};
		this.primitives = new ArrayList<>(Arrays.asList(c));
	}
	
	public void parseFieldNames(List<FieldDeclaration> fields, NameResults nr) {
		
		String field;
		String[] words;
		ArrayList<String> wordList;
		int indx = 0;
		boolean constant;
		int result;
		
		for (int i = 0; i < fields.size(); i++) {
			field = fields.get(i).toString();
			words = field.split("\\s+");
			wordList = new ArrayList<>(Arrays.asList(words));
			constant = wordList.contains("final");
			
			// go through each word in the field declaration and find the index of the primitßive type
			for (int j = 0; j < wordList.size(); j++) {
				if (primitives.contains(wordList.get(i))) {
					indx = j;
					break;
				}
			}
			
			String var = wordList.get(indx + 1);
			if (constant) {
				result = parseName(var, true, false, false);
				if (result == 0) {
					nr.constants.add(false);
				} else {
					nr.constants.add(true);
				}
			} else {
				result = parseName(var, false, false, false);
				nr.fields.add(result);
			}
			
		}
		
	}
	
	public void parseMethodNames(List<MethodDeclaration> methods, NameResults nr) {
		
		String methodDec;
		String methodNameWithParens;
		String methodName;
		int indx;
		int result;
		
		for (int i = 0; i < methods.size(); i++) {
			methodDec = methods.get(i).getDeclarationAsString(false, false, false);
			methodNameWithParens = methodDec.split("\\s+")[1];
			indx = methodNameWithParens.indexOf('(');
			methodName = methodNameWithParens.substring(0, indx);
			result = parseName(methodName, false, false, false);
			nr.methods.add(result);
		}
	
	}
	
	public void parseVariables(String[] methodLines, NameResults nr) {
		
		String name = null;	
		for (int i = 0; i < methodLines.length; i++) {
			if (!methodLines[i].trim().equals("")) {
				name = methodLines[i].split("\\s+")[0];
			}
			
			if ((name != null) && (primitives.contains(name))) {
				String var = methodLines[i].split("\\s+")[1];
				int result = parseName(var, false, false, false);
				nr.variables.add(result);
				
			}
		}
		
	}
	
	public void parseClassName(String className, NameResults nr) {
		int result = parseName(className, false, true, false);
		if (result == 2) {
			nr.classes.add(true);
		} else {
			nr.classes.add(false);
		}
	}
	
	
	// return 0 if weird style (not compliant with google)
	// return 1 if lower_snake_case 
	// return 2 if google style
	public int parseName(String var, boolean constant, boolean isClass, boolean isPackage) {
		
		boolean isAlphaNumeric = true;
		boolean isAlphaBetic = true;
		boolean underscores = false;
		boolean firstLetterUpperCase = false;
		boolean allUpperCase = false;
		boolean allLowerCase = false;
		char ch = var.charAt(0);
		
		firstLetterUpperCase = (Character.isAlphabetic(ch) && Character.isUpperCase(ch));
		
		for (int i = 0; i < var.length(); i++) {
			ch = var.charAt(i);
			
			// checking to see whether characters other than '_' and '.' are not alphabetic
			if (!Character.isAlphabetic(ch) || (ch != '_') || (ch != '.')) {
				isAlphaBetic = false;
			}
			
			// checking to see whether characters other than '.' are letters or digits
			if (!(Character.isLetterOrDigit(ch) && (ch != '.'))) {
				isAlphaNumeric = false;
				if (ch == '_') {
					underscores = true;
				}
			}
			
			if (Character.isAlphabetic(ch)) {
				allUpperCase &= Character.isUpperCase(ch);
				allLowerCase &= Character.isLowerCase(ch);
			}
		}
		
		if (isPackage) {
			if (allLowerCase && isAlphaBetic) {
				return 2;
			}
		} else if (constant) {
			if (underscores && allUpperCase && isAlphaBetic) {
				return 2;
			}
		} else if (isClass) {
			if (firstLetterUpperCase && isAlphaNumeric) {
				return 2;
			}
		} else { // it's a local variable or class variable or method name
			if (underscores && isAlphaNumeric && allLowerCase) {
				return 1;
			} else if (isAlphaNumeric && !firstLetterUpperCase) {
				return 2;
			}
		}
		
		return 0;
		
	}
	
}
