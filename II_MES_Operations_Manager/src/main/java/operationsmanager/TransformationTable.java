package operationsmanager;

import java.util.Objects;
import java.util.Vector;

public class TransformationTable {
    private static TransformationTable single_instance=null; 

    private static final int MACHA=0, MACHB=1, MACHC=2, TRANSFORMATIONS=12;

    //[PX,PY, MACHINE, Tool, PROCESSING TIME ] in total 12 rows
    private final int[][] transformationMatrix = new int [TRANSFORMATIONS][5];
   
    private final Vector<Vector<Integer>> transformationSequences = new Vector<>();

    private TransformationTable() {

        int[][] sequence=  { {1,2,3,7,9} , {1,2,3,4,5}, {1,2,3,4,8,9}, {1,3,4,5},{1,3,4,8}, 
                             {1,4,5}, {1,4,8,9}, {1,2,6,9}, {1,3,7,9}};
        
        for(int i=0; i<9;i++){
            
            Vector<Integer> aux= new Vector<>();
            for(int j=0; j< sequence[i].length ; j++){
                aux.add(sequence[i][j]);
            }
            transformationSequences.add(aux);
        }

        transformationMatrix[0][0]=1;//P1
        transformationMatrix[0][1]=2; //P2
        transformationMatrix[0][2]=MACHA; //Machine A
        transformationMatrix[0][3]=1;//Tool
        transformationMatrix[0][4]=15;//15s

        transformationMatrix[1][0]=2;//P2
        transformationMatrix[1][1]=3; //P3
        transformationMatrix[1][2]=MACHA; //Machine A
        transformationMatrix[1][3]=1;//Tool
        transformationMatrix[1][4]=15;//15s
        
        transformationMatrix[2][0]=2;//P2
        transformationMatrix[2][1]=6; //P6
        transformationMatrix[2][2]=MACHA; //Machine A
        transformationMatrix[2][3]=2;//Tool
        transformationMatrix[2][4]=15;//15s

        transformationMatrix[3][0]=6;//P6
        transformationMatrix[3][1]=9; //P9
        transformationMatrix[3][2]=MACHA; //Machine A
        transformationMatrix[3][3]=3;//Tool
        transformationMatrix[3][4]=15;//15s

        transformationMatrix[4][0]=1;//P1
        transformationMatrix[4][1]=3; //P3
        transformationMatrix[4][2]=MACHB; //Machine B
        transformationMatrix[4][3]=1;//Tool
        transformationMatrix[4][4]=20;//20s

        transformationMatrix[5][0]=3;//P3
        transformationMatrix[5][1]=4; //P4
        transformationMatrix[5][2]=MACHB; //Machine B
        transformationMatrix[5][3]=1;//Tool
        transformationMatrix[5][4]=15;//15s

        transformationMatrix[6][0]=3;//P3
        transformationMatrix[6][1]=7; //P7
        transformationMatrix[6][2]=MACHB; //Machine B
        transformationMatrix[6][3]=2;//Tool
        transformationMatrix[6][4]=20;//20s

        transformationMatrix[7][0]=7;//P7
        transformationMatrix[7][1]=9; //P9
        transformationMatrix[7][2]=MACHB; //Machine B
        transformationMatrix[7][3]=3;//Tool
        transformationMatrix[7][4]=20;//15s

        transformationMatrix[8][0]=1;//P1
        transformationMatrix[8][1]=4; //P4
        transformationMatrix[8][2]=MACHC; //Machine C
        transformationMatrix[8][3]=1;//Tool
        transformationMatrix[8][4]=10;//10s

        transformationMatrix[9][0]=4;//P4
        transformationMatrix[9][1]=5; //P5
        transformationMatrix[9][2]=MACHC; //Machine C
        transformationMatrix[9][3]=1;//Tool
        transformationMatrix[9][4]=30;//30s

        transformationMatrix[10][0]=4;//P4
        transformationMatrix[10][1]=8; //P8
        transformationMatrix[10][2]=MACHC; //Machine C
        transformationMatrix[10][3]=2;//Tool
        transformationMatrix[10][4]=10;//10s

        transformationMatrix[11][0]=8;//P8
        transformationMatrix[11][1]=9; //P9
        transformationMatrix[11][2]=MACHC; //Machine C
        transformationMatrix[11][3]=3;//Tool
        transformationMatrix[11][4]=10;//10s

    } 

    public static TransformationTable getInstance() {
        // To ensure only one instance is created 
        if (single_instance == null) {
            single_instance = new TransformationTable(); 
        } 
        return single_instance; 
    } 
    public Vector<Integer> getMachineSequence(int px, int py,Vector<Integer> machinesToIgnore){
        if(px==5 || px==9) {
            System.out.print("Cannot transform piece " + px+"\n");
            return null;
        }
        else if (py==1) {
            System.out.print("Cannot have final piece " + py+"\n");
            return null;
        }
        return findMachineSequence( findSequenceOfTransformation(px,py, machinesToIgnore) );
    }

    public boolean directTransformation(int px, int py){
        return getOriginWorkPiece(py).contains(px);
    }

    public Vector<Integer> getTransformationTimes(int px, int py,Vector<Integer> machinesToIgnore){

        Vector <Integer> times = new Vector<>();
        Vector <Integer> sequence = findSequenceOfTransformation(px,py,machinesToIgnore);

        for(int i = 0; i< Objects.requireNonNull(sequence).size()-1  ; i++){
            Integer time = getTransformationTime(sequence.get(i), sequence.get(i+1));
            times.add(time);
        }
        return times;
    }

    public Vector<Integer> getTools(int px, int py,Vector<Integer> machinesToIgnore){

        Vector <Integer> tools = new Vector<>();
        Vector <Integer> sequence = findSequenceOfTransformation(px,py, machinesToIgnore);
        for(int i = 0; i< Objects.requireNonNull(sequence).size()-1  ; i++){
            Integer tool = getTool(sequence.get(i), sequence.get(i+1));
            tools.add(tool);
        }
        return tools;
    }

    private int getMachine(int px, int py){
        int i;
        int flag=0;
        for ( i=0; i<12;i++){
            if(transformationMatrix[i][0]==px && transformationMatrix[i][1]==py ) {
                flag=1;
                break;
            }
        }
        if (0==flag){
            System.out.println("Can't find machine for transformation "+px+" "+py+"\n");
            return -1;
        }
        return transformationMatrix[i][2];
    }

    private int getTransformationTime(int px, int py){
        int i;
        int flag=0;
        for ( i=0; i<TRANSFORMATIONS;i++){
            if(transformationMatrix[i][0]==px && transformationMatrix[i][1]==py ) {
                flag=1;
                break;
            }
        }
        if (0==flag){
            System.out.println("Can't find time for transformation "+px+" "+py+"\n");
            return -1;
        }
        return transformationMatrix[i][4];
    }

    private int getTool(int px, int py){
        int i;
        int flag=0;

        for ( i=0; i<TRANSFORMATIONS;i++){
            if(transformationMatrix[i][0]==px && transformationMatrix[i][1]==py ) {
                flag=1;
                break;
            }
        }
        if (0==flag){
            System.out.println("Can't find tool for transformation "+px+" "+py+"\n");
            return -1;
        }
        return transformationMatrix[i][3];
    }

    private Vector <Integer> getOriginWorkPiece(int py){
        int i;
        Vector <Integer> px = new Vector<>();

        for (i=0; i<TRANSFORMATIONS; i++){
            if(transformationMatrix[i][1]==py){
                px.add(transformationMatrix[i][0]);
            }
        }
        return px;
    }

    public Vector<Vector<Integer>> getPossibleSequences(int px, int py){
        Vector<Vector<Integer>> possiblePxs= new Vector<>();
        Vector<Integer> aux, sequence;
        int cont;

        for (Vector<Integer> transformationSequence : transformationSequences) {
            cont = 0;
            sequence = new Vector<>();
            aux = transformationSequence;

            for (int j = 0; j < aux.size(); j++) {
                if (aux.elementAt(j) == px && cont == 0) {
                    cont++;
                    sequence.add(px);
                } else if (cont == 1 && aux.elementAt(j) != py) {
                    sequence.add(aux.elementAt(j));
                } else if (aux.elementAt(j) == py && cont == 1) {
                    sequence.add(py);
                    possiblePxs.add(sequence);// found a possible sequence;
                    break;
                }
            }
        }
        return possiblePxs;
    }

    private int getTotalProcessingTime(Vector<Integer> sequence){
        int sum=0;
        for(int i=0; i<sequence.size()-1;i++){
            sum+=getTransformationTime(sequence.elementAt(i), sequence.elementAt(i+1));// px py
        }
        return sum;
    }

    private Vector<Integer> getFastestSequence(Vector<Vector<Integer>> sequences){
        int min=Integer.MAX_VALUE;
        Vector<Integer> fastest=null;

        for (Vector<Integer> sequence : sequences) {

            if ((getTotalProcessingTime(sequence)) < min) {
                min = getTotalProcessingTime(sequence);
                fastest = sequence;
            }
        }
        return fastest;
    }

    public  Vector<Integer> findSequenceOfTransformation( int px, int py, Vector<Integer> machinesToIgnore){

        Vector<Vector<Integer>>  possiblePXs, remove;
        possiblePXs=getPossibleSequences(px,py);
        if(machinesToIgnore!=null){
            remove=new Vector<>();
            for (Vector<Integer> sequence: possiblePXs){
                Vector<Integer> machines= findMachineSequence(sequence);
                if(machinesToIgnore.contains(machines.firstElement())){
                    remove.add(sequence);
                }
            }

            for(Vector<Integer> sequence: remove){
                possiblePXs.remove(sequence);
            }
            if(possiblePXs.size()==0) return null;
        }
        return getFastestSequence(possiblePXs);
    }

    private Vector<Integer> findMachineSequence( Vector<Integer> sequence){
        if(sequence==null)
            return null;
        Vector<Integer> machines = new Vector<>();
        for(int i=0; i< sequence.size()-1; i++){
            machines.add( getMachine ( sequence.elementAt(i), sequence.elementAt(i+1) ) );
        }
        return machines;
    }
}