import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class was created to provide a summary of the whole project.
 * The idea is that it will loop through a local file that has all the JSON objects of each file from the repo.
 * It'll iterate through this file and create a summary JSON object.
 */
public class JSONifySummary {

    ArrayList<JSONObject> fileJSONObjects = new ArrayList<>();
    private JSONObject finalSummary;
    private Integer methodsVariableOther;
    private Integer methodsVariableLowerSnake;
    private Integer methodsVariablesGoogle;
    private Integer methodsIndentsTabs;
    private Integer methodsIndentsSpaces;
    private Integer methodsIndentsMinIndent;
    private Integer methodsIndentsAvgIndent;
    private Integer methodsIndentsMaxIndent;
    private Integer methodsLineLength;
    private Integer methodsNamingOther;
    private Integer methodsNamingLowerSnake;
    private Integer methodsNamingGoogle;
    private Integer methodsCurlyBracesSingleOther;
    private Integer methodsCurlyBracesSingleAllman;
    private Integer methodsCurlyBracesSingleExKr;
    private Integer methodsCurlyBracesSingleGoogle;
    private Integer methodsCurlyBracesMultipleOther;
    private Integer methodsCurlyBracesMultipleAllman;
    private Integer methodsCurlyBracesMultipleExKr;
    private Integer methodsCurlyBracesMultipleGoogle;
    private Integer namingOther;
    private Integer namingGoogle;
    private Integer constantsNamingOther;
    private Integer constantsNamingGoogle;
    private Integer fieldsNamingOther;
    private Integer fieldsNamingLowerSnake;
    private Integer fieldsNamingGoogle;
    private Integer blankLinesFailed;
    private Integer blankLinesPassed;
    private String repoUrl;
    private Integer LOCSourceCode;
    private Integer LOCSingleComments;
    private Integer LOCMultComments;
    private Integer LOCTotalComments;
    private Integer LOCBlankLines;
    private Integer LOCTotalLines;
    private Integer totalFiles;
    private Integer totalJavaFiles;
    private Integer totalImportWilcards;
    private Integer indentError;
    private JSONObject individual_summary;
    int count;

    public JSONifySummary(Long totalFiles, Long totalJavaFiles) {
        finalSummary = new JSONObject();
        this.totalFiles = totalFiles.intValue();
        this.totalJavaFiles = totalJavaFiles.intValue();
        this.indentError = 0;
        this.methodsVariableOther = 0;
        this.methodsVariableLowerSnake = 0;
        this.methodsVariablesGoogle = 0;
        this.methodsIndentsTabs = 0;
        this.methodsIndentsSpaces = 0;
        this.methodsIndentsMinIndent = 0;
        this.methodsIndentsAvgIndent = 0;
        this.methodsIndentsMaxIndent = 0;
        this.methodsLineLength = 0;
        this.methodsNamingOther = 0;
        this.methodsNamingLowerSnake = 0;
        this.methodsNamingGoogle = 0;
        this.methodsCurlyBracesSingleOther = 0;
        this.methodsCurlyBracesSingleAllman = 0;
        this.methodsCurlyBracesSingleExKr = 0;
        this.methodsCurlyBracesSingleGoogle = 0;
        this.methodsCurlyBracesMultipleOther = 0;
        this.methodsCurlyBracesMultipleAllman = 0;
        this.methodsCurlyBracesMultipleExKr = 0;
        this.methodsCurlyBracesMultipleGoogle = 0;
        this.namingOther = 0;
        this.namingGoogle = 0;
        this.constantsNamingOther = 0;
        this.constantsNamingGoogle = 0;
        this.fieldsNamingOther = 0;
        this.fieldsNamingLowerSnake = 0;
        this.fieldsNamingGoogle = 0;
        this.blankLinesFailed = 0;
        this.blankLinesPassed = 0;
        this.repoUrl = "";
        this.LOCSourceCode = 0;
        this.LOCSingleComments = 0;
        this.LOCMultComments = 0;
        this.LOCTotalComments = 0;
        this.LOCBlankLines = 0;
        this.LOCTotalLines = 0;
        this.totalImportWilcards = 0;
        this.individual_summary = new JSONObject();
        this.count = 0;
    }

    public void addObject(JSONObject o) {
        fileJSONObjects.add(o);
        addToFinalSummary(o);
        try {
            individual_summary.put(String.valueOf(count++), o);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addLOCMetrics(Map<String, Integer> LOCMetrics) {
        for (Map.Entry mapElement : LOCMetrics.entrySet()) {
            String key = (String)mapElement.getKey();
            int value = (Integer)mapElement.getValue();
            switch(key) {
                case "LOCSourceCode":
                    LOCSourceCode += value;
                    break;
                case "LOCSingleComments":
                    LOCSingleComments += value;
                    break;
                case "LOCMultComments":
                    LOCMultComments += value;
                    break;
                case "LOCTotalComments":
                    LOCTotalComments += value;
                    break;
                case "LOCBlankLines":
                    LOCBlankLines += value;
                    break;
                case "LOCTotalLines":
                    LOCTotalLines += value;
                    break;
            }
        }
    }

    public JSONObject getRepoErrorSummary() throws JSONException {
        TotalCategoryErrors generator = new TotalCategoryErrors(this);
        JSONObject reposummary = generator.createErrorSummary(individual_summary);
        //reposummary.put("individual_analysis", individual_summary);
        return reposummary;
    }

    public void addToFinalSummary(JSONObject o) {
        try {
            //count indent errors
            if (getMethodsIndentsTabs(o) > 0) {
                indentError++;
            }
            //count imports
            totalImportWilcards += addImportsWildcards(o);
            //add blank lines = failure if there is a blank line
            getBlankLines(o);
            //add method variables naming results
            repoUrl = getRepoUrl(o);
            methodsVariableLowerSnake += getMethodsVariablesNamingLowerSnake(o);
            methodsVariableOther += getMethodsVariablesNamingOther(o);
            methodsVariablesGoogle += getMethodsVariablesNamingGoogle(o);
            //add method indentation results
            methodsIndentsTabs += getMethodsIndentsTabs(o);
            methodsIndentsSpaces += getMethodsIndentsSpaces(o);
            methodsIndentsMinIndent += getMethodsIndentsMinIndent(o);
            methodsIndentsAvgIndent += getMethodsIndentsAvgIndent(o);
            methodsIndentsMaxIndent += getMethodsIndentsMaxIndent(o);
            //add line length results
            methodsLineLength += getMethodsLineLengthViolations(o);
            //add methods naming results
            methodsNamingOther += getMethodsNamingOther(o);
            methodsNamingLowerSnake += getMethodsNamingLowerSnake(o);
            methodsNamingGoogle += getMethodsNamingGoogle(o);
            //add curly braces results
            methodsCurlyBracesSingleOther += getMethodsCurlyBracesSingleOther(o);
            methodsCurlyBracesSingleAllman += getMethodsCurlyBracesSingleAllman(o);
            methodsCurlyBracesSingleExKr += getMethodsCurlyBracesSingleExKr(o);
            methodsCurlyBracesSingleGoogle += getMethodsCurlyBracesSingleGoogle(o);
            methodsCurlyBracesMultipleOther += getMethodsCurlyBracesMultipleOther(o);
            methodsCurlyBracesMultipleAllman += getMethodsCurlyBracesMultipleAllman(o);
            methodsCurlyBracesMultipleExKr += getMethodsCurlyBracesMultipleExKr(o);
            methodsCurlyBracesMultipleGoogle += getMethodsCurlyBracesMultipleGoogle(o);
            //add file naming results
            namingOther += getNamingOther(o);
            namingGoogle += getNamingGoogle(o);
            //add constants naming results
            constantsNamingOther += getConstantsNamingOther(o);
            constantsNamingGoogle += getConstantsNamingGoogle(o);
            //add method fields naming results
            fieldsNamingOther += getFieldsNamingOther(o);
            fieldsNamingLowerSnake += getFieldsNamingLowerSnake(o);
            fieldsNamingGoogle += getFieldsNamingGoogle(o);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject writeResults() {
        try {
            finalSummary.put("methodsVariableOther", methodsVariableOther);
            finalSummary.put("methodsVariableLowerSnake", methodsVariableLowerSnake);
            finalSummary.put("methodsVariablesGoogle", methodsVariablesGoogle);
            finalSummary.put("methodsIndentsTabs", methodsIndentsTabs);
            finalSummary.put("methodsIndentsSpaces", methodsIndentsSpaces);
            finalSummary.put("methodsIndentsMinIndent", methodsIndentsMinIndent);
            finalSummary.put("methodsIndentsAvgIndent", methodsIndentsAvgIndent);
            finalSummary.put("methodsIndentsMaxIndent", methodsIndentsMaxIndent);
            finalSummary.put("methodsLineLength", methodsLineLength);
            finalSummary.put("methodsNamingOther", methodsNamingOther);
            finalSummary.put("methodsNamingLowerSnake", methodsNamingLowerSnake);
            finalSummary.put("methodsNamingGoogle", methodsNamingGoogle);
            finalSummary.put("methodsCurlyBracesSingleOther", methodsCurlyBracesSingleOther);
            finalSummary.put("methodsCurlyBracesSingleAllman", methodsCurlyBracesSingleAllman);
            finalSummary.put("methodsCurlyBracesSingleExKr", methodsCurlyBracesSingleExKr);
            finalSummary.put("methodsCurlyBracesSingleGoogle", methodsCurlyBracesSingleGoogle);
            finalSummary.put("methodsCurlyBracesMultipleOther", methodsCurlyBracesMultipleOther);
            finalSummary.put("methodsCurlyBracesMultipleAllman", methodsCurlyBracesMultipleAllman);
            finalSummary.put("methodsCurlyBracesMultipleExKr", methodsCurlyBracesMultipleExKr);
            finalSummary.put("methodsCurlyBracesMultipleGoogle", methodsCurlyBracesMultipleGoogle);
            finalSummary.put("namingOther", namingOther);
            finalSummary.put("namingGoogle", namingGoogle);
            finalSummary.put("constantsNamingOther", constantsNamingOther);
            finalSummary.put("constantsNamingGoogle", constantsNamingGoogle);
            finalSummary.put("fieldsNamingOther", fieldsNamingOther);
            finalSummary.put("fieldsNamingLowerSnake", fieldsNamingLowerSnake);
            finalSummary.put("fieldsNamingGoogle", fieldsNamingGoogle);
            finalSummary.put("repoUrl", repoUrl);
            finalSummary.put("LOCSourceCode", LOCSourceCode);
            finalSummary.put("LOCSingleComments", LOCSingleComments);
            finalSummary.put("LOCMultComments", LOCMultComments);
            finalSummary.put("LOCTotalComments", LOCTotalComments);
            finalSummary.put("LOCBlankLines", LOCBlankLines);
            finalSummary.put("LOCTotalLines", LOCTotalLines);
            finalSummary.put("totalFiles", totalFiles);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalSummary;
    }

    private Integer addImportsWildcards(JSONObject o) throws JSONException {
        String result = o.get("import").toString();
        if (result.equals("true"))
            return 1;
        return 0;
    }

    private String getRepoUrl(JSONObject o) throws JSONException {
        return o.get("repoURL").toString();
    }
    private JSONObject getClassJSONObject(JSONObject o) throws JSONException {
        return o.getJSONObject("class");
    }

    private JSONObject getMethodsJSONObject(JSONObject o) throws JSONException {
        return getClassJSONObject(o).getJSONObject("methods");
    }

    private JSONObject getMethodsVariableNamingJSONObject(JSONObject o) throws JSONException {
        return getMethodsJSONObject(o).getJSONObject("variables").getJSONObject("naming");
    }

    private Integer getMethodsVariablesNamingOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsVariableNamingJSONObject(o).get("other").toString());
    }

    private Integer getMethodsVariablesNamingLowerSnake(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsVariableNamingJSONObject(o).get("lower_snake_case").toString());
    }

    private Integer getMethodsVariablesNamingGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsVariableNamingJSONObject(o).get("google").toString());
    }

    private Integer getMethodsIndentsTabs(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsIndentsJSONObject(o).get("tabs").toString());
    }

    private Integer getMethodsIndentsSpaces(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsIndentsJSONObject(o).get("spaces").toString());
    }

    private Integer getMethodsIndentsMinIndent(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsIndentsJSONObject(o).get("min_indent").toString());
    }

    private Integer getMethodsIndentsAvgIndent(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsIndentsJSONObject(o).get("avg_indent").toString());
    }

    private Integer getMethodsIndentsMaxIndent(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsIndentsJSONObject(o).get("max_indent").toString());
    }

    private Integer getMethodsLineLengthViolations(JSONObject o) throws JSONException {
        JSONArray line_length = getMethodsJSONObject(o).getJSONArray("line_length");
        Integer totalViolations = 0;
        for (int i = 0; i < line_length.length(); i++) {
            totalViolations += line_length.getInt(i);
        }
        return totalViolations;
    }

    private JSONObject getMethodsNamingJSONObject(JSONObject o) throws JSONException {
        return getMethodsJSONObject(o).getJSONObject("naming");
    }

    private Integer getMethodsNamingOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsNamingJSONObject(o).get("other").toString());
    }

    private Integer getMethodsNamingLowerSnake(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsNamingJSONObject(o).get("lower_snake_case").toString());
    }

    private Integer getMethodsNamingGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsNamingJSONObject(o).get("google").toString());
    }

    private JSONObject getMethodsCurlyBracesJSONObject(JSONObject o) throws JSONException {
        return getMethodsJSONObject(o).getJSONObject("curly_braces");
    }

    private JSONObject getMethodsCurlyBracesSingle(JSONObject o) throws JSONException {
        return getMethodsCurlyBracesJSONObject(o).getJSONObject("single");
    }

    private JSONObject getMethodsCurlyBracesMultiple(JSONObject o) throws JSONException {
        return getMethodsCurlyBracesJSONObject(o).getJSONObject("multiple");
    }

    private Integer getMethodsCurlyBracesSingleOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesSingle(o).get("other").toString());
    }

    private Integer getMethodsCurlyBracesSingleAllman(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesSingle(o).get("allman").toString());
    }

    private Integer getMethodsCurlyBracesSingleExKr(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesSingle(o).get("ex_kr").toString());
    }

    private Integer getMethodsCurlyBracesSingleGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesSingle(o).get("google").toString());
    }

    private Integer getMethodsCurlyBracesMultipleOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesMultiple(o).get("other").toString());
    }

    private Integer getMethodsCurlyBracesMultipleAllman(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesMultiple(o).get("allman").toString());
    }

    private Integer getMethodsCurlyBracesMultipleExKr(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesMultiple(o).get("ex_kr").toString());
    }

    private Integer getMethodsCurlyBracesMultipleGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getMethodsCurlyBracesMultiple(o).get("google").toString());
    }

    private JSONObject getMethodsIndentsJSONObject(JSONObject o) throws JSONException {
        return getMethodsJSONObject(o).getJSONObject("indents");
    }

    private void getBlankLines(JSONObject o) throws JSONException {
        String s = getClassJSONObject(o).get("blank_lines").toString();
        if (s.equals("true"))
            blankLinesFailed++;
        else
            blankLinesPassed++;
    }

    private JSONObject getNamingJSONObject(JSONObject o) throws JSONException {
        return getClassJSONObject(o).getJSONObject("naming");
    }

    private Integer getNamingOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getNamingJSONObject(o).get("other").toString());
    }

    private Integer getNamingGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getNamingJSONObject(o).get("google").toString());
    }

    private JSONObject getConstantsNamingJSONObject(JSONObject o) throws JSONException {
        return getClassJSONObject(o).getJSONObject("constants").getJSONObject("naming");
    }

    private Integer getConstantsNamingOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getConstantsNamingJSONObject(o).get("other").toString());
    }

    private Integer getConstantsNamingGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getConstantsNamingJSONObject(o).get("google").toString());
    }

    private JSONObject getFieldsNamingJSONObject(JSONObject o) throws JSONException {
        return getClassJSONObject(o).getJSONObject("fields").getJSONObject("naming");
    }

    private Integer getFieldsNamingOther(JSONObject o) throws JSONException {
        return Integer.parseInt(getFieldsNamingJSONObject(o).get("other").toString());
    }

    private Integer getFieldsNamingLowerSnake(JSONObject o) throws JSONException {
        return Integer.parseInt(getFieldsNamingJSONObject(o).get("lower_snake_case").toString());
    }

    private Integer getFieldsNamingGoogle(JSONObject o) throws JSONException {
        return Integer.parseInt(getFieldsNamingJSONObject(o).get("google").toString());
    }

    public Integer getMethodsVariableOther() {
        return methodsVariableOther;
    }

    public Integer getMethodsVariableLowerSnake() {
        return methodsVariableLowerSnake;
    }

    public Integer getMethodsVariablesGoogle() {
        return methodsVariablesGoogle;
    }

    public Integer getMethodsIndentsTabs() {
        return methodsIndentsTabs;
    }

    public Integer getMethodsIndentsSpaces() {
        return methodsIndentsSpaces;
    }

    public Integer getMethodsIndentsMinIndent() {
        return methodsIndentsMinIndent;
    }

    public Integer getMethodsIndentsAvgIndent() {
        return methodsIndentsAvgIndent;
    }

    public Integer getMethodsIndentsMaxIndent() {
        return methodsIndentsMaxIndent;
    }

    public Integer getMethodsLineLength() {
        return methodsLineLength;
    }

    public Integer getMethodsNamingOther() {
        return methodsNamingOther;
    }

    public Integer getMethodsNamingLowerSnake() {
        return methodsNamingLowerSnake;
    }

    public Integer getMethodsNamingGoogle() {
        return methodsNamingGoogle;
    }

    public Integer getMethodsCurlyBracesSingleOther() {
        return methodsCurlyBracesSingleOther;
    }

    public Integer getMethodsCurlyBracesSingleAllman() {
        return methodsCurlyBracesSingleAllman;
    }

    public Integer getMethodsCurlyBracesSingleExKr() {
        return methodsCurlyBracesSingleExKr;
    }

    public Integer getMethodsCurlyBracesSingleGoogle() {
        return methodsCurlyBracesSingleGoogle;
    }

    public Integer getMethodsCurlyBracesMultipleOther() {
        return methodsCurlyBracesMultipleOther;
    }

    public Integer getMethodsCurlyBracesMultipleAllman() {
        return methodsCurlyBracesMultipleAllman;
    }

    public Integer getMethodsCurlyBracesMultipleExKr() {
        return methodsCurlyBracesMultipleExKr;
    }

    public Integer getMethodsCurlyBracesMultipleGoogle() {
        return methodsCurlyBracesMultipleGoogle;
    }

    public Integer getNamingOther() {
        return namingOther;
    }

    public Integer getNamingGoogle() {
        return namingGoogle;
    }

    public Integer getConstantsNamingOther() {
        return constantsNamingOther;
    }

    public Integer getConstantsNamingGoogle() {
        return constantsNamingGoogle;
    }

    public Integer getFieldsNamingOther() {
        return fieldsNamingOther;
    }

    public Integer getFieldsNamingLowerSnake() {
        return fieldsNamingLowerSnake;
    }

    public Integer getFieldsNamingGoogle() {
        return fieldsNamingGoogle;
    }

    public Integer getBlankLinesFailed() {
        return blankLinesFailed;
    }

    public Integer getBlankLinesPassed() {
        return blankLinesPassed;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public Integer getTotalFiles() {
        return totalFiles;
    }

    public Integer getTotalJavaFiles() {
        return totalJavaFiles;
    }

    public Integer getTotalImportWilcards() {
        return totalImportWilcards;
    }

    public Integer getIndentError() {
        return indentError;
    }

    public JSONObject getFinalSummary() {
        return finalSummary;
    }
}
