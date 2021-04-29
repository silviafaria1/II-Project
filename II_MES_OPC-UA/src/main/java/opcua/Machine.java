package opcua;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

import java.util.TimerTask;

public class Machine extends TimerTask {
    NodesIds nodes = NodesIds.getInstance();
    private final int[] machineIDs = {1,2,3,4,5,6,7,8,9};

    public Machine() { }

    @Override
    public void run() {
        checkAvailability();
    }

    private void checkAvailability() {
        DataValue value;
        for(int machineID : machineIDs) {
            UaMonitoredItem item = PlcHandler.getItem(UAmaster.itemss, nodes.getMachineDone(machineID));
            value = PlcHandler.readValue(item, UAmaster.client);
            assert value != null;
            if (PlcHandler.readBoolValue(value)) {
                ReadMachines.updateAvailability(machineID, -1);
                PlcHandler.writeBool(UAmaster.client, item, false);
            }
        }
    }
}