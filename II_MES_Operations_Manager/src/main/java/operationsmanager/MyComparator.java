package operationsmanager;

import java.util.Comparator;

public class MyComparator implements Comparator<OrderMes> {

    @Override
    public int compare(OrderMes o1, OrderMes o2) {
        if(o2.getMaxFinishingTime().isBefore(o1.getMaxFinishingTime()))
            return 1;
        else if (o1.getMaxFinishingTime().isBefore(o2.getMaxFinishingTime()))
            return -1;
        else if (o2.getMaxFinishingTime().isEqual(o1.getMaxFinishingTime()))
            return Integer.compare(o1.getPx(), o2.getPx());
        else
            return 0;
    }
}