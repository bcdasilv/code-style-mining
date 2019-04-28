package util;

public class UrlFilepathPair {
    String repoBlobUrl;
    String filePath;

    public UrlFilepathPair(String repoBlobUrl, String filePath) {
        this.repoBlobUrl = repoBlobUrl;
        this.filePath = filePath;
    }

    public String getRepoBlobUrl() {
        return repoBlobUrl;
    }

    public String getFilePath() {
        return filePath;
    }
}
