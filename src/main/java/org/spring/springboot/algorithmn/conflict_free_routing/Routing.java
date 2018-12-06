package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.CommonGraphOperation;

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

    /**
     * Find the head-on conflict by checking the other AGV with the reverse direction
     * @param startNode
     * @param endNode
     * @param timeEnterPath The time the AGV has entered the edge
     * @param timeExistPath The time the AGV leaves the edge (starts to enter the crossing)
     * @return If there is no head-on conflict
     */
    private boolean noHeadOnConflict(int startNode, int endNode, double timeEnterPath, double timeExistPath) {
             for (TimeWindow reverseAGVStartTimeWindow : reservedTimeWindowList[endNode]) {
                 //One AGV comes into the edge in the reverse direction
                 if (reverseAGVStartTimeWindow.getNextNodeNumber() == startNode) {
                     int AGVNumber = reverseAGVStartTimeWindow.getAGVNumber();
                     //The time AGV has entered the edge
                     double reverseAGVStartTime = reverseAGVStartTimeWindow.getEndTime();
                     TimeWindow reverseAGVEndTimeWindow = findNextTimeWindow(startNode, reverseAGVStartTime, AGVNumber);
                     //The time AGV starts to leave the edge
                     double reverseAGVEndTime = reverseAGVEndTimeWindow.getStartTime();
                     //The other AGV comes before the AGV leaves and leaves after the AGV comes. It's a conflict.
                     if (reverseAGVStartTime <= timeExistPath &&
                             reverseAGVEndTime >= timeEnterPath) {
                         return false;
                     }
                 }
             }
             return true;
    }

    /**
     * Find the Time window immediately after the end time of the ongoing AGV coming out of the node
     * by checking the specific reserved time window
     * @param nextNode
     * @param startTime The time the AGV comes out of the node
     * @param AGVNumber
     * @return Next Time Window
     */
    private TimeWindow findNextTimeWindow(int nextNode, double startTime, int AGVNumber) {
        for (TimeWindow timeWindows : reservedTimeWindowList[nextNode]) {
            //First time window the specific AGV arrives
            if (timeWindows.getStartTime() > startTime && timeWindows.getAGVNumber() == AGVNumber) {
                return timeWindows;
            }
        }
        return null;
    }

    /**
     * Find the catch-up conflict by checking the other AGV with the same direction
     * @param startNode
     * @param endNode
     * @param timeEnterPath The time the AGV has entered the edge
     * @param timeExistPath The time the AGV leaves the edge (starts to enter the crossing)
     * @return If there is no catch-up conflict
     */
    private boolean noCatchUpConflict(int startNode, int endNode, double timeEnterPath, double timeExistPath) {
        for (TimeWindow otherAGVStartTimeWindow : reservedTimeWindowList[startNode]) {
            //The time AGV has entered the edge
            double otherAGVStartTime;
            //One other AGV comes into the edge in the same direction
            if (otherAGVStartTimeWindow.getNextNodeNumber() == endNode &&
                    (otherAGVStartTime = otherAGVStartTimeWindow.getEndTime()) != timeEnterPath) {
                int AGVNumber = otherAGVStartTimeWindow.getAGVNumber();
                TimeWindow otherAGVEndTimeWindow = findNextTimeWindow(startNode, otherAGVStartTime, AGVNumber);
                //The time AGV starts to leave the edge
                double otherAGVEndTime = otherAGVEndTimeWindow.getStartTime();
                //The other AGV comes before the AGV comes and leaves after the AGV leaves.
                // Or reverse the sequence of the two AGVs. Those are conflicts.
                if ((otherAGVStartTime < timeEnterPath &&
                        otherAGVEndTime > timeExistPath) ||
                        (timeEnterPath < otherAGVStartTime  &&
                                timeExistPath > otherAGVEndTime )) {
                    return false;
                }
            }
        }
        return true;
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
