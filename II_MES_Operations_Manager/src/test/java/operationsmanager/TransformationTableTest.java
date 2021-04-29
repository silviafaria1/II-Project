package operationsmanager;

import java.util.Vector;

import operationsmanager.TransformationTable;
import org.testng.Assert;
import org.testng.annotations.*;



public class TransformationTableTest {

    TransformationTable table= TransformationTable.getInstance();

    @BeforeTest

    @Test
    public void getTransformationSequence(){

/*        Vector<Integer> sequence, compare, ignore= new Vector<>();

        compare= new Vector<>();
        ignore.add(0);
       // ignore.add(1);
        ignore.add(2);
        compare.add(0);
        compare.add(0);

        sequence=table.getMachineSequence( 1,9, ignore);

        try {
            if(sequence==null)
                System.out.println("null");
            System.out.println(sequence);
            Assert.assertEquals(sequence, compare);
        } catch (AssertionError e) {
           System.out.println("Not equal");
           
            throw e;
        }
        System.out.println("Equal");*/
        Vector<Integer> machineIgnoreA=new Vector<>();
        Vector<Integer> machineIgnoreB=new Vector<>();
        Vector<Integer> machineIgnoreC=new Vector<>();
        machineIgnoreA.add(0);machineIgnoreB.add(1);machineIgnoreC.add(2);
        Assert.assertNull(table.getMachineSequence(1,2,machineIgnoreA));
        Assert.assertNull(table.getMachineSequence(2,3,machineIgnoreA));
        Assert.assertNull(table.getMachineSequence(2,6,machineIgnoreA));
        Assert.assertNull(table.getMachineSequence(6,9,machineIgnoreA));
        Assert.assertNotNull(table.getMachineSequence(1,3,machineIgnoreB)); // not direct
        Assert.assertNull(table.getMachineSequence(3,4,machineIgnoreB));
        Assert.assertNull(table.getMachineSequence(3,7,machineIgnoreB));
        Assert.assertNull(table.getMachineSequence(7,9,machineIgnoreB));
        Assert.assertNotNull(table.getMachineSequence(1,4,machineIgnoreC)); // not direct
        Assert.assertNull(table.getMachineSequence(4,5,machineIgnoreC));
        Assert.assertNull(table.getMachineSequence(4,8,machineIgnoreC));
        Assert.assertNull(table.getMachineSequence(8,9,machineIgnoreC));


        //System.out.println(table.findSequenceOfTransformation(1,2,machineIgnore).toString());
        //System.out.println(table.getPossibleSequences(1,2).toString());

    }
}

