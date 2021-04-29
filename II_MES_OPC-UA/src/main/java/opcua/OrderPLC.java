package opcua;

import java.time.LocalDateTime;
import java.util.*;

public class OrderPLC {
    private final String type; // transform, storage, load, delivery
    private final Short number;
    private final Short py;
    private final Short px;
    private final int totalQuantity;
    private final Vector<Short> path;
    private final Vector<Short> tools;
    private final Vector<Short> times;
    private final Vector<Integer> machine;
    private final LocalDateTime entryHour;
    private LocalDateTime finishTime;
    private LocalDateTime startTime;
    LocalDateTime aux;
    private int folga;
    private final Short id;
    private final LocalDateTime maxFinishingTime;

    public OrderPLC(String type, Short number, Short px, Short py, int totalQuantity,
                    Vector<Short> path, Vector<Short> times, Vector<Short> tools,
                    LocalDateTime maxFinishingTime, Short id, Vector<Integer> machine,
                    LocalDateTime entryHour
    ){
        this.type = type;
        this.number = number;
        this.px = px;
        this.totalQuantity = totalQuantity;
        this.path = path;
        this.times = times;
        this.tools = tools;
        this.py = py;
        this.maxFinishingTime = maxFinishingTime;
        this.id = id;
        this.machine = machine;
        this.entryHour = entryHour;
    }

    public OrderPLC(String type, Short number, Short px, Short py, int totalQuantity,
                    LocalDateTime maxFinishingTime, Short id,
                    LocalDateTime entryHour, LocalDateTime startTime, int folga
    ){
        this.type = type;
        this.number = number;
        this.px = px;
        this.totalQuantity = totalQuantity;
        this.path = null;
        this.times = null;
        this.tools = null;
        this.py = py;
        this.maxFinishingTime = maxFinishingTime;
        this.id = id;
        this.machine = null;
        this.entryHour = entryHour;
        this.startTime = startTime;
        this.folga = folga;
    }

    public Short getId() { return this.id; }

    public void setStartTime() { startTime= LocalDateTime.now(); }

    public LocalDateTime getStartTime(){
        return startTime;
    }

    public void setFolga(boolean done){
        if(! done) {
            aux = LocalDateTime.now();
            folga = maxFinishingTime.getSecond()-aux.getSecond()+
                    (maxFinishingTime.getMinute()-aux.getMinute())*60+
                    (maxFinishingTime.getHour()-aux.getHour())*3600+
                    (maxFinishingTime.getDayOfMonth()-aux.getDayOfMonth())*36000*24+
                    (maxFinishingTime.getMonthValue()-aux.getMonthValue())*36000*24*31+
                    (maxFinishingTime.getYear()-aux.getYear())*36000*24*31*12;
            // folga = maxFinishingTime - tempoAtual;
        }
        else {
            folga = maxFinishingTime.getSecond()-finishTime.getSecond()+
                    (maxFinishingTime.getMinute()-finishTime.getMinute())*60+
                    (maxFinishingTime.getHour()-finishTime.getHour())*3600+
                    (maxFinishingTime.getDayOfMonth()-finishTime.getDayOfMonth())*36000*24+
                    (maxFinishingTime.getMonthValue()-finishTime.getMonthValue())*36000*24*31+
                    (maxFinishingTime.getYear()-finishTime.getYear())*36000*24*31*12;
            // folga = maxFinishingTime - finishTime;
        }
    }

    public int getFolga(){ return folga; }

    public void setfinishTime() { finishTime = LocalDateTime.now(); }

    public LocalDateTime getfinishTime(){ return finishTime; }

    public String getType(){ return type; }

    public Short getNumber(){ return number; }

    public Short getPx(){ return px; }

    public Short getPy(){ return py; }

    public int getTotalQuantity(){ return totalQuantity; }

    public Vector<Short> getPath(){ return path;}

    public Vector<Short> getTools(){ return tools;}

    public Vector<Short> getTimes(){ return times;}

    public LocalDateTime getMaxFinishingTime() { return maxFinishingTime; }

    public Vector<Integer> getMachine() { return machine; }

    public LocalDateTime getEntryHour() { return entryHour; }

}