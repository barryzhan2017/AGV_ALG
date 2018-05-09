package org.spring.springboot.algorithmn.test;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.springboot.Application;
import org.spring.springboot.algorithmn.GA.AGVRecord;
import org.spring.springboot.algorithmn.GA.PathImprovement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PathImprovementTest {

    List<List<List<AGVRecord>>> records;
    List<List<List<Integer>>> paths;
    List<Integer[][]> chromosomes;

    //初始化agvRecords,agvPaths,agvChromosome
    @Before
    public void initializeRecordList() {
        records = new ArrayList<List<List<AGVRecord>>>();
        paths = new ArrayList<List<List<Integer>>>();
        chromosomes = new ArrayList<Integer[][]>();


        //2个子代
        List<List<AGVRecord>> recordsList1 = new ArrayList<List<AGVRecord>>();
        List<List<AGVRecord>> recordsList2 = new ArrayList<List<AGVRecord>>();
        records.add(recordsList1);
        records.add(recordsList2);

        List<List<Integer>> pathsList1 = new ArrayList<List<Integer>>();
        List<List<Integer>> pathsList2 = new ArrayList<List<Integer>>();
        paths.add(pathsList1);
        paths.add(pathsList2);

        //假设就4个节点0到3,两个子代的车子的任务的第一步的染色体不一致导致路径第二个子代更好
        Integer[][] chromosomeArray1 = {{1,2,3,4},{0,1,2,3}};
        Integer[][] chromosomeArray2 = {{1,2,4,3},{0,1,2,3}};
        chromosomes.add(chromosomeArray1);
        chromosomes.add(chromosomeArray2);

        //2辆车
        List<AGVRecord> records1 = new ArrayList<AGVRecord>();
        List<AGVRecord> records2 = new ArrayList<AGVRecord>();
        //2个车
        List<Integer> paths1 = new ArrayList<Integer>();
        List<Integer> paths2 = new ArrayList<Integer>();


        recordsList1.add(records1);
        recordsList2.add(records2);
        //每个车有1个任务，也就是两个记录
        pathsList1.add(paths1);
        pathsList2.add(paths2);


        //第一个车子的路径
        paths1.add(1);paths1.add(3);paths1.add(2);paths1.add(3);

        AGVRecord agvRecord1 = new AGVRecord(0,2,1,2,3,0,true);
        AGVRecord agvRecord3 = new AGVRecord(2,3,2,3,1,1,false);


        records1.add(agvRecord1);
        records1.add(agvRecord3);


        //第二个车子的路径
        paths2.add(1);paths2.add(2);paths2.add(3);

        AGVRecord agvRecord2 = new AGVRecord(0,1,1,2,1,0,true);
        AGVRecord agvRecord4 = new AGVRecord(1,2,2,3,1,1,false);


        records2.add(agvRecord2);
        records2.add(agvRecord4);



    }


    //test if the map is set up with all of the different record
    @Test
    public void testListInitializedCorrect() {
        PathImprovement pathImprovement = new PathImprovement();
        HashMap<AGVRecord,Double> agvRecordMap = pathImprovement.initialize(records,0);
        assertEquals(2,agvRecordMap.size());
        AGVRecord agvRecord1 = new AGVRecord(1,2,1,2,3,4,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,2,3,1,5,false);
        assertTrue(agvRecordMap.containsKey(agvRecord1));
        assertTrue(agvRecordMap.containsKey(agvRecord2));
        System.out.println(records);
    }

    //test if the map is set up with correct accumulated distance
    @Test
    public void testMapMeanDistanceCorrect() {
        PathImprovement pathImprovement = new PathImprovement();
        HashMap<AGVRecord,Double> agvRecordMap = pathImprovement.initialize(records, 0);
        //除了两个相同的路径累加了其他都是单独的未累加的路
        for (AGVRecord agvRecord: agvRecordMap.keySet()) {
            if (agvRecord.getStartNode()==1 && agvRecord.getEndNode()==2) {
                assertEquals((double) 2,agvRecordMap.get(agvRecord));
            }
            else if (agvRecord.getStartNode()==2 && agvRecord.getEndNode()==3){
                assertEquals((double) 1,agvRecordMap.get(agvRecord));
            }
        }
    }

    //test if corresponding chromosome changes to new one and the path changes to new one
    @Test
    public void testImprovePathCorrect() {
        PathImprovement pathImprovement = new PathImprovement();
        //优化路径和染色体
        pathImprovement.improvePath(records, chromosomes,0,paths);
        assertEquals(3,paths.get(0).get(0).size());
        assertEquals(1,(int)paths.get(0).get(0).get(0));
        assertEquals(2,(int)paths.get(0).get(0).get(1));
        assertEquals(3,(int)paths.get(0).get(0).get(2));
        assertEquals(1,(int)paths.get(1).get(0).get(0));
        assertEquals(2,(int)paths.get(1).get(0).get(1));
        assertEquals(3,(int)paths.get(1).get(0).get(2));
        assertEquals(1,(int)chromosomes.get(0)[0][0]);
        assertEquals(2,(int)chromosomes.get(0)[0][1]);
        assertEquals(4,(int)chromosomes.get(0)[0][2]);
        assertEquals(3,(int)chromosomes.get(0)[0][3]);
        assertEquals(1,(int)chromosomes.get(1)[0][0]);
        assertEquals(2,(int)chromosomes.get(1)[0][1]);
        assertEquals(4,(int)chromosomes.get(1)[0][2]);
        assertEquals(3,(int)chromosomes.get(1)[0][3]);
        //record中记录的终止点改成了更好的1，起始点不变，后头的record也得改变
        assertEquals(1,records.get(0).get(0).get(0).getPathEndIndex());
        assertEquals(0,records.get(0).get(0).get(0).getPathStartIndex());
        assertEquals(2,records.get(0).get(0).get(1).getPathEndIndex());
        assertEquals(1,records.get(0).get(0).get(1).getPathStartIndex());
    }


    @After
    public void releaseResource() {
        records = null;
        paths = null;
        chromosomes = null;
    }

}
