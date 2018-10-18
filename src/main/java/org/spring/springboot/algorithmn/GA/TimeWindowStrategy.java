package org.spring.springboot.algorithmn.GA;



import com.sun.prism.impl.Disposer;
import org.spring.springboot.algorithmn.GA.common.DistanceCalculation;

import java.util.List;

public class TimeWindowStrategy implements ConflictAvoidStrategy{

    private double AGVSpeed;
    private double[][] graph;
    private Double[] timeAlreadyPassing;
    private Double minDistance;
    private double timeInterval;
    private int AGVNumber;
    private List<List<AGVRecord>> AGVRecords;

    public TimeWindowStrategy(double AGVSpeed, double[][] graph, Double[] timeAlreadyPassing, Double minDistance,
                              List<List<AGVRecord>> AGVRecords) {
        this.AGVSpeed = AGVSpeed;
        this.graph = graph;
        this.timeAlreadyPassing = timeAlreadyPassing;
        this.minDistance = minDistance;
        //This much time can ensure the other AGV drive enough distance instead of conflicting
        this.timeInterval = minDistance/AGVSpeed;
        AGVNumber = timeAlreadyPassing.length;
        this.AGVRecords = AGVRecords;

    }

    @Override
    public void conflictAvoidance(List<List<Integer>> AGVPaths, double[] AGVFitness) {
        TimeWindow timeWindow = new TimeWindow();
        timeWindow.generateTimeWindow(AGVPaths, AGVSpeed, graph, timeAlreadyPassing, minDistance);
        resolveNodeConflict(timeWindow, AGVPaths);
        resolvePursuitConflict(timeWindow, AGVPaths);


    }

    //When AGV with low priority is close to the conflict node,
    // it would park to let AGV with high priority go first.
    // The AGV parked needs real-time update of path information table.
    //public for test
    public void resolveNodeConflict(TimeWindow timeWindow,List<List<Integer>> AGVPaths) {
        int sizeOfTimeWindow = timeWindow.size();
        for (int i = 0; i < sizeOfTimeWindow - 1; i++) {
            for (int j = i + 1; j < sizeOfTimeWindow; j++) {
                // Find all conflicts in two sub time windows
                //Create the priority method for further implementation
                int decelerateAGV = priorityJudgement(i, j);
                //The index in the sub time window causing the conflict
                int indexOfConflict;
                while ((indexOfConflict = timeWindow.containsSameTimeNode(i, j, decelerateAGV)) != -1) {
                    timeWindow.delay(decelerateAGV, timeInterval, indexOfConflict);
                    int indexInPath = timeWindow.getStep(decelerateAGV, indexOfConflict);
                    List<Integer> path = AGVPaths.get(decelerateAGV);
                    try {
                        path.get(indexInPath-1);
                    }
                    catch (NullPointerException e) {
                        System.out.println("This is because the IndexInPath - 1 NOT EXIST!");
                    }
                    //Because the record does not record the path in buffer, so we need to find the index in record by
                    //subtracting the number of paths in the buffer
                    int recordIndex = findActualIndexInRecord(path, indexInPath);
                    //The record that needs to be adjusted for the speed
                    System.out.println(decelerateAGV);
                    System.out.println(recordIndex);
                    AGVRecord decelerateSpeedRecord = AGVRecords.get(decelerateAGV).get(recordIndex);
                    double currentSpeed = decelerateSpeedRecord.getSpeed();
                    double distance =
                            DistanceCalculation.calculateDrivingDistance(graph, indexInPath-1 ,
                                    indexInPath, minDistance);
                    //Get new decreased speed
                    double decreasedSpeed = distance / (distance / currentSpeed + timeInterval);
                    //Put it in the AGVRecord
                    decelerateSpeedRecord.setSpeed(decreasedSpeed);
                }
            }
        }
    }

    private void resolvePursuitConflict(TimeWindow timeWindow,List<List<Integer>> AGVPaths) {

    }

    private void resolveOppositeConflict(TimeWindow timeWindow,List<List<Integer>> AGVPaths,
                                         List<double[]> AGVFitness) {


    }

    //Find the actual index in agv record by subtracting the number of path during the buffer
    private int findActualIndexInRecord(List<Integer> decelerateAGVPath, int indexInPath) {
        //The max index exists in the graph
        System.out.println(indexInPath);
        int maxGraphIndex = graph[0].length - 1;
        int numberOfNodeInBuffer = 0;
        boolean nodeAllInGraph = true;
        for (Integer node : decelerateAGVPath) {
            if (node > maxGraphIndex) {
                numberOfNodeInBuffer++;
                nodeAllInGraph = false;
            }
            //Only subtract the number of node before the current index
            if (node == indexInPath) {
                break;
            }
        }
        System.out.println(numberOfNodeInBuffer);
        System.out.println(decelerateAGVPath);
        if (!nodeAllInGraph) {
            return indexInPath - numberOfNodeInBuffer;
        }
        //If all the nodes are not in the buffer, the index should subtract 1 to be conform with the index in the record
        else {
            return indexInPath - 1;
        }
    }




    //Now always return the later one, but it may return the one with priority in the future implementation
    private int priorityJudgement(int i, int j) {
        return j;
    }

}
