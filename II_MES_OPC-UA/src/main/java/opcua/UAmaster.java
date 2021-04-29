package opcua;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class UAmaster extends Thread {
    String endPoint;
    public static OpcUaClient client;
    NodesIds nodeIds;
    public static List<UaMonitoredItem> itemss;
    public static Boolean connectionStatus = false;

    public UAmaster(String endPoint) {
        this.endPoint = endPoint;
        nodeIds = NodesIds.getInstance();
        connectionStatus=false;
    }

    void configureConnection() {
        Vector<MonitoredItemCreateRequest> request = new Vector<>();
        int clientHandle = 0;
        nodeIds.createNodes();

        for (NodeId node : nodeIds.getNodes()) {
            ReadValueId readValueId =
                    new ReadValueId(
                            node,
                            AttributeId.Value.uid(),
                            null,
                            null
                    );
            MonitoringParameters parameters =
                    new MonitoringParameters(
                            UInteger.valueOf(clientHandle),
                            10.0,
                            null,
                            UInteger.valueOf(10),
                            true
                    );
            clientHandle++;
            request.add(new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters));
        }

        UaSubscription subscription;
        try {
            subscription = client.getSubscriptionManager().createSubscription(10.0).get();
            itemss = subscription.createMonitoredItems(TimestampsToReturn.Both, request).get();
            System.out.println("Done");
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            client = createClient();
            if (client == null) {
                System.err.println("No OPC-UA server found");
                System.err.println("Retrying in 5 seconds...");
                Thread.sleep(5000);
                run();
            }
            if (connect()) {
                System.out.println("Connected to OPC-UA! Configuring...");
                connectionStatus = true;
                configureConnection();
            } else {
                System.err.println("Failed to connect");
                connectionStatus = false;
            }
        } catch (InterruptedException ignored) {}
    }

    private static OpcUaClientConfig buildConfiguration(EndpointDescription endpoint) {
        final OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(endpoint);
        return cfg.build();
    }

    private OpcUaClient createClient() {
        try {
            List<EndpointDescription> endpoints;
            endpoints = DiscoveryClient.getEndpoints(endPoint).get();
            if (endpoints.size() > 0) {
                client = OpcUaClient.create(buildConfiguration(endpoints.get(0)));
                return client;
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException ignored) {
            return null;
        } catch (UaException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean connect() {
        if (client != null) {
            try {
                UaClient uaClient = client.connect().get();
                return uaClient != null;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}