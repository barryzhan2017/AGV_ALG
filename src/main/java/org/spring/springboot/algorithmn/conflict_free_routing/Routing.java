package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;


import java.util.*;

//This is the algorithm to calculate the conflict-free route for one AGV given the current time windows.
public class Routing {

    //All of the info in free and reserved should be initialized from other methods
    //The time window the AGV can travel, each row is for a specific node.
    private List<Queue<TimeWindow>> freeTimeWindowList;
    //The time window is reserved by other AGVs, each row is for a specific node
    private List<Queue<TimeWindow>> reservedTimeWindowList;
    //The task for the specific AGV to finish
    private int[] task;
    private double[][] graph;
    //Current time window the AGV occupies. It contains the time the AGV is available.
    private TimeWindow currentTimeWindow;
    //Consider the buffer information by conversing the graph to the special one with buffer added to it
    //Use map to store the mapped buffer node value
    private HashMap<Integer, Integer> graphNodeToBuffer = new HashMap<>();
    private double speed;

    public Routing() {

    }

    public Routing(List<Queue<TimeWindow>> freeTimeWindowList, List<Queue<TimeWindow>> reservedTimeWindowList, int[] task, double[][] graph, TimeWindow currentTimeWindow, List<List<Integer>> bufferSet, double speed) {
        this.freeTimeWindowList = freeTimeWindowList;
        this.reservedTimeWindowList = reservedTimeWindowList;
        this.task = task;
        this.graph = initializeGraphWithBufferEndNode(graph, bufferSet);
        this.currentTimeWindow = currentTimeWindow;
        this.speed = speed;
    }


    /**
     * Get the route of the AGV by applying conflict-free routing algorithm
     * @return The specific path given the task
     */
    public List<Integer> getRoute() {
        List<Integer> path = new ArrayList<>();
        int sourceNode = task[0];
        int endNode = task[1];
        //The time window has been included in the path
        List<TimeWindow> occupiedTimeWindow = new ArrayList<>();
        //The time window is free and possibly reachable.
        List<TimeWindow> possibleTimeWindow = new ArrayList<>();
        TimeWindow headTimeWindow = currentTimeWindow;
        //Find all the elements that are reachable from the occupied time window
        for (Queue<TimeWindow> possibleNextTimeWindowQueue : freeTimeWindowList) {
            for (TimeWindow possibleNextTimeWindow : possibleNextTimeWindowQueue) {
                findPossibleNextTimeWindow(possibleNextTimeWindow, occupiedTimeWindow, possibleTimeWindow);
            }
        }
        occupiedTimeWindow.add(currentTimeWindow);
        return path;
    }

    /**
     * Labeling process
     * Check if the time window that is reachable form the occupied time window, if so, add it to the possible time window
     * If the time cost is less, change the current time window, path and time of the possible next time window to the less one.
     * @param possibleNextTimeWindow
     * @param occupiedTimeWindow All of the current time windows having the minimum distance path
     * @param possibleTimeWindow possible time window that can be reached
     */
    private void findPossibleNextTimeWindow(TimeWindow possibleNextTimeWindow, List<TimeWindow> occupiedTimeWindow, List<TimeWindow> possibleTimeWindow) {
        //Time window, time and path for the least time
        double minimumTimeToReachTimeWindow = CommonConstant.INFINITE;
        TimeWindow minimumTimeWindow = null;
        Integer[] minimumPath = {-1, -1, -1};
        //Find the least time required to go to the time window
        for (TimeWindow headTimeWindow: occupiedTimeWindow) {
            double timeToReachTimeWindow = CommonConstant.INFINITE;
            Integer[] possiblePath = {-1, -1, -1};
            //Not the same time window in the occupied time window
            if (!occupiedTimeWindow.contains(possibleNextTimeWindow)) {
                //It has the same node number, test the reachability by using the testReachabilityForSameNode
                if (headTimeWindow.getNodeNumber() == possibleNextTimeWindow.getNodeNumber()) {
                    timeToReachTimeWindow = testReachabilityForSameNode(possibleNextTimeWindow, headTimeWindow, possiblePath, speed);
                }
                //It has the different node number, test the reachability by using the testReachabilityForDifferentNode
                else if (headTimeWindow.getNodeNumber() != possibleNextTimeWindow.getNodeNumber()) {
                    timeToReachTimeWindow = testReachabilityForDifferentNode(possibleNextTimeWindow, headTimeWindow, possiblePath, speed);
                }
            }
            if (timeToReachTimeWindow < minimumTimeToReachTimeWindow) {
                minimumTimeToReachTimeWindow = timeToReachTimeWindow;
                minimumPath = possiblePath;
                minimumTimeWindow = headTimeWindow;
            }
        }
        // Less time required to go for the time window, so change the previous one
        if (minimumTimeToReachTimeWindow < possibleNextTimeWindow.getLeastTimeReachHere()) {
            possibleNextTimeWindow.setLeastTimeReachHere(minimumTimeToReachTimeWindow);
            possibleNextTimeWindow.setPath(minimumPath);
            possibleNextTimeWindow.setLastTimeWindow(minimumTimeWindow);
        }
        //Add it to the possible time window if it's reachable and it's not in the list
        if (possibleNextTimeWindow.getLeastTimeReachHere() < CommonConstant.INFINITE && !possibleTimeWindow.contains(possibleNextTimeWindow)) {
            possibleTimeWindow.add(possibleNextTimeWindow);
        }

    }


    //for testing
    public Map<Integer, Integer> getGraphNodeToBuffer() {
        return graphNodeToBuffer;
    }

    /**
     * Put the buffer end node into the graph to create a new graph, note that the buffer end node cannot be reached outsides (one-direction)
     * @param graph Original graph without adding buffer node
     * @param bufferSet A list stores the path of all buffers
     * @return Special graph contains the end node of buffers
     */
    public double[][] initializeGraphWithBufferEndNode(double[][] graph, List<List<Integer>> bufferSet) {
        if (bufferSet == null) {
            return  graph;
        }
        int originalGraphSize = graph[0].length;
        int bufferNumber = bufferSet.size();
        int newGraphSize = originalGraphSize + bufferNumber;
        double[][] newGraph = new double[newGraphSize][newGraphSize];
        for (int i = 0; i < newGraphSize; i++) {
            for (int j = 0; j < newGraphSize; j++) {
                //The original graph should be reserved
                if (i < originalGraphSize && j < originalGraphSize) {
                    newGraph[i][j] = graph[i][j];
                }
                //The other part should remain infinite at first
                else {
                    newGraph[i][j] = CommonConstant.INFINITE;
                }
            }
        }
        //Number of buffer
        int index = 0;
        //To store the actual number of the node into the map to fetch and set the linking relationship between buffer end node and graph node
        for (List<Integer> bufferPath : bufferSet) {
            int bufferSize = bufferPath.size();
            int actualNodeNumber = bufferPath.get(bufferSize - 2);
            int nodeLinkingBufferAndGraph = bufferPath.get(bufferSize - 1);
            graphNodeToBuffer.put(originalGraphSize + index, actualNodeNumber);
            newGraph[index + originalGraphSize][nodeLinkingBufferAndGraph] = CommonConstant.BUFFER_PATH_LENGTH;
            index++;
        }
        return newGraph;
    }





    /**
     *@param endTimeWindow Possible Destination time window
     * @param currentTimeWindow Current status of the vehicle
     * @param path Path if the end node can be reached
     * @param speed
     * @return Time required to go for the path, to the end node
     */
    public double testReachabilityForSameNode(TimeWindow endTimeWindow, TimeWindow currentTimeWindow, Integer[] path, double speed) {

        return -1;

    }

    /**
     * Test if one time window is reachable for the other different one and return the path and minimum time required to travel
     * @param endTimeWindow Possible Destination time window
     * @param currentTimeWindow Current status of the vehicle
     * @param path Path if the end node can be reached
     * @param speed
     * @return Time required to go for the path, to the end node
     */
     public double testReachabilityForDifferentNode(TimeWindow endTimeWindow, TimeWindow currentTimeWindow, Integer[] path, double speed) {
        int startNode = currentTimeWindow.getNodeNumber();
        double currentAGVStartTime = currentTimeWindow.getEndTime();
        int endNode = endTimeWindow.getNodeNumber();
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
        //Check for the time availability
        double validTimeToReachCrossing;
        double startTime = endTimeWindow.getStartTime();
        double endTime = endTimeWindow.getEndTime();
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
