package opcua;

import java.util.concurrent.BlockingQueue;

public class ReadUnloadZone extends Thread {
    BlockingQueue<Integer> fromServer;
    private static boolean available;
    private boolean init=true;

    public ReadUnloadZone(BlockingQueue<Integer> toUnloadZone) {
        fromServer = toUnloadZone;
    }

    @Override
    public void run() {
        if(init){
            available = true;
            init=false;
        }
        int x;
        boolean y;
        try {
            while(!this.isInterrupted()){
                x = fromServer.take();
                y = readUnloadZone();
                sendToOM(x, y);
            }
        } catch (InterruptedException ignored) { }
    }

    public static void updateAvailability(boolean value){ available = value; }

    private Boolean readUnloadZone() {
        return available;
    }

    private void sendToOM(int x, boolean y){
        StringBuilder send = new StringBuilder();
        send.append("<InfoUZ type=\"").append(x).append("\" info=\"").append(y).append("\"/>\n");
        OperationsManagerClient.putMessage(send.toString());
    }
}