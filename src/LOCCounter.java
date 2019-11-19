/*Inspired by our python version https://github.com/BryceV/python_loc_counter */
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LOCCounter {
    private String fileName;

    private int LOCSingleComments = 0;
    private int LOCMultComments = 0;
    private int LOCTotalComments = 0;
    private int LOCSourceCode = 0;
    private int LOCBlankLines = 0;
    private int LOCTotalLines = 0;

    public LOCCounter(String fileName) throws IOException {
        this.fileName = fileName;
        calcLOC();
    }

    public int getSingleCommentsLOC() {
        return LOCSingleComments;
    }

    public int getMultCommentsLOC() {
        return LOCMultComments;
    }

    public int getTotalCommentsLOC() {
        return LOCTotalComments;
    }

    public int getSourceLOC() {
        return LOCSourceCode;
    }

    public int getBlankLinesLOC() {
        return LOCBlankLines;
    }

    public int getTotalLinesLOC() {
        return LOCTotalLines;
    }

    public Map<String, Integer> getLOC(){
        //Return hash map or the JSON object we are using
        Map<String, Integer> LOCMetrics = new HashMap<>();
        LOCMetrics.put("LOCSourceCode", LOCSourceCode);
        LOCMetrics.put("LOCSingleComments", LOCSingleComments);
        LOCMetrics.put("LOCMultComments", LOCMultComments);
        LOCMetrics.put("LOCTotalComments", LOCTotalComments);
        LOCMetrics.put("LOCBlankLines", LOCBlankLines);
        LOCMetrics.put("LOCTotalLines", LOCTotalLines);

        return LOCMetrics;
    }

    private void calcLOC() throws IOException {
        File f = new File(fileName);
        Scanner sc = new Scanner(f);

        String currLine;

        boolean inComment = false;

        while (sc.hasNext()) {
            //boolean codeLine = true;
            currLine = sc.nextLine();

            LOCTotalLines++;

            if (currLine.trim().isEmpty()) {
                LOCBlankLines++;
                continue;
            }

            //Remove quotes for some comment analysis
            String noQuotes = currLine.replaceAll("((\"|').*?\\2)", "");
            //System.out.println(noQuotes)

            //set if we are in a mult comment
            if (((noQuotes.contains("/*")) && !(noQuotes.contains("*/"))) ||
                    ((noQuotes.contains("*/")) && !(noQuotes.contains("/*")))) {
                inComment = !inComment;
            }

            if (noQuotes.contains("//") && !inComment) {
                LOCSingleComments++;
            }

            if (noQuotes.contains("/*") || noQuotes.contains("*/") || inComment) {
                LOCMultComments++;
            }

            String removeComments = currLine.replaceAll("(\\/\\*).*(\\*\\/)|(\\/\\/).*", "");
            String removeUnmatchedComments = removeComments.replaceAll("(\\/\\*).*|.*(\\*\\/)", "");
            if (!(removeUnmatchedComments.trim().isEmpty()) && !(inComment)) {
                LOCSourceCode++;
            }

            LOCTotalComments = LOCMultComments + LOCSingleComments;
        }
    }

    public void printLOC(){
        System.out.println("Source Lines: " + LOCSourceCode);
        System.out.println("Single Comment Lines: " + LOCSingleComments);
        System.out.println("Mult Comment Lines: " + LOCMultComments);
        System.out.println("Total Comment Lines: " + LOCTotalComments);
        System.out.println("Blank Lines: " + LOCBlankLines);
        System.out.println("Total Lines: " + LOCTotalLines);
    }
}