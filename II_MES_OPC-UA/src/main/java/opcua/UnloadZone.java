package opcua;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

import java.util.TimerTask;

public class UnloadZone extends TimerTask {
    NodesIds nodes = NodesIds.getInstance();
    private final int[] unloadZoneIDs = {1,2,3};
    public UnloadZone() { }

    @Override
    public void run() { checkAvailability(); }

    private void checkAvailability() {
        DataValue value;
        for (int unloadZoneID : unloadZoneIDs) {
            value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getNodeReadUnloadZone(unloadZoneID)), UAmaster.client);
            assert value != null;
            ReadUnloadZone.updateAvailability(PlcHandler.readBoolValue(value));
        }
    }
}
