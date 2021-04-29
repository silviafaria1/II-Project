package data;

import logger.ClassLogger;

import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

public class MachineStats extends Stats {
    private final Vector<Long> dbWorkTimes;
    private Vector<Long> thisRunWorktimes;
    private final Vector<Long> totalWorkTimes;

    private MachineStats(
            Vector<Vector<Integer>> dbStats,
            Vector<Vector<Integer>> thisRun,
            Vector<Vector<Integer>> total,
            Vector<Long> dbWorkTimes,
            Vector<Long> thisRunWorkTimes,
            Vector<Long> totalWorkTimes,
            Logger logger
    ) {
        super(dbStats, thisRun, total, logger);
        this.dbWorkTimes = dbWorkTimes;
        this.thisRunWorktimes = thisRunWorkTimes;
        this.totalWorkTimes = totalWorkTimes;
    }

    public static MachineStats createNew(
            Properties properties,
            Vector<Vector<Integer>> dbStats,
            Vector<Long> dbWorkTimes,
            Logger parentLogger
    ) {
        Logger logger = ClassLogger.initLogger(
                UnloadZoneStats.class.getName(),
                parentLogger
        );
        int pieceCount = Integer.parseInt(
                properties.getProperty("PIECE_TYPE_COUNT")
        );
        int machineCount = Integer.parseInt(
                properties.getProperty("MACHINE_COUNT")
        );
        Vector<Vector<Integer>> thisRun = new Vector<>(machineCount);
        Vector<Vector<Integer>> total = new Vector<>(machineCount);
        Vector<Long> thisRunTimes = new Vector<>(machineCount);
        Vector<Long> totalTimes = new Vector<>(machineCount);
        for (int i = 0; i < machineCount; i++) {
            thisRun.add(new Vector<>(pieceCount));
            total.add(new Vector<>(pieceCount));
            thisRunTimes.add(i, 0L);
            totalTimes.add(i, dbWorkTimes.get(i));
            for (int j = 0; j < pieceCount; j++) {
                thisRun.get(i).add(j,0);
                total.get(i).add(j, dbStats.get(i).get(j));
            }
        }
        return new MachineStats(dbStats, thisRun, total, dbWorkTimes, thisRunTimes, totalTimes, logger);
    }

    public Long getWorkTimeStat(int machineId) {
        return totalWorkTimes.get(machineId);
    }

    public Integer getMachineCount() { return super.getPrimarySize(); }

    public Integer getPiecetypeCount() {
        return super.getSecondarySize();
    }

    public void updateWorktimes(Vector<Long> thisRunWorkTimes) {
        this.thisRunWorktimes = thisRunWorkTimes;
        calcTotalWorkTimes();
    }

    private void calcTotalWorkTimes() {
        for (int i = 0; i < totalWorkTimes.size(); i++) {
            totalWorkTimes.set(
                    i, dbWorkTimes.get(i) + thisRunWorktimes.get(i)
            );
        }
    }

    public Vector<Long> getWorkTimesCopy() {
        Vector<Long> tmp = new Vector<>(totalWorkTimes.size());
        for (int i = 0; i < totalWorkTimes.size(); i++) {
            tmp.add(i, totalWorkTimes.get(i));
        }
        return tmp;
    }

}
