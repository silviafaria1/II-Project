package data;

public class StatisticReleasePieces {

    private final int Zone;
    private final int Type;
    private final int[][] count = new int[1][];    //total count of pieces of type xx count[Zone][Type]
    private final int[] total = new int[1];    //total pieces released

    public StatisticReleasePieces(int discharge_zone, int type, int count, int total){
        Zone=discharge_zone;
        Type=type;
        this.count[Zone][Type]=count;
        this.total[Zone]=total;
    }

    public void setRelPieces(int count){
        this.count[Zone][Type]=count;
    }

    public void setTotalRelPieces(int total){
        this.total[Zone]=total;
    }

    public int getRelPieces(){
        return count[Zone][Type];
    }

    public int getTotalRelPieces(){
        return total[Zone];
    }
}
