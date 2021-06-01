package cli.command;

import app.AppConfig;

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
        try {
            List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

            AppConfig.chordState.commitFile(args, lines);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
