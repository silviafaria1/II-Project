package operationsmanager;

public class ShopFloor {

    protected DeliveryConv deliveryConv;
    protected TransformConv transformConv1,transformConv2,transformConv3;/* considering only first on e to the left*/
    protected StorageInConv storageInConv;
    protected StorageOutConv storageOutConv;
    
    // static variable single_instance of type ShopFLoor 
    private static ShopFloor single_instance = null;
        // static method to create instance of ShopFLoor class 

    public static ShopFloor getInstance() {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new ShopFloor();//CREATES SHOP FLOOR
        } 
        return single_instance; 
    }     
    protected ShopFloor(){/* x and y of top conveyor: (6,5) ; (4,5); (2,5) */
        deliveryConv= DeliveryConv.getInstance(1, 5);
        transformConv1=TransformConv.getInstance(6, 5);
        transformConv2=TransformConv.getInstance(4, 5);
        transformConv3=TransformConv.getInstance(2, 5);
        storageInConv= StorageInConv.getInstance();
        storageOutConv= StorageOutConv.getInstance();      
    }

}
