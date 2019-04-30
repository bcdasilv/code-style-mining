import config.Config;
import org.json.JSONObject;

import java.io.FileWriter;
import java.util.ArrayList;

public class JSONify {
    private FileParser fp;
    private String tempJSONFilePath = Config.getInstance().getTempJSONFilePath();

    public JSONify(FileParser fp) {
        this.fp = fp;
    }

    public JSONObject JSONify(String repoURL, String filePath) {
        String repoURLowner = repoURL.split("/repos/")[1];

        JSONObject obj = new JSONObject();
        JSONObject classJSON = new JSONObject();
        try {
            // file-specific attributes
            obj.put("import", fp.wildCard);
            obj.put("package", fp.packageDecName);

            // class attributes
            obj.put("repoURL", repoURLowner);
            obj.put("filename", filePath);
            JSONifyClassNames(classJSON);
            JSONifyBlankLines(classJSON);
            JSONifyConstants(classJSON);
            JSONifyFields(classJSON, false, false);
            JSONifyMethods(classJSON);

            // put class into json object
            obj.put("class", classJSON);

            // store the json object locally
            storeJSONLocally(obj);
            /**
             * Delete this print....
             */
            // System.out.println(file.toString(4));
            /**
             * End delete
             */
        } catch(Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void storeJSONLocally(JSONObject obj) {
        try (FileWriter fw = new FileWriter(tempJSONFilePath)){
            fw.write(obj.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void JSONifyClassNames(JSONObject classJSON) {
        int google = 0;
        int other = 0;
        JSONObject classNames = new JSONObject();
        for(boolean classNameStyle : fp.nr.classes) {
            if(classNameStyle) {
                google++;
            } else {
                other++;
            }
        }

        try {
            classNames.put("google", google);
            classNames.put("other", other);
            classJSON.put("naming", classNames);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void JSONifyBlankLines(JSONObject classObject) {
        boolean result = true;
        for (boolean bool : fp.classWhiteSpace) {
            result &= bool;
        }

        try {
            classObject.put("blank_lines", result);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void JSONifyConstants(JSONObject classObject) {
        int google = 0;
        int other = 0;
        JSONObject constants = new JSONObject();
        JSONObject names = new JSONObject();

        for(boolean constant : fp.nr.constants) {
            if(constant) {
                google++;
            } else {
                other++;
            }
        }

        try {
            names.put("google", google);
            names.put("other", other);
            constants.put("naming", names);
            classObject.put("constants", constants);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void JSONifyFields(JSONObject parentObject, boolean local, boolean method) {
        int google = 0;
        int other = 0;
        int lowerSnake = 0;
        ArrayList<Integer> styles;
        JSONObject attribute = new JSONObject();
        JSONObject names = new JSONObject();

        if (local) {
            styles = fp.nr.variables;
        } else if (method) {
            styles = fp.nr.methods;
        } else {
            styles = fp.nr.fields;
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

        try {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void JSONifyIndents(JSONObject methodObject) {
        JSONObject indentObject = new JSONObject();
        int tabs = 0, spaces = 0, avgIndent = 0;
        int minIndent = Integer.MAX_VALUE;
        int maxIndent = Integer.MIN_VALUE;

        for (MethodWhiteSpaceResults mwp : fp.methodWPs) {
            tabs += mwp.numTabOccurrences;
            spaces += mwp.numSpaceOccurrences;

            // minIndent and maxIndent
            minIndent = Math.min(minIndent, mwp.minIndent);
            maxIndent = Math.max(maxIndent, mwp.maxIndent);
            avgIndent += mwp.averageIndent;
        }

        if (fp.methodWPs.size() > 0) {
            avgIndent /= fp.methodWPs.size();
        }

        try {
            // start putting attributes to indentObject
            indentObject.put("tabs", tabs)
                    .put("spaces", spaces)
                    .put("min_indent", minIndent)
                    .put("max_indent", maxIndent)
                    .put("avg_indent", avgIndent);

            methodObject.put("indents", indentObject);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void JSONifyMethods(JSONObject classObject) {

        JSONObject methodObject = new JSONObject();

        JSONifyFields(methodObject, false, true);	// add method names
        JSONifyIndents(methodObject); // indents

        try {
            methodObject.put("line_length", fp.linesExceeding); // # of lines exceeding
            JSONifyFields(methodObject, true, false); // add local variables
            System.out.println(methodObject);
            JSONifyBraces(methodObject);

            classObject.put("methods", methodObject);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void JSONifyBraces(JSONObject methodObject) {
        JSONObject braceObject = new JSONObject();
        JSONObject single = new JSONObject();
        JSONObject multiple = new JSONObject();

        int sGoogle = 0, sAllman = 0, sEx_kr = 0, sOther = 0;
        int mGoogle = 0, mAllman = 0, mEx_kr = 0, mOther = 0;

        for (BraceResults br : fp.braces) {
            sGoogle += br.SINGLE_GOOGLE;
            sAllman += br.SINGLE_ALLMAN;
            sEx_kr += br.SINGLE_EX_KR;
            sOther += br.SINGLE_WEIRD;

            mGoogle += br.MUL_GOOGLE;
            mAllman += br.MUL_ALLMAN;
            mEx_kr += br.MUL_EX_KR;
            mOther += br.MUL_WEIRD;
        }

        try {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
