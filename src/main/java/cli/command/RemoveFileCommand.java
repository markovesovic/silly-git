package cli.command;

import app.AppConfig;
import app.DistributedMutex;

public class RemoveFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {
        DistributedMutex.lock();
        try {
            AppConfig.chordState.removeFile(args, AppConfig.myServentInfo.getChordId());
        } catch (Exception e) {
            e.printStackTrace();
            DistributedMutex.unlock();
        }
    }
}
