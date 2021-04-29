package opcua;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;


public class PlcHandler {
    public static UaMonitoredItem getItem(List<UaMonitoredItem> items, NodeId node) {
        for (UaMonitoredItem item : items) {
            if (getNodeId(item) == node)
                return item;
        }
        return null;
    }

    public static NodeId getNodeId(UaMonitoredItem item) {
        return item.getReadValueId().getNodeId();
    }

    public static DataValue readValue(UaMonitoredItem item, OpcUaClient client) {
        try {
            itemOk(item);
            return client.readValue(0, TimestampsToReturn.Both, getNodeId(item)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean readBoolValue(DataValue value) {
        return Boolean.parseBoolean(value.getValue().getValue().toString());
    }

    public static int readInt16Value(DataValue value) {
        return Integer.parseInt(value.getValue().getValue().toString());
    }

    public static void writeInt16(OpcUaClient client, UaMonitoredItem item, Short origin) {
        Variant variant = new Variant(origin);
        DataValue dv = new DataValue(variant);
        client.writeValue(getNodeId(item), dv);
    }

    public static void writeInt16Vector(OpcUaClient client, UaMonitoredItem item, Vector<Short> origin, int fixedDimension) {
        itemOk(item);
        short[] vector = new short[fixedDimension];
        if (fixedDimension == 1) {
            writeInt16(client, item, origin.get(0));
        }
        else {
            for (int i = 0; i < fixedDimension; i++) {
                short obj;
                if (origin.size() < fixedDimension && (i > origin.size() - 1)) {
                    obj = (short) 0;
                } else{
                    obj = origin.get(i);
                }
                vector[i] = obj;
                Variant variant = new Variant(vector);
                DataValue dv = new DataValue(variant);
                client.writeValue(getNodeId(item), dv);
            }
        }
    }

    public static void writeBool(OpcUaClient client, UaMonitoredItem item, Boolean value ){
        itemOk(item);
        Variant variant= new Variant(value);
        DataValue dv = new DataValue(variant);
        client.writeValue( getNodeId(item), dv);
    }

    private static void itemOk(UaMonitoredItem item) {
        if(item.getStatusCode().isBad() ){
            System.err.println("Cannot write, item is not ok");
            throw new NullPointerException();
        }
    }
}