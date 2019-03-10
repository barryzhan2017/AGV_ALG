package org.spring.springboot.algorithmn.conflict_free_routing;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestReachabilityTest {

    private double[][] graph;



    @Before
    public void initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        graph = CommonTestConstant.initializeGraph();
    }


    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV time interval for this path includes this ongoing AGV's time interval.
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario1() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8,4,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,8);
        assertFalse(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV but the leave time is earlier than it
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario2() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);

        TimeWindow currentTimeWindow = new TimeWindow(8,4,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,20);
        assertFalse(noHeadConflict);
    }


    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV and the leave time is earlier than its start time also
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario3() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8,6.5,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 4, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 5, 6, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,20);
        assertTrue(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV and the leave time is equal to its start time to go into to the path
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario4() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8,7,8,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 4, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 6, 7, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,8,20);
        assertTrue(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes earlier and leave later.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario1() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8, 4, 5, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 2, 4, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 5, 20);
        assertFalse(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes later and leave earlier.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario2() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 12, 15, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertFalse(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes later and leave later.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario3() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertTrue(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes earlier and leave earlier.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario4() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 6, 8, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 18, 20, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertTrue(noCatchUpConflict);
    }

    @Test
    public void shouldAGVNotPassCrossingWhenHavingNoEnoughTimeRegardingTimeWinodwInterval() {
        Routing routing = new Routing();
        assertEquals(-1.0, routing.timeToReachCrossing(5,0.5,3,6));
    }

    @Test
    public void shouldAGVPassCrossingWhenHavingJustEnoughTimeRegardingTimeWinodwInterval() {
        Routing routing = new Routing();
        double speedToJustPassCrossing = (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH)/(6-4);
        assertEquals(4.0, routing.timeToReachCrossing(4,speedToJustPassCrossing,3,6));
    }

    @Test
    public void shouldAGVPassCrossingWhenHavingMuchEnoughTimeRegardingTimeWindowdwInterval() {

        Routing routing = new Routing();
        assertEquals(4.0, routing.timeToReachCrossing(4,8,4,6));
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 9 to 8 the other time later
    @Test
    public void shouldAGVGoWhenGoingToThePathWillNotCauseHeadOnConflict() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, 22, 1, -1,6);
        //AGV 2
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 18, 19, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 0, 18, -1, -1);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(8, 19, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 23, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        assertEquals((double)timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }
    //AGV 1 starts from node 9 and goes to time window at node 3. But the given time window is earlier than current time window.
    @Test
    public void shouldAGVNotMoveWhenReachabilityIsAvaliableForSomePreviousTimeWindow() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer[] path = {-2, -2, -2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(8, 3, 6, 1, -1, 0);
        reservedTimeWindowList.get(8).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(2, 0, 3, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        double timeGoThrough = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals(-1, (int) path[0]);
        assertEquals(-1, (int) path[0]);
        assertEquals(-1, (int) path[2]);
        assertEquals((double) CommonConstant.INFINITE, timeGoThrough);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 9 to 8
    @Test
    public void shouldAGVNotGoWhenGoingToThePathWillCauseHeadOnConflict() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, 22, 1, -1, 6);
        //AGV 2
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(8, 0, 9, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 10, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 23, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 8 to 9 latter at first and surpasses the other AGV
    @Test
    public void shouldAGVNotGoWhenGoingToThePathWillCauseCatchUpConflict()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, 9, 1, -1, 6);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 9, 10, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 11, 13, 0, 1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(7, 10, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, 0, 11, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 13, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 8 to 9 latter at first and crosses the path still later
    @Test
    public void shouldAGVGoWhenGoingToThePathWillNotCauseCatchUpConflict()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, 9, 1, -1, 6);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 9, 10, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 20, 21, 0, 1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 0, 20, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 10, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(8, 21, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        assertEquals(timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }


    //The AGV 1 at node 8 wants to choose to go node 6, but the path does not exist.
    @Test
    public void shouldAGVNotGoWhenPathNotExists()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, CommonConstant.INFINITE, 1, -1, 6);
        TimeWindow endTimeWindow = new TimeWindow(5, 0, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(5).add(endTimeWindow);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the free time window is not available.
    @Test
    public void shouldAGVNotGoWhenTimeWindowIsNotAvailable() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, CommonConstant.INFINITE, 1, -1, 6);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode9 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH / CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing-0.1, timeCrossCrossing + 6, 2, 1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        // subtract 0.1 to not allow the AGV pass
        TimeWindow endTimeWindow = new TimeWindow(8, 9, timeCrossCrossing-0.1, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, and the free time window is available
    @Test
    public void shouldAGVGoCorrectlyWhenTimeWindowIsAvailable() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, CommonConstant.INFINITE, 1, -1, 6);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode9 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH/CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing + 0.1, timeCrossCrossing + 6, 2, 1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        // add 0.1 to allow the AGV pass
        TimeWindow endTimeWindow = new TimeWindow(8, 9, timeCrossCrossing + 0.1, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals(timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }

    //The AGV at node 8 wants to choose to go node 9, and the first free time window is just long enough to cross.
    @Test
    public void shouldAGVGoCorrectlyWhenTimeWindowIsJustAvailable() {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, CommonConstant.INFINITE, 1, -1, 6);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode9 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH/CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing, timeCrossCrossing+6, 2, 1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow endTimeWindow = new TimeWindow(8, 9, timeCrossCrossing, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals(timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }

    //should two buffer from graph 2 can be added into the special graph
    @Test
    public void shouldBufferBeAddedToTheGraphCorrectly() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        Routing routing = new Routing();
        double[][] newGraph = routing.initializeGraphWithBufferEndNode(graph, bufferSet);
        assertEquals(2.0,newGraph[9][3]);
        assertEquals((double)CommonConstant.MAX_EDGE,newGraph[3][9]);
        assertEquals(2.0,newGraph[10][7]);
        assertEquals((double)CommonConstant.MAX_EDGE,newGraph[7][10]);
        // Test if there is no link between buffer end node and other graph node in the new graph
        for (int i = 0; i < 9; i++) {
            if (i != 3) {
                assertEquals((double)CommonConstant.MAX_EDGE, newGraph[i][9]);
                assertEquals((double)CommonConstant.MAX_EDGE, newGraph[9][i]);
            }
            if (i != 7) {
                assertEquals((double)CommonConstant.MAX_EDGE, newGraph[i][10]);
                assertEquals((double)CommonConstant.MAX_EDGE, newGraph[10][i]);
            }
        }
        //Test if the graph node still exist in new graph
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertEquals(graph[i][j], newGraph[i][j]);
            }
        }
        //Test if the Hashmap stores the corresponding mapping for the buffer node
        Map<Integer, Integer> graphToBuffer = routing.getGraphNodeToBuffer();
        assertEquals(2, graphToBuffer.size());
        assertEquals(105, (int)graphToBuffer.get(9));
        assertEquals(205, (int)graphToBuffer.get(10));
    }

    //The AGV at node 105 wants to choose to go node 4, and the first free time window is long enough to cross.
    @Test
    public void shouldAGVGoCorrectlyFromBufferWhenTimeWindowIsAvailable() {
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(9, 0, CommonConstant.INFINITE, 1, -1, 6);
        double timeArriveNode4 = (2 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode4 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH/CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(3, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(3, timeCrossCrossing, timeCrossCrossing + 6, 2, 1);
        reservedTimeWindowList.get(3).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(3).add(reservedAGVTimeWindow2);
        TimeWindow endTimeWindow = new TimeWindow(3, 9, timeCrossCrossing, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(3, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(3).add(endTimeWindow);
        freeTimeWindowList.get(9).add(currentTimeWindow);
        freeTimeWindowList.get(3).add(freeAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        double timeGoThroughPath = routing.testReachabilityForDifferentNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) timeArriveNode4, timeGoThroughPath);
        assertEquals(9, (int)path[0]);
        assertEquals(3, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 9 wants to loop at the end node 9 and goes to node 4
    @Test
    public void shouldHeadOnConflictHappenWhenOtherAGVLoopsAtTheEndOfThePathBlockingThePath()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, CommonConstant.INFINITE, 1, -1,6);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 7, 10, 0, 8);
        reservedAGVTimeWindow1.setPath(new Integer[]{8, 7, 8});
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 16, 18, 0, 3);
        reservedAGVTimeWindow2.setPath(new Integer[]{8, 3, -1});
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(8, 0, 7, -1, -1);
        TimeWindow endTimeWindow = new TimeWindow(8, 10, 16, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(8, 18, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow4);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noHeadOnConflict = routing.noHeadOnConflict(7,8,9, (10 - CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED + 9);
        assertFalse(noHeadOnConflict);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, and the other AGV 0 comes from 9 wants to loop at the end node 9 and leaves earlier than the AGV 1
    @Test
    public void shouldHeadOnConflictNotHappenWhenOtherAGVLoopsAtTheEndOfThePathNotBlockingThePathLater()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 0, CommonConstant.INFINITE, 1, -1,6);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 7, 10, 0, 8);
        reservedAGVTimeWindow1.setPath(new Integer[]{8, 7, 8});
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedAGVTimeWindow2.setPath(new Integer[]{8, 3, -1});
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(8, 0, 7, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(8, 10, 12, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 15, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow4);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null, CommonTestConstant.AGV_SPEED);
        boolean noHeadOnConflict = routing.noHeadOnConflict(7,8,9,  15);
        assertTrue(noHeadOnConflict);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 9 wants to loop at the start node 9 and goes to node 4
    @Test
    public void shouldCatchUpConflictHappenWhenOtherAGVLoopsAtTheStartOfThePathBlockingThePath()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 6, 16, 1, -1,6);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 4, 6, 0, 7);
        reservedAGVTimeWindow1.setPath(new Integer[]{7, 8, 7});
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 16, 18, 0, 6);
        reservedAGVTimeWindow2.setPath(new Integer[]{7, 6, -1});
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 0, 4, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(7, 18, CommonConstant.INFINITE, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 0, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow4);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noCatchUpConflict = routing.noCatchUpConflict(7,8,9, (10 - CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED + 9);
        assertFalse(noCatchUpConflict);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, and the other AGV 0 comes from 9 wants to loop at the start node 9 and leaves earlier than the AGV 1
    @Test
    public void shouldCatchUpConflictNotHappenWhenOtherAGVLoopsAtTheStartOfThePathBlockingThePathEarlier()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, CommonConstant.INFINITE, 1, -1,8);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 0, 3, 0, 7);
        reservedAGVTimeWindow1.setPath(new Integer[]{7, 8, 7});
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 6, 8, 0, 6);
        reservedAGVTimeWindow2.setPath(new Integer[]{7, 6, -1});
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 3, 6, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 0, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(currentTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, null,CommonTestConstant.AGV_SPEED);
        boolean noCatchUpConflict = routing.noCatchUpConflict(7,8,11, (10 - CommonConstant.AGV_LENGTH) / CommonTestConstant.AGV_SPEED + 11);
        assertTrue(noCatchUpConflict);
    }

    //AGV 1 starts from node 9 and loops to node 9. But the given time window is earlier than current time window.
    @Test
    public void shouldAGVNotLoopWhenReachabilityIsAvaliableForSomePreviousTimeWindow(){
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(8, 3, 6, 1, -1, 0);

        reservedTimeWindowList.get(8).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(8, 0, 3, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        double timeGoThrough = routing.testReachabilityForSameNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals(-1, (int)path[0]);
        assertEquals(-1, (int)path[0]);
        assertEquals(-1, (int)path[2]);
        assertEquals(CommonConstant.INFINITE, timeGoThrough);
    }

    //Agv 1 starts from node 9 and loops to node 9 again, AGV 0 goes from node 8 to node 9 and backs to node 8 again. Not all paths are blocked.
    @Test
    public void shouldLoopSuccessfullyWhenReachabilityIsAvailableForSomePaths(){
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        Integer [] path = {-2,-2,-2};

        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(8, 0, 6, 1, -1, 0);
        // AGV 0
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7,6,7,0,8);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 8, 9, 0, 7);

        reservedTimeWindowList.get(8).add(currentTimeWindow);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        reservedAGVTimeWindow1.setLastTimeWindow(reservedAGVTimeWindow2);
        TimeWindow endTimeWindow = new TimeWindow(8, 10, 12, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(currentTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        double timeGoThrough = routing.testReachabilityForSameNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);

        assertEquals(8, (int)path[0]);
        assertTrue(path[1] == 1 || path[1] == 3 || path[1] == 5);
        assertEquals(8, (int)path[2]);
        assertEquals(10.0, timeGoThrough);
    }


    //Agv1 loops at node 9，Agv0 goes 2---9---6  Agv2 goes 8---9----4. So AGV cannot loop
    @Test
    public void shouldLoopReachabilityIsNotAvailableWhenAllPathsAreBlocked(){
        List<List<Integer>> bufferSet = CommonTestConstant.getBufferForTestGraph2();
        List<Queue<TimeWindow>> reservedTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        List<Queue<TimeWindow>> freeTimeWindowList = CommonTestConstant.initTimeWindowList(CommonTestConstant.SPECIAL_GRAPH_SIZE);;
        Integer [] path = {-2,-2,-2};

        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(8, 0, 7, 1, -1, 3);
        // AGV 0 and 2
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(1, 5, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 5, 6, 2, 8);
        TimeWindow reservedAGVTimeWindow3 = new TimeWindow(8, 7, 8, 0, 5);
        TimeWindow reservedAGVTimeWindow4 = new TimeWindow(8, 8, 9, 2, 3);
        reservedAGVTimeWindow3.setLastTimeWindow(reservedAGVTimeWindow1);
        reservedAGVTimeWindow4.setLastTimeWindow(reservedAGVTimeWindow2);

        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow4);
        reservedTimeWindowList.get(1).add(reservedAGVTimeWindow1);

        TimeWindow endTimeWindow = new TimeWindow(8, 10,12, -1, -1);

        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(currentTimeWindow);

        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, -1, graph, currentTimeWindow, bufferSet, CommonTestConstant.AGV_SPEED);
        double timeGoThrough = routing.testReachabilityForSameNode(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);

        assertEquals((double)CommonConstant.INFINITE, timeGoThrough);
        assertEquals(-1, (int)path[0]);
        assertEquals(-1, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }




}
