package cli.command;

import app.AppConfig;

import java.io.File;
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

//            File file = new File(AppConfig.ROOT_PATH + args);
//
//            if(file.isDirectory()) {
//                File[] files = file.listFiles();
//
//                if(files == null) {
//                    return;
//                }
//
//                for (File f : files) {
//                    System.out.println("file rel path: " + f.getPath());
//                }
//            }
            //Files.walk

            List<String> lines = Files.readAllLines(Paths.get(AppConfig.ROOT_PATH + args));

            AppConfig.chordState.addFile(args, lines);

        } catch (Exception e) {
            AppConfig.timestampedStandardPrint("Failed add command");
            e.printStackTrace();
        }
    }
}
