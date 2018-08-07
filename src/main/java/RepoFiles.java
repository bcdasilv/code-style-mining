import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

public class RepoFiles {

    public static void main(String[] args) throws IOException, GitAPIException {


        // TODO: read & clean this up later. temporary solution to get files to practice parsing
        // TODO NOTE: must include .git at end of URL. breaks otherwise. why?
        //Git git = Git.cloneRepository().setURI("https://github.com/django/django.git").call();
        //Git git = Git.cloneRepository().setURI( "https://github.com/zotroneneis/harry_potter_universe.git").call();
        //System.out.println("Finished cloning.");


        Git git = Git.open(new File("/Users/Kellie/Documents/Github/code-style-mining/django"));
        Repository repo = git.getRepository();

        System.out.println("Branch: " + repo.getBranch());

        System.out.println("DirPath: " + repo.getDirectory().getPath());

        System.out.println("WorkTree: " + repo.getWorkTree());
        File contents[] = repo.getWorkTree().listFiles();
        for (int i = 0; i < contents.length; i++) {
            String string = contents[i].getName();
            System.out.println("File: " + string);

        }

    }
}
