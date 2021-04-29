package operationsmanager;

import java.io.IOException;
import java.util.*;
import org.testng.annotations.Test;


public class dateToSecondsTest {
    private Date date;

    @Test
    public void testRun() throws IOException, InterruptedException {
        date = new Date();

        System.out.println(date);
        int hours = date.getHours();
        int minutes = date.getMinutes();
        int seconds = date.getSeconds();
        System.out.println(hours*3600);
        System.out.println(minutes*60);
        System.out.println(seconds);

        int maxFinishingTime = date.getHours()*3600 + date.getMinutes()*60 + date.getSeconds();
        System.out.println(maxFinishingTime);


    }
}


