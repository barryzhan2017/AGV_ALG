//package org.spring.springboot.algorithmn.test;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.spring.springboot.algorithmn.GA.AGVRecord;
//import org.spring.springboot.algorithmn.GA.TimeNode;
//import org.spring.springboot.algorithmn.GA.TimeWindow;
//import org.spring.springboot.algorithmn.GA.TimeWindowStrategy;
//import org.ujmp.core.Matrix;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static junit.framework.TestCase.assertEquals;
//
//public class TimeWindowStrategyTest {
//
//    private double[][] graph;
//
//
//    @Before
//    public void initializeGraph() throws IOException {
//        //从csv文件中读取矩阵
//        File file = new File("TestGraphSet/TestGraph2.csv");
//        graph = (Matrix.Factory.importFrom().file(file).asDenseCSV()).toDoubleArray();
////        System.out.println(graph);
//
//    }
//
//
//    //Two AGV paths overlap at node 9 and node 2, the later one in the path should slow down
//    @Test
//    public void shouldNodeConvergeConflictSolvedWhenTwoAGVsConvergeTwiceInPath() {
//        TimeWindow timeWindow = new TimeWindow();
//        Double[] timeAlreadyPassing = {-1.0, -1.0};
//        List<List<Integer>> AGVPaths = new ArrayList<>();
//        List<Integer> path1 = new ArrayList<>();
//        path1.add(105);
//        path1.add(7);
//        path1.add(8);
//        path1.add(1);
//        path1.add(0);
////        path1.add(101);
////        path1.add(102);
////        path1.add(103);
////        path1.add(104);
////        path1.add(105);
//        AGVPaths.add(path1);
//
//
//
//        List<Integer> path2 = new ArrayList<Integer>();
//        path2.add(205);
//        path2.add(3);
//        path2.add(8);
//        path2.add(1);
//        path2.add(2);
////        path2.add(201);
////        path2.add(202);
////        path2.add(203);
////        path2.add(204);
////        path2.add(205);
//        AGVPaths.add(path2);
//        List<List<AGVRecord>> AGVRecords = CommonTestMethod.manyPathsToRecords(AGVPaths, graph[0].length);
//
//        timeWindow.generateTimeWindow(AGVPaths, CommonTestMethod.SPEED, graph, timeAlreadyPassing, CommonTestMethod.MIN_DISTANCE);
//        TimeWindowStrategy timeWindowStrategy = new TimeWindowStrategy(CommonTestMethod.SPEED,graph,
//                timeAlreadyPassing,CommonTestMethod.MIN_DISTANCE, AGVRecords);
//        timeWindowStrategy.resolveNodeConflict(timeWindow,AGVPaths);
//
//        timeWindow.getAGVTimeSequence(0);
//        List<TimeNode> timeSequence1 = timeWindow.getAGVTimeSequence(0);
//        List<TimeNode> timeSequence2 = timeWindow.getAGVTimeSequence(1);
//        List<AGVRecord> record1 = AGVRecords.get(0);
//        List<AGVRecord> record3 = AGVRecords.get(1);
//
//
//        //AGV1 Path
//        assertEquals(5,path1.size());
//        assertEquals(105,(int)path1.get(0));
//        assertEquals(7,(int)path1.get(1));
//        assertEquals(8,(int)path1.get(2));
//        assertEquals(1,(int)path1.get(3));
//        assertEquals(0,(int)path1.get(4));
////        assertEquals(101,(int)path1.get(5));
////        assertEquals(102,(int)path1.get(6));
////        assertEquals(103,(int)path1.get(7));
////        assertEquals(104,(int)path1.get(8));
////        assertEquals(105,(int)path1.get(9));
//
//        //AGV1 Records
//        assertEquals(4,record1.size());
//        assertEquals(CommonTestMethod.SPEED,record1.get(0).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record1.get(1).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record1.get(2).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record1.get(3).getSpeed());
//
//        // AGV1 time sequence
//        assertEquals(5,timeSequence1.size());
//
//        assertEquals(105,timeSequence1.get(0).getNodeId());
//        assertEquals((double)0,timeSequence1.get(0).getTime());
//        assertEquals(0,timeSequence1.get(0).getNumberOfStep());
//
//        assertEquals(7,timeSequence1.get(1).getNodeId());
//        assertEquals((double)1,timeSequence1.get(1).getTime());
//        assertEquals(1,timeSequence1.get(1).getNumberOfStep());
//
//        assertEquals(8,timeSequence1.get(2).getNodeId());
//        assertEquals((double)6,timeSequence1.get(2).getTime());
//        assertEquals(2,timeSequence1.get(2).getNumberOfStep());
//
//        assertEquals(1,timeSequence1.get(3).getNodeId());
//        assertEquals((double)10,timeSequence1.get(3).getTime());
//        assertEquals(3,timeSequence1.get(3).getNumberOfStep());
//
//        assertEquals(0,timeSequence1.get(4).getNodeId());
//        assertEquals((double)15,timeSequence1.get(4).getTime());
//        assertEquals(4,timeSequence1.get(4).getNumberOfStep());
//
////        assertEquals(101,timeSequence1.get(5).getNodeId());
////        assertEquals((double)16,timeSequence1.get(5).getTime());
////        assertEquals(5,timeSequence1.get(5).getNumberOfStep());
////
////        assertEquals(102,timeSequence1.get(6).getNodeId());
////        assertEquals((double)17,timeSequence1.get(6).getTime());
////        assertEquals(6,timeSequence1.get(6).getNumberOfStep());
////
////        assertEquals(103,timeSequence1.get(7).getNodeId());
////        assertEquals((double)18,timeSequence1.get(7).getTime());
////        assertEquals(7,timeSequence1.get(7).getNumberOfStep());
////
////        assertEquals(104,timeSequence1.get(8).getNodeId());
////        assertEquals((double)19,timeSequence1.get(8).getTime());
////        assertEquals(8,timeSequence1.get(8).getNumberOfStep());
////
////        assertEquals(105,timeSequence1.get(9).getNodeId());
////        assertEquals((double)20,timeSequence1.get(9).getTime());
////        assertEquals(9,timeSequence1.get(9).getNumberOfStep());
//
//
//
//        //AGV2 Path
//        assertEquals(5,path2.size());
//        assertEquals(205,(int)path2.get(0));
//        assertEquals(3,(int)path2.get(1));
//        assertEquals(8,(int)path2.get(2));
//        assertEquals(1,(int)path2.get(3));
//        assertEquals(2,(int)path2.get(4));
////        assertEquals(201,(int)path2.get(5));
////        assertEquals(202,(int)path2.get(6));
////        assertEquals(203,(int)path2.get(7));
////        assertEquals(204,(int)path2.get(8));
////        assertEquals(205,(int)path2.get(9));
//
//        //AGV2 Records
//        assertEquals(4,record3.size());
//        assertEquals(CommonTestMethod.SPEED,record3.get(0).getSpeed());
//        assertEquals( 10*CommonTestMethod.SPEED/(CommonTestMethod.MIN_DISTANCE + 10), record3.get(1).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record3.get(2).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record3.get(3).getSpeed());
//
//
//        // AGV2 time sequence
//        assertEquals(5,timeSequence2.size());
//
//        assertEquals(205,timeSequence2.get(0).getNodeId());
//        assertEquals((double)0,timeSequence2.get(0).getTime());
//        assertEquals(0,timeSequence2.get(0).getNumberOfStep());
//
//        assertEquals(3,timeSequence2.get(1).getNodeId());
//        assertEquals((double)1,timeSequence2.get(1).getTime());
//        assertEquals(1,timeSequence2.get(1).getNumberOfStep());
//
//        assertEquals(8,timeSequence2.get(2).getNodeId());
//        assertEquals((double)7,timeSequence2.get(2).getTime());
//        assertEquals(2,timeSequence2.get(2).getNumberOfStep());
//
//        assertEquals(1,timeSequence2.get(3).getNodeId());
//        assertEquals((double)11,timeSequence2.get(3).getTime());
//        assertEquals(3,timeSequence2.get(3).getNumberOfStep());
//
//        assertEquals(2,timeSequence2.get(4).getNodeId());
//        assertEquals((double)16,timeSequence2.get(4).getTime());
//        assertEquals(4,timeSequence2.get(4).getNumberOfStep());
//
////        assertEquals(201,timeSequence2.get(5).getNodeId());
////        assertEquals((double)17,timeSequence2.get(5).getTime());
////        assertEquals(5,timeSequence2.get(5).getNumberOfStep());
////
////        assertEquals(202,timeSequence2.get(6).getNodeId());
////        assertEquals((double)18,timeSequence2.get(6).getTime());
////        assertEquals(6,timeSequence2.get(6).getNumberOfStep());
////
////        assertEquals(203,timeSequence2.get(7).getNodeId());
////        assertEquals((double)19,timeSequence2.get(7).getTime());
////        assertEquals(7,timeSequence2.get(7).getNumberOfStep());
////
////        assertEquals(204,timeSequence2.get(8).getNodeId());
////        assertEquals((double)20,timeSequence2.get(8).getTime());
////        assertEquals(8,timeSequence2.get(8).getNumberOfStep());
////
////        assertEquals(205,timeSequence2.get(9).getNodeId());
////        assertEquals((double)21,timeSequence2.get(9).getTime());
////        assertEquals(9,timeSequence2.get(9).getNumberOfStep());
//
//    }
//
//    //Three AGV paths overlap at node 9, the two latter should adjust their speed
//    @Test
//    public void shouldNodeConvergeConflictSolvedWhenThreeAGVsConvergeInNode() {
//        TimeWindow timeWindow = new TimeWindow();
//        Double[] timeAlreadyPassing = {-1.0, -1.0, 3.0};
//        List<List<Integer>> AGVPaths = new ArrayList<List<Integer>>();
//        List<Integer> path1 = new ArrayList<Integer>();
//        path1.add(105);
//        path1.add(7);
//        path1.add(8);
//        path1.add(1);
//        path1.add(0);
////        path1.add(101);
////        path1.add(102);
////        path1.add(103);
////        path1.add(104);
////        path1.add(105);
//        AGVPaths.add(path1);
//
//        List<Integer> path2 = new ArrayList<Integer>();
//        path2.add(205);
//        path2.add(3);
//        path2.add(8);
//        path2.add(1);
//        path2.add(2);
////        path2.add(201);
////        path2.add(202);
////        path2.add(203);
////        path2.add(204);
////        path2.add(205);
//        AGVPaths.add(path2);
//
//        List<Integer> path3 = new ArrayList<Integer>();
//        path3.add(6);
//        path3.add(5);
//        path3.add(8);
//        path3.add(1);
//        path3.add(2);
////        path3.add(201);
////        path3.add(202);
////        path3.add(203);
////        path3.add(204);
//        AGVPaths.add(path3);
//        List<List<AGVRecord>> AGVRecords  = CommonTestMethod.manyPathsToRecords(AGVPaths,graph[0].length);
//
//
//        timeWindow.generateTimeWindow(AGVPaths, CommonTestMethod.SPEED, graph, timeAlreadyPassing, CommonTestMethod.MIN_DISTANCE);
//        TimeWindowStrategy timeWindowStrategy = new TimeWindowStrategy(CommonTestMethod.SPEED, graph,timeAlreadyPassing,
//                CommonTestMethod.MIN_DISTANCE, AGVRecords);
//        timeWindowStrategy.resolveNodeConflict(timeWindow,AGVPaths);
//
//        timeWindow.getAGVTimeSequence(0);
//        List<TimeNode> timeSequence1 = timeWindow.getAGVTimeSequence(0);
//        List<TimeNode> timeSequence2 = timeWindow.getAGVTimeSequence(1);
//        List<TimeNode> timeSequence3 = timeWindow.getAGVTimeSequence(2);
//        List<AGVRecord> record1 = AGVRecords.get(0);
//        List<AGVRecord> record2 = AGVRecords.get(1);
//        List<AGVRecord> record3 = AGVRecords.get(2);
//
//        //AGV1 Path
//        assertEquals(5,path1.size());
//        assertEquals(105,(int)path1.get(0));
//        assertEquals(7,(int)path1.get(1));
//        assertEquals(8,(int)path1.get(2));
//        assertEquals(1,(int)path1.get(3));
//        assertEquals(0,(int)path1.get(4));
////        assertEquals(101,(int)path1.get(5));
////        assertEquals(102,(int)path1.get(6));
////        assertEquals(103,(int)path1.get(7));
////        assertEquals(104,(int)path1.get(8));
////        assertEquals(105,(int)path1.get(9));
//
//        //AGV1 Records
//        assertEquals(4,record1.size());
//        assertEquals(CommonTestMethod.SPEED,record1.get(0).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record1.get(1).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record1.get(2).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record1.get(3).getSpeed());
//
//        // AGV1 time sequence
//        assertEquals(5,timeSequence1.size());
//
//        assertEquals(105,timeSequence1.get(0).getNodeId());
//        assertEquals((double)0,timeSequence1.get(0).getTime());
//        assertEquals(0,timeSequence1.get(0).getNumberOfStep());
//
//        assertEquals(7,timeSequence1.get(1).getNodeId());
//        assertEquals((double)1,timeSequence1.get(1).getTime());
//        assertEquals(1,timeSequence1.get(1).getNumberOfStep());
//
//        assertEquals(8,timeSequence1.get(2).getNodeId());
//        assertEquals((double)6,timeSequence1.get(2).getTime());
//        assertEquals(2,timeSequence1.get(2).getNumberOfStep());
//
//        assertEquals(1,timeSequence1.get(3).getNodeId());
//        assertEquals((double)10,timeSequence1.get(3).getTime());
//        assertEquals(3,timeSequence1.get(3).getNumberOfStep());
//
//        assertEquals(0,timeSequence1.get(4).getNodeId());
//        assertEquals((double)15,timeSequence1.get(4).getTime());
//        assertEquals(4,timeSequence1.get(4).getNumberOfStep());
//
////        assertEquals(101,timeSequence1.get(5).getNodeId());
////        assertEquals((double)16,timeSequence1.get(5).getTime());
////        assertEquals(5,timeSequence1.get(5).getNumberOfStep());
////
////        assertEquals(102,timeSequence1.get(6).getNodeId());
////        assertEquals((double)17,timeSequence1.get(6).getTime());
////        assertEquals(6,timeSequence1.get(6).getNumberOfStep());
////
////        assertEquals(103,timeSequence1.get(7).getNodeId());
////        assertEquals((double)18,timeSequence1.get(7).getTime());
////        assertEquals(7,timeSequence1.get(7).getNumberOfStep());
////
////        assertEquals(104,timeSequence1.get(8).getNodeId());
////        assertEquals((double)19,timeSequence1.get(8).getTime());
////        assertEquals(8,timeSequence1.get(8).getNumberOfStep());
////
////        assertEquals(105,timeSequence1.get(9).getNodeId());
////        assertEquals((double)20,timeSequence1.get(9).getTime());
////        assertEquals(9,timeSequence1.get(9).getNumberOfStep());
//
//        //AGV2 Path
//        assertEquals(5, path2.size());
//        assertEquals(205,(int)path2.get(0));
//        assertEquals(3,(int)path2.get(1));
//        assertEquals(8,(int)path2.get(2));
//        assertEquals(1,(int)path2.get(3));
//        assertEquals(2,(int)path2.get(4));
////        assertEquals(201,(int)path2.get(5));
////        assertEquals(202,(int)path2.get(6));
////        assertEquals(203,(int)path2.get(7));
////        assertEquals(204,(int)path2.get(8));
//
//        //AGV2 Records
//        assertEquals(4,record2.size());
//        assertEquals(CommonTestMethod.SPEED,record2.get(0).getSpeed());
//        assertEquals( 10*CommonTestMethod.SPEED/(CommonTestMethod.MIN_DISTANCE + 10), record2.get(1).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record2.get(2).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record2.get(3).getSpeed());
//
//
//        // AGV2 time sequence
//        assertEquals(5,timeSequence2.size());
//
//        assertEquals(205,timeSequence2.get(0).getNodeId());
//        assertEquals((double)0,timeSequence2.get(0).getTime());
//        assertEquals(0,timeSequence2.get(0).getNumberOfStep());
//
//        assertEquals(3,timeSequence2.get(1).getNodeId());
//        assertEquals((double)1,timeSequence2.get(1).getTime());
//        assertEquals(1,timeSequence2.get(1).getNumberOfStep());
//
//        assertEquals(8,timeSequence2.get(2).getNodeId());
//        assertEquals((double)7,timeSequence2.get(2).getTime());
//        assertEquals(2,timeSequence2.get(2).getNumberOfStep());
//
//        assertEquals(1,timeSequence2.get(3).getNodeId());
//        assertEquals((double)11,timeSequence2.get(3).getTime());
//        assertEquals(3,timeSequence2.get(3).getNumberOfStep());
//
//        assertEquals(2,timeSequence2.get(4).getNodeId());
//        assertEquals((double)16,timeSequence2.get(4).getTime());
//        assertEquals(4,timeSequence2.get(4).getNumberOfStep());
//
////        assertEquals(201,timeSequence2.get(5).getNodeId());
////        assertEquals((double)17,timeSequence2.get(5).getTime());
////        assertEquals(5,timeSequence2.get(5).getNumberOfStep());
////
////        assertEquals(202,timeSequence2.get(6).getNodeId());
////        assertEquals((double)18,timeSequence2.get(6).getTime());
////        assertEquals(6,timeSequence2.get(6).getNumberOfStep());
////
////        assertEquals(203,timeSequence2.get(7).getNodeId());
////        assertEquals((double)19,timeSequence2.get(7).getTime());
////        assertEquals(7,timeSequence2.get(7).getNumberOfStep());
////
////        assertEquals(204,timeSequence2.get(8).getNodeId());
////        assertEquals((double)20,timeSequence2.get(8).getTime());
////        assertEquals(8,timeSequence2.get(8).getNumberOfStep());
////
////        assertEquals(205,timeSequence2.get(9).getNodeId());
////        assertEquals((double)21,timeSequence2.get(9).getTime());
////        assertEquals(9,timeSequence2.get(9).getNumberOfStep());
//
//
//        //AGV3 Path
//        assertEquals(5,path3.size());
//        assertEquals(6,(int)path3.get(0));
//        assertEquals(5,(int)path3.get(1));
//        assertEquals(8,(int)path3.get(2));
//        assertEquals(1,(int)path3.get(3));
//        assertEquals(2,(int)path3.get(4));
////        assertEquals(201,(int)path3.get(5));
////        assertEquals(202,(int)path3.get(6));
////        assertEquals(203,(int)path3.get(7));
////        assertEquals(204,(int)path3.get(8));
////        assertEquals(205,(int)path3.get(9));
//
//
//        //AGV3 Records
//        assertEquals(4,record3.size());
//        assertEquals(CommonTestMethod.SPEED,record3.get(0).getSpeed());
//        assertEquals( 10*CommonTestMethod.SPEED/(2*CommonTestMethod.MIN_DISTANCE + 10), record3.get(1).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record3.get(2).getSpeed());
//        assertEquals(CommonTestMethod.SPEED,record3.get(3).getSpeed());
//
//        // AGV3 time sequence
//        assertEquals(4,timeSequence3.size());
//
//        assertEquals(5,timeSequence3.get(0).getNodeId());
//        assertEquals((double)2,timeSequence3.get(0).getTime());
//        assertEquals(1,timeSequence3.get(0).getNumberOfStep());
//
//        assertEquals(8,timeSequence3.get(1).getNodeId());
//        assertEquals((double)8,timeSequence3.get(1).getTime());
//        assertEquals(2,timeSequence3.get(1).getNumberOfStep());
//
//        assertEquals(1,timeSequence3.get(2).getNodeId());
//        assertEquals((double)12,timeSequence3.get(2).getTime());
//        assertEquals(3,timeSequence3.get(2).getNumberOfStep());
//
//        assertEquals(2,timeSequence3.get(3).getNodeId());
//        assertEquals((double)17,timeSequence3.get(3).getTime());
//        assertEquals(4,timeSequence3.get(3).getNumberOfStep());
//
////
////        assertEquals(201,timeSequence3.get(5).getNodeId());
////        assertEquals((double)17,timeSequence3.get(5).getTime());
////        assertEquals(5,timeSequence3.get(5).getNumberOfStep());
////
////        assertEquals(202,timeSequence3.get(6).getNodeId());
////        assertEquals((double)18,timeSequence3.get(6).getTime());
////        assertEquals(6,timeSequence3.get(6).getNumberOfStep());
////
////        assertEquals(203,timeSequence3.get(7).getNodeId());
////        assertEquals((double)19,timeSequence3.get(7).getTime());
////        assertEquals(7,timeSequence3.get(7).getNumberOfStep());
////
////        assertEquals(204,timeSequence3.get(8).getNodeId());
////        assertEquals((double)20,timeSequence3.get(8).getTime());
////        assertEquals(8,timeSequence3.get(8).getNumberOfStep());
//
//
//    }
//
//
//}
