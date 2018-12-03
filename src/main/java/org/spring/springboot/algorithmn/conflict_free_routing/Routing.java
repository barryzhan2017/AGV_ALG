package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;

import java.util.List;

//This is the algorithm to calculate the conflict-free route for one AGV given the current time windows.
public class Routing {

    //The time window the AGV can travel
    private List<TimeWindow> freeTimeWindowList;
    //The time window is reserved by other AGVs
    private List<TimeWindow> reservedTimeWindowList;
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

    //Test if one time window is reachable for the other one and return the path and minimum time required to travel
    private double testReachability(int endNode, TimeWindow currentTimeWindow, Integer[] path, double speed) {
        int startNode = currentTimeWindow.getNodeNumber();
        //Check for path availability
        if (graph[startNode][endNode] == CommonConstant.MAX_EDGE) {
            path[0] = -1;
            return CommonConstant.INFINITE;
        }
        //Check for time availability. If the free time window is long enough for the AGV to pass the crossing
        double distance = graph[startNode][endNode];
        //The time for the AGV to reach the entrance edge of crossing
        double timeToReachCrossing =  (distance - AGVLength)/speed;
        //Search for the nearest available time window
        for (TimeWindow possibleTimeWindow : freeTimeWindowList) {
            if (possibleTimeWindow.getNodeNumber() == endNode
                    && possibleTimeWindow.getStartTime() > currentTimeWindow.getEndTime()) {
                //Find the nearest possible time to enter the crossing
                double validTimeToCrossEndNode = Math.max(possibleTimeWindow.getStartTime(), timeToReachCrossing);
                if (validTimeToCrossEndNode > possibleTimeWindow.getEndTime()) {

                }
            }
        }
    }


}
