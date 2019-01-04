package org.spring.springboot.algorithmn.conflict_free_routing;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 1, -1);
        reservedTimeWindowList.get(9).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(0, 0, CommonConstant.INFINITE, -1, -1);
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
        assertEquals(-1, (int)endTimeWindow.getPath()[0]);
    }

    //Start from source node 9, and try to add free time window in node 4. it should succeed because there is a link and he time window is available.
    @Test
    public void shouldNextPossibleTimeWindowBeAddedAndTimeWindowStatusChangesCorrectlyWhenItIsReachable() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        int task = 0;
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 1, -1);
        reservedTimeWindowList.get(9).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(3, 0, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(0).add(endTimeWindow);
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
        assertEquals((2-1)/2.0, endTimeWindow.getLeastTimeReachHere(), 0.000000001);
        assertEquals(currentTimeWindow, endTimeWindow.getLastTimeWindow());
        assertEquals(9, (int)endTimeWindow.getPath()[0]);
        assertEquals(3, (int)endTimeWindow.getPath()[1]);
        assertEquals(-1, (int)endTimeWindow.getPath()[2]);
    }


}
