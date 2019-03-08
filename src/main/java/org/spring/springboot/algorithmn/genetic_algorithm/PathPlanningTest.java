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

    //Start from node 8, AGV wants to go node 8. Path should form as 8-8. Time and fitness should remain the same.
    @Test
    public void shouldPathCreatedCorrectlyWhenAGVGoesToTheCurrentPosition() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        int startNode = 7;
        int endNode = 7;
        double[] fitness = {0};
        double[] time = {0};
        PathPlanning pathPlanning = new PathPlanning(1, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, 10);
        List<Path> paths = pathPlanning.getPath(routing, endNode, startNode, 0, fitness, time);
        assertEquals(1, paths.size());
        Path path1 = paths.get(0);
        assertEquals(7, path1.getStartNode());
        assertEquals(7, path1.getEndNode());
        assertEquals(0, path1.getTime(), 0.000001);
        assertFalse(path1.isLoop());
        assertEquals(0, fitness[0], 0.000001);
        assertEquals(0, time[0], 0.000001);
    }

    //One previous AGV 0 goes as 206--8--7. The current AGV 1 goes from 7 to 8.
    //It should adjust the route as 7--6--9--8 to avoid conflict. Time and fitness should be changed correspondingly.
    @Test
    public void shouldGetPathCorrectlyFromRoutingWhenThereIsOnePreviousAGVGoingFrom206To8To7() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        int startNode = 205;
        int endNode = 6;
        double[] fitness = {0, 0};
        double[] time = {0, 0};
        PathPlanning pathPlanning = new PathPlanning(1, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, 10);
        pathPlanning.getPath(routing, endNode, startNode, 0, fitness, time);
        int currentStartNode = 6;
        int currentEndNode = 7;
        List<Path> paths = pathPlanning.getPath(routing, currentEndNode, currentStartNode, 1, fitness, time);
        assertEquals(3, paths.size());
        Path path1 = paths.get(0);
        assertEquals(6, path1.getStartNode());
        assertEquals(5, path1.getEndNode());
        assertEquals(10 / CommonTestConstant.AGV_SPEED, path1.getTime(), 0.000001);
        assertFalse(path1.isLoop());
        Path path2 = paths.get(1);
        assertEquals(5, path2.getStartNode());
        assertEquals(8, path2.getEndNode());
        assertEquals(8 / CommonTestConstant.AGV_SPEED, path2.getTime(), 0.000001);
        assertFalse(path2.isLoop());
        Path path3 = paths.get(2);
        assertEquals(8, path3.getStartNode());
        assertEquals(7, path3.getEndNode());
        assertEquals(10 / CommonTestConstant.AGV_SPEED, path3.getTime(), 0.000001);
        assertFalse(path3.isLoop());
        assertEquals((10 * 2 + 8 + 3 * CommonConstant.CROSSING_DISTANCE)/ CommonTestConstant.AGV_SPEED, fitness[1], 0.000001);
        assertEquals((10 * 2 + 8 + 3 * CommonConstant.CROSSING_DISTANCE)/ CommonTestConstant.AGV_SPEED, time[1], 0.000001);
    }

//    //
//    @Test
//    public void shouldTwoAGVsAdjustTheirPositionsIn3SpaceBufferWhenOneAGVsGoesOutOfTheBuffer() {
//
//    }
}
