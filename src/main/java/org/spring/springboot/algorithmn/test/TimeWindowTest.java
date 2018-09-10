package org.spring.springboot.algorithmn.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.springboot.Application;
import org.spring.springboot.algorithmn.GA.TimeNode;
import org.spring.springboot.algorithmn.GA.TimeWindow;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;


public class TimeWindowTest {

    private double AGVSpeed = 2;
    private Double[] timeAlreadyPass = {-1.0000,-1.0000,-1.0000,-1.0000};
    private double[][] graph;
    private double minDistance = 2;

    @Before
    public void initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        File file = new File("TestGraphSet/TestGraph2.csv");
        graph = (Matrix.Factory.importFrom().file(file).asDenseCSV()).toDoubleArray();
//        System.out.println(graph);

    }

    @Test
    public void shouldTimeWindowCreateWithNullForAllStaticAGVsInBuffer() {
        TimeWindow timeWindow = new TimeWindow();
        Double[] timeAlreadyPassing = {-1.0,-1.0};
        List<List<List<Integer>>> AGVPaths = new ArrayList<List<List<Integer>>>();

        List<List<Integer>> AGV1 = new ArrayList<List<Integer>>();
        List<Integer> path1 = new ArrayList<Integer>();
        path1.add(101);
        AGV1.add(path1);

        List<List<Integer>> AGV2 = new ArrayList<List<Integer>>();
        List<Integer> path2 = new ArrayList<Integer>();
        path2.add(201);
        AGV2.add(path2);

        AGVPaths.add(AGV1);
        AGVPaths.add(AGV2);

        timeWindow.generateTimeWindow(AGVPaths,AGVSpeed,graph,timeAlreadyPassing,minDistance);
        List<TimeNode> timeSequence1 = timeWindow.getAGVTimeSequence(0);
        List<TimeNode> timeSequence2 = timeWindow.getAGVTimeSequence(1);
        assertNull(timeSequence1);
        assertNull(timeSequence2);
    }

    // One AGV goes to the next node in buffer just when the other goes into the graph from that node
    @Test
    public void shouldTimeWindowWorksForScenario1() {
        TimeWindow timeWindow = new TimeWindow();
        Double[] timeAlreadyPassing = {-1.0, -1.0};
        List<List<List<Integer>>> AGVPaths = new ArrayList<List<List<Integer>>>();

        List<List<Integer>> AGV1 = new ArrayList<List<Integer>>();
        List<Integer> path1 = new ArrayList<Integer>();
        path1.add(105);
        path1.add(7);
        path1.add(8);
        path1.add(1);
        path1.add(0);
        path1.add(101);
        path1.add(102);
        path1.add(103);
        path1.add(104);
        AGV1.add(path1);

        List<List<Integer>> AGV2 = new ArrayList<List<Integer>>();
        List<Integer> path2 = new ArrayList<Integer>();
        path2.add(104);
        path2.add(105);
        AGV2.add(path2);

        AGVPaths.add(AGV1);
        AGVPaths.add(AGV2);

        timeWindow.generateTimeWindow(AGVPaths,AGVSpeed,graph,timeAlreadyPassing,minDistance);
        List<TimeNode> timeSequence1 = timeWindow.getAGVTimeSequence(0);
        List<TimeNode> timeSequence2 = timeWindow.getAGVTimeSequence(1);

        // AGV1
        assertEquals(9,timeSequence1.size());
        assertEquals(105,timeSequence1.get(0).getNodeId());
        assertEquals((double)0,timeSequence1.get(0).getTime());

        assertEquals(7,timeSequence1.get(1).getNodeId());
        assertEquals((double)1,timeSequence1.get(1).getTime());

        assertEquals(8,timeSequence1.get(2).getNodeId());
        assertEquals((double)6,timeSequence1.get(2).getTime());

        assertEquals(1,timeSequence1.get(3).getNodeId());
        assertEquals((double)10,timeSequence1.get(3).getTime());

        assertEquals(0,timeSequence1.get(4).getNodeId());
        assertEquals((double)15,timeSequence1.get(4).getTime());

        assertEquals(101,timeSequence1.get(5).getNodeId());
        assertEquals((double)16,timeSequence1.get(5).getTime());

        assertEquals(102,timeSequence1.get(6).getNodeId());
        assertEquals((double)17,timeSequence1.get(6).getTime());

        assertEquals(103,timeSequence1.get(7).getNodeId());
        assertEquals((double)18,timeSequence1.get(7).getTime());

        assertEquals(104,timeSequence1.get(8).getNodeId());
        assertEquals((double)19,timeSequence1.get(8).getTime());


        // AGV2
        assertEquals(2,timeSequence2.size());
        assertEquals(104,timeSequence2.get(0).getNodeId());
        assertEquals((double)0,timeSequence2.get(0).getTime());

        assertEquals(105,timeSequence2.get(1).getNodeId());
        assertEquals((double)1,timeSequence2.get(1).getTime());
    }

    // One AGV goes to the next node beginning in an edge in the map,
    // the other heads to the next node in an edge in buffer
    @Test
    public void shouldTimeWindowWorksForScenario2() {
        TimeWindow timeWindow = new TimeWindow();
        Double[] timeAlreadyPassing = {1.0, 0.5};
        List<List<List<Integer>>> AGVPaths = new ArrayList<List<List<Integer>>>();

        List<List<Integer>> AGV1 = new ArrayList<List<Integer>>();
        List<Integer> path1 = new ArrayList<Integer>();
        path1.add(7);
        path1.add(8);
        path1.add(1);
        path1.add(0);
        path1.add(101);
        path1.add(102);
        path1.add(103);
        path1.add(104);
        AGV1.add(path1);

        List<List<Integer>> AGV2 = new ArrayList<List<Integer>>();
        List<Integer> path2 = new ArrayList<Integer>();
        path2.add(104);
        path2.add(105);
        AGV2.add(path2);

        AGVPaths.add(AGV1);
        AGVPaths.add(AGV2);

        timeWindow.generateTimeWindow(AGVPaths,AGVSpeed,graph,timeAlreadyPassing,minDistance);
        List<TimeNode> timeSequence1 = timeWindow.getAGVTimeSequence(0);
        List<TimeNode> timeSequence2 = timeWindow.getAGVTimeSequence(1);

        // AGV1
        assertEquals(7,timeSequence1.size());
        assertEquals(8,timeSequence1.get(0).getNodeId());
        assertEquals((double)4,timeSequence1.get(0).getTime());

        assertEquals(1,timeSequence1.get(1).getNodeId());
        assertEquals((double)8,timeSequence1.get(1).getTime());

        assertEquals(0,timeSequence1.get(2).getNodeId());
        assertEquals((double)13,timeSequence1.get(2).getTime());

        assertEquals(101,timeSequence1.get(3).getNodeId());
        assertEquals((double)14,timeSequence1.get(3).getTime());

        assertEquals(102,timeSequence1.get(4).getNodeId());
        assertEquals((double)15,timeSequence1.get(4).getTime());

        assertEquals(103,timeSequence1.get(5).getNodeId());
        assertEquals((double)16,timeSequence1.get(5).getTime());

        assertEquals(104,timeSequence1.get(6).getNodeId());
        assertEquals((double)17,timeSequence1.get(6).getTime());


        // AGV2
        assertEquals(1,timeSequence2.size());
        assertEquals(105,timeSequence2.get(0).getNodeId());
        assertEquals(0.5,timeSequence2.get(0).getTime());
    }

}