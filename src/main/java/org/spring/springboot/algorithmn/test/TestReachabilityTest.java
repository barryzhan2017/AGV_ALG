package org.spring.springboot.algorithmn.test;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindow;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindowComparator;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestReachabilityTest {

    private double AGVSpeed = 2;
    private double[][] graph;



    @Before
    public void initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        File file = new File("TestGraphSet/TestGraph2.csv");
        graph = (Matrix.Factory.importFrom().file(file).asDenseCSV()).toDoubleArray();
//       System.out.println(graph);
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
    public void ShouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario1() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8,4,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,8);
        assertFalse(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV but the leave time is earlier than it
    @Test
    public void ShouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario2() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8,4,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,20);
        assertFalse(noHeadConflict);
    }


    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV and the leave time is earlier than its start time also
    @Test
    public void ShouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario3() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8,6.5,7,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 4, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 5, 6, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,7,20);
        assertTrue(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV comes from 8 to 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV start time is earlier than this ongoing AGV and the leave time is equal to its start time to go into to the path
    @Test
    public void ShouldOtherAGVComeInReverseDirectionCauseHeadOnConflictInScenario4() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8,7,8,1,7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 4, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 6, 7, 0, 3);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noHeadConflict = routing.noHeadOnConflict(8,7,8,20);
        assertTrue(noHeadConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes earlier and leave later.
    @Test
    public void ShouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario1() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8, 4, 5, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 2, 4, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 5, 20);
        assertFalse(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes later and leave earlier.
    @Test
    public void ShouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario2() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 12, 15, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertFalse(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes later and leave later.
    @Test
    public void ShouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario3() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 9, 10, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 22, 23, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertTrue(noCatchUpConflict);
    }

    //Ongoing AGV goes to 8 from 9, the other AGV goes to 8 from 9 node and then to 4. Actual node number will be subtracted by 1.
    //The other AGV comes earlier and leave earlier.
    @Test
    public void ShouldOtherAGVComeInSameDirectionCauseCatchUpConflictInScenario4() {
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        List<Queue<TimeWindow>> freeTimeWindowList = initTimeWindowList();
        int[] task = null;
        double[][] graph = this.graph;
        TimeWindow currentTimeWindow = new TimeWindow(8, 8, 9, 1, 7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(8, 6, 8, 0, 7);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(7, 18, 20, 0, 3);
        reservedTimeWindowList.get(8).add(reservedAGVTimeWindow1);
        reservedTimeWindowList.get(7).add(reservedAGVTimeWindow2);
        Routing routing = new Routing(freeTimeWindowList, reservedTimeWindowList, task, graph, currentTimeWindow);
        boolean noCatchUpConflict = routing.noCatchUpConflict(8, 7, 9, 20);
        assertTrue(noCatchUpConflict);
    }

}
