package cli.command;

import app.AppConfig;
import app.DistributedMutex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PushFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "push";
    }

    @Override
    public void execute(String args) {
        DistributedMutex.lock();
        try {
            List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

            File tempFile = new File(AppConfig.ROOT_PATH + args + ".temp");
            tempFile.delete();

            AppConfig.chordState.pushFile(args, lines, AppConfig.myServentInfo.getChordId());

        } catch (IOException e) {
            AppConfig.timestampedStandardPrint("Failed to find file");
            DistributedMutex.unlock();
        }
    }
}
