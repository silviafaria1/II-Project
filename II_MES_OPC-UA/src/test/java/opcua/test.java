package opcua;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

public class test {
    boolean teste;
    String send ="";

    @Test
    public void testboolsend() {
//        teste = false;
//       // send = send + String.valueOf(teste);
//        send = send + "<InfoM type=\"" + 1 +  "\" info=\"" + teste + "\"/>\n";
//        System.out.println(send);

        LocalDateTime now, later;
        now=LocalDateTime.now();
        later=LocalDateTime.now().plusSeconds(10);
        System.out.println(String.valueOf(now));
    }

}
