package org.spring.springboot.algorithmn.common;

import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindow;
import org.spring.springboot.algorithmn.conflict_free_routing.TimeWindowComparator;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class CommonTestConstant {
    public static final double AGV_SPEED = 2;
    public static final int SPECIAL_GRAPH_SIZE = 9 + 2;

    public static List<List<Integer>> getBufferForTestGraph2() {
        List<List<Integer>> bufferPath = new ArrayList<List<Integer>>();
        List<Integer> bufferPath1 = new ArrayList<Integer>();
        bufferPath1.add(2);
        bufferPath1.add(101);
        bufferPath1.add(102);
        bufferPath1.add(103);
        bufferPath1.add(104);
        bufferPath1.add(105);
        bufferPath1.add(3);
        List<Integer> bufferPath2 = new ArrayList<Integer>();
        bufferPath2.add(0);
        bufferPath2.add(201);
        bufferPath2.add(202);
        bufferPath2.add(203);
        bufferPath2.add(204);
        bufferPath2.add(205);
        bufferPath2.add(7);
        bufferPath.add(bufferPath1);
        bufferPath.add(bufferPath2);
        return  bufferPath;
    }


    //Initialize the time window list for the graph
    public static List<Queue<TimeWindow>>  initTimeWindowList(int graphNodeNumber) {
        List<Queue<TimeWindow>> timeWindowList = new ArrayList<>(graphNodeNumber);
        for (int i = 0; i < graphNodeNumber; i++) {
            PriorityQueue<TimeWindow> timeWindowQueue =
                    new PriorityQueue<>(10, new TimeWindowComparator());
            timeWindowList.add(timeWindowQueue);
        }
        return timeWindowList;
    }

    public static double[][] initializeGraph() throws IOException {
        //从csv文件中读取矩阵
        File file = new File("TestGraphSet/TestGraph2.csv");
        return (Matrix.Factory.importFrom().file(file).asDenseCSV()).toDoubleArray();
    }
}
