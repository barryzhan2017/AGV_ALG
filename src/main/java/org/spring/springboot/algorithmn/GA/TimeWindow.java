package org.spring.springboot.algorithmn.GA;

import org.spring.springboot.algorithmn.GA.common.DistanceCalculation;

import java.util.ArrayList;
import java.util.List;

public class TimeWindow {

    //All AGVS Time window
    private List<List<TimeNode>> timeWindow;


    //Generate time window of AGVs by analysing each timing to get on a node
    public void generateTimeWindow(List<List<Integer>> AGVPaths, double AGVSpeed,
                                                   double[][] graph, Double[] timeAlreadyPassing,
                                                   Double minDistance) {
        List<List<TimeNode>> timeWindow = new ArrayList<List<TimeNode>>();

        //TimeAlreadyPassing can be -1 meaning the AGV is static,
        // path at least contains one node indicating the static position
            for (List<Integer> path : AGVPaths) {
                // One AGV Time window
                List<TimeNode> AGVTimeWindow = new ArrayList<TimeNode>();
                int AGVIndex = timeWindow.size();
                //How many time node exists
                int numberOfTimeNode = 0;
                //The size of the path
                int numberOfNode = path.size();
                //numberOfTimeNode == index for node
                for (Integer node: path) {
                    TimeNode timeNode = null;
                    //The AGV stays still, just leave a null to the time window
                    if (numberOfNode == 1) {
                        AGVTimeWindow = null;
                        break;
                    }
                    double timeToGetToNode = 0;
                    double drivingDistance = 0;
                    //The first step for AGV should have initial time, they should have at least 2 nodes
                    if (numberOfTimeNode == 0) {
                        //Means AGV starts at a node
                        if (timeAlreadyPassing[AGVIndex] == -1) {
                            timeNode = new TimeNode(0, node.intValue(), 0);
                            AGVTimeWindow.add(timeNode);
                            //To syc by adding the next time node
                            int nextNode = path.get(numberOfTimeNode + 1).intValue();
                            drivingDistance =
                                    DistanceCalculation.calculateDrivingDistance(graph, node.intValue(),
                                            nextNode, minDistance);
                            timeToGetToNode = drivingDistance/AGVSpeed;
                            TimeNode nextTimeNode = new TimeNode(timeToGetToNode, nextNode, 1);
                            AGVTimeWindow.add(nextTimeNode);
                            numberOfTimeNode++;
                            continue;
                        }
                        // Starts in an edge
                        int nextNode = path.get(numberOfTimeNode + 1).intValue();
                        drivingDistance =
                                DistanceCalculation.calculateDrivingDistance(graph, node.intValue(),
                                        nextNode, minDistance);
                        timeToGetToNode = drivingDistance/AGVSpeed - timeAlreadyPassing[AGVIndex];
                        //This criteria the 0 step is ignored to correctly trace the step in the path
                        timeNode = new TimeNode(timeToGetToNode, nextNode, 1);
                        AGVTimeWindow.add(timeNode);
                    }
                    else {
                        //Check if there is another node in the path
                        if (numberOfTimeNode >= numberOfNode -1) {
                            break;
                        }
                        // the node in the buffer
                        int nextNode = path.get(numberOfTimeNode + 1).intValue();
                        int lastTimeNode = AGVTimeWindow.size() - 1;
                        double previousDrivingTime = AGVTimeWindow.get(lastTimeNode).getTime();
                        drivingDistance =
                                DistanceCalculation.calculateDrivingDistance(graph, node.intValue(),
                                        nextNode, minDistance);
                        timeToGetToNode = drivingDistance/AGVSpeed + previousDrivingTime;
                        timeNode = new TimeNode(timeToGetToNode, nextNode, numberOfTimeNode + 1);
                        AGVTimeWindow.add(timeNode);
                    }
                    numberOfTimeNode++;
                }
                timeWindow.add(AGVTimeWindow);
            }

        this.timeWindow = timeWindow;
    }

    //Used for test
    public List<TimeNode> getAGVTimeSequence(int index) {
        return timeWindow.get(index);
    }

    public int size() {
        return timeWindow.size();
    }

    //Check if any time nodes in i and j list of time nodes have same nodeId and time
    public int containsSameTimeNode(int i, int j, int stopAGV) {
       for (TimeNode timeNode1 : timeWindow.get(i)) {
           for (TimeNode timeNode2 : timeWindow.get(j)) {
               if (timeNode1.equals(timeNode2)) {
                   // Return the index of the stopAGV's node causing conflict
                   return stopAGV == i ?
                           timeWindow.get(i).indexOf(timeNode1) : timeWindow.get(j).indexOf(timeNode2);
               }
           }
       }
       return -1;
    }

    //Delay the AGV that will cause conflict by letting it wait for a moment
    public void delay(int decelerateAGV, double waitTime, int index) {
        List<TimeNode> AGVTimeSequence = getAGVTimeSequence(decelerateAGV);
        // Change all of the nodes behind it inclusive to additional waitTime.
        for (int i = index; i < AGVTimeSequence.size(); i++) {
            TimeNode timeNode = AGVTimeSequence.get(i);
            timeNode.setTime(timeNode.getTime() + waitTime);
        }
    }

    //Get which step in the path the AGV is in
    public int getStep(int decelerateAGV, int indexOfConflict) {
        return getAGVTimeSequence(decelerateAGV).get(indexOfConflict).getNumberOfStep();
    }
}

