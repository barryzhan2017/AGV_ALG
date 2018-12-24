package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.exception.InvalidReachingTimeException;


import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

//This is the algorithm to calculate the conflict-free route for one AGV given the current time windows.
public class Routing {

    //The time window the AGV can travel, each row is for a specific node
    private List<Queue<TimeWindow>> freeTimeWindowList;
    //The time window is reserved by other AGVs, each row is for a specific node
    private List<Queue<TimeWindow>> reservedTimeWindowList;
    //The task for the specific AGV to finish
    private int[] task;
    private double[][] graph;
    //Current time window the AGV occupies
    private TimeWindow currentTimeWindow;
    //consider the buffer information. how to get

    public Routing() {

    }

    public Routing(List<Queue<TimeWindow>> freeTimeWindowList, List<Queue<TimeWindow>> reservedTimeWindowList, int[] task, double[][] graph, TimeWindow currentTimeWindow) {
        this.freeTimeWindowList = freeTimeWindowList;
        this.reservedTimeWindowList = reservedTimeWindowList;
        this.task = task;
        this.graph = graph;
        this.currentTimeWindow = currentTimeWindow;
    }


//    //Get the route of the AGV by applying conflict-free routing algorithm
//    public List<Integer> route() {
//
//    }


    /**
     *
     * @param currentTimeWindow Current status of the vehicle
     * @param path Path if the end node can be reached
     * @param speed
     * @return Time required to go for the path, to the end node
     */
    public double testReachabilityForSameNode(TimeWindow currentTimeWindow, Integer[] path, double speed) {

        return -1;

    }

    /**
     * Test if one time window is reachable for the other different one and return the path and minimum time required to travel
     * @param endNode Possible Destination
     * @param currentTimeWindow Current status of the vehicle
     * @param path Path if the end node can be reached
     * @param speed
     * @return Time required to go for the path, to the end node
     */
     public double testReachabilityForDifferentNode(int endNode, TimeWindow currentTimeWindow, Integer[] path, double speed) {
        int startNode = currentTimeWindow.getNodeNumber();
        double currentAGVStartTime = currentTimeWindow.getEndTime();
        path[0] = -1;
        path[1] = -1;
        path[2] = -1;
        //Check for space availability
        if (graph[startNode][endNode] == CommonConstant.MAX_EDGE) {
            return CommonConstant.INFINITE;
        }
        //Check for time availability. If the free time window is long enough for the AGV to pass the crossing
        double distance = graph[startNode][endNode];
        //The time for the AGV to reach the entrance edge of crossing
        double timeToReachCrossing =  (distance - CommonConstant.AGV_LENGTH) / speed + currentAGVStartTime;
        //Search for the nearest available time window
        double validTimeToReachCrossing;
        for (TimeWindow possibleTimeWindow : freeTimeWindowList.get(endNode)) {
            double startTime = possibleTimeWindow.getStartTime();
            double endTime = possibleTimeWindow.getEndTime();
            if ((validTimeToReachCrossing = timeToReachCrossing(timeToReachCrossing, speed, startTime, endTime)) != -1) {
                //Check for the conflict
                double timeEnterPath = currentTimeWindow.getEndTime();
                if (noCatchUpConflict(startNode, endNode ,timeEnterPath, validTimeToReachCrossing)
                        && noHeadOnConflict(startNode, endNode ,timeEnterPath, validTimeToReachCrossing)) {
                    path[0] = startNode;
                    path[1] = endNode;
                    path[2] = -1;
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
     * @param timeExitPath The time the AGV leaves the edge (starts to enter the crossing)
     * @return If there is no head-on conflict
     */
    public boolean noHeadOnConflict(int startNode, int endNode, double timeEnterPath, double timeExitPath) {
             for (TimeWindow reverseAGVStartTimeWindow : reservedTimeWindowList.get(endNode)) {
                 //One AGV comes into the edge in the reverse direction
                 if (reverseAGVStartTimeWindow.getNextNodeNumber() == startNode) {
                     int AGVNumber = reverseAGVStartTimeWindow.getAGVNumber();
                     //The time AGV has entered the edge
                     double reverseAGVStartTime = reverseAGVStartTimeWindow.getEndTime();
                     TimeWindow reverseAGVEndTimeWindow = findNextTimeWindow(startNode, reverseAGVStartTime, AGVNumber, reservedTimeWindowList);
                     //Not have such next step, continue find next AGV
                     if (reverseAGVEndTimeWindow == null) {
                         continue;
                     }
                     //The time AGV starts to leave the edge
                     double reverseAGVEndTime = reverseAGVEndTimeWindow.getStartTime();
                     //The other AGV comes before the AGV leaves and leaves after the AGV comes. It's a conflict.
                     if (reverseAGVStartTime <= timeExitPath &&
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
    public TimeWindow findNextTimeWindow(int nextNode, double startTime, int AGVNumber, List<Queue<TimeWindow>> reservedTimeWindowList) {
        for (TimeWindow timeWindows : reservedTimeWindowList.get(nextNode)) {
            //First time window the specific AGV arrives
            if (timeWindows.getStartTime() > startTime && timeWindows.getAGVNumber() == AGVNumber) {
                return timeWindows;
            }
        }
        return null;
    }

    /**
     * Find the catch-up conflict by checking the other AGV with the same direction
     * Consider the realistic condition:
     * if the variance of the time two AGV arrives at the end node is enough for the quick AGV drives the distance (AGV_Length),
     * Will it cause a conflict? No.
     * If the quick AGV is the previously planed AGV, given to the last moment the quick one just adjacent to the end of the slow AGV's back side
     * , it leads to the fact that quick one have already occupied a reserved time window that the slow AGV will definitely crashed into it.
     * The crossing should be larger than the AGV length a lot.
     * So the moment will not happen. Hence the possibility to cause the catch up conflict for a quick AGV as a previously planned AGV is only limited to the
     * fact the current AGV choose the free time window after the reserved one.
     * @param startNode
     * @param endNode
     * @param timeEnterPath The time the AGV has entered the edge
     * @param timeExitPath The time the AGV leaves the edge (starts to enter the crossing)
     * @return If there is no catch-up conflict
     */
    public boolean noCatchUpConflict(int startNode, int endNode, double timeEnterPath, double timeExitPath) {
        for (TimeWindow otherAGVStartTimeWindow : reservedTimeWindowList.get(startNode)) {
            //The time AGV has entered the edge
            double otherAGVStartTime;
            //The other different AGV comes into the edge in the same direction
            if ((otherAGVStartTimeWindow.getNextNodeNumber() == endNode) &&
                    ((otherAGVStartTime = otherAGVStartTimeWindow.getEndTime()) != timeEnterPath)) {
                int AGVNumber = otherAGVStartTimeWindow.getAGVNumber();
                TimeWindow otherAGVEndTimeWindow = findNextTimeWindow(endNode, otherAGVStartTime, AGVNumber, reservedTimeWindowList);
                //Not have such next step, continue find next AGV
                if (otherAGVEndTimeWindow == null) {
                    continue;
                }
                //The time AGV starts to leave the edge
                double otherAGVEndTime = otherAGVEndTimeWindow.getStartTime();
                //The other AGV comes before the AGV comes and leaves after the AGV leaves.
                // Or reverse the sequence of the two AGVs. Those are conflicts.
                if ((otherAGVStartTime < timeEnterPath &&
                        otherAGVEndTime > timeExitPath) ||
                        (timeEnterPath < otherAGVStartTime  &&
                                timeExitPath > otherAGVEndTime )) {
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
    public double timeToReachCrossing(double timeToReachCrossing, double speed, double startTime, double endTime) {
            //Find the nearest possible time to enter the crossing
            double validTimeToReachEndNode = Math.max(startTime, timeToReachCrossing);
            //Time the AGV passes the crossing, the AGV should totally get out of the crossing -> consider AGV_Length
            double validTimeToCrossEndNode = validTimeToReachEndNode + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / speed;
            //AGV can pass the free time window into the path
            if (validTimeToCrossEndNode <= endTime) {
                return validTimeToReachEndNode;
            }
        return -1;
    }


}
