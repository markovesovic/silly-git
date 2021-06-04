package servent.response;

import java.io.Serializable;
import java.util.List;

public class PullFileResponse implements Serializable {

    private final String filePath;
    private final List<String> content;
    private final int version;

    public PullFileResponse(String filePath, List<String> content, int version) {
        this.filePath = filePath;
        this.content = content;
        this.version = version;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<String> getContent() {
        return content;
    }

    public int getVersion() {
        return version;
    }
}
