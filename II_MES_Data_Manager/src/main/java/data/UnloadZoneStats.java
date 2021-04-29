package data;

import logger.ClassLogger;

import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

public class UnloadZoneStats extends Stats {
    private UnloadZoneStats(
            Vector<Vector<Integer>> delieveredPieces,
            Vector<Vector<Integer>> thisRun,
            Vector<Vector<Integer>> total,
            Logger logger
    ) {
        super(delieveredPieces, thisRun, total, logger);
    }

    public static UnloadZoneStats createNew(
            Properties properties,
            Vector<Vector<Integer>> unloadZones,
            Logger parentLogger
    ) {
        Logger logger = ClassLogger.initLogger(UnloadZoneStats.class.getName(), parentLogger);
        int pieceCount = Integer.parseInt(properties.getProperty("PIECE_TYPE_COUNT"));
        int unloadZoneCount = Integer.parseInt(properties.getProperty("UNLOAD_ZONE_COUNT"));
        Vector<Vector<Integer>> thisRun = new Vector<>(unloadZoneCount);
        Vector<Vector<Integer>> total = new Vector<>(unloadZoneCount);
        for (int i = 0; i < unloadZoneCount; i++) {
            thisRun.add(new Vector<>(pieceCount));
            total.add(new Vector<>(pieceCount));
            for (int j = 0; j < pieceCount; j++) {
                thisRun.get(i).add(j,0);
                total.get(i).add(j, unloadZones.get(i).get(j));
            }
        }
        return new UnloadZoneStats(unloadZones, thisRun, total, logger);
    }

    public Integer getUnloadZoneCount() { return super.getPrimarySize(); }

    public Integer getPiecetypeCount() { return super.getSecondarySize(); }

}
