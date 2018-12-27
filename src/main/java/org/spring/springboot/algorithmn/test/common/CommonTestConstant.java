package org.spring.springboot.algorithmn.test.common;

import java.util.ArrayList;
import java.util.List;

public class CommonTestConstant {
    public static double AGV_SPEED = 2;

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
}
