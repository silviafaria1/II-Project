package operationsmanager;

import operationsmanager.OrderMes;
import operationsmanager.Task;
import operationsmanager.TransformationTable;
import org.testng.annotations.*;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TaskTest{
    TransformationTable table= TransformationTable.getInstance();
    Date date;
    //operationsmanager.OrderMes order= new operationsmanager.OrderMes("transform", 2, 1,9,5,5, date);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String send = "";
    @BeforeTest

  


    @Test
    public void sendOrders() throws IOException {
        Task task= Task.getInstance();
       // for (int i=0; i<40;i++) {
      //  task.createTransformOrder(60, 6, 9, 4, 30,0,1);
        OrderQueueMes current = task.getCurrent();
        Iterator<OrderMes> iteratorC = current.iterator();

        while (iteratorC.hasNext()) {
            OrderMes o = iteratorC.next();
            send = send + "<current Number=\"" + o.getNumber() + "\" OrderType=\"" + o.getType()
                    + "\" px=\"" + o.getPx() + "\" py=\"" + o.getPy()
                    + "\" Quantity=\"" + o.getTotalQuantity() + "\" Deadline=\"" + o.getDeadline()
                    + "\" MaxFinishingTime=\"" + o.getMaxFinishingTime()
                    + "\" EntryHour=\"" + dateFormat.format(o.getEntryHour()) + "\"/>\n";
            System.out.println(o.getPath());
        }

        System.out.println(send);


         //   task.createUnloadOrder(3, 5, 1, 8);
            //task.createUnloadOrder(3, 5, 1, 8);
       // }
       /*date = new Date();
         send = send + "<waiting Number=\"" + String.valueOf(10) + "\" OrderType=\"" + "Transform"
                + "\" px=\"" + String.valueOf(1) + "\" py=\"" + String.valueOf(2)
                + "\" Quantity=\"" + String.valueOf(20) + "\" Deadline=\"" + String.valueOf(10)
                + "\" MaxFinishingTime=\"" + String.valueOf(43535)
                + "\" EntryHour=\"" + dateFormat.format(date) + "\"/>\n";

         System.out.println(send);*/


       
       
    }

  /* @Test
    public void getCurrent() throws IOException {
        Task task= Task.getInstance(); 
        //task.createTransformOrder(1,1 ,8, 10, 30);
        task.createUnloadOrder(3, 5, 1, 8);
        task.getCurrent();

        Iterator<OrderMes> iterator = task.getCurrent().iterator();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        String strDate;
        while (iterator.hasNext()) {
            OrderMes o = (OrderMes) iterator.next();
            System.out.println(o.getNumber());
            strDate = dateFormat.format(o.getEntryHour());
            System.out.println(strDate);
            System.out.println(o.getTotalQuantity());
            System.out.println(o.getDeadline());
            System.out.println(o.getPx());
            System.out.println(o.getPy());
            System.out.println(o.getType());
            System.out.println(o.getPath());


        }
       // System.out.println(task.getCurrent().getSize());

    }

    @Test
    public void  getPending() throws IOException {
        Task task= Task.getInstance();
        task.createUnloadOrder(3, 5, 1, 60);
        //task.createTransformOrder(6,2 ,8, 1, 30);
        Iterator<OrderMes> iterator = task.getPending().iterator();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        String strDate;
        while (iterator.hasNext()) {
            OrderMes o = (OrderMes) iterator.next();
            System.out.println(o.getNumber());
            strDate = dateFormat.format(o.getEntryHour());
            System.out.println(strDate);
            System.out.println(o.getTotalQuantity());
            System.out.println(o.getDeadline());
            System.out.println(o.getPx());
            System.out.println(o.getPy());
            System.out.println(o.getType());
            System.out.println(o.getPath());
        }
        //System.out.println(task.getPending());
    }*/

}
