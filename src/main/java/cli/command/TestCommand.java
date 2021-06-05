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
            AppConfig.timestampedStandardPrint("TEST COMMAND");

            DistributedMutex.lock();
            AppConfig.timestampedStandardPrint("All nodes in system: ");
            AppConfig.chordState.getAllNodeInfo().forEach(node -> AppConfig.timestampedStandardPrint(node.toString()));
            AppConfig.timestampedStandardPrint("All files in warehouse on this node: ");
            AppConfig.chordState.getWarehouseFiles().forEach((filename, versions) -> {
                AppConfig.timestampedStandardPrint(filename + ": " + versions);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        DistributedMutex.unlock();
    }
}
