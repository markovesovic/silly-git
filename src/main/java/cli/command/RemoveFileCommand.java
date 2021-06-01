package cli.command;

import app.AppConfig;

public class RemoveFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {
        try {
            AppConfig.chordState.removeFile(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
