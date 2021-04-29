package operationsmanager;

import operationsmanager.PathCalculator;
import org.testng.annotations.*;
import java.time.Period;

import java.time.LocalDateTime;
import java.time.Period;

public class PathCalculatorTest {

    PathCalculator path = PathCalculator.getInstance();
    Period period;

    @BeforeTest

    @Test
    public void getPath() {
     /*   System.out.println( path.getPath(1,9 ,"transform") );
        System.out.println(path.getPath(5,1,"unload"));
        System.out.println(path.getPath(6,2,"unload"));
        System.out.println(path.getPath(7,3,"unload"));*/

        LocalDateTime aux, folga,maxFinishingTime;
        int folga_int;
        aux = LocalDateTime.now();
        maxFinishingTime=LocalDateTime.now().plusSeconds(300);
        folga = maxFinishingTime.minusSeconds(aux.getSecond()).minusMinutes(aux.getMinute())
                .minusHours(aux.getHour()).minusDays(aux.getDayOfMonth())
                .minusMonths(aux.getMonthValue()).minusYears(aux.getYear());
        System.out.println("aux:"+aux);
        System.out.println("maxFinishingTime:"+maxFinishingTime);
        System.out.println(folga);
        System.out.println(aux.getSecond()-maxFinishingTime.getSecond());
        System.out.println(aux.getMinute()-maxFinishingTime.getMinute());
        System.out.println(aux.getDayOfMonth()-maxFinishingTime.getDayOfMonth());
        System.out.println(aux.minusDays(maxFinishingTime.getDayOfMonth()));

        period = Period.between(aux.toLocalDate(),maxFinishingTime.toLocalDate());
        System.out.println("Period: " +period);

        folga_int = maxFinishingTime.getSecond()-aux.getSecond()+
                (maxFinishingTime.getMinute()-aux.getMinute())*60+
                (maxFinishingTime.getHour()-aux.getHour())*3600+
                (maxFinishingTime.getDayOfMonth()-aux.getDayOfMonth())*36000*24+
                (maxFinishingTime.getMonthValue()-aux.getMonthValue())*36000*24*31+
                (maxFinishingTime.getYear()-aux.getYear())*36000*24*31*12;

        System.out.println("Folga: "+folga_int);

    }

}