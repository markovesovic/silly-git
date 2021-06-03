package cli.command;

import app.AppConfig;
import app.DistributedMutex;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CommitFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "commit";
    }

    @Override
    public void execute(String args) {
        DistributedMutex.lock();
        try {
            List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

            int version = AppConfig.chordState.getCurrentFileVersionsInWorkingDir().get(args);
            AppConfig.chordState.commitFile(args, lines, version);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
