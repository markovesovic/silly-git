package cli.command;

import app.AppConfig;
import app.DistributedMutex;

import java.io.IOException;
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
            if(!args.contains(" ")) {
                List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

                int version = AppConfig.chordState.getCurrentFileVersionsInWorkingDir().get(args);
                AppConfig.chordState.commitFile(args, lines, version, AppConfig.myServentInfo.getChordId());
            } else {
                String[] argArray = args.split(" ");
                String filePath;
                String argParam;
                if(argArray[0].contains("--")) {
                    argParam = argArray[0];
                    filePath = argArray[1];
                } else {
                    filePath = argArray[0];
                    argParam = argArray[1];
                }
                if(argParam.equals("--force")) {
                    List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + filePath));
                    AppConfig.chordState.pushFile(filePath, lines, AppConfig.myServentInfo.getChordId());
                } else {
                    DistributedMutex.unlock();
                    AppConfig.timestampedErrorPrint("Unknown arg!");
                }
            }
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Failed to find given file!");
            DistributedMutex.unlock();
        }
    }
}
