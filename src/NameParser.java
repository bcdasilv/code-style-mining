import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.body.FieldDeclaration;

public class NameParser {
	
	public void parseFieldNames(List<FieldDeclaration> fields) {
		
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
	
	public void parseVariables(String[] methodLines) {
		
		String[] c = {"String", "int", "boolean", "char", "long", "float", "double"};
		ArrayList<String> primitives = new ArrayList<>(Arrays.asList(c));
		int underscores = 0;
		int camelCase = 0;
		String name = null;
		
		for (int i = 0; i < methodLines.length; i++) {
			if (!methodLines[i].trim().equals("")) {
				name = methodLines[i].split("\\s+")[0];
			}
			
			if ((name != null) && (primitives.contains(name))) {
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
	public boolean parseName(String var) {
		boolean underscores = false;
		
		if (var.contains("_")) {
			underscores = true;
		}
		
		return underscores;
	}
	
}
