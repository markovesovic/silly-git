package cli.command;

import app.AppConfig;
import app.DistributedMutex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AddFileCommand implements CLICommand {

    private final List<String> allFiles = new ArrayList<>();

    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {
        DistributedMutex.lock();
        try {
            File file = new File(AppConfig.ROOT_PATH + args);

            if(file.isFile()) {
                List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

                AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(args, 0);
                AppConfig.chordState.addFile(args, lines, AppConfig.myServentInfo.getChordId());
                return;
            }
            allFiles.clear();

            getAllFiles(AppConfig.ROOT_PATH + args);

            AppConfig.timestampedStandardPrint("All files before: " + allFiles);

            List<String> allFilesFromRoot = new ArrayList<>();

            for(String name : allFiles) {
                allFilesFromRoot.add(name.replace(AppConfig.ROOT_PATH.replace("/", "\\"), ""));
            }

            for(String filename : allFilesFromRoot) {
                List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + filename));

                AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(filename, 0);
                AppConfig.chordState.addFile(filename, lines, AppConfig.myServentInfo.getChordId());
            }
            AppConfig.chordState.addFile(args + "::dir", allFilesFromRoot, AppConfig.myServentInfo.getChordId());

            DistributedMutex.unlock();
        } catch (IOException e) {
            AppConfig.timestampedStandardPrint("Failed add command");
            DistributedMutex.unlock();
        }
    }

    private void getAllFiles(String dirName) {
        File dir = new File(dirName);

        File[] dirFiles = dir.listFiles();
        if(dirFiles == null) {
            return;
        }

        for(File f : dirFiles) {
            if(f.isFile()) {
                allFiles.add(f.getPath());
                continue;
            }
            getAllFiles(f.getPath());
        }
    }

}
