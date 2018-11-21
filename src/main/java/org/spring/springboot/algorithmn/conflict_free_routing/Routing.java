package org.spring.springboot.algorithmn.conflict_free_routing;

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
    //consider the buffer information. how to get

}
