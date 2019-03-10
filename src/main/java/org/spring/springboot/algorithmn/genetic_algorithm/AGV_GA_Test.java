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

    @Before
    public void initializeGraph() throws IOException {
        graph = CommonTestConstant.initializeGraph();
    }

    //Create one task for one AGV starting from node 9 to node 2, Check if the path is optimal one.
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
        assertEquals(10, pathsAfterComputing.size());
        //Set up buffer path for testing
        Path buffer0path0 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path1 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path2 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path3 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path4 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path5 = new Path(105, 3, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path node4To9 = new Path(3, 8, 10 / CommonTestConstant.AGV_SPEED, false);
        Path node9To2 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path node2To3 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        assertEquals(path0ForAGV0, pathsAfterComputing.get(0));
        assertEquals(buffer0path5, pathsAfterComputing.get(1));
        assertEquals(node4To9, pathsAfterComputing.get(2));
        assertEquals(node9To2, pathsAfterComputing.get(3));
        assertEquals(node2To3, pathsAfterComputing.get(4));
        assertEquals(buffer0path0, pathsAfterComputing.get(5));
        assertEquals(buffer0path1, pathsAfterComputing.get(6));
        assertEquals(buffer0path2, pathsAfterComputing.get(7));
        assertEquals(buffer0path3, pathsAfterComputing.get(8));
        assertEquals(buffer0path4, pathsAfterComputing.get(9));
    }
}
