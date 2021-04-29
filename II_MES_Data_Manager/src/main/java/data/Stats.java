package data;

import java.util.Vector;
import java.util.logging.Logger;

public abstract class Stats {
    Vector<Vector<Integer>> dbValues;
    Vector<Vector<Integer>> thisRunValues;
    Vector<Vector<Integer>> totalValues;
    Logger logger;

    public Stats(
            Vector<Vector<Integer>> dbValues,
            Vector<Vector<Integer>> thisRunValues,
            Vector<Vector<Integer>> totalValues,
            Logger logger
    ) {
        this.dbValues = dbValues;
        this.thisRunValues = thisRunValues;
        this.totalValues = totalValues;
        this.logger = logger;
    }

    public Integer getStat(int unloadZone, int pieceType) {
        return totalValues.get(unloadZone).get(pieceType);
    }

    public Vector<Vector<Integer>> getCopy() {
        Vector<Vector<Integer>> copy = new Vector<>(totalValues.size());
        for (int i = 0; i < copy.capacity(); i++) {
            copy.add(new Vector<>(totalValues.get(0).capacity()));
            for (int j = 0; j < totalValues.get(0).capacity(); j++) {
                copy.get(i).add(totalValues.get(i).get(j));
            }
        }
        return copy;
    }

    public void update(Vector<Vector<Integer>> thisRun) {
        this.thisRunValues = thisRun;
        calcTotal();
    }

    private void calcTotal() {
        for (int i = 0; i < totalValues.size(); i++) {
            for (int j = 0; j < totalValues.get(0).size(); j++) {
                totalValues.get(i).set(
                        j,
                        dbValues.get(i).get(j) + thisRunValues.get(i).get(j)
                );
            }
        }
    }

    public Integer getPrimarySize() { return totalValues.size(); }

    public Integer getSecondarySize() { return totalValues.get(0).size(); }

}
