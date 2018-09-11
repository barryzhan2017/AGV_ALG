package org.spring.springboot.algorithmn.GA;



import java.lang.annotation.Annotation;
import java.util.List;

public class TimeWindowStrategy implements ConflictAvoidStrategy{

    private double AGVSpeed;
    private double[][] graph;
    private Double[] timeAlreadyPassing;
    private Double minDistance;

    @Override
    public void conflictAvoidance(List<List<Integer>> AGVPaths, List<double[]> AGVFitness) {
        TimeWindow timeWindow = new TimeWindow();
        timeWindow.generateTimeWindow(AGVPaths, AGVSpeed, graph, timeAlreadyPassing, minDistance);
        resolveNodeConflict(timeWindow, AGVPaths, AGVFitness);


    }

    //When AGV with low priority is close to the conflict node,
    // it would park to let AGV with high priority go first.
    // The AGV parked needs real-time update of path information table.
    private void resolveNodeConflict(TimeWindow timeWindow,List<List<Integer>> AGVPaths,
                                     List<double[]> AGVFitness) {
        int sizeOfTimeWindow = timeWindow.size();
        for (int i = 0; i < sizeOfTimeWindow - 1; i++) {

            for (int j = i + 1; j < sizeOfTimeWindow; j++) {
                if (timeWindow.containsSameTimeNode(i,j)) {

                }


            }

        }
    }

    private void resolvePursuitConflict(TimeWindow timeWindow,List<List<Integer>> AGVPaths,
                                        List<double[]> AGVFitness) {

    }

    private void resolveOppositeConflict(TimeWindow timeWindow,List<List<Integer>> AGVPaths,
                                         List<double[]> AGVFitness) {


    }



}
