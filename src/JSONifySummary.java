import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class was created to provide a summary of the whole project.
 * The idea is that it will loop through a local file that has all the JSON objects of each file from the repo.
 * It'll iterate through this file and create a summary JSON object.
 */
public class JSONifySummary {
    ArrayList<JSONObject> fileJSONObjects = new ArrayList<>();
    public JSONObject finalSummary;
    //Integer blankLines = new Integer(0);
    Integer methodsVariableOther = new Integer(0);
    Integer methodsVariableLowerSnake = new Integer(0);
    Integer methodsVariablesGoogle = new Integer(0);
    Integer methodsIndentsTabs = new Integer(0);
    Integer methodsIndentsSpaces = new Integer(0);
    Integer methodsIndentsMinIndent = new Integer(0);
    Integer methodsIndentsAvgIndent = new Integer(0);
    Integer methodsIndentsMaxIndent = new Integer(0);
    Integer methodsLineLength = new Integer(0);
    Integer methodsNamingOther = new Integer(0);
    Integer methodsNamingLowerSnake = new Integer(0);
    Integer methodsNamingGoogle = new Integer(0);
    Integer methodsCurlyBracesSingleOther = new Integer(0);
    Integer methodsCurlyBracesSingleAllman = new Integer(0);
    Integer methodsCurlyBracesSingleExKr = new Integer(0);
    Integer methodsCurlyBracesSingleGoogle = new Integer(0);
    Integer methodsCurlyBracesMultipleOther = new Integer(0);
    Integer methodsCurlyBracesMultipleAllman = new Integer(0);
    Integer methodsCurlyBracesMultipleExKr = new Integer(0);
    Integer methodsCurlyBracesMultipleGoogle = new Integer(0);
    Integer namingOther = new Integer(0);
    Integer namingGoogle = new Integer(0);
    Integer constantsNamingOther = new Integer(0);
    Integer constantsNamingGoogle = new Integer(0);
    Integer fieldsNamingOther = new Integer(0);
    Integer fieldsNamingLowerSnake = new Integer(0);
    Integer fieldsNamingGoogle = new Integer(0);
    String repoUrl;
    Integer linesOfCode = new Integer(0);
    Integer totalFiles = new Integer(0);

    public JSONifySummary() {
        finalSummary = new JSONObject();
    }

    public void addObject(JSONObject o){
            fileJSONObjects.add(o);
            addToFinalSummary(o);
    }

    public void addToFinalSummary(JSONObject o) {
        try {
            //#blank lines
            //blankLines += getBlankLines(o);
            //add method variables naming results
            repoUrl = o.get("repoURL").toString();
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

    public void writeResults(){
        totalFiles = fileJSONObjects.size();
        try {
            finalSummary.put("methodsVariableOther", methodsVariableOther);
            finalSummary.put("methodsVariableLowerSnake", methodsVariableLowerSnake);
            finalSummary.put("methodsVariablesGoogle", methodsVariablesGoogle);
            finalSummary.put("methodsIndentsTabs", methodsIndentsTabs);
            finalSummary.put("methodsIndentsSpaces", methodsIndentsSpaces);
            finalSummary.put("methodsIndentsMinIndent", methodsIndentsMinIndent);
            finalSummary.put("methodsIndentsAvgIndent", methodsIndentsAvgIndent);
            finalSummary.put("methodsIndentsMaxIndent", methodsIndentsMaxIndent);



        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*
        Integer methodsVariableOther = new Integer(0);
        Integer methodsVariableLowerSnake = new Integer(0);
        Integer methodsVariablesGoogle = new Integer(0);
        Integer methodsIndentsTabs = new Integer(0);
        Integer methodsIndentsSpaces = new Integer(0);
        Integer methodsIndentsMinIndent = new Integer(0);
        Integer methodsIndentsAvgIndent = new Integer(0);
        Integer methodsIndentsMaxIndent = new Integer(0);
        Integer methodsLineLength = new Integer(0);
        Integer methodsNamingOther = new Integer(0);
        Integer methodsNamingLowerSnake = new Integer(0);
        Integer methodsNamingGoogle = new Integer(0);
        Integer methodsCurlyBracesSingleOther = new Integer(0);
        Integer methodsCurlyBracesSingleAllman = new Integer(0);
        Integer methodsCurlyBracesSingleExKr = new Integer(0);
        Integer methodsCurlyBracesSingleGoogle = new Integer(0);
        Integer methodsCurlyBracesMultipleOther = new Integer(0);
        Integer methodsCurlyBracesMultipleAllman = new Integer(0);
        Integer methodsCurlyBracesMultipleExKr = new Integer(0);
        Integer methodsCurlyBracesMultipleGoogle = new Integer(0);
        Integer namingOther = new Integer(0);
        Integer namingGoogle = new Integer(0);
        Integer constantsNamingOther = new Integer(0);
        Integer constantsNamingGoogle = new Integer(0);
        Integer fieldsNamingOther = new Integer(0);
        Integer fieldsNamingLowerSnake = new Integer(0);
        Integer fieldsNamingGoogle = new Integer(0);
        String repoUrl;
        Integer linesOfCode = new Integer(0);
        Integer totalFiles = new Integer(0);
        */
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

    private Integer getBlankLines(JSONObject o) throws JSONException {
        Integer s = Integer.parseInt(getClassJSONObject(o).get("blank_lines").toString());
        return s;
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

}
