package operationsmanager;

import java.util.Vector;

public class StorageOutConv {

    private final Vector <Conveyor> conveyors;
    
    // static variable single_instance of type TransfromationTable 
    private static StorageOutConv single_instance = null;

    // static method to create instance of TransfromationTable class 
    public static StorageOutConv getInstance() {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new StorageOutConv(); 
        } 
        return single_instance; 
    } 

    private  StorageOutConv(){
        int i;
        int id=9;
        conveyors= new Vector<>();

        for( i=8; i>-1;i--){
            /*form left to right according to Sílvia's numbering*/
            Conveyor conv= new Conveyor(i, 6,id);
            id++;
            conveyors.add(conv);
        }
    }
    public Vector<Conveyor> getConveyors(){
        return conveyors;
    }
}
