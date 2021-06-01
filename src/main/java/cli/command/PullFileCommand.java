package cli.command;

import app.AppConfig;
import servent.response.PullFileResponse;

public class PullFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "pull";
    }

    @Override
    public void execute(String args) {
        try {
            String[] splitArgs = args.split(" ");

            String filePath = splitArgs[0];
            int version;
            if(splitArgs.length == 1) {
                version = -1;
            } else {
                version = Integer.parseInt(splitArgs[1]);
            }

            PullFileResponse response = AppConfig.chordState.pullFile(filePath, version);

            if(response == null) {
                AppConfig.timestampedStandardPrint("No such file tracked in system");

            } else if(response.getFilePath().equals("")) {
                AppConfig.timestampedStandardPrint("Please wait...");

            } else {
                AppConfig.timestampedStandardPrint("Filepath: " + filePath + ", with content: " +
                        response.getContent() + "; Successfully added to system");

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
