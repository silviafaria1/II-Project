package gui;

import org.testng.annotations.Test;

import java.util.logging.Logger;

import static org.testng.Assert.*;

public class ServerTest {

    @Test
    public void start() throws InterruptedException {
        Logger logger = Logger.getLogger(getClass().getName());
        //Server server = new Server(logger);
        Thread.sleep(100);
    }

}