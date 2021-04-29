package operationsmanager;

import java.util.Vector;

public class TransformConv {

    private final Vector <Conveyor> conveyors;
    private final Vector <Conveyor> machineLocation;// [MACHINE A, MACHINE B, MACHINE C]

    public static TransformConv getInstance(int x, int y) {
        return new TransformConv(x, y);
    } 

    private  TransformConv(int x, int y){
        int i;
        int id=18;
        if(x==4){ id=26;}
        else if(x==2){ id=34;}
        
        conveyors= new Vector<>();
        for(i=y; i>(y-5);i--){
            // form up to down
            Conveyor conv= new Conveyor(x, i,id);
            id++;
            conveyors.add(conv);
        }
        
        machineLocation= new Vector<>();
        for(i=(y-1); i>(y-4); i--){
            Conveyor conv= new Conveyor(x+1, i,id);
            id++;
            machineLocation.add(conv);
        }
    }

    public Vector<Conveyor> getConveyors(){ return conveyors; }
    
    public Conveyor getMachineLocation(int machine){ return machineLocation.elementAt(machine); }
}