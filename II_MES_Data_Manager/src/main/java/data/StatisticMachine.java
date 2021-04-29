package data;

public class StatisticMachine {

    private final int Type;
    private final int Machine;
    private final long[] TotalTime = new long[1];   //total time of operation
    private final int[][] count = new int[1][];    //total count of pieces of type xx
    private final int[] total = new int[1];    //total pieces transformed

    public StatisticMachine(int machine, int pieceType, int count, int total, int totalTime){
        Type=pieceType;
        Machine=machine;
        this.count[Machine][Type]=count;
        this.total[Machine]=total;
        this.TotalTime[Machine]=totalTime;
    }

    public void setTimeOp(int totalTime){
        this.TotalTime[Machine]=totalTime;
    }

    public void setOpPieces(int count){
        this.count[Machine][Type]=count;
    }

    public void setTotalOpPieces(int total){
        this.total[Machine]=total;
    }

    public long getTimeOp(){
        return TotalTime[Machine];
    }

    public int getOpPieces(){
        return count[Machine][Type];
    }

    public int getTotalOpPieces(){
        return total[Machine];
    }
}
