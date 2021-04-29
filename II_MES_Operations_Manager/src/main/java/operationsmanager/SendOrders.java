package operationsmanager;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SendOrders extends Thread {
    public static final String TRANSFORM = "Transform";
    public static final String UNLOAD = "Unload";
    private final BlockingQueue<String> toOpcua;
    private final BlockingQueue<String> fromServer;
    private final BlockingQueue<String> toDataManager;
    private final BlockingQueue<Boolean> machine1;
    private final BlockingQueue<Boolean> machine2;
    private final BlockingQueue<Boolean> machine3;
    private final BlockingQueue<Boolean> unloadZone;
    private final BlockingQueue<Integer> stores;
    private final Task task;
    private final TransformationTable transformationTable;
    public static final int TRANSFORM1=0, TRANSFORM2=1, TRANSFORM3=2;
    public Vector<Boolean> machinesAvailable=new Vector<>(3);

    public SendOrders(BlockingQueue<String> toOpcua, BlockingQueue<String> fromServer, BlockingQueue<Boolean> machine1,
                      BlockingQueue<Boolean> machine2, BlockingQueue<Boolean> machine3,
                      BlockingQueue<String> toDataManager, BlockingQueue<Integer> stores, BlockingQueue<Boolean> unloadZone) { // TO OPC-UA
        this.toOpcua = toOpcua;
        this.fromServer = fromServer;
        this.toDataManager = toDataManager;
        this.machine1 = machine1;
        this.machine2 = machine2;
        this.machine3 = machine3;
        this.stores = stores;
        this.unloadZone = unloadZone;
        task = Task.getInstance();
        transformationTable = TransformationTable.getInstance();
        machinesAvailable.add(0,true);
        machinesAvailable.add(1,true);
        machinesAvailable.add(2,true);
    }

    @Override
    public void run() {
        sendToOPCUA("<OPCUAstatus/>\n");
        try {
            while (!this.isInterrupted()) {
                OrderMes orderToSend;
                OrderQueueMes current;
                String status;
                current = task.getCurrent();
                sendToOPCUA("<OPCUAstatus/>\n");
                status = fromServer.take();
                if (current.hasOrders() && status.equals("FREE")) {
                    Vector<Integer> machineIgnore = new Vector<>();
                    Vector<Vector<Integer>> answer;
                    Vector<Boolean> aux;
                    for (int i = 0; i < current.getSize();i++) {
                        orderToSend = current.getIOrder(i,current);
                        if (orderToSend.getType().equals(TRANSFORM)) {
                            if(i==0){
                                for (int k = 0; k <= 2; k++) {
                                    aux=machineFree(k+1);
                                    machinesAvailable.set(k, aux.get(0) || aux.get(1) || aux.get(2));
                                }
                            } else{
                                machineIgnore.clear();
                                for (int j = 0; j <= 2; j++) {
                                    if(!machinesAvailable.get(j)){
                                        machineIgnore.add(j);
                                    }
                                }
                            }
                            if (hasStock(orderToSend)) {
                                answer=getFastestAvailableMachine(orderToSend, machineIgnore);
                                if (answer != null) {
                                    orderToSend.setPath(answer.get(0).get(0), answer.get(1));
                                    sendTransform(orderToSend, answer.get(3), answer.get(2));
                                    current.removeOrder(orderToSend);
                                    break;
                                }
                            }
                        } else if (orderToSend.getType().equals(UNLOAD)) {
                            if (hasStock(orderToSend)) {
                                if(getavailabilityUZ(orderToSend)){
                                    sendUnload(orderToSend);
                                    current.removeOrder(orderToSend);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException ignored) { }
    }

    public void sendToOPCUA(String message){
        try {
            toOpcua.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendToDataManager(String message){
        try {
            toDataManager.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendTransform(OrderMes orderToSend, Vector<Integer> machineSequence, Vector<Integer> machineIgnore){
        XMLwriter writer = new XMLwriter();
        Vector<Integer> tool, time;
        String message;
        tool = transformationTable.getTools(orderToSend.getPx(), orderToSend.getPy(),machineIgnore);
        time = transformationTable.getTransformationTimes(orderToSend.getPx(), orderToSend.getPy(),machineIgnore);

        message = writer.createTransformOrderXML(orderToSend.getPath(), tool, time,
                String.valueOf(orderToSend.getTotalQuantity()), String.valueOf(orderToSend.getNumber()),
                String.valueOf(orderToSend.getPx()), String.valueOf(orderToSend.getPy()),
                orderToSend.getEntryHour(), String.valueOf(orderToSend.getMaxFinishingTime()),
                orderToSend.getId(), machineSequence);
        sendToOPCUA(message);
    }

    public Boolean hasStock(OrderMes orderToSend) throws InterruptedException {
        Integer availability;
        do {    // FIXME para fazer exit é preciso mudar isto
            sendToDataManager("<Request_Stores type=\"" + orderToSend.getPx() + "\"/>\n");
            availability = stores.poll(2000, TimeUnit.SECONDS);
        } while (availability==null);
        return (availability>0);
    }

    public Vector<Boolean> machineFree(int machineType) throws InterruptedException {
        Vector<Boolean> info = new Vector<>();
        Boolean info1, info2, info3;
        StringBuilder sendAR= new StringBuilder();
        sendAR.append("<InfoM type1=\"").append(machineType).append("\" type2=\"").append((machineType + 3)).append( "\" type3=\"").append((machineType + 6) ).append("\"/>\n");
        do {
            sendToOPCUA( sendAR.toString());
            info1=machine1.poll(500,TimeUnit.SECONDS);
            info2=machine2.poll(500,TimeUnit.SECONDS);
            info3=machine3.poll(500, TimeUnit.SECONDS);
        } while (info1==null || info2==null || info3==null);

        info.add(0,info1);
        info.add(1,info2);
        info.add(2,info3);
        return info;
    }

    private Vector<Vector<Integer>> getFastestAvailableMachine(
            OrderMes orderToSend, Vector<Integer> machineIgnore) throws InterruptedException {
        
        Vector<Vector<Integer>> result=new Vector<>();
        Vector<Integer> machineSequence;
        Vector<Boolean> info;
        Vector<Integer> machineNumbers = new Vector<>();
        Vector<Integer> transformZone=new Vector<>();
        int machineType;
        int tamanho=1;
        if(!transformationTable.directTransformation(orderToSend.getPx(),orderToSend.getPy())){
            tamanho=transformationTable.getPossibleSequences(orderToSend.getPx(),
                    orderToSend.getPy()).size();
        }
        for (int i = 0; i < tamanho; i++) {
            machineSequence=transformationTable.getMachineSequence(orderToSend.getPx(),
                    orderToSend.getPy(), machineIgnore);
            if(machineSequence==null){ break; }
            machineType = machineSequence.firstElement();
            machineType++;
            info=machineFree(machineType);
            if (info.get(0)){
                transformZone.add(TRANSFORM1);
                result.add(0,transformZone);
                result.add(1,machineSequence);
                result.add(2, machineIgnore);
                result.add(3, machineSequence);
                return result;
            }
            if (info.get(1)){
                transformZone.add(TRANSFORM2);
                for (int j = 0; j < machineSequence.size(); j++) {
                    machineNumbers.add(j,machineSequence.get(j)+3);
                }
                result.add(0,transformZone);
                result.add(1,machineSequence);
                result.add(2, machineIgnore);
                result.add(3, machineNumbers);
                return result;
            }
            if (info.get(2)){
                transformZone.add(TRANSFORM3);
                for (int j = 0; j < machineSequence.size(); j++) {
                    machineNumbers.add(j,machineSequence.get(j)+6);
                }
                result.add(0,transformZone);
                result.add(1,machineSequence);
                result.add(2, machineIgnore);
                result.add(3, machineNumbers);
                return result;
            }
            machineIgnore.add(machineType-1);
            machinesAvailable.set(machineType-1,false);
        }
        return null;
    }

    public boolean getavailabilityUZ(OrderMes orderToSend) throws InterruptedException {
        boolean info;
        int unloadZoneType = orderToSend.getPy();

        sendToOPCUA("<InfoUZ type=\"" + unloadZoneType + "\"/>\n");

        info = unloadZone.take();

        return info;
    }

    public void sendUnload(OrderMes orderToSend){
        XMLwriter writer = new XMLwriter();
        String message;

        message = writer.createUnloadOrderXML(orderToSend.getPath(),
                String.valueOf(orderToSend.getTotalQuantity()), String.valueOf(orderToSend.getNumber()),
                String.valueOf(orderToSend.getPx()), orderToSend.getEntryHour(),
                String.valueOf(orderToSend.getMaxFinishingTime()), orderToSend.getId(),
                String.valueOf(orderToSend.getPy()) );

        sendToOPCUA(message);
    }
}

