package logger;

import java.util.logging.Logger;

public class ClassLogger {

    public static Logger initLogger(String name, Logger parentLogger) {
        Logger logger = Logger.getLogger(name);
        logger.setParent(parentLogger);
        logger.setUseParentHandlers(true);
        return logger;
    }

}
