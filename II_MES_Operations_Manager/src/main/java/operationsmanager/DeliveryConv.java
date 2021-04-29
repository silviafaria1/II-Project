package operationsmanager;

import java.util.Vector;

public class DeliveryConv {

    private final Vector <Conveyor> conveyors;
    private final Vector <Conveyor> sliderLocation; /* [slider 1, slider 2, slider 3] */

    private static DeliveryConv single_instance = null;

    public static DeliveryConv getInstance(int x, int y) /* x and y of top conveyor: (1,5)*/ {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new DeliveryConv(x, y);
        } 
        return single_instance; 
    } 

    private  DeliveryConv(int x, int y){
        int i;
        int id=42;
        conveyors= new Vector<>();

        for( i=y; i>(y-5);i--){
            /* form top to down */
            Conveyor conv= new Conveyor(x, i,id);
            conveyors.add(conv);
            id++;
        }
        
        sliderLocation= new Vector<>();
        
        for(i=(y-1); i>(y-1-3); i--){   /*slider at y=4,y=3, y=2*/
            Conveyor conv= new Conveyor(x-1, i,id); /*(0,4) (0,3) (0,2)*/
            id++;
            sliderLocation.add(conv);
        }
    }

    public Vector<Conveyor> getConveyors(){
        return conveyors;
    }
    
    public Conveyor getSlider(int slider){ return sliderLocation.elementAt(slider-1); }
}
