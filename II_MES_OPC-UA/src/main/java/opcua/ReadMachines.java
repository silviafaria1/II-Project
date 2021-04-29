package opcua;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

public class ReadMachines extends Thread {
    BlockingQueue<Integer> fromServer1, fromServer2, fromServer3;
    private static final Vector<Integer> available = new Vector<>();
    private boolean init=true;

    public ReadMachines(BlockingQueue<Integer> toMachine1, BlockingQueue<Integer> toMachine2, BlockingQueue<Integer> toMachine3) {
        fromServer1 = toMachine1;
        fromServer2 = toMachine2;
        fromServer3 = toMachine3;
    }

    @Override
    public void run() {
        if(init){
            available.clear();
            for(int i=0; i<10; i++){
                available.add(i, 0);
            }
            init=false;
        }
        int x1, x2, x3;
        boolean y1, y2, y3;
        try {
            while(!this.isInterrupted()){
                    x1 = fromServer1.take();
                    x2 = fromServer2.take();
                    x3 = fromServer3.take();

                    y1 = readMachineF(x1);
                    y2 = readMachineF(x2);
                    y3 = readMachineF(x3);
                    sendToOM(x1, y1, x2, y2, x3, y3);
            }
        } catch (InterruptedException ignored) { }
    }

    public static void updateAvailability(int machineType, int value){
        int count=available.get(machineType)+value;
        if(count<0){ count=0; }
        available.set(machineType, count);
    }
    
    private Boolean readMachineF(int machineType) {
        return available.elementAt(machineType) == 0;
    }

    private void sendToOM(int x1, boolean y1, int x2, boolean y2, int x3, boolean y3){
        StringBuilder send = new StringBuilder();
        send.append("<InfoM type1=\"").append(x1).append("\" info1=\"").append(y1).append("\" type2=\"")
                .append(x2).append("\" info2=\"").append(y2).append("\" type3=\"").append(x3)
                .append("\" info3=\"").append(y3).append("\"/>\n");

        OperationsManagerClient.putMessage(send.toString());
    }
}

