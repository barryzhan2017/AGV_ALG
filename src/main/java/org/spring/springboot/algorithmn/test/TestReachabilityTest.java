package org.spring.springboot.algorithmn.test;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindow;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindowComparator;
import org.spring.springboot.algorithmn.test.common.CommonTestConstant;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestReachabilityTest {

    private double[][] graph;



    @Before
    public void initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        File file = new File("TestGraphSet/TestGraph2.csv");
        graph = (Matrix.Factory.importFrom().file(file).asDenseCSV()).toDoubleArray();
    }

    //Initialize the time window list for the graph
    private List<Queue<TimeWindow>> initTimeWindowList() {
        int graphNodeNumber = graph[0].length;
        List<Queue<TimeWindow>> timeWindowList = new ArrayList<>(graphNodeNumber);
        for (int i = 0; i < graphNodeNumber; i++) {
            PriorityQueue<TimeWindow> timeWindowQueue =
                    new PriorityQueue<>(10, new TimeWindowComparator());
            timeWindowList.add(timeWindowQueue);
        }
        return timeWindowList;
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV time interval for this path includes this ongoing AGV's time interval.
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario1() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8,4,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,8);
        assertFalse(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV but the leave time is earlier than it
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario2() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();

        TimeWindow currentTimeWindow = new TimeWindow(8,4,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,20);
        assertFalse(noHeadConflict);
    }


    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV and the leave time is earlier than its start time also
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario3() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8,6.5,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 4, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 5, 6, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,20);
        assertTrue(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV and the leave time is equal to its start time to go into to the path
    @Test
    public void shouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario4() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8,7,8,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 4, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 6, 7, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,8,20);
        assertTrue(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes earlier and leave later.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario1() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8, 4, 5, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 2, 4, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 5, 20);
        assertFalse(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes later and leave earlier.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario2() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 12, 15, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertFalse(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes later and leave later.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario3() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertTrue(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes earlier and leave earlier.
    @Test
    public void shouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario4() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 6, 8, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 18, 20, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertTrue(noCatchUpConflict);
    }

    @Test
    public void shouldAGVCannotPassCrossingWhenHavingNoEnoughTimeRegardingTimeWinodwInterval() {
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
    public void shouldAGVCanGoWhenGoingToThePathWillNotCauseHeadOnConflict() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        //AGV 2
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 18, 19, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 0, 18, -1, -1);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(8, 18, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 9, 22, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(7, 23, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow4);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        assertEquals((double)timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 9 to 8
    @Test
    public void shouldAGVCannotGoWhenGoingToThePathWillCauseHeadOnConflict() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        //AGV 2
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(8, 0, 9, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 10, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 9, 22, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(7, 23, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow4);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 8 to 9 latter at first and surpasses the other AGV
    @Test
    public void shouldAGVCannotGoWhenGoingToThePathWillCauseCatchUpConflict()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 9, 10, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 11, 13, 0, 1);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 10, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(8, 0, 11, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 13, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the other AGV 0 comes from 8 to 9 latter at first and crosses the path still later
    @Test
    public void shouldAGVCanGoWhenGoingToThePathWillNotCauseCatchUpConflict()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        // AGV 0
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 9, 10, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 20, 21, 0, 1);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow freeAGVTimeWindow1 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 10, CommonConstant.INFINITE, -1, -1);
        //leave enough time for the AGV to not miss the time availability to cross the crossing
        TimeWindow endTimeWindow = new TimeWindow(8, 0, 20, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(8, 21, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow1);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(8).add(endTimeWindow);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        assertEquals((double)timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }


    //The AGV 1 at node 8 wants to choose to go node 6, but the path does not exist.
    @Test
    public void shouldAGVCannotGoWhenPathNotExists()  {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        TimeWindow endTimeWindow = new TimeWindow(5, 0, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 9, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(5).add(endTimeWindow);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, but the free time window is not available.
    @Test
    public void shouldAGVNotGoWhenTimeWindowIsNotAvailable() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode9 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH/CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing-0.1, timeCrossCrossing+6, 2, 1);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        // subtract 0.1 to not allow the AGV pass
        TimeWindow endTimeWindow = new TimeWindow(8, 9, timeCrossCrossing-0.1, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(7, 9, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow4);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) CommonConstant.INFINITE, timeGoThroughPath);
        assertEquals(-1, (int)path[0]);
    }

    //The AGV 1 at node 8 wants to choose to go node 9, and the free time window is available
    @Test
    public void shouldAGVGoCorrectlyWhenTimeWindowIsAvailable() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode9 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH/CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing + 0.1, timeCrossCrossing + 6, 2, 1);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        // add 0.1 to allow the AGV pass
        TimeWindow endTimeWindow = new TimeWindow(8, 9, timeCrossCrossing + 0.1, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(7, 9, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow4);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }

    //The AGV at node 8 wants to choose to go node 9, and the first free time window is just long enough to cross.
    @Test
    public void shouldAGVGoCorrectlyWhenTimeWindowIsJustAvailable() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        Integer [] path = {-2,-2,-2};
        //AGV 1
        TimeWindow currentTimeWindow = new TimeWindow(7, 8, 9, 1, -1);
        double timeArriveNode9 = (10 - CommonConstant.AGV_LENGTH)/CommonTestConstant.AGV_SPEED + 9;
        double timeCrossCrossing = timeArriveNode9 + CommonConstant.CROSSING_DISTANCE/CommonTestConstant.AGV_SPEED + CommonConstant.AGV_LENGTH/CommonTestConstant.AGV_SPEED;
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 0, 9, 0, 1);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing, timeCrossCrossing+6, 2, 1);
        reservedTimeWindowList.get(7).add(currentTimeWindow);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        TimeWindow endTimeWindow = new TimeWindow(8, 9, timeCrossCrossing, -1, -1);
        TimeWindow freeAGVTimeWindow2 = new TimeWindow(8, timeCrossCrossing+6, CommonConstant.INFINITE, -1, -1);
        TimeWindow freeAGVTimeWindow3 = new TimeWindow(7, 0, 8, -1, -1);
        TimeWindow freeAGVTimeWindow4 = new TimeWindow(7, 9, CommonConstant.INFINITE, -1, -1);
        freeTimeWindowList.get(8).add(endTimeWindow);
        freeTimeWindowList.get(8).add(freeAGVTimeWindow2);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow3);
        freeTimeWindowList.get(7).add(freeAGVTimeWindow4);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, null, graph, currentTimeWindow);
        double timeGoThroughPath = routing.testReachabilityForDifferentTimeWindow(endTimeWindow, currentTimeWindow, path, CommonTestConstant.AGV_SPEED);
        assertEquals((double) timeArriveNode9, timeGoThroughPath);
        assertEquals(7, (int)path[0]);
        assertEquals(8, (int)path[1]);
        assertEquals(-1, (int)path[2]);
    }






}
