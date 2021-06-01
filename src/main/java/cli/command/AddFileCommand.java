package cli.command;

import app.AppConfig;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AddFileCommand implements CLICommand {
    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {
        try {

            List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

            AppConfig.chordState.addFile(args, lines);

        } catch (Exception e) {
            AppConfig.timestampedStandardPrint("Failed add command");
            e.printStackTrace();
        }
    }
}
