package operationsmanager;

import java.util.Vector;

public class PathCalculator{
    private static PathCalculator single_instance=null;
    private final StaticPath staticPath;
    public static final int MACHA=0, MACHB=1, MACHC=2;
    private final Vector<Vector<Conveyor>> toMachineA;
    private final Vector<Vector<Conveyor>> toMachineB;
    private final Vector<Vector<Conveyor>> toMachineC; // 3 paths to 3 machines of each type
    private final Vector<Vector<Conveyor>>  toDelivery; //3 paths for 3 delivery conveyors
    private Vector<Conveyor> fromLoadUpToStorage, fromLoadDownToStorage; // 1 path for each
    public  final String TRANSFORM="Transform", LOAD="Load",UNLOAD="Unload";

    private PathCalculator(){
        //create predefined paths
        staticPath= StaticPath.getInstance();
        toMachineA= new Vector<>();
        toMachineB= new Vector<>();
        toMachineC= new Vector<>();
        toDelivery= new Vector<>();

        // Create all paths at the plant
        for(int i=0; i<3; i++){
            setTransform(i,6);
            setTransform(i,4);
            setTransform(i,2);
            setToDelivery(i+1);
        }
        setFromLoadUpToStorage();
        setFromLoadDownToStorage();
    }

    public static PathCalculator getInstance() {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new PathCalculator(); 
        } 
        return single_instance; 
    }

    public Vector <Integer> getPath(int px, int py, String type, int transformConveyorNumber, Vector <Integer> machineSequence) {
        switch (type) {
            case TRANSFORM:
                return getTransformPath(transformConveyorNumber,machineSequence );
            case LOAD:
                return getLoadPath(px);
            case UNLOAD:
                return getDeliveryPath(py);
        }
        return null;
    }

    private Vector <Integer> getTransformPath(int transformConveyorNumber, Vector<Integer> machineSequence) {
        Vector<Vector<Integer>> path = new Vector<>();
        for(Integer machine : machineSequence){
            if(machine==MACHA){
                path.add(getPathIDs(toMachineA.elementAt(transformConveyorNumber)));
            }
            else if (machine==MACHB){
                path.add(getPathIDs(toMachineB.elementAt(transformConveyorNumber)));
            }
            else if (machine==MACHC){
                path.add(getPathIDs(toMachineC.elementAt(transformConveyorNumber)));
            }  
        }
        if(path.size()==1) { return path.firstElement(); }
        else{ return joinMachinePaths(path); }
    }

    private Vector <Integer> joinMachinePaths(Vector<Vector<Integer>> paths){
        int index, id;
        Vector <Integer> path;
        Vector<Vector<Integer>> sequences= new Vector<>();

        for(int i=1; i< paths.size();i++){
            sequences.add(findMachineIds(paths.elementAt(i)));            
        }

        path=paths.firstElement();

        for(Vector<Integer> sequence: sequences){
            index=path.indexOf(sequence.firstElement());
            id= path.get(index+1);

            path.add(index+1, sequence.elementAt(1));

            if(!(id==sequence.elementAt(1)))
            {
                path.add(index+2, sequence.elementAt(0));
            }
        }
        return path;
    }

    private Vector <Integer> findMachineIds(Vector <Integer> paths){
        int id, plus2;

        for(int j=0; j< paths.size()-2; j++){
            id=paths.get(j);
            plus2=paths.get(j+2);
            if(id==plus2){
                Vector <Integer> sequence = new Vector<>();
                sequence.add(id);
                sequence.add(paths.get(j+1));
                sequence.add(plus2);
                return sequence;
            }
        }
        return null;
    }
    private Vector <Integer> getDeliveryPath(int py) {
        if(py<1 || py>3)
            return null;
        else
            return getPathIDs(toDelivery.elementAt(py-1) );
    }
    private Vector <Integer> getLoadPath(int px) {
        if(px==1){
            return getPathIDs(fromLoadUpToStorage);
        }
        else if (px==2){
            return getPathIDs(fromLoadDownToStorage);
        }
        else
            return null;
    }
    
    private void setTransform(int machine, int targetX){
        Vector<Conveyor> path;
        path=staticPath.getTransformationPath(targetX, machine);

        if(machine==MACHA){
            toMachineA.add(path);
        }
        else if (machine==MACHB){
            toMachineB.add( path);
        }
        else if (machine==MACHC){
            toMachineC.add( path);
        }
    }

    private void setToDelivery(int slider){
        if(slider==1){
            toDelivery.add( staticPath.getDeliveryPath(slider));
        }
        else if (slider==2){
            toDelivery.add( staticPath.getDeliveryPath(slider));
        }
        else if (slider==3){
            toDelivery.add( staticPath.getDeliveryPath(slider));
        }          
    }
    private void setFromLoadUpToStorage(){
        fromLoadUpToStorage=staticPath.getStorageInUpPath();
    }
    private void setFromLoadDownToStorage(){
        fromLoadDownToStorage=staticPath.getStorageInDownPath();
    }

    private Vector<Integer> getPathIDs( Vector<Conveyor> path){
        int i;
        Vector<Integer> convertPath = new Vector<>();
        for(i=0;i<path.size();i++){
            convertPath.add(path.elementAt(i).getID());
        }
        return convertPath;
    }
}