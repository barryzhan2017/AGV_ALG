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

    //One AGV comes from 8 to 9 node and then to 4, the other goes to 9 from 8. Actual node number will be subtracted by 1.
    @Test
    public void ShouldOtherAGVComeInReverseDirectionCauseHeadOnConflict() {
        Routing routing = new Routing();
        List<Queue<TimeWindow>> reservedTimeWindowList = initTimeWindowList();
        PriorityQueue<TimeWindow> reservedTimeWindowQueueForNode7 = new PriorityQueue<>(5, new TimeWindowComparator());
        reservedTimeWindowList.add(reservedTimeWindowQueueForNode7);
        PriorityQueue<TimeWindow> reservedTimeWindowQueueForNode8 = new PriorityQueue<>(5, new TimeWindowComparator());
        reservedTimeWindowList.add(reservedTimeWindowQueueForNode7);
        TimeWindow reservedAGVTimeWindow1 = new TimeWindow(7, 3, 6, 0, 8);
        TimeWindow reservedAGVTimeWindow2 = new TimeWindow(8, 12, 15, 0, 3);
        reservedTimeWindowQueueForNode7.add(reservedAGVTimeWindow1);


    }


}
