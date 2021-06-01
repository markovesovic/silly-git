package servent.response;

import java.io.Serializable;
import java.util.List;

public class PullFileResponse implements Serializable {

    private final String filePath;
    private final List<String> content;

    public PullFileResponse(String filePath, List<String> content) {
        this.filePath = filePath;
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<String> getContent() {
        return content;
    }
}
