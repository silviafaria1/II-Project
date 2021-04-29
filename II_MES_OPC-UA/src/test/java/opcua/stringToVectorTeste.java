package opcua;

import org.testng.annotations.Test;

import java.util.*;

public class stringToVectorTeste {


    @Test
    public void sendOrders() {
        String a = "1";
        //using String split function
        String[] words = a.split(",");
        Vector <Long> numbers = new Vector<>();
        for (String word : words) {
            numbers.add((long) 1000 * Integer.parseInt(word));
        }

        System.out.println(numbers.toString());
    }
}
