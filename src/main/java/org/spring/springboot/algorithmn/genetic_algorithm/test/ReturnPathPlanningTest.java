package org.spring.springboot.algorithmn.genetic_algorithm.test;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.genetic_algorithm.AGVRecord;
import org.spring.springboot.algorithmn.genetic_algorithm.AGV_GA;
import org.spring.springboot.algorithmn.genetic_algorithm.FeasiblePathGrowth;
import org.spring.springboot.algorithmn.genetic_algorithm.ReturnPathPlanning;
import org.ujmp.core.Matrix;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class ReturnPathPlanningTest {
    private FeasiblePathGrowth feasiblePathGrowth;
    private List<List<AGVRecord>> agvRecord;
    private double AGVSpeed;
    private double min_distance;
    private final int MAX_EDGE = 999999;
    private List<List<Integer>> bufferSet;
    private List<List<Integer>> AGVPath;


    //初始化地图
    //   0号buffer    (0——0,3,1,10,11) ->        1号buffer
        //6<- 4—— —— —— —— —— ——0—— —— —— —— ——2<- 13(1——13,2,0,3)
        //    |                         |                     |
        //7   |                         |                     |   12(2——12,13)
        //    |                         |                     |
        //8   |                         |                     |   11
        //9-> 5—— —— —— —— —— ——3—— —— —— —— ——1-> 10
    //
    @Before
    public void initializeReturnPathSetting() {
        Matrix testGraph = Matrix.Factory.ones(6,6);
        testGraph = testGraph.times(MAX_EDGE);
        testGraph.setAsInt(4,0,3);
        testGraph.setAsInt(5,0,2);
        testGraph.setAsInt(6,0,4);
        testGraph.setAsInt(5,1,3);
        testGraph.setAsInt(4,2,1);
        testGraph.setAsInt(6,3,5);
        testGraph.setAsInt(4,4,5);
        testGraph.setAsInt(4,3,0);
        testGraph.setAsInt(5,2,0);
        testGraph.setAsInt(6,4,0);
        testGraph.setAsInt(5,3,1);
        testGraph.setAsInt(4,1,2);
        testGraph.setAsInt(4,5,4);
        testGraph.setAsInt(6,5,3);



        bufferSet = new ArrayList<List<Integer>>();

        List<Integer> bufferPath = new ArrayList<Integer>();
        bufferPath.add(4);
        bufferPath.add(6);
        bufferPath.add(7);
        bufferPath.add(8);
        bufferPath.add(9);
        bufferPath.add(5);
        bufferSet.add(bufferPath);

        List<Integer> bufferPath1 = new ArrayList<Integer>();
        bufferPath1.add(1);
        bufferPath1.add(10);
        bufferPath1.add(11);
        bufferPath1.add(12);
        bufferPath1.add(13);
        bufferPath1.add(2);
        bufferSet.add(bufferPath1);

        AGVPath = new ArrayList<List<Integer>>();//agv的位置, 1号车在0点，2号车在1点
        List<Integer> position1 = new ArrayList<Integer>();
        position1.add(0);
        position1.add(3);
        position1.add(1);
        position1.add(10);
        position1.add(11);
        List<Integer> position2 = new ArrayList<Integer>();
        position2.add(13);
        position2.add(2);
        position2.add(0);
        position2.add(3);
        AGVPath.add(position1);
        AGVPath.add(position2);
        List<Integer> position3 = new ArrayList<Integer>();
        position3.add(12);
        position3.add(13);
        AGVPath.add(position3);
        //两个车都闲着

        AGVSpeed = 1.0;
        min_distance = 1.0;

        agvRecord = new ArrayList<List<AGVRecord>>();
        agvRecord.add(new ArrayList<AGVRecord>());
        agvRecord.add(new ArrayList<AGVRecord>());
        agvRecord.add(new ArrayList<AGVRecord>());
        AGV_GA agv_ga = new AGV_GA();
        feasiblePathGrowth = new FeasiblePathGrowth(testGraph,
                agv_ga.transferGraphToConnectedGraph(testGraph,testGraph.toIntArray()[0].length));
    }


    //看看闲置的车，回buffer的车和在外头执行任务的车都能正确回到buffer
    @Test
    public void shouldAGVReturnToBufferWhenAGVIsIdleAndAGVIsComingBackAndAGVIsPerformingTask() {
        Integer[] bufferForAGV = {1,1,1};
        double[] AGVTime = {14.5,10.0,2.0};
        double[] fitness = {0,10.0,1.0};
        //要让点1的优先级最高
        Integer[][] priChromosome = {{6,5,4,3,2,1},{6,100,4,3,2,1},{6,5,4,3,2,1}};
        ReturnPathPlanning returnPathPlanning = new ReturnPathPlanning();
        int startIndex = 0;
        returnPathPlanning.returnAGVToBuffer(AGVPath,fitness,priChromosome,startIndex,
                AGVTime,agvRecord,feasiblePathGrowth,bufferForAGV,bufferSet,AGVSpeed,min_distance);
        //第一个车子路径正确
        System.out.println(AGVPath);
        assertEquals(6,AGVPath.get(0).size());
        assertEquals(0,(int)AGVPath.get(0).get(0));
        assertEquals(3,(int)AGVPath.get(0).get(1));
        assertEquals(1,(int)AGVPath.get(0).get(2));
        assertEquals(10,(int)AGVPath.get(0).get(3));
        assertEquals(11,(int)AGVPath.get(0).get(4));
        assertEquals(12,(int)AGVPath.get(0).get(5));
        //第二个车子路径正确
        assertEquals(7,AGVPath.get(1).size());
        assertEquals(13,(int)AGVPath.get(1).get(0));
        assertEquals(2,(int)AGVPath.get(1).get(1));
        assertEquals(0,(int)AGVPath.get(1).get(2));
        assertEquals(3,(int)AGVPath.get(1).get(3));
        assertEquals(1,(int)AGVPath.get(1).get(4));
        assertEquals(10,(int)AGVPath.get(1).get(5));
        assertEquals(11,(int)AGVPath.get(1).get(6));
        //第三个车子路径正确
        assertEquals(2,AGVPath.get(2).size());
        assertEquals(12,(int)AGVPath.get(2).get(0));
        assertEquals(13,(int)AGVPath.get(2).get(1));
        //三个车适应度正确
        assertEquals((double) 1,fitness[0]);
        assertEquals((double)17,fitness[1]);
        assertEquals((double)1,fitness[2]);
    }

}
