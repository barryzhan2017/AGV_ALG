package org.spring.springboot.algorithmn.GA;



import org.spring.springboot.algorithmn.GA.common.DistanceCalculation;

import java.util.List;

public class TimeWindowStrategy implements ConflictAvoidStrategy{

    private double AGVSpeed;
    private double[][] graph;
    private Double[] timeAlreadyPassing;
    private Double minDistance;
    private double timeInterval;

    public TimeWindowStrategy(double AGVSpeed, double[][] graph, Double[] timeAlreadyPassing, Double minDistance) {
        this.AGVSpeed = AGVSpeed;
        this.graph = graph;
        this.timeAlreadyPassing = timeAlreadyPassing;
        this.minDistance = minDistance;
        //This much time can ensure the other AGV drive enough distance instead of conflicting
        this.timeInterval = minDistance/AGVSpeed;
    }

    @Override
    public void conflictAvoidance(List<List<Double>> AGVPaths, double[] AGVFitness) {
        TimeWindow timeWindow = new TimeWindow();
        timeWindow.generateTimeWindow(AGVPaths, AGVSpeed, graph, timeAlreadyPassing, minDistance);
        resolveNodeConflict(timeWindow, AGVPaths, AGVFitness);


    }

    //When AGV with low priority is close to the conflict node,
    // it would park to let AGV with high priority go first.
    // The AGV parked needs real-time update of path information table.
    //public for test
    public void resolveNodeConflict(TimeWindow timeWindow,List<List<Double>> AGVPaths,
                                     double[] AGVFitness) {
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
                    List<Double> path = AGVPaths.get(decelerateAGV);
                    try {
                        path.get(indexInPath-1);
                    }
                    catch (NullPointerException e) {
                        System.out.println("This is because the IndexInPath - 1 NOT EXIST!");
                    }
                    //Have been decreased
                    if (path.get(indexInPath-1).intValue() < 0 ) {

                    }
                    int endNode = AGVPaths.get(decelerateAGV).get(indexInPath).intValue();
                    double distance =
                            DistanceCalculation.calculateDrivingDistance(graph, indexInPath-1 ,indexInPath, minDistance);
                    //Get new decreased speed
                    double decreasedSpeed = distance / (distance / AGVSpeed + timeInterval);
                    //Put it in the path
                    AGVPaths.get(decelerateAGV).add(indexInPath,-decreasedSpeed);
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

    //Now always return the later one, but it may return the one with priority in the future implementation
    private int priorityJudgement(int i, int j) {
        return j;
    }

}
