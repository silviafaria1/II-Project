package operationsmanager;

import java.time.LocalDateTime;

public class Task{

    private final OrderQueueMes current;
    private OrderMes order;
    private LocalDateTime entryHour;
    private LocalDateTime maxFinishingTime;
    public static final int IGNORE=-1;
    private int id=0;

    public static final String TRANSFORM="Transform", UNLOAD="Unload";
    private static Task single_instance=null;
    public static Task getInstance() {
         // To ensure only one instance is created 
         if (single_instance == null) {
             single_instance = new Task(); 
         } 
         return single_instance; 
    }
    private Task(){
        current=new OrderQueueMes();
    }
    public void insertCurrent(OrderMes order){
         current.insertOrder(order);
    }

    public OrderQueueMes getCurrent(){
        return current;
    }

    public void createTransformOrder(int orderNumber, int px, int py, int quantityToProduce, int deadline) {
        if (px<1 || py<1 || px>9 || py>9) { return; }
        entryHour = LocalDateTime.now();
        maxFinishingTime = entryHour.plusSeconds(deadline);
        for (int i=0; i<quantityToProduce;i++) {
            order = new OrderMes(TRANSFORM, orderNumber, px, py, quantityToProduce, deadline,
                    entryHour, maxFinishingTime,id);
            id++;
            current.insertOrder(order);
        }
    }

    public void createUnloadOrder(int orderNumber, int px, int unload_station, int quantityToProduce) {
        if (px<1 || unload_station<1 || px>9 || unload_station>3) { return ; }
        entryHour = LocalDateTime.now();
        maxFinishingTime = entryHour;
        for (int i=0; i<quantityToProduce;i++) {
            order = new OrderMes(UNLOAD, orderNumber, px, unload_station, quantityToProduce,
                    0, entryHour, maxFinishingTime, id);
            id++;
            order.setPath(IGNORE, null);
            current.insertOrder(order);
        }
    }
}