package opcua;
import org.testng.annotations.Test;

public class testsendstatistics {
    int Machine1_P1, Machine1_P2, Machine1_P3, Machine1_P4, Machine1_P5, Machine1_P6, Machine1_P7, Machine1_P8,Machine1_P9;
    int Machine1_TotalPieces;
    int Machine1_TotalTime;

    //Unload Zone
    int Unload1_P1, Unload1_P2, Unload1_P3, Unload1_P4, Unload1_P5, Unload1_P6, Unload1_P7, Unload1_P8, Unload1_P9;
    int Unload1_TotalPieces;

    String send="";


    @Test
    public void sendStatistics() {
        Machine1_P1 = 1; Machine1_P2 = 2;  Machine1_P3=3; Machine1_P4=4;
        Machine1_P5 = 5;  Machine1_P6 = 6; Machine1_P7 = 7; Machine1_P8 = 8; Machine1_P9 = 9;
        Machine1_TotalPieces = 22;
        Machine1_TotalTime = 60;

        Unload1_P1 = 9; Unload1_P2 = 8; Unload1_P3=7;
        Unload1_P4 = 6;  Unload1_P5 = 5;  Unload1_P6 = 4; Unload1_P7 = 3; Unload1_P8 = 2;  Unload1_P9 = 1;
        Unload1_TotalPieces = 40;

        send = "<Statistics>\n";
        send = send + "<Machine id=\"" + "1" + "\">\n";
        send = send + "<P1=\"" + Machine1_P1 + "\"/>\n";
        send = send + "<P2=\"" + Machine1_P2 + "\"/>\n";
        send = send + "<P3=\"" + Machine1_P3 + "\"/>\n";
        send = send + "<P4=\"" + Machine1_P4 + "\"/>\n";
        send = send + "<P5=\"" + Machine1_P5 + "\"/>\n";
        send = send + "<P6=\"" + Machine1_P6 + "\"/>\n";
        send = send + "<P7=\"" + Machine1_P7 + "\"/>\n";
        send = send + "<P8=\"" + Machine1_P8 + "\"/>\n";
        send = send + "<P9=\"" + Machine1_P9 + "\"/>\n";
        send = send + "<TotalPieces=\"" + Machine1_TotalPieces + "\"/>\n";
        send = send + "<TotalTime=\"" + Machine1_TotalTime + "\"/>\n";
        send = send + "</Machine>\n";

        send = send + "<UnloadZone id=\"" + "1" + "\">\n";
        send = send + "<P1=\"" + Unload1_P1 + "\"/>\n";
        send = send + "<P2=\"" + Unload1_P2 + "\"/>\n";
        send = send + "<P3=\"" + Unload1_P3 + "\"/>\n";
        send = send + "<P4=\"" + Unload1_P4 + "\"/>\n";
        send = send + "<P5=\"" + Unload1_P5 + "\"/>\n";
        send = send + "<P6=\"" + Unload1_P6 + "\"/>\n";
        send = send + "<P7=\"" + Unload1_P7 + "\"/>\n";
        send = send + "<P8=\"" + Unload1_P8 + "\"/>\n";
        send = send + "<P9=\"" + Unload1_P9 + "\"/>\n";
        send = send + "<TotalPieces=\"" + Unload1_TotalPieces + "\"/>\n";
        send = send + "</UnloadZone>\n";

        send = send + "</Statistics>\n";

        System.out.println(send);


    }
}
