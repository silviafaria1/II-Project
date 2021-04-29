package opcua;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public final class NodesIds {
    private static NodesIds single_instance;
    private final HashMap<String, NodeId> nodes;

    private void setNodes(Vector<String> names) {
        for (String name : names) {
            String URL = "|var|CODESYS Control Win V3 x64.Application.PLANTA.";
            String fullName = buildString(URL, name);
            NodeId nodeId = new NodeId(4, fullName);
            nodes.put(name, nodeId);
        }
    }

    private String buildString(Object... args) {
        StringBuilder stringBuilder = new StringBuilder(50);
        for(Object arg: args) {
            stringBuilder.append(arg);
        }
        return stringBuilder.toString();
    }

    public NodeId getNewOrder () { return nodes.get("newOrder"); }
    public NodeId getArmazemOutFree() { return nodes.get("ArmazemOutFree"); }
    public NodeId getNodePath() { return nodes.get("caminhoI"); }
    public NodeId getNodeTime() { return nodes.get("tempoI"); }
    public NodeId getNodeTool() { return nodes.get("toolI"); }
    public NodeId getNodeOrderNumber() { return nodes.get("orderNumberI"); }
    public NodeId getNodePieceType() { return nodes.get("pieceType"); }
    public NodeId getOrderNumberOut() { return nodes.get("orderNumberOut"); }
    public NodeId getStorageInPiece() { return nodes.get("StorageInPiece"); }
    public NodeId getOrderNumberReadBool() { return nodes.get("orderNumberReadI"); }
    public NodeId getStorageInUse() { return nodes.get("StorageInUse"); }

    public NodeId getPusherLoadNotified(int pusher) {
        String name = buildString("C7T", 2+pusher, ".LoadDone");
        return nodes.get(name);
    }

    public NodeId getPusherNumberRead(int pusher){
        String name = buildString("C7T", 2+pusher, ".OrderNumberRead");
        return nodes.get(name);
    }

    public NodeId getPusherOrderNumber(int pusher){
        String name = buildString("C7T", 2+pusher, ".OrderNumber_O");
        return nodes.get(name);
    }

    //Machines
    public NodeId getNodeMachine(int machine, int piece){
        String name = buildString("MAQUINA", machine, ".P", piece);
        return nodes.get(name);
    }

    public NodeId getNodeMachineTotalPieces(int machine){
        String name = buildString("MAQUINA",machine,".TotalPecas");
        return nodes.get(name);
    }

    public NodeId getNodeMachineTotalTime(int machine){
        String name = buildString("MAQUINA",machine,".TotalTempo");
        return nodes.get(name);
    }

    //Unload
    public NodeId getNodeUnloading(int pusher, int piece){
        String name = buildString("DESCARGA",pusher,".P",piece);
        return nodes.get(name);
    }

    public NodeId getNodeUnloadingTotalPieces(int pusher){
        String name = buildString("DESCARGA",pusher,".Total");
        return nodes.get(name);
    }

    public NodeId getMachineDone(int machine){
        if (machine < 1 || machine > 9) return null;
        int x, y;
        if (machine <= 3) {
            x = 1; y = machine + 2;
        } else if (machine <= 6) {
            x = 3; y = machine - 1;
        } else {
            x = 5; y = machine - 4;
        }
        String name = buildString("C", x, "T", y, ".DONE");
        return nodes.get(name);
    }

    public NodeId getNodeReadUnloadZone(int unloadZone) {
        String name = buildString("DISPONIBILIDADE", unloadZone, ".disponibilidade");
        return nodes.get(name);
    }

    public static NodesIds getInstance() {
        // To ensure only one instance is created
        return hasInstance() ? single_instance : new NodesIds();
    }

    private NodesIds() {
        single_instance = this;
        nodes = new HashMap<>(200);
    }

    private static boolean hasInstance() { return single_instance != null; }

    public Collection<NodeId> getNodes(){ return nodes.values(); }

    public void createNodes() {
        Vector<String> names = new Vector<>();
        names.add("caminhoI");
        names.add("tempoI");
        names.add("toolI");
        names.add("orderNumberI");
        names.add("pieceType");
        names.add("newOrder");
        names.add("ArmazemOutFree");
        names.add("orderNumberReadI");
        names.add("orderNumberOut");
        names.add("StorageInPiece");
        names.add("StorageInUse");
        for (int i=1;i<=9;i++) {
            for (int j=1;j<=9;j++) {
                names.add(buildString("MAQUINA", i, ".P", j));
                if (i<=3) {
                    names.add(buildString("DESCARGA", i, ".P", j));
                }
                if (j>=3 && j<=5) {
                    if (i==1||i==3||i==5) {
                        String[] opts = {".STATE_LIVRE.x",".DONE", ".OrderNumber_I", ".OPC_read"};
                        for (String opt: opts) {
                            names.add(buildString("C", i, "T", j, opt));
                        }
                    }
                    if (i==1) {
                        String[] opts = {".OrderNumberRead",".LoadDone",".OrderNumber_O"};
                        for (String opt: opts) {
                            names.add(buildString("C7T", j, opt));
                        }
                    }
                }
            }
            String[] opts = {".TotalPecas", ".TotalTempo"};
            for (String opt: opts) {
                names.add(buildString("MAQUINA", i, opt));
            }
            if (i<=3) {
                names.add(buildString("DESCARGA", i, ".Total"));
                names.add(buildString("DISPONIBILIDADE", i, ".disponibilidade"));
            }
        }
        setNodes(names);
    }
}