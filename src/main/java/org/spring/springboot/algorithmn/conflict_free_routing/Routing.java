package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;

import java.util.List;
import java.util.PriorityQueue;

//This is the algorithm to calculate the conflict-free route for one AGV given the current time windows.
public class Routing {

    //The time window the AGV can travel, each row is for a specific node
    private PriorityQueue<TimeWindow>[] freeTimeWindowList;
    //The time window is reserved by other AGVs, each row is for a specific node
    private PriorityQueue<TimeWindow>[] reservedTimeWindowList;
    //The task for the specific AGV to finish
    private int[] task;
    private double[][] graph;
    //Current time window the AGV occupies
    private TimeWindow currentTimeWindow;
    private double AGVLength;
    //consider the buffer information. how to get


    //Get the route of the AGV by applying conflict-free routing algorithm
    public List<Integer> route() {

    }

    /**
     * Test if one time window is reachable for the other different one and return the path and minimum time required to travel
     * @param endNode Possible Destination
     * @param currentTimeWindow Current status of the vehicle
     * @param path Path if the end node can be reached
     * @param speed
     * @return Time required to go for the path
     */
     private double testReachabilityForDifferentNode(int endNode, TimeWindow currentTimeWindow, Integer[] path, double speed) {
        int startNode = currentTimeWindow.getNodeNumber();
         path[0] = -1;
        //Check for space availability
        if (graph[startNode][endNode] == CommonConstant.MAX_EDGE) {
            return CommonConstant.INFINITE;
        }
        //Check for time availability. If the free time window is long enough for the AGV to pass the crossing
        double distance = graph[startNode][endNode];
        //The time for the AGV to reach the entrance edge of crossing
        double timeToReachCrossing =  (distance - AGVLength) / speed;
        //Search for the nearest available time window
        double validTimeToReachCrossing;
        for (TimeWindow possibleTimeWindow : freeTimeWindowList[endNode]) {
            double startTime = possibleTimeWindow.getStartTime();
            double endTime = possibleTimeWindow.getEndTime();
            if ((validTimeToReachCrossing = timeToReachCrossing(timeToReachCrossing, speed, startTime, endTime)) != -1) {
                //Check for the conflict
                double timeEnterPath = currentTimeWindow.getEndTime();
                if (noCatchUpConflict(startNode, endNode ,timeEnterPath, validTimeToReachCrossing)
                        && noHeadOnConflict(startNode, endNode ,timeEnterPath, validTimeToReachCrossing)) {
                    path[0] = startNode;
                    path[1] = endNode;
                    return validTimeToReachCrossing;
                }
                //No other solution if the nearest available time window cannot avoid conflicts
                return CommonConstant.INFINITE;
            }
        }
        //Cannot have enough time to pass the AGV
         return CommonConstant.INFINITE;
     }

    private boolean noHeadOnConflict(int startNode, int endNode, double timeEnterPath, double validTimeToReachCrossing) {
    }

    private boolean noCatchUpConflict(int startNode, int endNode, double timeEnterPath, double validTimeToReachCrossing) {
    }

    /**
     * Test if the free time window has enough time to let the AGV pass the crossing
     * @param timeToReachCrossing
     * @param speed
     * @param startTime Start time of the possible free time window
     * @param endTime End time of the possible free time window
     * @return The time to reach the crossing if the free time window has enough time or -1 if not enough time
     */
    private double timeToReachCrossing(double timeToReachCrossing, double speed, double startTime, double endTime) {
            //startTime of the possible time window
            if (startTime > currentTimeWindow.getEndTime()) {
                //Find the nearest possible time to enter the crossing
                double validTimeToReachEndNode = Math.max(startTime, timeToReachCrossing);
                //Time the AGV passes the crossing
                double validTimeToCrossEndNode = validTimeToReachEndNode + CommonConstant.CROSSING_DISTANCE / speed;
                //AGV can pass the free time window into the path
                if (validTimeToCrossEndNode < endTime) {
                    return validTimeToReachEndNode;
                }
            }
        return -1;
    }


}
