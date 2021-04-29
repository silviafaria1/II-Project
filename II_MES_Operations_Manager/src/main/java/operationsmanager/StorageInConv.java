package operationsmanager;

import java.util.Vector;

public class StorageInConv {

    private final Vector <Conveyor> conveyors;
    
    // static variable single_instance of type TransfromationTable 
    private static StorageInConv single_instance = null;

    // static method to create instance of TransfromationTable class 
    public static StorageInConv getInstance() {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new StorageInConv(); 
        } 
        return single_instance; 
    } 

    private  StorageInConv(){
        int i;
        int id=0;
        conveyors= new Vector<>();

        for( i=0; i<9;i++){
            /*form right to left according to Sílvia's numbering*/

            Conveyor conv= new Conveyor(i, 0,id);
            id++;
            conveyors.add(conv);
        }
    }
    public Vector<Conveyor> getConveyors(){
        return conveyors;
    }
}
