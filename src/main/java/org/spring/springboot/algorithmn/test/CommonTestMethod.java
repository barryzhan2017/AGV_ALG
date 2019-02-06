//package org.spring.springboot.algorithmn.test;
//
//import org.spring.springboot.algorithmn.GA.AGVRecord;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CommonTestMethod {
//
//    public static double SPEED = 2;
//    public static double MIN_DISTANCE = 2;
//    //Generate the basic AGV records by path, exclude the node in the buffer
//    public static List<AGVRecord> pathToRecords(List<Integer> path, int graphSize) {
//        List<AGVRecord> records = new ArrayList<>();
//        for (int i = 0; i < path.size() - 1; i++) {
//            int node1 = path.get(i + 1);
//            if (node1 < graphSize) {
//                AGVRecord record = new AGVRecord(i, i + 1, (int)path.get(i), node1, SPEED);
//                records.add(record);
//            }
//        }
//        return records;
//    }
//    public static List<List<AGVRecord>> manyPathsToRecords(List<List<Integer>> paths, int graphSize) {
//        List<List<AGVRecord>> records = new ArrayList<>();
//        for (List<Integer> path : paths) {
//            List<AGVRecord> record = pathToRecords(path, graphSize);
//            records.add(record);
//        }
//        return records;
//
//    }
//}
