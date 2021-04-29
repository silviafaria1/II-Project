package operationsmanager;

import java.time.LocalDateTime;
import java.util.Vector;

public class XMLwriter {

    public String createTransformOrderXML(Vector<Integer> orderPath, Vector<Integer>  tool,
                                          Vector<Integer>  time, String quantity, String orderNumber,
                                          String px, String py, LocalDateTime entryHour, String maxFinishingTime,
                                          int id, Vector<Integer> machine){
        StringBuilder message= new StringBuilder();
        int i;
        StringBuilder path= new StringBuilder();
        StringBuilder tools= new StringBuilder();
        StringBuilder times= new StringBuilder();
        StringBuilder machines= new StringBuilder();

        for(i=0; i<orderPath.size(); i++){
            path.append(orderPath.elementAt(i)) ;
            if( (1+ i )== orderPath.size())
            break;
            path.append(",") ;
        }

        for(i=0; i<tool.size(); i++){
            tools.append(tool.elementAt(i)) ;
            if( (1+ i )== tool.size())
            break;
            tools.append(",") ;
        }

        for(i=0; i<time.size(); i++){
            times.append(time.elementAt(i)) ;
            if( (1+ i )== time.size())
            break;
            times.append(",") ;
        }

        for(i=0; i<machine.size(); i++){
            machines.append(machine.elementAt(i)+1) ;
            if( (1+ i )== machine.size())
                break;
            machines.append(",") ;
        }

        message.append("<TRANSFORM orderNumber=\"").append(orderNumber).append("\" orderPath=\"")
                .append(path.toString()).append("\" time= \"").append(times.toString()).append("\" tool=\"")
                .append(tools.toString()).append("\" quantity=\"").append(quantity).append("\" px=\"")
                .append(px).append("\" py=\"").append(py).append("\" EntryHour=\"").append(entryHour)
                .append("\" maxFinishingTime=\"").append(maxFinishingTime).append("\" id=\"")
                .append(id).append("\" machine=\"").append(machines.toString()).append("\">\n")
                .append(("</TRANSFORM>\n"));
        return message.toString();
    }

    public String createUnloadOrderXML( Vector<Integer> orderPath, String quantity,String orderNumber,
                                        String px, LocalDateTime entryHour, String maxFinishingTime,
                                        int id, String py){
        StringBuilder message= new StringBuilder();
        int i;
        StringBuilder path= new StringBuilder();

        for(i=0; i<orderPath.size(); i++){
            path.append(orderPath.elementAt(i)) ;
            if( (1+ i )== orderPath.size())
                break;
            path.append(",") ;
        }
        message.append("<UNLOAD orderNumber=\"").append(orderNumber).append("\" orderPath=\"")
                .append(path.toString()).append("\" quantity=\"").append(quantity)
                .append("\" px=\"").append(px).append("\" py=\"").append(py)
                .append("\" EntryHour=\"").append(entryHour).append("\" maxFinishingTime=\"")
                .append(maxFinishingTime).append("\" id=\"").append(id)
                .append("\">\n").append("</UNLOAD>\n");
        return message.toString();
    }
}