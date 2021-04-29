package operationsmanager;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class OrderQueueMes {

    private final PriorityBlockingQueue<OrderMes> queue; //priority is order's maxFinishingTime

    public OrderQueueMes(){
        Comparator<OrderMes> comparator = new MyComparator();
        queue = new PriorityBlockingQueue<>(10,comparator);
    }

    public void insertOrder(OrderMes order){
        queue.add(order);    
    }

    public void removeOrder(OrderMes o){queue.remove(o);}

    public OrderMes getIOrder(int i, OrderQueueMes c){
        Iterator<OrderMes> it = c.iterator();
        int count = 0;
        while (it.hasNext()){
            OrderMes o = it.next();
            if(count == i){
                return o;
            }
            count ++;
        }
        return null;
    }

    public int getSize(){return queue.size();}

    public Iterator<OrderMes> iterator(){return queue.iterator();}

    public boolean hasOrders(){
        return !(queue.isEmpty());
    }

    public PriorityBlockingQueue<OrderMes> getCopy() {
        return new PriorityBlockingQueue<>(queue);
    }

}