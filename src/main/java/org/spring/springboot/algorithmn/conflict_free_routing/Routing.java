package org.spring.springboot.algorithmn.conflict_free_routing;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.exception.NoPathFeasibleException;
import java.util.*;
import java.util.stream.Collectors;

//This is the algorithm to calculate the conflict-free route for one AGV given the current time windows.
public class Routing {

    //All of the info in free and reserved should be initialized from other methods
    //The time window the AGV can travel, each row is for a specific node.
    private List<Queue<TimeWindow>> freeTimeWindowList;
    //The time window is reserved by other AGVs, each row is for a specific node
    private List<Queue<TimeWindow>> reservedTimeWindowList;
    //The endNode for the specific AGV to go
    private int endNode;
    private double[][] graph;
    //Current time window the AGV occupies. It contains the time the AGV is available. It should also contain the AGV number for reference.
    //But it will not contain the path. The next time window will start to contain path info.
    private TimeWindow currentTimeWindow;
    //Consider the buffer information by conversing the graph to the special one with buffer added to it
    //Use map to store the mapped buffer node value
    private HashMap<Integer, Integer> graphNodeToBuffer = new HashMap<>();
    private double speed;
    private int initialCapacity;
    private int graphNodeNumber;
    private int originalGraphNodeNumber;
    Routing() {

    }

    Routing(List<Queue<TimeWindow>> freeTimeWindowList, List<Queue<TimeWindow>> reservedTimeWindowList, int endNode, double[][] graph, TimeWindow currentTimeWindow, List<List<Integer>> bufferSet, double speed) {
        this.freeTimeWindowList = freeTimeWindowList;
        this.reservedTimeWindowList = reservedTimeWindowList;
        this.endNode = endNode;
        this.graph = initializeGraphWithBufferEndNode(graph, bufferSet);
        this.currentTimeWindow = currentTimeWindow;
        this.speed = speed;
    }

    //Used for test situation with some initial time window
    Routing(List<Queue<TimeWindow>> freeTimeWindowList, List<Queue<TimeWindow>> reservedTimeWindowList, double[][] graph, List<List<Integer>> bufferSet, double speed) {
        this.freeTimeWindowList = freeTimeWindowList;
        this.reservedTimeWindowList = reservedTimeWindowList;
        this.graph = initializeGraphWithBufferEndNode(graph, bufferSet);
        this.speed = speed;
    }
    //Used for realistic situation without initial time window
    public Routing(double[][] graph, List<List<Integer>> bufferSet, double speed, int initialCapacity) {
        this.originalGraphNodeNumber = graph.length;
        this.graph = initializeGraphWithBufferEndNode(graph, bufferSet);
        this.graphNodeNumber = this.graph.length;
        this.initialCapacity = initialCapacity;
        this.freeTimeWindowList = initTimeWindowList(graphNodeNumber, initialCapacity);
        this.reservedTimeWindowList = initTimeWindowList(graphNodeNumber, initialCapacity);
        //Create free time window for all nodes
        initializeFreeTimeWindowList(graphNodeNumber);
        this.speed = speed;
    }

    List<Queue<TimeWindow>> getFreeTimeWindowList() {
        return freeTimeWindowList;
    }

    List<Queue<TimeWindow>> getReservedTimeWindowList() {
        return reservedTimeWindowList;
    }

    private void initializeFreeTimeWindowList(int graphNodeNumber) {
        for (int i = 0; i < graphNodeNumber; i++) {
            Queue<TimeWindow> freeTimeWindows = freeTimeWindowList.get(i);
            TimeWindow timeWindow = new TimeWindow(i, 0, CommonConstant.INFINITE);
            freeTimeWindows.add(timeWindow);
        }
    }

    private void setCurrentTimeWindow(int nodeNumber, double leastTimeReachHere, int indexOfAGV) {
        currentTimeWindow = new TimeWindow(nodeNumber, leastTimeReachHere, getTimeCrossTheCrossing(leastTimeReachHere, speed), indexOfAGV, leastTimeReachHere);
        currentTimeWindow.setFirstStep(true);
    }

    private void setEndNode(int endNode) {
        this.endNode = endNode;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    //Initialize the time window list for the graph
    private static List<Queue<TimeWindow>> initTimeWindowList(int graphNodeNumber, int initialCapacity) {
        List<Queue<TimeWindow>> timeWindowList = new ArrayList<>(graphNodeNumber);
        for (int i = 0; i < graphNodeNumber; i++) {
            Queue<TimeWindow> timeWindowQueue =
                    new PriorityQueue<>(initialCapacity, new TimeWindowComparator());
            timeWindowList.add(timeWindowQueue);
        }
        return timeWindowList;
    }

    /**
     *
     * @param nodeNumber Node number of the current position
     * @param leastTimeReachHere Least time to reach the node
     * @param indexOfAGV Index of AGV
     * @param terminalNode Node number the AGV wants to go
     * @return Path the AGV goes to the end node from the current time window
     * @throws NoPathFeasibleException No feasible path to be found.
     */
    public List<Path> getPath(int nodeNumber, double leastTimeReachHere, int indexOfAGV, int terminalNode) throws NoPathFeasibleException {
        //To prevent searching buffer node, first transfer the buffer node number into graph node number
        if ((graphNodeNumber = findGraphNumberFromBufferNumber(nodeNumber)) != -1) {
            nodeNumber = graphNodeNumber;
        }
        setEndNode(terminalNode);
        setCurrentTimeWindow(nodeNumber, leastTimeReachHere, indexOfAGV);
        List<Path> paths = new ArrayList<>();
        if (currentTimeWindow.getNodeNumber() == endNode) {
            Path path1 = new Path(endNode, endNode, CommonConstant.CROSSING_DISTANCE / speed, false);
            paths.add(path1);
            return paths;
        }
        //To avoid to search the current node as a possible next time window
        List<TimeWindow> timeWindowList = getRoute();
        //At least there will be 2 time windows
        for (int i = 0; i < timeWindowList.size() - 1; i++) {
            Path path;
            TimeWindow startTimeWindow = timeWindowList.get(i);
            int startNode = startTimeWindow.getNodeNumber();
            TimeWindow endTimeWindow = timeWindowList.get(i + 1);
            int endNode = endTimeWindow.getNodeNumber();
            double time;
            //When it starts from buffer end edge, the time calculation should change.
            if (startTimeWindow.isFirstStep() && startTimeWindow.getLeastTimeReachHere() == 0) {
                time = endTimeWindow.getLeastTimeReachHere() - startTimeWindow.getLeastTimeReachHere();
            }
            else {
                time = endTimeWindow.getLeastTimeReachHere() - startTimeWindow.getLeastTimeReachHere() - CommonConstant.CROSSING_DISTANCE / speed;
            }
            //Check if it is a loop
            if (endTimeWindow.getPath()[2] != -1) {
                path = new Path(startNode, endTimeWindow.getPath()[1], time, true);
            }
            else {
                path = new Path(startNode, endNode, time, false);
            }
            paths.add(path);
        }
        //Clear the least time reach here in free time window
        List<TimeWindow> freeTimeWindows = freeTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
        for (TimeWindow timeWindow : freeTimeWindows) {
            timeWindow.setLeastTimeReachHere(CommonConstant.INFINITE);
        }
        return paths;
    }

    /**
     * Get the route of the AGV by applying conflict-free routing algorithm
     * Case excludes when the current time window is the destination
     * @return The specific path given the task
     */
    List<TimeWindow> getRoute() throws NoPathFeasibleException {
        List<TimeWindow> path = new ArrayList<>();
        //The time window has been included in the path
        List<TimeWindow> occupiedTimeWindows = new ArrayList<>();
        //The time window is free and reachable.
        List<TimeWindow> possibleTimeWindows = new ArrayList<>();
        int indexOfAGV = currentTimeWindow.getAGVNumber();
        //Easy to be removed from the free time window using path info
        currentTimeWindow.setAGVNumber(-1);
        TimeWindow headTimeWindow = currentTimeWindow;
        occupiedTimeWindows.add(headTimeWindow);
        boolean isPathFound = false;
        TimeWindow endTimeWindow = null;
        while(!isPathFound) {
            //Find all the elements that are reachable from the occupied time window
            List<TimeWindow> possibleNextTimeWindows = freeTimeWindowList.stream().flatMap(Queue::stream).collect(Collectors.toList());
            for (TimeWindow possibleNextTimeWindow : possibleNextTimeWindows) {
                findPossibleNextTimeWindow(possibleNextTimeWindow, occupiedTimeWindows, possibleTimeWindows);
            }
            //Find the head time window by checking the time window with least time to get to it
            headTimeWindow = possibleTimeWindows.stream().min(Comparator.comparing(TimeWindow::getLeastTimeReachHere)).orElse(null);
            if (possibleTimeWindows.isEmpty()) {
                throw new NoPathFeasibleException("Cannot find any possible time window to form the path!");
            }
            possibleTimeWindows.remove(headTimeWindow);
            occupiedTimeWindows.add(headTimeWindow);
            //If the endNode is got, break the loop
            for (TimeWindow possibleEndTimeWindow: occupiedTimeWindows) {
                if (possibleEndTimeWindow.getNodeNumber() == endNode) {
                    isPathFound = true;
                    endTimeWindow = possibleEndTimeWindow;
                    break;
                }
            }
        }
        //Add all time windows into the path with correct order
        TimeWindow pathTimeWindow = endTimeWindow;
        while (pathTimeWindow != null) {
            if (pathTimeWindow.getLastTimeWindow() != null) {
                pathTimeWindow.getLastTimeWindow().setNextNodeNumber(pathTimeWindow.getNodeNumber());
            }
            path.add(0, pathTimeWindow);
            pathTimeWindow = pathTimeWindow.getLastTimeWindow();
        }

        //Change the time window list owing to the newly created time window path
        //Remove the current time window from reserved time window, should add the AGV number for verification
        reservedTimeWindowList.get(currentTimeWindow.getNodeNumber()).remove(currentTimeWindow);
        int countOfPath = 0;
        for (TimeWindow newPath: path) {
            int nodeNumber = newPath.getNodeNumber();
            double startTime = newPath.getLeastTimeReachHere();
            double endTime;
            //Start from the buffer
            if (newPath.isFirstStep() && newPath.getLeastTimeReachHere() == 0) {
                endTime = CommonConstant.AGV_LENGTH / speed + startTime;
            }
            else {
                endTime = getTimeCrossTheCrossing(startTime, speed);
            }
            int nextNodeNumber = newPath.getNextNodeNumber();
            Integer[] pathForTimeWindow = {nodeNumber, nextNodeNumber, -1};
            //Last path
            if (countOfPath == path.size() - 1) {
                pathForTimeWindow = new Integer[]{-1, -1, -1};
            }
            //It is a loop time window and it must have next path.
            if (nextNodeNumber == nodeNumber) {
                TimeWindow nextPath = path.get(countOfPath + 1);
                pathForTimeWindow = new Integer[] {nodeNumber, nextPath.getPath()[1], nodeNumber};
            }
            //Generate corresponding reserved time window
            TimeWindow newReservedTimeWindow = new TimeWindow(nodeNumber, startTime, endTime, indexOfAGV, nextNodeNumber, pathForTimeWindow);
            Queue<TimeWindow> freeTimeWindowsForThisNode = freeTimeWindowList.get(nodeNumber);
            Queue<TimeWindow> reservedTimeWindowsForThisNode = reservedTimeWindowList.get(nodeNumber);
            freeTimeWindowsForThisNode.remove(newPath);
            reservedTimeWindowsForThisNode.add(newReservedTimeWindow);
            double freeTimeWindowEndTime = newPath.getEndTime();
            double freeTimeWindowStartTime = newPath.getStartTime();
            // Add new free time window according to the reserved time interval
            if (startTime == freeTimeWindowStartTime && endTime < freeTimeWindowEndTime) {
                TimeWindow newFreeTimeWindow = new TimeWindow(nodeNumber, endTime, freeTimeWindowEndTime, -1, -1);
                freeTimeWindowsForThisNode.add(newFreeTimeWindow);
            }
            else if (startTime > freeTimeWindowStartTime && endTime < freeTimeWindowEndTime) {
                TimeWindow newFreeTimeWindow1 = new TimeWindow(nodeNumber, freeTimeWindowStartTime, startTime, -1, -1);
                TimeWindow newFreeTimeWindow2 = new TimeWindow(nodeNumber, endTime, freeTimeWindowEndTime, -1, -1);
                freeTimeWindowsForThisNode.add(newFreeTimeWindow1);
                freeTimeWindowsForThisNode.add(newFreeTimeWindow2);
            }
            else if (startTime > freeTimeWindowStartTime && endTime == freeTimeWindowEndTime) {
                TimeWindow newFreeTimeWindow = new TimeWindow(nodeNumber, freeTimeWindowStartTime, startTime, -1, -1);
                freeTimeWindowsForThisNode.add(newFreeTimeWindow);
            }
            countOfPath++;
        }
        //Convert the buffer node number to the original one
        for (TimeWindow newPath : path) {
            if (graphNodeToBuffer.containsKey(newPath.getNodeNumber())) {
                newPath.setNodeNumber(graphNodeToBuffer.get(newPath.getNodeNumber()));
            }
            for (int i = 0; i < newPath.getPath().length; i++) {
                int nodeNumber = newPath.getPath()[i];
                if (nodeNumber != -1 && graphNodeToBuffer.containsKey(nodeNumber)) {
                    newPath.setNodeNumberInPath(graphNodeToBuffer.get(nodeNumber), i);
                }
            }
        }
        return path;
    }


    /**
     * Labeling process
     * Check if the time window is reachable form the occupied time window, if so, add it to the possible time window
     * If the time to reach from some occupied time window is less, change the current time window, path and time of the possible next time window to the less one
     * @param possibleNextTimeWindow Potential time window that can be used as the next one
     * @param occupiedTimeWindow All of the current time windows having the minimum distance path
     * @param possibleTimeWindow possible time window that can be reached
     */
    void findPossibleNextTimeWindow(TimeWindow possibleNextTimeWindow, List<TimeWindow> occupiedTimeWindow, List<TimeWindow> possibleTimeWindow) {
        //Time window, time and path for the least time
        double minimumTimeToReachTimeWindow = CommonConstant.INFINITE;
        TimeWindow minimumTimeWindow = null;
        Integer[] minimumPath = {-1, -1, -1};
        //should not be the same time window in the occupied time window
        if (occupiedTimeWindow.contains(possibleNextTimeWindow)) {
            return;
        }
        //Don't loop when the time interval is too short to get through the time window
        if (timeToReachCrossing(possibleNextTimeWindow.getStartTime(),
                speed, possibleNextTimeWindow.getStartTime(), possibleNextTimeWindow.getEndTime()) == -1) {
            return;
        }
        //Find the least time required to go to the time window
        for (TimeWindow headTimeWindow: occupiedTimeWindow) {
            double timeToReachTimeWindow = CommonConstant.INFINITE;
            Integer[] possiblePath = {-1, -1, -1};
            //It has the same node number, test the reachability by using the testReachabilityForSameNode
            if (headTimeWindow.getNodeNumber() == possibleNextTimeWindow.getNodeNumber()) {
                timeToReachTimeWindow = testReachabilityForSameNode(possibleNextTimeWindow, headTimeWindow, possiblePath, speed);
            }
            //It has the different node number, test the reachability by using the testReachabilityForDifferentNode
            else if (headTimeWindow.getNodeNumber() != possibleNextTimeWindow.getNodeNumber()) {
                timeToReachTimeWindow = testReachabilityForDifferentNode(possibleNextTimeWindow, headTimeWindow, possiblePath, speed);
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
        if (possibleNextTimeWindow.getLeastTimeReachHere() < CommonConstant.INFINITE
                && !possibleTimeWindow.contains(possibleNextTimeWindow)) {
            possibleTimeWindow.add(possibleNextTimeWindow);
        }
    }


    //for testing
    Map<Integer, Integer> getGraphNodeToBuffer() {
        return graphNodeToBuffer;
    }

    /**
     * Put the buffer end node into the graph to create a new graph, note that the buffer end node cannot be reached outsides (one-direction)
     * @param graph Original graph without adding buffer node
     * @param bufferSet A list stores the path of all buffers
     * @return Special graph contains the end node of buffers
     */
    double[][] initializeGraphWithBufferEndNode(double[][] graph, List<List<Integer>> bufferSet) {
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
                    newGraph[i][j] = CommonConstant.MAX_EDGE;
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
     * @param speed Speed of AGV
     * @return Time required to go for the path, to the end node
     */
    double testReachabilityForSameNode(TimeWindow endTimeWindow, TimeWindow currentTimeWindow, Integer[] path, double speed) {
        int endNode = endTimeWindow.getNodeNumber();
        int length = graph.length;
        //Initialize the path
        path[0] = -1;
        path[1] = -1;
        path[2] = -1;
        //Don't loop when the end time window is earlier than the current one.
        if (endTimeWindow.getStartTime() < currentTimeWindow.getStartTime()) {
            return CommonConstant.INFINITE;
        }
        ArrayList<Integer> incidentNodes = new ArrayList<>();// All lanes(node number) incident to node i
        for(int i = 0; i < length && i != endNode; i++){
            if (graph[endNode][i] != CommonConstant.MAX_EDGE && graph[i][endNode] != CommonConstant.MAX_EDGE) {
                incidentNodes.add(i);
            }
        }
        //if incidentNodes is empty, then return(no lanes icident to node i)
        if(incidentNodes.isEmpty())
            return CommonConstant.INFINITE;

        //Find all reserve time windows between 2 free time windows and find all the lanes these cars will use to get in and out
        Set<Integer> temp = new HashSet<>();
        Queue<TimeWindow> reservedTimeWindowsInEndNode = reservedTimeWindowList.get(endNode);
        for(TimeWindow t : reservedTimeWindowsInEndNode) {
            //To include the loop time window, we should use the path to check the outward direction
            if (t.getStartTime() >= currentTimeWindow.getEndTime() &&
                    t.getEndTime() <= endTimeWindow.getStartTime()) {
                    temp.add(t.getPath()[1]);
                //Find the path going to the end time window
                for (Integer incidentNode: incidentNodes) {
                    TimeWindow lastTimeWindow = findLastTimeWindow(endNode, incidentNode, t.getStartTime(), t.getAGVNumber(), reservedTimeWindowList);
                    if (lastTimeWindow != null) {
                        int lastTimeWindowStartNode = lastTimeWindow.getNodeNumber();
                        temp.add(lastTimeWindowStartNode);
                    }
                }
            }
        }
        //Find if there are lanes available for the loop
        incidentNodes.removeAll(temp);

        //if no avaliable lanes, then return
        if(incidentNodes.isEmpty()) {
            return CommonConstant.INFINITE;
        }

        path[0] = endNode;
        path[1] = incidentNodes.get(0);
        path[2] = endNode;

        return endTimeWindow.getStartTime();
    }

    /**
     * Test if one time window is reachable for the other different one and return the path and minimum time required to travel
     * @param endTimeWindow Possible Destination time window
     * @param currentTimeWindow Current status of the vehicle
     * @param path Path if the end node can be reached
     * @param speed Speed of AGV
     * @return Time to reach the end node (start side of the crossing) from the time leaving the start time window
     */
    double testReachabilityForDifferentNode(TimeWindow endTimeWindow, TimeWindow currentTimeWindow, Integer[] path, double speed) {
        double currentAGVStartTime;
        //For the special case when the AGV starts to move first time in initial condition where the AGV locates at the end of the crossing
        if (currentTimeWindow.isFirstStep() && currentTimeWindow.getLeastTimeReachHere() == 0) {
            currentAGVStartTime = CommonConstant.AGV_LENGTH / speed;
        }
        else{
            currentAGVStartTime = getTimeCrossTheCrossing(currentTimeWindow.getLeastTimeReachHere(), speed);
        }
        int startNode = currentTimeWindow.getNodeNumber();
        //Time finishing the crossing
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
            if (noCatchUpConflict(startNode, endNode ,currentAGVStartTime, validTimeToReachCrossing)
                    && noHeadOnConflict(startNode, endNode ,currentAGVStartTime, validTimeToReachCrossing)) {
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
     * Give the time when the AGV finish (cross) the crossing
     * @param leastTimeReachHere Time to reach the start edge of the crossing
     * @param speed Speed of AGV
     * @return AGV finish (cross) the crossing
     */
    private double getTimeCrossTheCrossing(double leastTimeReachHere, double speed) {
        return leastTimeReachHere + (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / speed;
    }

    /**
     * Find the head-on conflict by checking the other AGV with the reverse direction
     * @param startNode Start node for the path checked
     * @param endNode End node for the path checked
     * @param timeEnterPath The time the AGV has entered the edge
     * @param timeExitPath The time the AGV leaves the edge (starts to enter the crossing)
     * @return If there is no head-on conflict
     */
    boolean noHeadOnConflict(int startNode, int endNode, double timeEnterPath, double timeExitPath) {
        for (TimeWindow reverseAGVStartTimeWindow : reservedTimeWindowList.get(endNode)) {
            int AGVNumber = reverseAGVStartTimeWindow.getAGVNumber();
            //The time AGV has entered the edge
            double reverseAGVStartTime = reverseAGVStartTimeWindow.getEndTime();
            //One AGV comes into the edge in the reverse direction
            if (reverseAGVStartTimeWindow.getNextNodeNumber() == startNode) {
                TimeWindow reverseAGVEndTimeWindow = findNextTimeWindow(startNode, reverseAGVStartTime, AGVNumber, reservedTimeWindowList);
                //The time AGV starts to leave the edge
                double reverseAGVEndTime = reverseAGVEndTimeWindow.getStartTime();
                //The other AGV comes before the AGV leaves and leaves after the AGV comes. It's a conflict.
                if (reverseAGVStartTime <= timeExitPath &&
                        reverseAGVEndTime >= timeEnterPath) {
                    return false;
                }
            }
            //consider the conflict generated by looping routing behavior
            else if (reverseAGVStartTimeWindow.getPath()[1] == startNode && reverseAGVStartTimeWindow.getPath()[2] == endNode) {
                TimeWindow reverseAGVEndTimeWindow = findNextTimeWindow(endNode, reverseAGVStartTime, AGVNumber, reservedTimeWindowList);
                //The time AGV starts to leave the edge
                if (reverseAGVEndTimeWindow == null) {
                    System.out.println("null pointer");
                }
                double reverseAGVEndTime = reverseAGVEndTimeWindow.getStartTime();
                //When the AGV exits at the time the reverseAGV still locates at the path, the conflict happens
                if (timeExitPath <= reverseAGVEndTime && timeExitPath >= reverseAGVStartTime) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Find the Time window immediately after the end time of the ongoing AGV coming out of the node
     * by checking the specific reserved time window
     * @param nextNode Node that the current time window will go
     * @param startTime The time the AGV comes out of the node
     * @param AGVNumber Index of AGV
     * @return Next Time Window
     */
    TimeWindow findNextTimeWindow(int nextNode, double startTime, int AGVNumber, List<Queue<TimeWindow>> reservedTimeWindowList) {
        for (TimeWindow timeWindows : reservedTimeWindowList.get(nextNode)) {
            //First time window the specific AGV arrives
            //When it's in the buffer, the next time window start time can be equal to the current time window end time
            if (timeWindows.getStartTime() >= startTime && timeWindows.getAGVNumber() == AGVNumber) {
                return timeWindows;
            }
        }
        return null;
    }

    /**
     * Find the Time window immediately before the time window of this ongoing AGV
     * by checking the specific reserved time window
     *
     * @param endNode Current node number
     * @param lastNode Node that current AGV comes from
     * @param startTime The time the AGV comes out of the node
     * @param indexOfAGV Index of AGV
     * @return Next Time Window
     */
    TimeWindow findLastTimeWindow(int endNode, int lastNode, double startTime, int indexOfAGV, List<Queue<TimeWindow>> reservedTimeWindowList) {
        TimeWindow lastTimeWindow = null;
        for (TimeWindow timeWindows : reservedTimeWindowList.get(lastNode)) {
            //Last time window the specific AGV comes from, do not worry about the loop time window
            if (timeWindows.getStartTime() <= startTime && timeWindows.getAGVNumber() == indexOfAGV
                    && timeWindows.getNextNodeNumber() == endNode) {
                lastTimeWindow =  timeWindows;
            }
        }
        return lastTimeWindow;
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
     * @param startNode Start node for the path checked
     * @param endNode End node for the path checked
     * @param timeEnterPath The time the AGV has entered the edge
     * @param timeExitPath The time the AGV leaves the edge (starts to enter the crossing)
     * @return If there is no catch-up conflict
     */
    boolean noCatchUpConflict(int startNode, int endNode, double timeEnterPath, double timeExitPath) {
        for (TimeWindow otherAGVStartTimeWindow : reservedTimeWindowList.get(startNode)) {
            //The time AGV has entered the edge
            double otherAGVStartTime;
            int indexOfAGV = otherAGVStartTimeWindow.getAGVNumber();
            //The other different AGV comes into the edge in the same direction
            if ((otherAGVStartTimeWindow.getNextNodeNumber() == endNode) &&
                    ((otherAGVStartTime = otherAGVStartTimeWindow.getEndTime()) != timeEnterPath)) {
                TimeWindow otherAGVEndTimeWindow = findNextTimeWindow(endNode, otherAGVStartTime, indexOfAGV, reservedTimeWindowList);
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
            //consider the conflict generated by looping routing behavior
            else if (otherAGVStartTimeWindow.getPath()[1] == endNode && otherAGVStartTimeWindow.getPath()[2] == startNode) {
                otherAGVStartTime = otherAGVStartTimeWindow.getEndTime();
                TimeWindow otherAGVEndTimeWindow = findNextTimeWindow(startNode, otherAGVStartTime, indexOfAGV, reservedTimeWindowList);
                //The time AGV starts to leave the edge
                double otherAGVEndTime = otherAGVEndTimeWindow.getStartTime();
                //Enter the path when the other AGV locates the entry, conflict happens
                if (timeEnterPath < otherAGVEndTime && timeEnterPath > otherAGVStartTime) {
                   return false;
                }
            }
        }
        return true;
    }



    /**
     * Test if the free time window has enough time to let the AGV pass the crossing
     * @param timeToReachCrossing Time to arrive the start point of the crossing
     * @param speed Speed of AGV
     * @param startTime Start time of the possible free time window
     * @param endTime End time of the possible free time window
     * @return The time to reach the crossing if the free time window has enough time or -1 if not enough time
     */
    double timeToReachCrossing(double timeToReachCrossing, double speed, double startTime, double endTime) {
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

    /**
     * Find the node number for the node in the special graph given the node number in the buffer.
     * @param bufferNodeNumber buffer node number
     * @return graph node number
     */
    private int findGraphNumberFromBufferNumber(Integer bufferNodeNumber) {
        int graphNumber = -1;
        for (Integer graphNodeNumber : graphNodeToBuffer.keySet()) {
            if (graphNodeToBuffer.get(graphNodeNumber).equals(bufferNodeNumber)) {
                graphNumber = graphNodeNumber;
                break;
            }
        }
        return graphNumber;
    }

    /**
     * Set the interval of the last reserved time window in this node to the time of crossing as an AGV
     * Create corresponding free time window for this node
     * @param bufferNodeNumber Node number of the buffer
     */
    public void releaseReservedTimeWindow(Integer bufferNodeNumber) {
        //Change the last time window to a new time window
        Queue<TimeWindow> timeWindowQueue = reservedTimeWindowList.get(bufferNodeNumber);
        TimeWindow removedTimeWindow = (TimeWindow) timeWindowQueue.toArray()[timeWindowQueue.size() - 1];
        timeWindowQueue.remove(removedTimeWindow);
        double startTime = removedTimeWindow.getStartTime();
        double endTime = getTimeCrossTheCrossing(startTime, speed);
        removedTimeWindow.setEndTime(endTime);
        timeWindowQueue.add(removedTimeWindow);
        TimeWindow newFreeTimeWindow = new TimeWindow(bufferNodeNumber, endTime, CommonConstant.INFINITE);
        freeTimeWindowList.get(bufferNodeNumber).add(newFreeTimeWindow);
    }

    /**
     * Add the current AGV's path as the reserved time window
     * Time counted when the AGV just cross the crossing
     * @param paths Path from ongoing AGV
     * @param timeAlreadyPassed Time the AGV has passed from the current node
     * @param indexOfAGV Index of current AGV
     */
    public void setCurrentPathsToTimeWindows(List<Path> paths, Double timeAlreadyPassed, int indexOfAGV) {
        double endTime = 0;
        double startTime = 0;
        int count = 0;
        for (Path path : paths) {
            int startNode = path.getStartNode();
            double timeToNextNode = path.getTime();
            boolean isLoop = path.isLoop();
            int nextNodeNumber = isLoop ? startNode : path.getEndNode();
            Integer[] pathForTimeWindow = {-1, -1, -1};
            //AGV is still going.
            if (nextNodeNumber < originalGraphNodeNumber) {
                //Check if the path is loop to determine the end node
                int loopNode = isLoop ? path.getStartNode() : -1;
                pathForTimeWindow = new Integer[]{startNode, path.getEndNode(), loopNode};
            }
            TimeWindow reservedTimeWindow;
            if (nextNodeNumber >= originalGraphNodeNumber) {
                nextNodeNumber = -1;
            }
            //First round
            if (count == 0) {
                //When the AGV reaches the next node but not cross it
                if (timeAlreadyPassed >= timeToNextNode) {
                    startTime = 0;
                    endTime = (CommonConstant.CROSSING_DISTANCE + CommonConstant.AGV_LENGTH) / speed
                            + timeToNextNode - timeAlreadyPassed;
                }
                //When AGV passes the first node but not reach the next node
                else if (timeAlreadyPassed >= CommonConstant.AGV_LENGTH / speed) {
                    startTime = timeToNextNode - timeAlreadyPassed;
                    endTime = getTimeCrossTheCrossing(startTime, speed);
                }
                //AGV is still crossing the current node
                else {
                    startTime = 0;
                    endTime = CommonConstant.AGV_LENGTH / speed - timeAlreadyPassed;
                    if (startNode < originalGraphNodeNumber) {
                        reservedTimeWindow = new TimeWindow(startNode, startTime, endTime, indexOfAGV,
                                nextNodeNumber, pathForTimeWindow);
                        reservedTimeWindowList.get(startNode).add(reservedTimeWindow);
                    }
                    startTime = endTime + timeToNextNode - CommonConstant.AGV_LENGTH / speed;
                    endTime += timeToNextNode + CommonConstant.CROSSING_DISTANCE / speed;
                }
            }
            else {
                if (startNode < originalGraphNodeNumber) {
                    reservedTimeWindow = new TimeWindow(startNode, startTime, endTime, indexOfAGV, nextNodeNumber,
                            pathForTimeWindow);
                    reservedTimeWindowList.get(startNode).add(reservedTimeWindow);
                }
                startTime = endTime + timeToNextNode - CommonConstant.AGV_LENGTH / speed;
                endTime += timeToNextNode + CommonConstant.CROSSING_DISTANCE / speed;
            }
            count++;
        }
    }

    /**
     * Set the free time window according to the newly added reserved time window
     */
    public void setFreeTimeWindow() {
        int indexOfNodeNumber = 0;
        for (Queue<TimeWindow> reservedTimeWindowQueue : reservedTimeWindowList) {
            Queue<TimeWindow> freeTimeWindows = freeTimeWindowList.get(indexOfNodeNumber);
            for (TimeWindow reservedTimeWindow : reservedTimeWindowQueue) {
                double endTimeForFirstTimeWindow = reservedTimeWindow.getStartTime();
                double startTimeForSecondTimeWindow = reservedTimeWindow.getEndTime();
                //Get the last time window in the queue
                TimeWindow freeTimeWindow = (TimeWindow) freeTimeWindows.toArray()[freeTimeWindows.size() - 1];
                freeTimeWindows.remove(freeTimeWindow);
                double startTimeForFirstTimeWindow = freeTimeWindow.getStartTime();
                TimeWindow firstFreeTimeWindow = new TimeWindow(indexOfNodeNumber, startTimeForFirstTimeWindow, endTimeForFirstTimeWindow);
                TimeWindow secondFreeTimeWindow = new TimeWindow(indexOfNodeNumber, startTimeForSecondTimeWindow, CommonConstant.INFINITE);
                freeTimeWindows.add(firstFreeTimeWindow);
                freeTimeWindows.add(secondFreeTimeWindow);
            }
            indexOfNodeNumber++;
        }
    }
}