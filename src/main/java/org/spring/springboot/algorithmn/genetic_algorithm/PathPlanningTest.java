package org.spring.springboot.algorithmn.genetic_algorithm;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindow;
import org.spring.springboot.algorithmn.exception.NoAGVInTheBuffer;

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
        PathPlanning pathPlanning = new PathPlanning(2, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
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

    //The AGV gets out of the buffer 0 from node 103 to node 4, check if the other AGV will be moved forward
    //Don't check the time window in buffer because it will not affect the algorithm
    @Test
    public void shouldTwoAGVsAdjustTheirPositionsIn5SpaceBufferWhenOneAGVsGoesOutOfTheBuffer() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, 10);
        routing.setInitialCapacity(10);
        List<List<Path>> generationForAGVPath = new ArrayList<>();
        List<Path> pathForAGV0 = new ArrayList<>();
        List<Path> pathForAGV1 = new ArrayList<>();
        List<Path> pathForAGV2 = new ArrayList<>();
        generationForAGVPath.add(pathForAGV0);
        generationForAGVPath.add(pathForAGV1);
        generationForAGVPath.add(pathForAGV2);
        Path currentPathForAGV0 = new Path(103, 103, 0, false);
        pathForAGV0.add(currentPathForAGV0);
        Path currentPathForAGV1 = new Path(104, 104, 0, false);
        pathForAGV1.add(currentPathForAGV1);
        Path currentPath0ForAGV2 = new Path(105, 105, 0, false);
        pathForAGV2.add(currentPath0ForAGV2);
        Path currentPath1ForAGV2 = new Path(105, 3, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        pathForAGV2.add(currentPath1ForAGV2);
        //Actually fitness for AGV 2 should not be 0. Be 0 is for convenience.
        double[] fitness = {0, 0, 0};
        PathPlanning pathPlanning = new PathPlanning(3, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
        pathPlanning.adjustOtherAGVPositions(bufferSet.get(0), generationForAGVPath, fitness, routing);
        
        assertEquals(2, pathForAGV0.size());
        Path path0ForAGV0 = pathForAGV0.get(0);
        assertEquals(currentPathForAGV0, path0ForAGV0);
        Path path1ForAGV0 = pathForAGV0.get(1);
        assertEquals(103, path1ForAGV0.getStartNode());
        assertEquals(104, path1ForAGV0.getEndNode());
        assertEquals(CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED,
                path1ForAGV0.getTime(), 0.000001);
        assertFalse(path1ForAGV0.isLoop());

        assertEquals(2, pathForAGV1.size());
        Path path0ForAGV1 = pathForAGV1.get(0);
        assertEquals(currentPathForAGV1, path0ForAGV1);
        Path path1ForAGV1 = pathForAGV1.get(1);
        assertEquals(104, path1ForAGV1.getStartNode());
        assertEquals(105, path1ForAGV1.getEndNode());
        assertEquals(CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED,
                path1ForAGV1.getTime(), 0.000001);
        assertFalse(path1ForAGV1.isLoop());

        assertEquals(2, pathForAGV2.size());
        Path path0ForAGV2 = pathForAGV2.get(0);
        assertEquals(currentPath0ForAGV2, path0ForAGV2);
        Path path1ForAGV2 = pathForAGV2.get(1);
        assertEquals(currentPath1ForAGV2, path1ForAGV2);

        assertEquals((CommonConstant.BUFFER_PATH_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, fitness[0], 0.000001);
        assertEquals((CommonConstant.BUFFER_PATH_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, fitness[1], 0.000001);
        assertEquals(0, fitness[2], 0.000001);
    }

    //Two AGVs are returning to the same buffer and the other one is return for one buffer, check if their final routing is correct.
    //One AGV is still in the buffer when the other is coming in.
    @Test
    public void shouldAllOfAGVsFrom2BuffersBackToTheBufferAccordingToTheirTimeArrivingEntry() throws NoAGVInTheBuffer {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Integer[] bufferForAGV = {0, 0, 1, 1};
        List<List<Path>> generationForAGVPath = new ArrayList<>();
        List<Path> pathForAGV0 = new ArrayList<>();
        List<Path> pathForAGV1 = new ArrayList<>();
        List<Path> pathForAGV2 = new ArrayList<>();
        List<Path> pathForAGV3 = new ArrayList<>();
        generationForAGVPath.add(pathForAGV0);
        generationForAGVPath.add(pathForAGV1);
        generationForAGVPath.add(pathForAGV2);
        generationForAGVPath.add(pathForAGV3);
        //AGV 0 Path is 4--3
        double time0 = 8 / CommonTestConstant.AGV_SPEED;
        Path path1ForAGV0 = new Path(3,2, time0, false);
        pathForAGV0.add(path1ForAGV0);
        //AGV 1 Path is 2--3
        double time1 = 10 / CommonTestConstant.AGV_SPEED;
        Path path1ForAGV1 = new Path(1,2, time1, false);
        pathForAGV1.add(path1ForAGV1);
        //AGV 2 Path is 8--1
        double time2 = 8 / CommonTestConstant.AGV_SPEED;
        Path path1ForAGV2 = new Path(7,0, time2, false);
        pathForAGV2.add(path1ForAGV2);
        //AGV 3 Path is just node 206
        double time3 = 0;
        Path path1ForAGV3 = new Path(205,205, time3, false);
        pathForAGV3.add(path1ForAGV3);
        //Actually fitness for AGV 2 should not be 0. Be 0 is for convenience.
        double[] time = {time0 + CommonTestConstant.timeForCrossingBuffers(5),
                time1 + CommonTestConstant.timeForCrossingBuffers(5),
                time2 + CommonTestConstant.timeForCrossingBuffers(5),
                time3};
        PathPlanning pathPlanning = new PathPlanning(4, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
        pathPlanning.setBackingAGV(0);
        pathPlanning.setBackingAGV(1);
        pathPlanning.setBackingAGV(2);
        pathPlanning.navigateAGVsToInnerBuffer(generationForAGVPath, bufferSet, bufferForAGV, time);
        //Set up buffer path for testing
        Path buffer0path0 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path1 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path2 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path3 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path4 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);

        Path buffer1path0 = new Path(0, 201, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer1path1 = new Path(201, 202, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer1path2 = new Path(202, 203, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer1path3 = new Path(203, 204, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);

        //Check for AGV 0
        assertEquals(6, pathForAGV0.size());
        assertEquals(path1ForAGV0, pathForAGV0.get(0));
        assertEquals(buffer0path0, pathForAGV0.get(1));
        assertEquals(buffer0path1, pathForAGV0.get(2));
        assertEquals(buffer0path2, pathForAGV0.get(3));
        assertEquals(buffer0path3, pathForAGV0.get(4));
        assertEquals(buffer0path4, pathForAGV0.get(5));
        
        //Check for AGV 1
        assertEquals(5, pathForAGV1.size());
        assertEquals(path1ForAGV1, pathForAGV1.get(0));
        assertEquals(buffer0path0, pathForAGV1.get(1));
        assertEquals(buffer0path1, pathForAGV1.get(2));
        assertEquals(buffer0path2, pathForAGV1.get(3));
        assertEquals(buffer0path3, pathForAGV1.get(4));

        //Check for AGV 2
        assertEquals(5, pathForAGV2.size());
        assertEquals(path1ForAGV2, pathForAGV2.get(0));
        assertEquals(buffer1path0, pathForAGV2.get(1));
        assertEquals(buffer1path1, pathForAGV2.get(2));
        assertEquals(buffer1path2, pathForAGV2.get(3));
        assertEquals(buffer1path3, pathForAGV2.get(4));

        //Check for AGV 3
        assertEquals(1, pathForAGV3.size());
        assertEquals(path1ForAGV3, pathForAGV3.get(0));
    }


    //Five AGVs are returning to the same buffer, check if their final routing is correct. Two AGVs just adjust their position and stay idle.
    @Test
    public void shouldAllOfAGVsBackToTheBufferAccordingToTheirTimeArrivingEntry() throws NoAGVInTheBuffer {
        List<List<Integer>> bufferSet = CommonTestConstant.getOneBufferForTestGraph2();
        Integer[] bufferForAGV = {0, 0, 0, 0, 0};
        //Set up buffer path
        Path buffer0path0 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path1 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path2 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path3 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path buffer0path4 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        List<List<Path>> generationForAGVPath = new ArrayList<>();
        List<Path> pathForAGV0 = new ArrayList<>();
        List<Path> pathForAGV1 = new ArrayList<>();
        List<Path> pathForAGV2 = new ArrayList<>();
        List<Path> pathForAGV3 = new ArrayList<>();
        List<Path> pathForAGV4 = new ArrayList<>();
        generationForAGVPath.add(pathForAGV0);
        generationForAGVPath.add(pathForAGV1);
        generationForAGVPath.add(pathForAGV2);
        generationForAGVPath.add(pathForAGV3);
        generationForAGVPath.add(pathForAGV4);
        //AGV 0 Path is 4--3
        double time0 = 8 / CommonTestConstant.AGV_SPEED;
        Path path1ForAGV0 = new Path(3,2, time0, false);
        pathForAGV0.add(path1ForAGV0);
        //AGV 1 Path is 2--3
        double time1 = 10 / CommonTestConstant.AGV_SPEED;
        Path path1ForAGV1 = new Path(1,2, time1, false);
        pathForAGV1.add(path1ForAGV1);
        //AGV 2 Path loops at node 3 by going 3--2--3
        double time2 = 2;
        Path path1ForAGV2 = new Path(3,2, time2, true);
        pathForAGV2.add(path1ForAGV2);
        //AGV 3 Path is 101--102--103--104
        pathForAGV3.add(buffer0path1);
        pathForAGV3.add(buffer0path2);
        pathForAGV3.add(buffer0path3);
        //AGV 4 Path is 102--103--104--105
        pathForAGV4.add(buffer0path2);
        pathForAGV4.add(buffer0path3);
        pathForAGV4.add(buffer0path4);
        //Actually fitness for AGV 2 should not be 0. Be 0 is for convenience.
        double[] time = {time0 + CommonTestConstant.timeForCrossingBuffers(5),
                time1 + CommonTestConstant.timeForCrossingBuffers(5),
                time2 + CommonTestConstant.timeForCrossingBuffers(5),
                CommonTestConstant.timeForCrossingBuffers(3),
                CommonTestConstant.timeForCrossingBuffers(3)};
        PathPlanning pathPlanning = new PathPlanning(5, penalty, CommonTestConstant.AGV_SPEED, CommonConstant.BUFFER_PATH_LENGTH);
        pathPlanning.setBackingAGV(0);
        pathPlanning.setBackingAGV(1);
        pathPlanning.setBackingAGV(2);
        pathPlanning.navigateAGVsToInnerBuffer(generationForAGVPath, bufferSet, bufferForAGV, time);

        //Check for AGV 0
        assertEquals(3, pathForAGV0.size());
        assertEquals(path1ForAGV0, pathForAGV0.get(0));
        assertEquals(buffer0path0, pathForAGV0.get(1));
        assertEquals(buffer0path1, pathForAGV0.get(2));

        //Check for AGV 1
        assertEquals(2, pathForAGV1.size());
        assertEquals(path1ForAGV1, pathForAGV1.get(0));
        assertEquals(buffer0path0, pathForAGV1.get(1));

        //Check for AGV 2
        assertEquals(4, pathForAGV2.size());
        assertEquals(path1ForAGV2, pathForAGV2.get(0));
        assertEquals(buffer0path0, pathForAGV2.get(1));
        assertEquals(buffer0path1, pathForAGV2.get(2));
        assertEquals(buffer0path2, pathForAGV2.get(3));

        //Check for AGV 3
        assertEquals(3, pathForAGV3.size());
        assertEquals(buffer0path1, pathForAGV3.get(0));
        assertEquals(buffer0path2, pathForAGV3.get(1));
        assertEquals(buffer0path3, pathForAGV3.get(2));

        //Check for AGV 4
        assertEquals(3, pathForAGV4.size());
        assertEquals(buffer0path2, pathForAGV4.get(0));
        assertEquals(buffer0path3, pathForAGV4.get(1));
        assertEquals(buffer0path4, pathForAGV4.get(2));
    }
    

}
