package opcua;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessOrders{
    private final ConcurrentHashMap<Short, OrderPLC> beingProcessed;
    private final ConcurrentHashMap <Short, OrderPLC> arrivedT;
    private final ConcurrentHashMap <Short, OrderPLC> arrivedUNLOAD;
    public static final String TRANSFORM = "Transform", UNLOAD = "Unload";
    private static ProcessOrders single_instance=null;
    public static ProcessOrders getInstance() {
        // To ensure only one instance is created
        if (single_instance == null) {
            single_instance = new ProcessOrders();
        }
        return single_instance;
    }

    private ProcessOrders(){
        beingProcessed= new ConcurrentHashMap<>();
        arrivedT= new ConcurrentHashMap<>();
        arrivedUNLOAD= new ConcurrentHashMap<>();
    }   

    public  Vector <OrderPLC> getBeingProcessed(){
        return new Vector<>(beingProcessed.values());
    }
    public void addOrderToBeingProcessed(OrderPLC order){
        beingProcessed.put(order.getId(), order);
    }

    public OrderPLC getOrder(Short orderNumber){
        return beingProcessed.get(orderNumber);
    }
   public long getFolgaAtual(OrderPLC order){
        order.setFolga(false);
        return order.getFolga();
    }
    public  Vector <OrderPLC>  getDone(){
        Vector <OrderPLC>  done = new Vector<>() ;
        done.addAll(arrivedT.values());
        done.addAll(arrivedUNLOAD.values());
        return done;
    }

    public void processEntry(Short orderNumber){
        if(orderNumber==-2 || orderNumber==-1)
            return;
        OrderPLC order = getOrder(orderNumber);
        if (order == null) { return; }
        order.setfinishTime();
        order.setFolga(true);
        if(order.getType().equals(TRANSFORM)){ arrivedT.put(order.getId(), order); }
        else if(order.getType().equals(UNLOAD)){ arrivedUNLOAD.put(order.getId(), order); }
        beingProcessed.remove(orderNumber);
    }
}


