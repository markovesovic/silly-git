package cli.command;

import app.AppConfig;
import app.DistributedMutex;
import servent.response.PullFileResponse;

public class PullFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "pull";
    }

    @Override
    public void execute(String args) {
        DistributedMutex.lock();
        try {
            String[] splitArgs = args.split(" ");

            String filePath = splitArgs[0];
            int version;
            if(splitArgs.length == 1) {
                version = -1;
            } else {
                version = Integer.parseInt(splitArgs[1]);
            }

            PullFileResponse response = AppConfig.chordState.pullFile(filePath, version, AppConfig.myServentInfo.getChordId());

            if(response.getVersion() == -1) {
                AppConfig.timestampedStandardPrint(response.getFilePath());
                DistributedMutex.unlock();
            } else if(response.getVersion() == -2) {
                AppConfig.timestampedStandardPrint("Please wait...");
            } else {
                AppConfig.timestampedStandardPrint("File successfully retrieved from remote!");
                AppConfig.chordState.saveFileToFs(response.getFilePath(), response.getContent());
                AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(filePath, response.getVersion());
                DistributedMutex.unlock();
            }

        } catch(Exception e) {
            DistributedMutex.unlock();
            e.printStackTrace();
        }
    }
}
