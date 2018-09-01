import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ParsingHelper {

    public String[] readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        ArrayList<String> lines = new ArrayList<String>();
        String line = null;
        
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        
        bufferedReader.close();
        return lines.toArray(new String[lines.size()]);
    }
	
	public boolean onlyContains(String s, char c) {

		if (s.equals(""))
			return false;
		
		s = s.trim();
		
		if (!s.equals("") && (s.length() == 1)) {
			boolean value = s.charAt(0) == c;
			return value;
		}
		
		return false;
	}

	public String[] getNextBlock(String control, String[] lines, int index) {
		String s = lines[index];
		
		while (true) {
			if (s.trim().equals("")) {
				s = lines[++index];
			} else if (!s.trim().equals("") && s.contains(control)) {
				return getMethodBody(control, lines, index);
			} else {
				return null;
			}
		}
	}
	
	// created in case the sequence contains white spaces
	public boolean startsWith(String line, String seq) {

		if (line.equals("") || line.trim().equals(""))
			return false;
		
		int j = 0;
		int k = 0;
		
		while (Character.isWhitespace(line.charAt(j))) {
			j++;
		}
		
		for (k = 0; k < seq.length(); k++) {
			if (line.charAt(j + k) != seq.charAt(k)) {
				return false;
			}
		}
	
		if (line.charAt(j + k) == ' ')
			return true;
		
		return false;
	}
	
	public String[] getMethodBody(String name, String[] classLines, int fromLine) {
		
		ArrayList<String> methodLines = new ArrayList<String>();
		int startingLine = 0;
		int numOpenBraces = 0;
		int numClosedBraces = 0;
		int i;
		
		for (i = fromLine; i < classLines.length; i++) {
			String line = classLines[i];
			
			if (line.contains(name)) {
				startingLine = i;
				break;
			}
				
		}
		
		
		for (i = startingLine; i < classLines.length; i++) {
			
			String line = classLines[i];
			methodLines.add(line);
			
			numOpenBraces += countMatches(line, '{');
			numClosedBraces += countMatches(line, '}');
						
			if (numClosedBraces != 0 && (numClosedBraces == numOpenBraces)) {
				break;
			}
						
		}
		
		return methodLines.toArray(new String[methodLines.size()]);
			
	}

	public int countMatches(String s, char c) {
		int matches = 0;
		if (s.equals(""))
			return 0;

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c)
				matches++;
		}
		
		return matches;
	}

}
