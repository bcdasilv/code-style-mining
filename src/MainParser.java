import java.io.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

public class MainParser {
	public static final String gitURL = "https://github.com/janani-sridhar/CaesarCipher";
	public static final String directory = "/Users/jananisridhar/Desktop/CC";
	public static final String localFile = "./RandomFile.java";

	public static FileParser fp = new FileParser();
	
	public static void main(String[] args) {
		RepoTraversal tester = new RepoTraversal();
		tester.findJavaFilesToParse();
	}

	/*public static void getGitFiles(String url, String directory) {
		File gitFolder = new File(directory);
		Git git = null;

		if(!gitFolder.exists()) {
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

		for(File content : repo.getWorkTree().listFiles()) {
			classifyFiles(content);
		}
	}*/

	/*public static void classifyFiles(File f) {
		if(f.isFile()) {
			if(f.getName().contains("java")) {
				fp.parseFile(f.getAbsolutePath());
			}
		}
		else {
			for(File k : f.listFiles()) {
				classifyFiles(k);
			}
		}
	}
	
	public static void analyzeResults() {
		
	}*/
}
