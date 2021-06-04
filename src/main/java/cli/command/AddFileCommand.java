package cli.command;

import app.AppConfig;
import app.DistributedMutex;

import java.io.IOException;
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
        DistributedMutex.lock();
        try {
            Thread.sleep(10000);

            List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

            AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(args, 0);
            AppConfig.chordState.addFile(args, lines, AppConfig.myServentInfo.getChordId());

        } catch (IOException e) {
            AppConfig.timestampedStandardPrint("Failed add command");
            DistributedMutex.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
