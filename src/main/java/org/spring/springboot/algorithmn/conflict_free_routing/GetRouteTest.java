package org.spring.springboot.algorithmn.conflict_free_routing;


import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.exception.NoPathFeasibleException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GetRouteTest {

    private double[][] graph;


    @Before
    public void initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        graph = CommonTestConstant.initializeGraph();
    }

    //Start from source node 9, and try to add free time window in node 1. it should fail because no link exists.
    @Test
    public void shouldNextPossibleTimeWindowNotBeAddedWhenItIsNotReachable() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 0;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 1, -1, 0);
        currentTimeWindow.setFirstStep(true);
        reservedTimeWindowList.get(9).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(0, 0, CommonConstant.INFINITE);
        freeTimeWindowList.get(0).add(endTimeWindow);
        freeTimeWindowList.get(9).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> occupiedTimeWindow = new ArrayList<>();
        occupiedTimeWindow.add(currentTimeWindow);
        List<TimeWindow> possibleTimeWindow = new ArrayList<>();
        routing.findPossibleNextTimeWindow(endTimeWindow, occupiedTimeWindow, possibleTimeWindow);
        assertEquals(0, possibleTimeWindow.size());
        assertEquals(1, occupiedTimeWindow.size());
        assertEquals(currentTimeWindow, occupiedTimeWindow.get(0));
        assertEquals(CommonConstant.INFINITE, endTimeWindow.getLeastTimeReachHere(), 0.000000001);
        assertNull(endTimeWindow.getLastTimeWindow());
        assertEquals(-1, (int) endTimeWindow.getPath()[0]);
    }

    //Start from source node 9, and try to add free time window in node 4. it should succeed because there is a link and he time window is available.
    @Test
    public void shouldNextPossibleTimeWindowBeAddedAndTimeWindowStatusChangesCorrectlyWhenItIsReachable() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 0;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1, 0);
        currentTimeWindow.setFirstStep(true);
        reservedTimeWindowList.get(9).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(3, 0, CommonConstant.INFINITE);
        freeTimeWindowList.get(3).add(endTimeWindow);
        freeTimeWindowList.get(9).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> occupiedTimeWindow = new ArrayList<>();
        occupiedTimeWindow.add(currentTimeWindow);
        List<TimeWindow> possibleTimeWindow = new ArrayList<>();
        routing.findPossibleNextTimeWindow(endTimeWindow, occupiedTimeWindow, possibleTimeWindow);
        assertEquals(1, possibleTimeWindow.size());
        assertEquals(endTimeWindow, possibleTimeWindow.get(0));
        assertEquals(1, occupiedTimeWindow.size());
        assertEquals(currentTimeWindow, occupiedTimeWindow.get(0));
        assertEquals((CommonConstant.BUFFER_PATH_LENGTH) / 2.0, endTimeWindow.getLeastTimeReachHere(), 0.000000001);
        assertEquals(currentTimeWindow, endTimeWindow.getLastTimeWindow());
        assertEquals(9, (int) endTimeWindow.getPath()[0]);
        assertEquals(3, (int) endTimeWindow.getPath()[1]);
        assertEquals(-1, (int) endTimeWindow.getPath()[2]);
    }

    //Given just one AGV and task started from node 10(start from the first buffer(right one)) to node 6, check if the path is one of the best one and the time calculation is correct.
    //Check if the free time window and reserved time window is changed correspondingly.
    @Test
    public void shouldAGVGoCorrectlyFrom1To6WhenThereIsNoPreviousRouting() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 5;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1, 0);
        currentTimeWindow.setFirstStep(true);
        TimeWindow reservedTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1);
        reservedTimeWindowList.get(9).add(reservedTimeWindow);
        //Initialize all the free time windows
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE, -1, -1);
            freeTimeWindowList.get(i).add(freeTimeWindow);
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> path = routing.getRoute();
        //The path should be 10->4->5->6
        assertEquals(4, path.size());
        //Test for the first time window(node 10)
        TimeWindow timeWindow0 = path.get(0);
        //It should be the mappped value for buffer node
        assertEquals(105, timeWindow0.getNodeNumber());
        assertEquals(null, timeWindow0.getLastTimeWindow());
        assertEquals(0, timeWindow0.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow0.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow0.getStartTime(), 0.000000001);
        assertEquals(3, timeWindow0.getNextNodeNumber());
        assertEquals(-1, (int) timeWindow0.getPath()[0]);
        assertEquals(-1, (int) timeWindow0.getPath()[1]);
        assertEquals(-1, (int) timeWindow0.getPath()[2]);

        //Test for the second time window(node 4)
        double timeToReachNode4 = 0 + CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow1 = path.get(1);
        assertEquals(3, timeWindow1.getNodeNumber());
        assertEquals(timeWindow0, timeWindow1.getLastTimeWindow());
        assertEquals(timeToReachNode4, timeWindow1.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow1.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow1.getStartTime(), 0.000000001);
        assertTrue(timeWindow1.getNextNodeNumber() == 4);
        assertEquals(105, (int) timeWindow1.getPath()[0]);
        assertEquals(3, (int) timeWindow1.getPath()[1]);
        assertEquals(-1, (int) timeWindow1.getPath()[2]);

        //Test for third time window (node 5)
        double timeToReachNode5 = timeToReachNode4 + (8 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow2 = path.get(2);
        assertTrue(timeWindow2.getNodeNumber() == 4 && timeWindow2.getLeastTimeReachHere() == timeToReachNode5);
        assertEquals(timeWindow1, timeWindow2.getLastTimeWindow());
        assertEquals(CommonConstant.INFINITE, timeWindow2.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow2.getStartTime(), 0.000000001);
        assertTrue(timeWindow2.getNextNodeNumber() == 5);
        assertEquals(3, (int) timeWindow2.getPath()[0]);
        assertTrue(timeWindow2.getPath()[1] == 4);
        assertEquals(-1, (int) timeWindow2.getPath()[2]);


        //Test for last time window(node 6)
        double timeToReachNode6 = timeToReachNode5 + (10 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow3 = path.get(3);
        assertEquals(5, timeWindow3.getNodeNumber());
        assertEquals(timeWindow2, timeWindow3.getLastTimeWindow());
        assertEquals(timeToReachNode6, timeWindow3.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow3.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow3.getStartTime(), 0.000000001);
        assertTrue(timeWindow3.getNextNodeNumber() == -1);
        assertTrue(timeWindow3.getPath()[0] == 4);
        assertEquals(5, (int) timeWindow3.getPath()[1]);
        assertEquals(-1, (int) timeWindow3.getPath()[2]);

        //Test for free time window list
        Queue<TimeWindow> freeTimeWindowListForNode10 = freeTimeWindowList.get(9);
        assertEquals(freeTimeWindowListForNode10.poll(),new TimeWindow(9, CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0));
        Queue<TimeWindow> freeTimeWindowListForNode4 = freeTimeWindowList.get(3);
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3, 0,
                timeToReachNode4, -1, 0));
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3,
                getCrossingTime(timeToReachNode4),
                CommonConstant.INFINITE, -1, 0));
        Queue<TimeWindow> freeTimeWindowListForNode5 = freeTimeWindowList.get(4);
        assertTrue(freeTimeWindowListForNode5.poll().equals(new TimeWindow(4,
                0, timeToReachNode5, -1, 0)) && freeTimeWindowListForNode5.poll().equals(new TimeWindow(4,
                        getCrossingTime(timeToReachNode5),
                CommonConstant.INFINITE, -1, 0)));
        Queue<TimeWindow> freeTimeWindowListForNode6 = freeTimeWindowList.get(5);
        assertEquals(freeTimeWindowListForNode6.poll(), new TimeWindow(5,
                0, timeToReachNode6, -1));
        
        //other free time window should be available all the time
        assertTrue(freeTimeWindowList.size() == 11);
        assertEquals(freeTimeWindowList.get(0).poll(), new TimeWindow(0, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(1).poll(), new TimeWindow(1, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(2).poll(), new TimeWindow(2, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(6).poll(), new TimeWindow(6, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(7).poll(), new TimeWindow(7, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(8).poll(), new TimeWindow(8, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(10).poll(), new TimeWindow(10, 0,
                CommonConstant.INFINITE, -1, 0));


        //Test for reserved time window list
        //reserved time window needs to check the next node
        //Check the path of the reserved time window list
        Queue<TimeWindow> reservedTimeWindowListForNode10 = reservedTimeWindowList.get(9);
        assertEquals(3, reservedTimeWindowListForNode10.peek().getNextNodeNumber());
        assertEquals(9, (int)reservedTimeWindowListForNode10.peek().getPath()[0]);
        assertEquals(3, (int)reservedTimeWindowListForNode10.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode10.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode10.poll(), new TimeWindow(9, 0,CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                 0, 4));

        Queue<TimeWindow> reservedTimeWindowListForNode4 = reservedTimeWindowList.get(3);
        assertEquals(4, reservedTimeWindowListForNode4.peek().getNextNodeNumber());
        assertEquals(3, (int)reservedTimeWindowListForNode4.peek().getPath()[0]);
        assertEquals(4, (int)reservedTimeWindowListForNode4.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode4.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode4.poll(), new TimeWindow(3, 
                timeToReachNode4, getCrossingTime(timeToReachNode4),
                0, 4));

        Queue<TimeWindow> reservedTimeWindowListForNode5 = reservedTimeWindowList.get(4);
        assertEquals(4, (int)reservedTimeWindowListForNode5.peek().getPath()[0]);
        assertEquals(5, (int)reservedTimeWindowListForNode5.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode5.peek().getPath()[2]);
        assertEquals(5, reservedTimeWindowListForNode5.peek().getNextNodeNumber());
        assertTrue(reservedTimeWindowListForNode5.poll().equals(new TimeWindow(4,
                        timeToReachNode5, getCrossingTime(timeToReachNode5),0, 5)));

        Queue<TimeWindow> reservedTimeWindowListForNode6 = reservedTimeWindowList.get(5);
        assertEquals(-1, (int)reservedTimeWindowListForNode6.peek().getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowListForNode6.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode6.peek().getPath()[2]);
        assertEquals(-1, reservedTimeWindowListForNode6.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode6.poll(), new TimeWindow(5,
                 timeToReachNode6, getCrossingTime(timeToReachNode6),0, -1));
        //other reserved time window should be empty
        assertTrue(reservedTimeWindowList.size() == 11);
        assertTrue(reservedTimeWindowList.get(0).isEmpty());
        assertTrue(reservedTimeWindowList.get(1).isEmpty());
        assertTrue(reservedTimeWindowList.get(2).isEmpty());
        assertTrue(reservedTimeWindowList.get(6).isEmpty());
        assertTrue(reservedTimeWindowList.get(7).isEmpty());
        assertTrue(reservedTimeWindowList.get(8).isEmpty());
        assertTrue(reservedTimeWindowList.get(10).isEmpty());
    }

    //Given just one AGV, task started from node 10(start from the first buffer(right one)) to node 6 and one AGV blocks the path from node 4 to node 5 (5->4->3)
    //Check if the path is 10->4->9->6  and the time calculfree time window and reserved time window is changed correspondingly.
    @Test
    public void shouldAGVGoCorrectlyFrom1To6WhenThereIsOneAGVBlockingPath() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 5;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1, 0);
        currentTimeWindow.setFirstStep(true);
        TimeWindow reservedTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1);
        TimeWindow reservedTimeWindowForNode5 = new TimeWindow(4, 0, 1, 1, 3, new Integer[] {4, 3, -1});
        TimeWindow reservedTimeWindowForNode4 = new TimeWindow(3, 5, 8, 1, 2, new Integer[] {3, 2, -1});
        TimeWindow reservedTimeWindowForNode3 = new TimeWindow(2, 10, CommonConstant.INFINITE, 1, -1, new Integer[] {-1, -1, -1});
        reservedTimeWindowList.get(9).add(reservedTimeWindow);
        reservedTimeWindowList.get(4).add(reservedTimeWindowForNode5);
        reservedTimeWindowList.get(3).add(reservedTimeWindowForNode4);
        reservedTimeWindowList.get(2).add(reservedTimeWindowForNode3);
        //Initialize all the free time windows
        TimeWindow freeTimeWindowForNode5 = new TimeWindow(4, 1, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode4 = new TimeWindow(3, 0, 5);
        TimeWindow freeTimeWindow2ForNode4 = new TimeWindow(3, 8, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode3 = new TimeWindow(2, 0, 10);
        freeTimeWindowList.get(4).add(freeTimeWindowForNode5);
        freeTimeWindowList.get(3).add(freeTimeWindow1ForNode4);
        freeTimeWindowList.get(3).add(freeTimeWindow2ForNode4);
        freeTimeWindowList.get(2).add(freeTimeWindow1ForNode3);
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            if (i != 4 && i != 3 && i != 2) {
                TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE);
                freeTimeWindowList.get(i).add(freeTimeWindow);
            }
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> path = routing.getRoute();
        //The path should be 10->4->9->6
        assertEquals(4, path.size());
        //Test for the first time window(node 10)
        TimeWindow timeWindow0 = path.get(0);
        //It should be the mappped value for buffer node
        assertEquals(105, timeWindow0.getNodeNumber());
        assertEquals(null, timeWindow0.getLastTimeWindow());
        assertEquals(0, timeWindow0.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow0.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow0.getStartTime(), 0.000000001);
        assertEquals(3, timeWindow0.getNextNodeNumber());
        assertEquals(-1, (int) timeWindow0.getPath()[0]);
        assertEquals(-1, (int) timeWindow0.getPath()[1]);
        assertEquals(-1, (int) timeWindow0.getPath()[2]);

        //Test for the second time window(node 4)
        double timeToReachNode4 = 0 + CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow1 = path.get(1);
        assertEquals(3, timeWindow1.getNodeNumber());
        assertEquals(timeWindow0, timeWindow1.getLastTimeWindow());
        assertEquals(timeToReachNode4, timeWindow1.getLeastTimeReachHere(), 0.000000001);
        assertEquals(5, timeWindow1.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow1.getStartTime(), 0.000000001);
        assertEquals(8, timeWindow1.getNextNodeNumber());
        assertEquals(105, (int) timeWindow1.getPath()[0]);
        assertEquals(3, (int) timeWindow1.getPath()[1]);
        assertEquals(-1, (int) timeWindow1.getPath()[2]);

        //Test for third time window(node 9)
        double timeToReachNode9 = timeToReachNode4 + (10 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow2 = path.get(2);
        assertEquals(8, timeWindow2.getNodeNumber());
        assertEquals(timeToReachNode9, timeWindow2.getLeastTimeReachHere(), 0.000000001);
        assertEquals(timeWindow1, timeWindow2.getLastTimeWindow());
        assertEquals(CommonConstant.INFINITE, timeWindow2.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow2.getStartTime(), 0.000000001);
        assertEquals(5, timeWindow2.getNextNodeNumber());
        assertEquals(3, (int) timeWindow2.getPath()[0]);
        assertEquals(8, (int) timeWindow2.getPath()[1]);
        assertEquals(-1, (int) timeWindow2.getPath()[2]);


        //Test for last time window(node 6)
        double timeToReachNode6 = timeToReachNode9 + (8 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow3 = path.get(3);
        assertEquals(5, timeWindow3.getNodeNumber());
        assertEquals(timeWindow2, timeWindow3.getLastTimeWindow());
        assertEquals(timeToReachNode6, timeWindow3.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow3.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow3.getStartTime(), 0.000000001);
        assertEquals(-1, timeWindow3.getNextNodeNumber());
        assertEquals(8, (int) timeWindow3.getPath()[0]);
        assertEquals(5, (int) timeWindow3.getPath()[1]);
        assertEquals(-1, (int) timeWindow3.getPath()[2]);

        //Test for free time window list
        Queue<TimeWindow> freeTimeWindowListForNode10 = freeTimeWindowList.get(9);
        assertEquals(1, freeTimeWindowListForNode10.size());
        assertEquals(freeTimeWindowListForNode10.poll(),new TimeWindow(9, CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode4 = freeTimeWindowList.get(3);
        assertEquals(3, freeTimeWindowListForNode4.size());
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3, 0,
                timeToReachNode4, -1, 0));
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3,
                getCrossingTime(timeToReachNode4), 5,  -1, 0));
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3,8,
                CommonConstant.INFINITE, -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode9 = freeTimeWindowList.get(8);
        assertEquals(2, freeTimeWindowListForNode9.size());
                assertEquals(freeTimeWindowListForNode9.poll(), new TimeWindow(8, 0,
                timeToReachNode9, -1, 0));
        assertEquals(freeTimeWindowListForNode9.poll(), new TimeWindow(8, getCrossingTime(timeToReachNode9),
                CommonConstant.INFINITE, -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode5 = freeTimeWindowList.get(4);
        assertEquals(1, freeTimeWindowListForNode5.size());
        assertEquals(freeTimeWindowListForNode5.poll(), freeTimeWindowForNode5);

        Queue<TimeWindow> freeTimeWindowListForNode6 = freeTimeWindowList.get(5);
        assertEquals(freeTimeWindowListForNode6.poll(), new TimeWindow(5,
                0, timeToReachNode6, -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode3 = freeTimeWindowList.get(2);
        assertEquals(freeTimeWindowListForNode3.poll(), freeTimeWindow1ForNode3);

        //other free time window should be available all the time
        assertTrue(freeTimeWindowList.size() == 11);
        assertEquals(freeTimeWindowList.get(0).poll(), new TimeWindow(0, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(1).poll(), new TimeWindow(1, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(6).poll(), new TimeWindow(6, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(7).poll(), new TimeWindow(7, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(10).poll(), new TimeWindow(10, 0,
                CommonConstant.INFINITE, -1, 0));


        //Test for reserved time window list
        //reserved time window needs to check the next node
        Queue<TimeWindow> reservedTimeWindowListForNode10 = reservedTimeWindowList.get(9);
        assertEquals(9, (int)reservedTimeWindowListForNode10.peek().getPath()[0]);
        assertEquals(3, (int)reservedTimeWindowListForNode10.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode10.peek().getPath()[2]);
        assertEquals(3, reservedTimeWindowListForNode10.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode10.poll(), new TimeWindow(9, 0,CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                0));

        Queue<TimeWindow> reservedTimeWindowListForNode4 = reservedTimeWindowList.get(3);
        assertEquals(8, reservedTimeWindowListForNode4.peek().getNextNodeNumber());
        assertEquals(3, (int)reservedTimeWindowListForNode4.peek().getPath()[0]);
        assertEquals(8, (int)reservedTimeWindowListForNode4.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode4.peek().getPath()[2]);
        assertEquals(2, reservedTimeWindowListForNode4.size());
        assertEquals(reservedTimeWindowListForNode4.poll(), new TimeWindow(3,
                timeToReachNode4, getCrossingTime(timeToReachNode4), 0));
        assertEquals(2, reservedTimeWindowListForNode4.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode4.poll(), reservedTimeWindowForNode4);

        Queue<TimeWindow> reservedTimeWindowListForNode5 = reservedTimeWindowList.get(4);
        assertEquals(4, (int)reservedTimeWindowListForNode5.peek().getPath()[0]);
        assertEquals(3, (int)reservedTimeWindowListForNode5.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode5.peek().getPath()[2]);
        assertEquals(3, reservedTimeWindowListForNode5.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode5.poll(), reservedTimeWindowForNode5);

        Queue<TimeWindow> reservedTimeWindowListForNode9 = reservedTimeWindowList.get(8);
        assertEquals(8, (int)reservedTimeWindowListForNode9.peek().getPath()[0]);
        assertEquals(5, (int)reservedTimeWindowListForNode9.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode9.peek().getPath()[2]);
        assertEquals(5, reservedTimeWindowListForNode9.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode9.poll(), new TimeWindow(8,
                timeToReachNode9, getCrossingTime(timeToReachNode9), 0, 5));

        Queue<TimeWindow> reservedTimeWindowListForNode6 = reservedTimeWindowList.get(5);
        assertEquals(-1, (int)reservedTimeWindowListForNode6.peek().getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowListForNode6.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode6.peek().getPath()[2]);
        assertEquals(-1, reservedTimeWindowListForNode6.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode6.poll(), new TimeWindow(5,
                timeToReachNode6, getCrossingTime(timeToReachNode6),0, -1));

        Queue<TimeWindow> reservedTimeWindowListForNode3 = reservedTimeWindowList.get(2);
        assertEquals(-1, (int)reservedTimeWindowListForNode3.peek().getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowListForNode3.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode3.peek().getPath()[2]);
        assertEquals(-1, reservedTimeWindowListForNode3.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode3.poll(), reservedTimeWindowForNode3);

        //other reserved time window should be empty
        assertTrue(reservedTimeWindowList.size() == 11);
        assertTrue(reservedTimeWindowList.get(0).isEmpty());
        assertTrue(reservedTimeWindowList.get(1).isEmpty());
        assertTrue(reservedTimeWindowList.get(6).isEmpty());
        assertTrue(reservedTimeWindowList.get(7).isEmpty());
        assertTrue(reservedTimeWindowList.get(10).isEmpty());
    }

    //Given just one AGV 0, task started from node 10 and goes to node 8.
    //AGV 1 blocks path 9 to 8 by going 2--9--8--1. AGV 2 blocks path 9 to 4 by going 4--9--4.
    //Check if AGV 0 chooses best path by looping at node 9 and then going to node 8
    @Test
    public void shouldAGVGoCorrectlyFrom10To8WhenThereAreTwoAGVsBlockingPath() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 7;
        //AGV 0
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, 0, 0);
        currentTimeWindow.setFirstStep(true);
        TimeWindow reservedTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1);
        TimeWindow reservedTimeWindowForNode2 = new TimeWindow(1, 0, 1, 1, 8, new Integer[]{1, 8, -1});
        TimeWindow reservedTimeWindow1ForNode9 = new TimeWindow(8, 11, 12, 1, 7, new Integer[]{8, 7, -1});
        TimeWindow reservedTimeWindowForNode8 = new TimeWindow(7, 14, 18, 1, 0, new Integer[]{7, 0, -1});
        TimeWindow reservedTimeWindow2ForNode9 = new TimeWindow(8, 12, 15, 2, 3,new Integer[]{8, 3, -1});
        TimeWindow reservedTimeWindow2ForNode4 = new TimeWindow(3, 18, CommonConstant.INFINITE, 2, -1, new Integer[]{-1, -1, -1});
        TimeWindow reservedTimeWindow1ForNode4 = new TimeWindow(3, 4, 5, 2, 8, new Integer[]{3, 8, -1});
        TimeWindow reservedTimeWindowForNode1= new TimeWindow(0, 20, CommonConstant.INFINITE, 1, -1, new Integer[]{-1, -1, -1});
        reservedTimeWindowList.get(9).add(reservedTimeWindow);
        reservedTimeWindowList.get(1).add(reservedTimeWindowForNode2);
        reservedTimeWindowList.get(8).add(reservedTimeWindow1ForNode9);
        reservedTimeWindowList.get(8).add(reservedTimeWindow2ForNode9);
        reservedTimeWindowList.get(7).add(reservedTimeWindowForNode8);
        reservedTimeWindowList.get(0).add(reservedTimeWindowForNode1);
        reservedTimeWindowList.get(3).add(reservedTimeWindow1ForNode4);
        reservedTimeWindowList.get(3).add(reservedTimeWindow2ForNode4);

        //Initialize all the free time windows
        TimeWindow freeTimeWindow1ForNode2 = new TimeWindow(1, 1, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode4 = new TimeWindow(3, 0, 4);
        TimeWindow freeTimeWindow2ForNode4 = new TimeWindow(3, 4, 18);
        TimeWindow freeTimeWindow1ForNode9 = new TimeWindow(8, 0, 11);
        TimeWindow freeTimeWindow2ForNode9 = new TimeWindow(8, 15, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode8 = new TimeWindow(7, 0, 14);
        TimeWindow freeTimeWindow2ForNode8 = new TimeWindow(7, 18, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode1 = new TimeWindow(0, 0, 20);
        freeTimeWindowList.get(1).add(freeTimeWindow1ForNode2);
        freeTimeWindowList.get(3).add(freeTimeWindow1ForNode4);
        freeTimeWindowList.get(3).add(freeTimeWindow2ForNode4);
        freeTimeWindowList.get(8).add(freeTimeWindow1ForNode9);
        freeTimeWindowList.get(8).add(freeTimeWindow2ForNode9);
        freeTimeWindowList.get(7).add(freeTimeWindow1ForNode8);
        freeTimeWindowList.get(7).add(freeTimeWindow2ForNode8);
        freeTimeWindowList.get(0).add(freeTimeWindow1ForNode1);
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            if (i != 1 && i != 8 && i != 7 && i != 0 && i != 3) {
                TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE);
                freeTimeWindowList.get(i).add(freeTimeWindow);
            }
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        List<TimeWindow> path = routing.getRoute();
        //The path should be 10->4->9->(9)->8
        assertEquals(5, path.size());
        //Test for the first time window(node 10)
        TimeWindow timeWindow0 = path.get(0);
        //It should be the mappped value for buffer node
        assertEquals(105, timeWindow0.getNodeNumber());
        assertEquals(null, timeWindow0.getLastTimeWindow());
        assertEquals(0, timeWindow0.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow0.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow0.getStartTime(), 0.000000001);
        assertEquals(3, timeWindow0.getNextNodeNumber());
        assertEquals(-1, (int) timeWindow0.getPath()[0]);
        assertEquals(-1, (int) timeWindow0.getPath()[1]);
        assertEquals(-1, (int) timeWindow0.getPath()[2]);

        //Test for the second time window(node 4)
        double timeToReachNode4 = 0 + CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow1 = path.get(1);
        assertEquals(3, timeWindow1.getNodeNumber());
        assertEquals(timeWindow0, timeWindow1.getLastTimeWindow());
        assertEquals(timeToReachNode4, timeWindow1.getLeastTimeReachHere(), 0.000000001);
        assertEquals(4, timeWindow1.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow1.getStartTime(), 0.000000001);
        assertEquals(8, timeWindow1.getNextNodeNumber());
        assertEquals(105, (int) timeWindow1.getPath()[0]);
        assertEquals(3, (int) timeWindow1.getPath()[1]);
        assertEquals(-1, (int) timeWindow1.getPath()[2]);

        //Test for third time window(node 9)
        double timeToReachNode9 = timeToReachNode4 + (10 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow2 = path.get(2);
        assertEquals(8, timeWindow2.getNodeNumber());
        assertEquals(timeToReachNode9, timeWindow2.getLeastTimeReachHere(), 0.000000001);
        assertEquals(timeWindow1, timeWindow2.getLastTimeWindow());
        assertEquals(11, timeWindow2.getEndTime(), 0.000000001);
        assertEquals(0, timeWindow2.getStartTime(), 0.000000001);
        assertEquals(8, timeWindow2.getNextNodeNumber());
        assertEquals(3, (int) timeWindow2.getPath()[0]);
        assertEquals(8, (int) timeWindow2.getPath()[1]);
        assertEquals(-1, (int) timeWindow2.getPath()[2]);


        //Test for fourth time window(node 9)
        double loopTimeToReachNode9 = 15;
        TimeWindow timeWindow3 = path.get(3);
        assertEquals(8, timeWindow3.getNodeNumber());
        assertEquals(loopTimeToReachNode9, timeWindow3.getLeastTimeReachHere(), 0.000000001);
        assertEquals(timeWindow2, timeWindow3.getLastTimeWindow());
        assertEquals(CommonConstant.INFINITE, timeWindow3.getEndTime(), 0.000000001);
        assertEquals(15, timeWindow3.getStartTime(), 0.000000001);
        assertEquals(7, timeWindow3.getNextNodeNumber());
        assertEquals(8, (int) timeWindow3.getPath()[0]);
        assertEquals(5, (int) timeWindow3.getPath()[1]);
        assertEquals(8, (int) timeWindow3.getPath()[2]);

        //Test for last time window(node 6)
        double timeToReachNode8 = loopTimeToReachNode9 + (10 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow timeWindow4 = path.get(4);
        assertEquals(7, timeWindow4.getNodeNumber());
        assertEquals(timeWindow3, timeWindow4.getLastTimeWindow());
        assertEquals(timeToReachNode8, timeWindow4.getLeastTimeReachHere(), 0.000000001);
        assertEquals(CommonConstant.INFINITE, timeWindow4.getEndTime(), 0.000000001);
        assertEquals(18, timeWindow4.getStartTime(), 0.000000001);
        assertEquals(-1, timeWindow4.getNextNodeNumber());
        assertEquals(8, (int) timeWindow4.getPath()[0]);
        assertEquals(7, (int) timeWindow4.getPath()[1]);
        assertEquals(-1, (int) timeWindow4.getPath()[2]);

        //Test for free time window list
        Queue<TimeWindow> freeTimeWindowListForNode10 = freeTimeWindowList.get(9);
        assertEquals(1, freeTimeWindowListForNode10.size());
        assertEquals(freeTimeWindowListForNode10.poll(),new TimeWindow(9, CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                CommonConstant.INFINITE, -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode4 = freeTimeWindowList.get(3);
        assertEquals(2, freeTimeWindowListForNode4.size());
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3, 0,
                timeToReachNode4, -1, 0));
        assertEquals(freeTimeWindowListForNode4.poll(), new TimeWindow(3,
                4, 18,  -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode9 = freeTimeWindowList.get(8);
        assertEquals(2, freeTimeWindowListForNode9.size());
        assertEquals(freeTimeWindowListForNode9.poll(), new TimeWindow(8, 0,
                timeToReachNode9, -1, 0));
        assertEquals(freeTimeWindowListForNode9.poll(), new TimeWindow(8, getCrossingTime(loopTimeToReachNode9),
                CommonConstant.INFINITE, -1, 0));

        Queue<TimeWindow> freeTimeWindowListForNode2 = freeTimeWindowList.get(1);
        assertEquals(1, freeTimeWindowListForNode2.size());
        assertEquals(freeTimeWindowListForNode2.poll(), freeTimeWindow1ForNode2);

        Queue<TimeWindow> freeTimeWindowListForNode8 = freeTimeWindowList.get(7);
        assertEquals(3, freeTimeWindowListForNode8.size());
        assertEquals(freeTimeWindowListForNode8.poll(), freeTimeWindow1ForNode8);
        assertEquals(freeTimeWindowListForNode8.poll(), new TimeWindow(7, 18,
                timeToReachNode8, -1, 0));
        assertEquals(freeTimeWindowListForNode8.poll(), new TimeWindow(7, getCrossingTime(timeToReachNode8),
                CommonConstant.INFINITE, -1, 0));


        Queue<TimeWindow> freeTimeWindowListForNode1 = freeTimeWindowList.get(0);
        assertEquals(1, freeTimeWindowListForNode1.size());
        assertEquals(freeTimeWindowListForNode1.poll(), freeTimeWindow1ForNode1);

        //other free time window should be available all the time
        assertTrue(freeTimeWindowList.size() == 11);
        assertEquals(freeTimeWindowList.get(2).poll(), new TimeWindow(2, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(4).poll(), new TimeWindow(4, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(5).poll(), new TimeWindow(5, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(6).poll(), new TimeWindow(6, 0,
                CommonConstant.INFINITE, -1, 0));
        assertEquals(freeTimeWindowList.get(10).poll(), new TimeWindow(10, 0,
                CommonConstant.INFINITE, -1, 0));


        //Test for reserved time window list
        //reserved time window needs to check the next node
        Queue<TimeWindow> reservedTimeWindowListForNode10 = reservedTimeWindowList.get(9);
        assertEquals(3, reservedTimeWindowListForNode10.peek().getNextNodeNumber());
        assertEquals(9, (int)reservedTimeWindowListForNode10.peek().getPath()[0]);
        assertEquals(3, (int)reservedTimeWindowListForNode10.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode10.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode10.poll(), new TimeWindow(9, 0,CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED,
                0, 4));

        Queue<TimeWindow> reservedTimeWindowListForNode4 = reservedTimeWindowList.get(3);
        assertEquals(3, (int)reservedTimeWindowListForNode4.peek().getPath()[0]);
        assertEquals(8, (int)reservedTimeWindowListForNode4.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode4.peek().getPath()[2]);
        assertEquals(8, reservedTimeWindowListForNode4.peek().getNextNodeNumber());
        assertEquals(3, reservedTimeWindowListForNode4.size());
        assertEquals(reservedTimeWindowListForNode4.poll(), new TimeWindow(3,
                timeToReachNode4, 4, 0));

        assertEquals(8, reservedTimeWindowListForNode4.peek().getNextNodeNumber());
        assertEquals(3, (int)reservedTimeWindowListForNode4.peek().getPath()[0]);
        assertEquals(8, (int)reservedTimeWindowListForNode4.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode4.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode4.poll(), reservedTimeWindow1ForNode4);
        assertEquals(-1, reservedTimeWindowListForNode4.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode4.poll(), reservedTimeWindow2ForNode4);

        Queue<TimeWindow> reservedTimeWindowListForNode2 = reservedTimeWindowList.get(1);
        assertEquals(1, reservedTimeWindowListForNode2.size());
        assertEquals(1, (int)reservedTimeWindowListForNode2.peek().getPath()[0]);
        assertEquals(8, (int)reservedTimeWindowListForNode2.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode2.peek().getPath()[2]);
        assertEquals(8, reservedTimeWindowListForNode2.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode2.poll(), reservedTimeWindowForNode2);

        Queue<TimeWindow> reservedTimeWindowListForNode9 = reservedTimeWindowList.get(8);
        assertEquals(4, reservedTimeWindowListForNode9.size());
        assertEquals(8, reservedTimeWindowListForNode9.peek().getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowListForNode9.peek().getPath()[0]);
        assertEquals(5, (int)reservedTimeWindowListForNode9.peek().getPath()[1]);
        assertEquals(8, (int)reservedTimeWindowListForNode9.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode9.poll(), new TimeWindow(8,
                timeToReachNode9, getCrossingTime(timeToReachNode9), 0));

        assertEquals(7, reservedTimeWindowListForNode9.peek().getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowListForNode9.peek().getPath()[0]);
        assertEquals(7, (int)reservedTimeWindowListForNode9.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode9.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode9.poll(), reservedTimeWindow1ForNode9);

        assertEquals(3, reservedTimeWindowListForNode9.peek().getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowListForNode9.peek().getPath()[0]);
        assertEquals(3, (int)reservedTimeWindowListForNode9.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode9.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode9.poll(), reservedTimeWindow2ForNode9);

        assertEquals(7, reservedTimeWindowListForNode9.peek().getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowListForNode9.peek().getPath()[0]);
        assertEquals(7, (int)reservedTimeWindowListForNode9.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode9.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode9.poll(), new TimeWindow(8,
                loopTimeToReachNode9, getCrossingTime(loopTimeToReachNode9), 0, 0));

        Queue<TimeWindow> reservedTimeWindowListForNode8 = reservedTimeWindowList.get(7);
        assertEquals(2, reservedTimeWindowListForNode8.size());
        assertEquals(0, reservedTimeWindowListForNode8.peek().getNextNodeNumber());
        assertEquals(7, (int)reservedTimeWindowListForNode8.peek().getPath()[0]);
        assertEquals(0, (int)reservedTimeWindowListForNode8.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode8.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode8.poll(), reservedTimeWindowForNode8);

        assertEquals(-1, reservedTimeWindowListForNode8.peek().getNextNodeNumber());
        assertEquals(-1, (int)reservedTimeWindowListForNode8.peek().getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowListForNode8.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode8.peek().getPath()[2]);
        assertEquals(reservedTimeWindowListForNode8.poll(), new TimeWindow(7,
                timeToReachNode8, getCrossingTime(timeToReachNode8), 0, 0));

        Queue<TimeWindow> reservedTimeWindowListForNode1 = reservedTimeWindowList.get(0);
        assertEquals(1, reservedTimeWindowListForNode1.size());
        assertEquals(-1, (int)reservedTimeWindowListForNode1.peek().getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowListForNode1.peek().getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowListForNode1.peek().getPath()[2]);
        assertEquals(-1, reservedTimeWindowListForNode1.peek().getNextNodeNumber());
        assertEquals(reservedTimeWindowListForNode1.poll(), reservedTimeWindowForNode1);

        //other reserved time window should be empty
        assertTrue(reservedTimeWindowList.size() == 11);
        assertTrue(reservedTimeWindowList.get(2).isEmpty());
        assertTrue(reservedTimeWindowList.get(4).isEmpty());
        assertTrue(reservedTimeWindowList.get(5).isEmpty());
        assertTrue(reservedTimeWindowList.get(6).isEmpty());
        assertTrue(reservedTimeWindowList.get(10).isEmpty());
    }

    //Given just one AGV, task started from node 10(start from the first buffer(right one)) to node 6 and one AGV blocks the path from node 4 to node 5 (5->4->3)
    //heck if the path created is 105->4->9->6  and the time calculation is correct.
    //Check if the reach time for free time window has been set to infinite
    @Test
    public void shouldPathCreatedCorrectlyWhenThereIsOneAGVBlockingPath() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);

        TimeWindow reservedTimeWindowForNode5 = new TimeWindow(4, 0, 1, 1, 3, new Integer[] {4, 3, -1});
        TimeWindow reservedTimeWindowForNode4 = new TimeWindow(3, 5, 8, 1, 2, new Integer[] {3, 2, -1});
        TimeWindow reservedTimeWindowForNode3 = new TimeWindow(2, 10, CommonConstant.INFINITE, 1, -1, new Integer[] {-1, -1, -1});
        reservedTimeWindowList.get(4).add(reservedTimeWindowForNode5);
        reservedTimeWindowList.get(3).add(reservedTimeWindowForNode4);
        reservedTimeWindowList.get(2).add(reservedTimeWindowForNode3);
        //Initialize all the free time windowsE
        TimeWindow freeTimeWindowForNode5 = new TimeWindow(4, 1, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode4 = new TimeWindow(3, 0, 5);
        TimeWindow freeTimeWindow2ForNode4 = new TimeWindow(3, 8, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode3 = new TimeWindow(2, 0, 10);
        freeTimeWindowList.get(4).add(freeTimeWindowForNode5);
        freeTimeWindowList.get(3).add(freeTimeWindow1ForNode4);
        freeTimeWindowList.get(3).add(freeTimeWindow2ForNode4);
        freeTimeWindowList.get(2).add(freeTimeWindow1ForNode3);
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            if (i != 4 && i != 3 && i != 2) {
                TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE);
                freeTimeWindowList.get(i).add(freeTimeWindow);
            }
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, graph, bufferSet, CommonTestConstant.AGV_SPEED);

        List<Path> path = routing.getPath(9, 0, 0, 5);
        assertEquals(3, path.size());
        Path path0 = path.get(0);
        Path path1 = path.get(1);
        Path path2 = path.get(2);
        assertEquals(105 ,path0.getStartNode());
        assertEquals(3 ,path0.getEndNode());
        assertEquals(CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, path0.getTime(), 0.0000000001);
        assertFalse(path0.isLoop());
        assertEquals(3 ,path1.getStartNode());
        assertEquals(8 ,path1.getEndNode());
        assertEquals(10 / CommonTestConstant.AGV_SPEED, path1.getTime(), 0.0000000001);
        assertFalse(path1.isLoop());
        assertEquals(8 ,path2.getStartNode());
        assertEquals(5 ,path2.getEndNode());
        assertEquals(8 / CommonTestConstant.AGV_SPEED, path2.getTime(), 0.0000000001);
        assertFalse(path2.isLoop());
        List<TimeWindow> freeTimeWindows = freeTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        for (TimeWindow freeTimeWindow : freeTimeWindows) {
            assertEquals(CommonConstant.INFINITE, freeTimeWindow.getLeastTimeReachHere(), 0.0000000001);
        }
    }

    //Check if the return path consists of just one path and free time window least time get here is all infinite when end node locates at current time window (node 6)
    @Test
    public void shouldPathCreatedCorrectlyWhenEndNodeLocatesAtCurrentTimeWindow() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 5;
        //Initialize all the free time windowsE
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE, -1, -1);
            freeTimeWindowList.get(i).add(freeTimeWindow);
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, graph, bufferSet, CommonTestConstant.AGV_SPEED);
        List<Path> path = routing.getPath(5, 0, 0, task);
        assertEquals(1, path.size());
        Path path0 = path.get(0);
        assertEquals(5 ,path0.getStartNode());
        assertEquals(5 ,path0.getEndNode());
        assertEquals(CommonConstant.CROSSING_DISTANCE / CommonTestConstant.AGV_SPEED, path0.getTime(), 0.0000000001);
        assertFalse(path0.isLoop());
        List<TimeWindow> freeTimeWindows = freeTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        for (TimeWindow freeTimeWindow : freeTimeWindows) {
            assertEquals(CommonConstant.INFINITE, freeTimeWindow.getLeastTimeReachHere(), 0.0000000001);
        }
    }


    //Given just one AGV 0, task started from node 10 and goes to node 8.
    //AGV 1 blocks path 9 to 8 by going 2--9--8--1. AGV 2 blocks path 9 to 4 by going 4--9--4.
    //Check if AGV 0 chooses best path by looping at node 9 and then going to node 8
    @Test
    public void shouldPathCreatedCorrectlyFrom10To8WhenThereAreTwoAGVsBlockingPath() throws NoPathFeasibleException {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 7;
        //AGV 0
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 0, -1, 0);
        currentTimeWindow.setFirstStep(true);
        TimeWindow reservedTimeWindowForNode2 = new TimeWindow(1, 0, 1, 1, 8, new Integer[] {1, 8, -1});
        TimeWindow reservedTimeWindow1ForNode9 = new TimeWindow(8, 11, 12, 1, 7, new Integer[] {8, 7, -1});
        TimeWindow reservedTimeWindowForNode8 = new TimeWindow(7, 14, 18, 1, 0, new Integer[] {7, 0, -1});
        TimeWindow reservedTimeWindow2ForNode9 = new TimeWindow(8, 12, 15, 2, 3, new Integer[] {8, 3, -1});
        TimeWindow reservedTimeWindow2ForNode4 = new TimeWindow(3, 18, CommonConstant.INFINITE, 2, -1, new Integer[] {-1, -1, -1});
        TimeWindow reservedTimeWindow1ForNode4 = new TimeWindow(3, 4, 5, 2, 8, new Integer[] {3, 8, -1});
        TimeWindow reservedTimeWindowForNode1 = new TimeWindow(0, 20, CommonConstant.INFINITE, 1, -1, new Integer[] {-1, -1, -1});
        reservedTimeWindowList.get(1).add(reservedTimeWindowForNode2);
        reservedTimeWindowList.get(8).add(reservedTimeWindow1ForNode9);
        reservedTimeWindowList.get(8).add(reservedTimeWindow2ForNode9);
        reservedTimeWindowList.get(7).add(reservedTimeWindowForNode8);
        reservedTimeWindowList.get(0).add(reservedTimeWindowForNode1);
        reservedTimeWindowList.get(3).add(reservedTimeWindow1ForNode4);
        reservedTimeWindowList.get(3).add(reservedTimeWindow2ForNode4);

        //Initialize all the free time windows
        TimeWindow freeTimeWindow1ForNode2 = new TimeWindow(1, 1, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode4 = new TimeWindow(3, 0, 4);
        TimeWindow freeTimeWindow2ForNode4 = new TimeWindow(3, 4, 18);
        TimeWindow freeTimeWindow1ForNode9 = new TimeWindow(8, 0, 11);
        TimeWindow freeTimeWindow2ForNode9 = new TimeWindow(8, 15, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode8 = new TimeWindow(7, 0, 14);
        TimeWindow freeTimeWindow2ForNode8 = new TimeWindow(7, 18, CommonConstant.INFINITE);
        TimeWindow freeTimeWindow1ForNode1 = new TimeWindow(0, 0, 20);
        freeTimeWindowList.get(1).add(freeTimeWindow1ForNode2);
        freeTimeWindowList.get(3).add(freeTimeWindow1ForNode4);
        freeTimeWindowList.get(3).add(freeTimeWindow2ForNode4);
        freeTimeWindowList.get(8).add(freeTimeWindow1ForNode9);
        freeTimeWindowList.get(8).add(freeTimeWindow2ForNode9);
        freeTimeWindowList.get(7).add(freeTimeWindow1ForNode8);
        freeTimeWindowList.get(7).add(freeTimeWindow2ForNode8);
        freeTimeWindowList.get(0).add(freeTimeWindow1ForNode1);
        for (int i = 0; i < CommonTestConstant.SPECIAL_GRAPH_SIZE; i++) {
            if (i != 1 && i != 8 && i != 7 && i != 0 && i != 3) {
                TimeWindow freeTimeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE);
                freeTimeWindowList.get(i).add(freeTimeWindow);
            }
        }
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, graph, bufferSet, CommonTestConstant.AGV_SPEED);
        List<Path> path = routing.getPath(9, 0, 0, task);
        assertEquals(4, path.size());
        Path path0 = path.get(0);
        assertEquals(105, path0.getStartNode());
        assertEquals(3, path0.getEndNode());
        assertEquals(1, path0.getTime(), 0.0000000001);
        assertFalse(path0.isLoop());

        Path path1 = path.get(1);
        assertEquals(3, path1.getStartNode());
        assertEquals(8, path1.getEndNode());
        assertEquals(5, path1.getTime(), 0.0000000001);
        assertFalse(path1.isLoop());

        Path path2 = path.get(2);
        assertEquals(8, path2.getStartNode());
        assertEquals(5, path2.getEndNode());
        assertEquals(5, path2.getTime(), 0.0000000001);
        assertTrue(path2.isLoop());

        Path path3 = path.get(3);
        assertEquals(8, path3.getStartNode());
        assertEquals(7, path3.getEndNode());
        assertEquals(5, path3.getTime(), 0.0000000001);
        assertFalse(path3.isLoop());

        List<TimeWindow> freeTimeWindows = freeTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        for (TimeWindow freeTimeWindow : freeTimeWindows) {
            assertEquals(CommonConstant.INFINITE, freeTimeWindow.getLeastTimeReachHere(), 0.0000000001);
        }

    }

    @Test
    //Path of AGV 0 is 9-2-3-101-102-103-104-105
    public void shouldPathFromGraphToInnerBufferBeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        Double timeAlreadyPassed = 0.0;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(3, reservedTimeWindows.size());
        double endTimeInNode9 = CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode9 = new TimeWindow(8, 0, endTimeInNode9, 0);
        double endTimeInNode2 = endTimeInNode9 + (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, endTimeInNode2 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode9 = reservedTimeWindows.get(2);
        assertEquals(reservedTimeWindowInNode9, reservedTimeWindowForNode9);
        assertEquals(1, reservedTimeWindowForNode9.getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowForNode9.getPath()[0]);
        assertEquals(1, (int)reservedTimeWindowForNode9.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode9.getPath()[2]);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());
    }


    @Test
    //Path of AGV 0 is 9-2-3-101-102-103-104-105, but it starts by having gone 1s
    public void shouldPathFromGraphToInnerBufferWith1SecondPassedBeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        Double timeAlreadyPassed = CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(2, reservedTimeWindows.size());

        double endTimeInNode2 = (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, endTimeInNode2 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());
    }

    @Test
    //Path of AGV 0 is 9-2-3-101-102-103-104-105, but it starts by having gone 2s
    public void shouldPathFromGraphToInnerBufferWith2SecondPassedBeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        Double timeAlreadyPassed = CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED + 1;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(2, reservedTimeWindows.size());

        double endTimeInNode2 = (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED - 1;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, endTimeInNode2 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());
    }

    @Test
    //Path of AGV 0 is 9-2-3-101-102-103-104-105, but it starts by having gone 4s
    public void shouldPathFromGraphToInnerBufferWith4SecondPassedBeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        Double timeAlreadyPassed = 8 / CommonTestConstant.AGV_SPEED;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(2, reservedTimeWindows.size());

        double endTimeInNode2 = (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, endTimeInNode2 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());
    }

    @Test
    //Path of AGV 0 is 9-2-3-101-102-103-104-105, but it starts by having gone 5s
    public void shouldPathFromGraphToInnerBufferWith5SecondPassedBeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        Double timeAlreadyPassed = 8 / CommonTestConstant.AGV_SPEED + 1;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(2, reservedTimeWindows.size());

        double endTimeInNode2 = (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED - 1;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, 0, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());
    }

    @Test
    //Path of AGV 0 is 9(loop at 9 to 8)-2-3-101-102-103-104-105
    public void shouldPathFromGraphToInnerBufferWithLoopAtNode9BeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        double timeToLoop = 3;
        Path path0 = new Path(8, 7, timeToLoop, true);
        Path path1 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path7 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        paths.add(path7);
        Double timeAlreadyPassed = 0.0;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(4, reservedTimeWindows.size());
        double endTimeInNode9 = CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode9 = new TimeWindow(8, 0, endTimeInNode9, 0);
        double endTime1InNode9 = endTimeInNode9 + timeToLoop + CommonConstant.CROSSING_DISTANCE / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindow1InNode9 = new TimeWindow(8, endTime1InNode9 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTime1InNode9, 0);
        double endTimeInNode2 = endTime1InNode9 + (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, endTimeInNode2 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);


        TimeWindow reservedTimeWindowForNode9 = reservedTimeWindows.get(2);
        assertEquals(reservedTimeWindowInNode9, reservedTimeWindowForNode9);
        assertEquals(8, reservedTimeWindowForNode9.getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowForNode9.getPath()[0]);
        assertEquals(7, (int)reservedTimeWindowForNode9.getPath()[1]);
        assertEquals(8, (int)reservedTimeWindowForNode9.getPath()[2]);

        TimeWindow reservedTimeWindow1ForNode9 = reservedTimeWindows.get(3);
        assertEquals(reservedTimeWindow1InNode9, reservedTimeWindow1ForNode9);
        assertEquals(1, reservedTimeWindow1ForNode9.getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindow1ForNode9.getPath()[0]);
        assertEquals(1, (int)reservedTimeWindow1ForNode9.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindow1ForNode9.getPath()[2]);
    }

    @Test
    //Path of AGV 0 is 9-2-3-101-102-103-104-105-4-3-101-102-103-104-105
    public void shouldPathFromGraphToInnerBufferTwiceBeTurnedToReservedTimeWindowCorrectly() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(8, 1, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(1, 2, 10 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path7 = new Path(105, 3, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path8 = new Path(3, 2, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path9 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path10 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path11 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path12 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path13 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        paths.add(path7);
        paths.add(path8);
        paths.add(path9);
        paths.add(path10);
        paths.add(path11);
        paths.add(path12);
        paths.add(path13);
        Double timeAlreadyPassed = 0.0;
        routing.setCurrentPathsToTimeWindows(paths, timeAlreadyPassed, 0);
        List<Queue<TimeWindow>> reservedTimeWindowList = routing.getReservedTimeWindowList();
        assertEquals(11, reservedTimeWindowList.size());
        List<TimeWindow> reservedTimeWindows = reservedTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        assertEquals(5, reservedTimeWindows.size());
        double endTimeInNode9 = CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode9 = new TimeWindow(8, 0, endTimeInNode9, 0);
        double endTimeInNode2 = endTimeInNode9 + (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode2 = new TimeWindow(1, endTimeInNode2 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode2, 0);
        double endTimeInNode3 = endTimeInNode2 + (CommonConstant.CROSSING_DISTANCE + 10) / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedTimeWindowInNode3 = new TimeWindow(2, endTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode3, 0);
        double endTimeInNode4 = endTimeInNode3 + 6 * ((CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED);
        TimeWindow reservedTimeWindowInNode4 = new TimeWindow(3, endTimeInNode4 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, endTimeInNode4, 0);
        double anotherEndTimeInNode3 = endTimeInNode4 + (8 + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED;
        TimeWindow anotherReservedTimeWindowInNode3 = new TimeWindow(2, anotherEndTimeInNode3 - (CommonConstant.AGV_LENGTH + CommonConstant.CROSSING_DISTANCE) / CommonTestConstant.AGV_SPEED, anotherEndTimeInNode3, 0);

        TimeWindow reservedTimeWindowForNode2 = reservedTimeWindows.get(0);
        assertEquals(reservedTimeWindowInNode2, reservedTimeWindowForNode2);
        assertEquals(2, reservedTimeWindowForNode2.getNextNodeNumber());
        assertEquals(1, (int)reservedTimeWindowForNode2.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode2.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode2.getPath()[2]);

        TimeWindow reservedTimeWindowForNode3 = reservedTimeWindows.get(1);
        assertEquals(reservedTimeWindowInNode3, reservedTimeWindowForNode3);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, reservedTimeWindowForNode3.getNextNodeNumber());

        TimeWindow anotherReservedTimeWindowForNode3 = reservedTimeWindows.get(2);
        assertEquals(anotherReservedTimeWindowInNode3, anotherReservedTimeWindowForNode3);
        assertEquals(-1, (int)anotherReservedTimeWindowForNode3.getPath()[0]);
        assertEquals(-1, (int)anotherReservedTimeWindowForNode3.getPath()[1]);
        assertEquals(-1, (int)anotherReservedTimeWindowForNode3.getPath()[2]);
        assertEquals(-1, anotherReservedTimeWindowForNode3.getNextNodeNumber());

        TimeWindow reservedTimeWindowForNode4 = reservedTimeWindows.get(3);
        assertEquals(reservedTimeWindowInNode4, reservedTimeWindowForNode4);
        assertEquals(3, (int)reservedTimeWindowForNode4.getPath()[0]);
        assertEquals(2, (int)reservedTimeWindowForNode4.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode4.getPath()[2]);
        assertEquals(2, reservedTimeWindowForNode4.getNextNodeNumber());


        TimeWindow reservedTimeWindowForNode9 = reservedTimeWindows.get(4);
        assertEquals(reservedTimeWindowInNode9, reservedTimeWindowForNode9);
        assertEquals(1, reservedTimeWindowForNode9.getNextNodeNumber());
        assertEquals(8, (int)reservedTimeWindowForNode9.getPath()[0]);
        assertEquals(1, (int)reservedTimeWindowForNode9.getPath()[1]);
        assertEquals(-1, (int)reservedTimeWindowForNode9.getPath()[2]);
    }

    //Set 2 reserved time windows for node 4 and 1 for node 5 and node 3 to stand for the path 4--5--4--3--...
    //Check if the free time window created is correct.
    @Test
    public void shouldFreeTimeWindowsSetCorrectlyWhenThereAreSomeReservedOneSet() {
        int capacity = 10;
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing(graph, bufferSet, CommonTestConstant.AGV_SPEED, capacity);
        List<Path> paths = new ArrayList<>();
        Path path0 = new Path(105, 3, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path1 = new Path(3, 4, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path2 = new Path(4, 3, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path3 = new Path(3, 2, 8 / CommonTestConstant.AGV_SPEED, false);
        Path path4 = new Path(2, 101, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path5 = new Path(101, 102, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path6 = new Path(102, 103, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path7 = new Path(103, 104, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        Path path8 = new Path(104, 105, CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED, false);
        paths.add(path0);
        paths.add(path1);
        paths.add(path2);
        paths.add(path3);
        paths.add(path4);
        paths.add(path5);
        paths.add(path6);
        paths.add(path7);
        paths.add(path8);
        routing.setCurrentPathsToTimeWindows(paths, 0.0, 0);
        routing.setFreeTimeWindow();
        List<Queue<TimeWindow>> freeTimeWindowList = routing.getFreeTimeWindowList();
        assertEquals(11, freeTimeWindowList.size());
        double timeToEnterNode4 = CommonConstant.BUFFER_PATH_LENGTH / CommonTestConstant.AGV_SPEED;
        double timeToEnterNode5 = timeToEnterNode4 + (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;
        double time1ToEnterNode4 = timeToEnterNode5 + (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;
        double timeToEnterNode3 = time1ToEnterNode4 + (CommonConstant.CROSSING_DISTANCE + 8) / CommonTestConstant.AGV_SPEED;

        Queue<TimeWindow> freeTimeWindowForNode4 = freeTimeWindowList.get(3);
        assertEquals(3, freeTimeWindowForNode4.size());
        assertEquals(new TimeWindow(3, 0, timeToEnterNode4), freeTimeWindowForNode4.poll());
        assertEquals(new TimeWindow(3, getCrossingTime(timeToEnterNode4), time1ToEnterNode4), freeTimeWindowForNode4.poll());
        assertEquals(new TimeWindow(3, getCrossingTime(time1ToEnterNode4), CommonConstant.INFINITE), freeTimeWindowForNode4.poll());

        Queue<TimeWindow> freeTimeWindowForNode5 = freeTimeWindowList.get(4);
        assertEquals(2, freeTimeWindowForNode5.size());
        assertEquals(new TimeWindow(4,0, timeToEnterNode5), freeTimeWindowForNode5.poll());
        assertEquals(new TimeWindow(4, getCrossingTime(timeToEnterNode5), CommonConstant.INFINITE), freeTimeWindowForNode5.poll());

        Queue<TimeWindow> freeTimeWindowForNode3 = freeTimeWindowList.get(2);
        assertEquals(2, freeTimeWindowForNode3.size());
        assertEquals(new TimeWindow(2, 0, timeToEnterNode3), freeTimeWindowForNode3.poll());
        assertEquals(new TimeWindow(2, getCrossingTime(timeToEnterNode3), CommonConstant.INFINITE), freeTimeWindowForNode3.poll());
        int count = 0;
        for (Queue<TimeWindow> timeWindowQueue : freeTimeWindowList) {
            if (count != 2 && count != 3 && count != 4) {
                assertEquals(1, timeWindowQueue.size());
                assertEquals(new TimeWindow(count, 0, CommonConstant.INFINITE), timeWindowQueue.poll());
            }
            count++;
        }
    }

    //Get the time when the AGV cross the crossing
    private double getCrossingTime(double reachTime) {
        return (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED + reachTime;
    }


}
