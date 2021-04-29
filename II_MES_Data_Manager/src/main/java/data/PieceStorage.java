package data;

import logger.ClassLogger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PieceStorage {
    private final ConcurrentHashMap<Integer, Integer> stocks;
    private final Logger logger;

    PieceStorage(
            ConcurrentHashMap<Integer, Integer> stocks,
            Logger logger
    ){
        this.stocks = stocks;
        this.logger = logger;
    }

    public static PieceStorage createNew(HashMap<Integer, Integer> newStocks, Logger parentLogger) {
        Logger logger = ClassLogger.initLogger(PieceStorage.class.getName(), parentLogger);
        ConcurrentHashMap<Integer, Integer> stocks = new ConcurrentHashMap<>(newStocks);
        return new PieceStorage(stocks, logger);
    }

    public int getQuantity(int type){ return this.stocks.get(type); }

    public int addQuantity(int type, int amount) {
        int qty = stocks.get(type) + amount;
        this.stocks.replace(type, qty);
        logger.info("Stock for piece " + type + " increased to " + qty);
        return qty;
    }

    public int subQuantity(int type, int amount) {
        int qty = stocks.get(type) - amount;
        if (qty >= 0) {
            this.stocks.replace(type, qty);
            logger.info("Stock for piece " + type + " decreased to " + qty);
        } else {
            qty = 0;
            logger.warning("Ignoring stock for piece" + type + "below 0");
        }
        return qty;
    }

    public HashMap<Integer, Integer> getMapCopy() {
        return new HashMap<>(stocks);
    }

}
