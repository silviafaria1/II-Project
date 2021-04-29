package operationsmanager;

import java.time.LocalDateTime;
import java.util.*;

public class OrderMes{

    private final String type; // 3 types of path LOAD, TRANSFORM, DELIVERY
    private final int number;
    private final int px;
    private final int py;
    private final int totalQuantity;
    private final int deadline;
    private final LocalDateTime maxFinishingTime;
    private final LocalDateTime entryHour;
    private Vector <Integer> path;
    private final PathCalculator pathCalculator;
    private final int id;

    public OrderMes(String type, int number, int px, int py, int totalQuantity,
                    int deadline, LocalDateTime entryHour, LocalDateTime maxFinishingTime, int id){
        this.type=type;
        this.number=number;
        this.px=px;
        this.py=py;
        this.totalQuantity=totalQuantity;
        this.deadline=deadline;
        this.entryHour=entryHour;
        pathCalculator=PathCalculator.getInstance();
        this.path=null;
        this.maxFinishingTime=maxFinishingTime;
        this.id=id;
    }

    public int getId() { return this.id; }

    public void setPath(int transformConveyorNumber, Vector <Integer> machineSequence){
        this.path=pathCalculator.getPath(this.px, this.py, this.type,
                transformConveyorNumber, machineSequence);
    }

    public String getType(){ return type; }

    public int getNumber(){ return number; }

    public int getPx(){ return px; }

    public int getPy(){ return py; }

    public int getTotalQuantity(){ return totalQuantity; }

    public LocalDateTime getEntryHour(){return entryHour; }
    
    public int getDeadline(){ return deadline; }

    public Vector <Integer> getPath(){return path; }

    public LocalDateTime getMaxFinishingTime() { return maxFinishingTime; }

}