package org.spring.springboot.algorithmn.genetic_algorithm;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.exception.NoAGVInTheBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AGV_GA_Test {

    private double[][] graph;


    //Set up paths for testing
    private Path node4To9 = new Path(3, 8, 10 / CommonTestConstant.AGV_SPEED, false);
    private Path node9To2 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node2To3 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
    private Path node9To8 = new Path(8, 7, 10 / CommonTestConstant.AGV_SPEED, false);
    private Path node8To1 = new Path(7, 0, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node1To2 = new Path(0, 1, 10 / CommonTestConstant.AGV_SPEED, false);
    private Path node4To3 = new Path(3, 2, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node3To4 = new Path(2, 3, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node4To5 = new Path(3, 4, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node5To4 = new Path(4, 3, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node5To6 = new Path(4, 5, 10 / CommonTestConstant.AGV_SPEED, false);
    private Path node6To9 = new Path(5, 8, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node8To7 = new Path(7, 6, 8 / CommonTestConstant.AGV_SPEED, false);
    private Path node2To1 = new Path(1, 0, 10 / CommonTestConstant.AGV_SPEED, false);


    private Path buffer0path0 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer0path1 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer0path2 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer0path3 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer0path4 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer0path5 = new Path(105, 3, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer1path0 = new Path(0, 201, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer1path1 = new Path(201, 202, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer1path2 = new Path(202, 203, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer1path3 = new Path(203, 204, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer1path4 = new Path(204, 205, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    private Path buffer1path5 = new Path(205, 7, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
    
    
    
    
    @Before
    public void initializeGraph() throws IOException {
        graph = CommonTestConstant.initializeGraph();
    }

    //Create one task for one AGV starting from node 9 to node 2, Check if the path is optimal one. (4--9--2--3)
    @Test
    public void shouldOneTaskBePerformedAsOptimalConditionByOneAGV() throws NoAGVInTheBuffer {
        List<List<Integer>> bufferSet = CommonTestConstant.getOneBufferForTestGraph2();
        List<List<Path>> pathsForAGVs = new ArrayList<>();
        List<Path> pathsForAGV0 = new ArrayList<>();
        pathsForAGVs.add(pathsForAGV0);
        Path path0ForAGV0 = new Path(105, 105, 0, false);
        pathsForAGV0.add(path0ForAGV0);
        Integer[] bufferForAGVs = {0};
        Integer[][] tasks = {{8,1}};
        Double[] timeAlreadyPassed = {-1.0};
        AGV_GA geneticAlgorithmForAGV = new AGV_GA(graph, tasks, timeAlreadyPassed, pathsForAGVs, CommonTestConstant.AGV_SPEED, bufferSet, bufferForAGVs);
        List<List<Path>> paths = geneticAlgorithmForAGV.singleObjectGenericAlgorithm();
        List<Path> pathsAfterComputing = paths.get(0);
        assertEquals(1, paths.size());
        assertEquals(9, pathsAfterComputing.size());
        int index = -1;
        assertEquals(buffer0path5, pathsAfterComputing.get(++index));
        assertEquals(node4To9, pathsAfterComputing.get(++index));
        assertEquals(node9To2, pathsAfterComputing.get(++index));
        assertEquals(node2To3, pathsAfterComputing.get(++index));
        assertEquals(buffer0path0, pathsAfterComputing.get(++index));
        assertEquals(buffer0path1, pathsAfterComputing.get(++index));
        assertEquals(buffer0path2, pathsAfterComputing.get(++index));
        assertEquals(buffer0path3, pathsAfterComputing.get(++index));
        assertEquals(buffer0path4, pathsAfterComputing.get(++index));
    }

    //Create two tasks(8--1,9--8) for one AGV, Check if the path is optimal one. (4--9--8--1--2--3)
    @Test
    public void shouldTwoTasksBePerformedAsOptimalConditionByOneAGV() throws NoAGVInTheBuffer {
        List<List<Integer>> bufferSet = CommonTestConstant.getOneBufferForTestGraph2();
        List<List<Path>> pathsForAGVs = new ArrayList<>();
        List<Path> pathsForAGV0 = new ArrayList<>();
        pathsForAGVs.add(pathsForAGV0);
        Path path0ForAGV0 = new Path(105, 105, 0, false);
        pathsForAGV0.add(path0ForAGV0);
        Integer[] bufferForAGVs = {0};
        Integer[][] tasks = {{7, 0}, {8, 7}};
        Double[] timeAlreadyPassed = {-1.0};
        AGV_GA geneticAlgorithmForAGV = new AGV_GA(graph, tasks, timeAlreadyPassed, pathsForAGVs, CommonTestConstant.AGV_SPEED, bufferSet, bufferForAGVs);
        List<List<Path>> paths = geneticAlgorithmForAGV.singleObjectGenericAlgorithm();
        List<Path> pathsAfterComputing = paths.get(0);
        assertEquals(1, paths.size());
        assertEquals(11, pathsAfterComputing.size());
        int index = -1;
        assertEquals(buffer0path5, pathsAfterComputing.get(++index));
        assertEquals(node4To9, pathsAfterComputing.get(++index));
        assertEquals(node9To8, pathsAfterComputing.get(++index));
        assertEquals(node8To1, pathsAfterComputing.get(++index));
        assertEquals(node1To2, pathsAfterComputing.get(++index));
        assertEquals(node2To3, pathsAfterComputing.get(++index));
        assertEquals(buffer0path0, pathsAfterComputing.get(++index));
        assertEquals(buffer0path1, pathsAfterComputing.get(++index));
        assertEquals(buffer0path2, pathsAfterComputing.get(++index));
        assertEquals(buffer0path3, pathsAfterComputing.get(++index));
        assertEquals(buffer0path4, pathsAfterComputing.get(++index));
    }


    //Create two tasks(3--4, 4--5) for two AGVs in the same buffer, Check if the path is optimal one.
    //AGV 0 should go for 3--4 and AGV 1 should go for 4--5
    @Test
    public void shouldTwoTasksBePerformedAsOptimalConditionByTwoAGVs() throws NoAGVInTheBuffer {
        List<List<Integer>> bufferSet = CommonTestConstant.getOneBufferForTestGraph2();
        List<List<Path>> pathsForAGVs = new ArrayList<>();
        List<Path> pathsForAGV0 = new ArrayList<>();
        List<Path> pathsForAGV1 = new ArrayList<>();
        pathsForAGVs.add(pathsForAGV0);
        pathsForAGVs.add(pathsForAGV1);
        Path path0ForAGV0 = new Path(105, 105, 0, false);
        Path path0ForAGV1 = new Path(104, 104, 0, false);
        pathsForAGV0.add(path0ForAGV0);
        pathsForAGV1.add(path0ForAGV1);
        Integer[] bufferForAGVs = {0, 0};
        Integer[][] tasks = {{2, 3}, {3, 4}};
        Double[] timeAlreadyPassed = {-1.0, -1.0};
        AGV_GA geneticAlgorithmForAGV = new AGV_GA(graph, tasks, timeAlreadyPassed, pathsForAGVs, CommonTestConstant.AGV_SPEED, bufferSet, bufferForAGVs);
        List<List<Path>> paths = geneticAlgorithmForAGV.singleObjectGenericAlgorithm();
        assertEquals(2, paths.size());
        int index = -1;
        List<Path> paths0AfterComputing = paths.get(0);
        assertEquals(9, paths0AfterComputing.size());
        assertEquals(buffer0path5, paths0AfterComputing.get(++index));
        assertEquals(node4To3, paths0AfterComputing.get(++index));
        assertEquals(node3To4, paths0AfterComputing.get(++index));
        assertEquals(node4To3, paths0AfterComputing.get(++index));
        assertEquals(buffer0path0, paths0AfterComputing.get(++index));
        assertEquals(buffer0path1, paths0AfterComputing.get(++index));
        assertEquals(buffer0path2, paths0AfterComputing.get(++index));
        assertEquals(buffer0path3, paths0AfterComputing.get(++index));
        assertEquals(buffer0path4, paths0AfterComputing.get(++index));

        index = -1;
        List<Path> paths1AfterComputing = paths.get(1);
        assertEquals(9, paths1AfterComputing.size());
        assertEquals(buffer0path4, paths1AfterComputing.get(++index));
        assertEquals(buffer0path5, paths1AfterComputing.get(++index));
        assertEquals(node4To5, paths1AfterComputing.get(++index));
        assertEquals(node5To4, paths1AfterComputing.get(++index));
        assertEquals(node4To3, paths1AfterComputing.get(++index));
        assertEquals(buffer0path0, paths1AfterComputing.get(++index));
        assertEquals(buffer0path1, paths1AfterComputing.get(++index));
        assertEquals(buffer0path2, paths1AfterComputing.get(++index));
        assertEquals(buffer0path3, paths1AfterComputing.get(++index));
    }

    //Create two tasks(5--6, 7--6) for two AGVs in the different buffer, Check if the path is optimal one.
    //AGV 0 should go for 5--6 and AGV 1 should go for 7--6
    @Test
    public void shouldTwoTasksBePerformedAsOptimalConditionByTwoAGVsInDifferentBuffer() throws NoAGVInTheBuffer {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<List<Path>> pathsForAGVs = new ArrayList<>();
        List<Path> pathsForAGV0 = new ArrayList<>();
        List<Path> pathsForAGV1 = new ArrayList<>();
        pathsForAGVs.add(pathsForAGV0);
        pathsForAGVs.add(pathsForAGV1);
        Path path0ForAGV0 = new Path(105, 105, 0, false);
        Path path0ForAGV1 = new Path(205, 205, 0, false);
        pathsForAGV0.add(path0ForAGV0);
        pathsForAGV1.add(path0ForAGV1);
        Integer[] bufferForAGVs = {0, 1};
        Integer[][] tasks = {{4, 5}, {6, 5}};
        Double[] timeAlreadyPassed = {-1.0, -1.0};
        AGV_GA geneticAlgorithmForAGV = new AGV_GA(graph, tasks, timeAlreadyPassed, pathsForAGVs, CommonTestConstant.AGV_SPEED, bufferSet, bufferForAGVs);
        List<List<Path>> paths = geneticAlgorithmForAGV.singleObjectGenericAlgorithm();
        assertEquals(2, paths.size());
        double timeToReserveNode6 = (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED;
        Path adjustNode7To6 = new Path(6, 5, timeToReserveNode6 + 10 / CommonTestConstant.AGV_SPEED, false);
        int index = -1;
        List<Path> paths0AfterComputing = paths.get(0);
        assertEquals(11, paths0AfterComputing.size());
        assertEquals(buffer0path5, paths0AfterComputing.get(++index));
        assertEquals(node4To5, paths0AfterComputing.get(++index));
        assertEquals(node5To6, paths0AfterComputing.get(++index));
        assertEquals(node6To9, paths0AfterComputing.get(++index));
        assertEquals(node9To2, paths0AfterComputing.get(++index));
        assertEquals(node2To3, paths0AfterComputing.get(++index));
        assertEquals(buffer0path0, paths0AfterComputing.get(++index));
        assertEquals(buffer0path1, paths0AfterComputing.get(++index));
        assertEquals(buffer0path2, paths0AfterComputing.get(++index));
        assertEquals(buffer0path3, paths0AfterComputing.get(++index));
        assertEquals(buffer0path4, paths0AfterComputing.get(++index));

        index = -1;
        List<Path> paths1AfterComputing = paths.get(1);
        assertEquals(11, paths1AfterComputing.size());
        assertEquals(buffer1path5, paths1AfterComputing.get(++index));
        assertEquals(node8To7, paths1AfterComputing.get(++index));
        assertEquals(adjustNode7To6, paths1AfterComputing.get(++index));
        assertEquals(node6To9, paths1AfterComputing.get(++index));
        assertEquals(node9To2, paths1AfterComputing.get(++index));
        assertEquals(node2To1, paths1AfterComputing.get(++index));
        assertEquals(buffer1path0, paths1AfterComputing.get(++index));
        assertEquals(buffer1path1, paths1AfterComputing.get(++index));
        assertEquals(buffer1path2, paths1AfterComputing.get(++index));
        assertEquals(buffer1path3, paths1AfterComputing.get(++index));
        assertEquals(buffer1path4, paths1AfterComputing.get(++index));
    }

    
}
