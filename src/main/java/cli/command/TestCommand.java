package cli.command;

import app.AppConfig;
import app.DistributedMutex;

public class TestCommand implements CLICommand {

    @Override
    public String commandName() {
        return "test";
    }

    @Override
    public void execute(String args) {
        try {
//            AppConfig.timestampedStandardPrint("Waiting for lock");
//            DistributedMutex.lock();
//            AppConfig.timestampedStandardPrint("Got lock");
//
//            Thread.sleep(10000);
//
//            AppConfig.timestampedStandardPrint("Before unlocking");
//            DistributedMutex.unlock();
//            AppConfig.timestampedStandardPrint("Released lock");

            AppConfig.chordState.getAllNodeInfo().forEach(node -> AppConfig.timestampedStandardPrint(node.toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
