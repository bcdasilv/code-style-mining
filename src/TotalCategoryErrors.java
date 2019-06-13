import org.json.JSONException;
import org.json.JSONObject;

public class TotalCategoryErrors {

    private JSONifySummary summary;

    public TotalCategoryErrors(JSONifySummary summary) {
        this.summary = summary;
    }

    public JSONObject createErrorSummary(JSONObject individualAnalysis) throws JSONException {
        JSONObject repo_analysis = new JSONObject();
        JSONObject sum = new JSONObject();
        repo_analysis.put("repo_url", summary.getRepoUrl());
        sum.put("file_count", summary.getTotalFiles());
        sum.put("total_analyzed_files", summary.getTotalJavaFiles());
        sum.put("repo_errors", computeRepoErrors());
        sum.put("total_category_errors", computeTotalCategoryErrors());
        repo_analysis.put("error_summary", sum);
        //System.out.println(summary.getFinalSummary());
        repo_analysis.put("analysis", summary.writeResults());
        repo_analysis.put("individual_analysis", individualAnalysis);
        return repo_analysis;
    }

    public JSONObject computeTotalCategoryErrors() throws JSONException {
        JSONObject totalCategoryErrors = new JSONObject();
        totalCategoryErrors.put("naming", computeTotalNamingErrors());
        totalCategoryErrors.put("indentation", computeIndentErrors());
        totalCategoryErrors.put("tabs_vs_spaces", computeTabsVsSpacesErrors());
        totalCategoryErrors.put("curly_brace", computeCurlyBraceErrors());
        totalCategoryErrors.put("line_length", computeLineLengthErrors());
        totalCategoryErrors.put("import_wildcard", computeImportErrors());
        return totalCategoryErrors;
    }

    public Integer computeRepoErrors() {
        return computeTotalNamingErrors() + computeTabsVsSpacesErrors() + computeCurlyBraceErrors()
                + computeLineLengthErrors() + computeImportErrors();
    }

    public Integer computeTabsVsSpacesErrors() {
        return Math.abs(summary.getMethodsIndentsTabs() - summary.getMethodsIndentsSpaces());
    }

    public Integer computeMethodNamingErrors() {
        return summary.getMethodsNamingLowerSnake() + summary.getMethodsNamingOther();
    }

    public Integer computeConstantsNamingErrors() {
        return summary.getConstantsNamingOther();
    }

    public Integer computeFieldsNamingErrors() {
        return summary.getFieldsNamingLowerSnake() + summary.getFieldsNamingOther();
    }

    public Integer computeClassNamingErrors() {
        return summary.getNamingOther();
    }

    public Integer computeTotalNamingErrors() {
        return (computeClassNamingErrors() + computeConstantsNamingErrors() + computeMethodNamingErrors()
                + computeFieldsNamingErrors());
    }

    public Integer computeLineLengthErrors() {
        return summary.getMethodsLineLength();
    }

    public Integer computeCurlyBraceErrors() {
        return (summary.getMethodsCurlyBracesMultipleAllman() + summary.getMethodsCurlyBracesMultipleExKr()
                + summary.getMethodsCurlyBracesMultipleOther()
                + summary.getMethodsCurlyBracesSingleAllman() + summary.getMethodsCurlyBracesSingleExKr()
                + summary.getMethodsCurlyBracesSingleOther());
    }

    public Integer computeIndentErrors() {
        return summary.getIndentError();
    }

    public Integer computeImportErrors() {
        return summary.getTotalImportWilcards();
    }

}
