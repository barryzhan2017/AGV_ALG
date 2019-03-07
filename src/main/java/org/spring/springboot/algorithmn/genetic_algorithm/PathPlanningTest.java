package org.spring.springboot.algorithmn.genetic_algorithm;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class PathPlanningTest {

    private double[][] graph;
    private double penalty = 9999;

    @Before
    public void initializeGraph() throws IOException {
        graph = CommonTestConstant.initializeGraph();
    }

    //Start from node 206, AGV wants to go node 7. Path should form as 206--8--7. Time and fitness should be changed correspondingly.
    @Test
    public void shouldGetPathCorrectlyFromRoutingWhenThereIsNoPreviousAGV() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        int startNode = 205;
        int endNode = 6;
        double[] fitness = {0};
        double[] time = {0};
        PathPlanning pathPlanning = new PathPlanning(1, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, 10);
        List<Path> paths = pathPlanning.getPath(routing, endNode, startNode, 0, fitness, time);
        assertEquals(2, paths.size());
        Path path1 = paths.get(0);
        assertEquals(205, path1.getStartNode());
        assertEquals(7, path1.getEndNode());
        assertEquals(CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, path1.getTime(), 0.000001);
        assertFalse(path1.isLoop());
        Path path2 = paths.get(1);
        assertEquals(7, path2.getStartNode());
        assertEquals(6, path2.getEndNode());
        assertEquals(8 / CommonTestConstant.AGV_SPEED, path2.getTime(), 0.000001);
        assertFalse(path2.isLoop());
        assertEquals((CommonConstant.BUFFER_PATH_LENGTH + 8 + 2 * CommonConstant.CROSSING_DISTANCE)/ CommonTestConstant.AGV_SPEED, fitness[0], 0.000001);
        assertEquals((CommonConstant.BUFFER_PATH_LENGTH + 8 + 2 * CommonConstant.CROSSING_DISTANCE)/ CommonTestConstant.AGV_SPEED, time[0], 0.000001);
    }
}
