package operationsmanager;

public class Conveyor {
    private final int x;
    private final int y;
    private final int id;

    public Conveyor(int x, int y, int id){
        this.x=x;
        this.y=y;
        this.id=id;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getID(){
        return id;
    }
}
