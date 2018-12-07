package org.spring.springboot.algorithmn.test;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindow;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindowComparator;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;

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


    //
    @Test
    public void ShouldOtherAGVComeInReverseDirectionCauseHeadOnConflict() {
        Routing routing = new Routing();
        PriorityQueue<TimeWindow> reservedTimeWindowList = new PriorityQueue<>(20, new TimeWindowComparator());
        PriorityQueue<TimeWindow> freeTimeWindowList = new PriorityQueue<>(20, new TimeWindowComparator());
        
    }


}
