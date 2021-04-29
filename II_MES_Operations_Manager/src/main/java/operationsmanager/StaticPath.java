package operationsmanager;

import java.util.Collections;
import java.util.Vector;

public class StaticPath extends ShopFloor {
    
    private Vector<Conveyor> path;


    // static variable single_instance of type operationsmanager.StaticPath
    private static StaticPath single_instance = null;
        // static method to create instance of operationsmanager.StaticPath class

    public static StaticPath getInstance() {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new StaticPath();//CREATES SHOP FLOOR
        } 
        return single_instance; 
    } 

    /*targetX represents the x of the transform block*/
    public Vector<Conveyor> getTransformationPath(int targetX, int machine){      
        path = new Vector<>();
        
        /*look for target X in storage out */
        gotoTargetUp(targetX);

        /*get machine location and carry on*/
        assert getTransformationConv(targetX) != null;
        gotoTransformation(machine,  getTransformationConv(targetX));
        /*go to storage in*/
        gotoTargetDown(targetX, storageInConv.getConveyors(), false);
        return path;
    }

    public Vector<Conveyor> getDeliveryPath(int slider){
        int i;
        Conveyor current, sliderLocation;
        path = new Vector<>();

        /*go until delivery conveyor targetX=1*/
        gotoTargetUp(1);
        
        /*get slider location*/
        sliderLocation=deliveryConv.getSlider(slider);
        /*go until slider*/

        for(i=0; i<deliveryConv.getConveyors().size();i++){
            
            current=deliveryConv.getConveyors().elementAt(i);
            path.add(current);
            
            if(current.getY()==sliderLocation.getY()){
                /*add slider conveyor to path*/
                path.add(sliderLocation);
                break;
            }
        }
        return path;
    }
    
    public Vector<Conveyor> getStorageInDownPath(){
        path = new Vector<>();
        gotoTargetDown(0, storageInConv.getConveyors(), false);//starts at (0,0)
        return path;
    }
 
     Vector<Conveyor> getStorageInUpPath(){

        Vector <Conveyor> reverse;
        path = new Vector<>();
        reverse=storageOutConv.getConveyors();
        Collections.reverse(reverse);

        /*move to the left*/
        /*1 can be modified to change to which block of conveyors to use*/
        gotoTargetDown(deliveryConv.getConveyors().firstElement().getX(), reverse,true);//starts at (0,6) and goes until (1,6)

       /*go down*/
        goDown(deliveryConv.getConveyors());
     
        /*go to storage*/
        gotoTargetDown(deliveryConv.getConveyors().firstElement().getX(), storageInConv.getConveyors(),false);

        return path;
    }
      

    /* go until storage in down*/
    private void goDown(Vector <Conveyor> conveyors){
        int i;
        Conveyor current;

        for(i=0; i<conveyors.size();i++){
            current=conveyors.elementAt(i);
            path.add(current);
        } 
    }
    /*look for target X in storage out from image left to right*/
    private void gotoTargetUp(int targetX){
        int i;
        Conveyor current;

        for(i=0; i<storageOutConv.getConveyors().size(); i++){
            /*add to path */
            current=storageOutConv.getConveyors().elementAt(i);
            path.add(current);

            if(current.getX()==targetX){
                break;
            }
        }
    }

    /*go to transformation conveyors*/
    private void gotoTransformation(int machine, TransformConv transformConv){
        int i;
        Conveyor current, machineLocation;
        machineLocation=transformConv.getMachineLocation(machine);
        /*add conveyors until machine y and continue*/

        for(i=0; i<transformConv.getConveyors().size();i++){
            current=transformConv.getConveyors().elementAt(i);
            path.add(current);

            if(current.getY()==machineLocation.getY()){
                /*add machine conveyor to path*/
                path.add(machineLocation);
                /*add again previous conveyor*/
                path.add(current);
                /*continue to add rest of transformation conveyors*/
            }
        }
    }

    /*decide which transformation conveyor to use*/
    private TransformConv getTransformationConv(int targetX){
        if(targetX==6)
            return transformConv1;
        else if(targetX==4)
            return transformConv2;
        else if(targetX==2)
            return transformConv3;
        else
            return null;
    }
    /*look for target X in storage in or out from image rigth to left
      boolean up represents if we ask to go down when we are in the up conveyors (storage out)
    */    
    private void gotoTargetDown(int targetX, Vector<Conveyor> conveyors, boolean up){
        /* now in storage in numbering goes from right(0,0) to left(8,0) */
        int i;
        Conveyor current;

        for(i=0; i<conveyors.size(); i++){
            /*add to path */
            current=conveyors.elementAt(i);

            if(current.getX()>=targetX && !up){/*to add the conveyors example: (6,0) (7,0) (8,0)*/
                path.add(current);
            }
            else if(current.getX()<=targetX && up){
                path.add(current);
            }
        }
    }
}
