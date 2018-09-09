package org.spring.springboot.algorithmn.GA;

import org.apache.poi.ss.formula.functions.T;
import org.spring.springboot.algorithmn.GA.common.DistanceCalculation;
import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.List;

public class TimeWindow {

    //All AGVS Time window
    private List<List<TimeNode>> timeWindow;


    //Generate time window of AGVs by analysing each timing to get on a node
    public void generateTimeWindow(List<List<List<Integer>>> AGVPaths, double AGVSpeed,
                                                   double[][] graph, Double[] timeAlreadyPassing,
                                                   Double minDistance) {
        List<List<TimeNode>> timeWindow = new ArrayList<List<TimeNode>>();

        //TimeAlreadyPassing can be -1 meaning the AGV is static,
        // path at least contains one node indicating the static position
        for (List<List<Integer>> paths : AGVPaths) {
            for (List<Integer> path : paths) {
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
                            timeNode = new TimeNode(0,node);
                            AGVTimeWindow.add(timeNode);

                            //To syc by adding the next time node
                            int nextNode = path.get(numberOfTimeNode + 1);
                            drivingDistance =
                                    DistanceCalculation.calculateDrivingDistance(graph, node,
                                            nextNode, minDistance);
                            timeToGetToNode = drivingDistance/AGVSpeed;
                            TimeNode nextTimeNode = new TimeNode(timeToGetToNode, nextNode);
                            AGVTimeWindow.add(nextTimeNode);
                            numberOfTimeNode++;
                            continue;
                        }
                        // Starts in an edge
                        int nextNode = path.get(numberOfTimeNode + 1);
                        drivingDistance =
                                DistanceCalculation.calculateDrivingDistance(graph, node,
                                        nextNode, minDistance);
                        timeToGetToNode = drivingDistance/AGVSpeed - timeAlreadyPassing[AGVIndex];
                        timeNode = new TimeNode(timeToGetToNode, nextNode);
                        AGVTimeWindow.add(timeNode);
                    }
                    else {
                        //Check if there is another node in the path
                        if (numberOfTimeNode >= numberOfNode -1) {
                            break;
                        }
                        // the node in the buffer
                        int nextNode = path.get(numberOfTimeNode + 1);
                        int lastTimeNode = AGVTimeWindow.size() - 1;
                        double previousDrivingTime = AGVTimeWindow.get(lastTimeNode).getTime();
                        drivingDistance =
                                DistanceCalculation.calculateDrivingDistance(graph, node,
                                        nextNode, minDistance);
                        timeToGetToNode = drivingDistance/AGVSpeed + previousDrivingTime;
                        timeNode = new TimeNode(timeToGetToNode, nextNode);
                        AGVTimeWindow.add(timeNode);
                    }
                    numberOfTimeNode++;
                }
                timeWindow.add(AGVTimeWindow);
            }

        }
        this.timeWindow = timeWindow;
    }

    public List<TimeNode> getAGVTimeSequence(int index) {
        return timeWindow.get(index);
    }





}

