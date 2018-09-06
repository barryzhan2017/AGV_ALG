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
    public List<List<TimeNode>> generateTimeWindow(List<List<List<Integer>>> AGVPaths, double AGVSpeed, Double[][] graph,
                                                   Double[] timeAlreadyPassing, Double minDistance) {
        List<List<TimeNode>> timeWindow = new ArrayList<List<TimeNode>>();

        //TimeAlreadyPassing can be -1 meaning the AGV is static,
        // path at least contain one node indicating the static position
        for (List<List<Integer>> paths : AGVPaths) {
            for (List<Integer> path : paths) {
                // One AGV Time window
                List<TimeNode> AGVTimeWindow = new ArrayList<TimeNode>();
                int AGVIndex = timeWindow.size();
                int numberOfNode = 0;
                for (Integer node: path) {
                    TimeNode timeNode = null;
                    double timeToGetToNode = 0;
                    double drivingDistance = 0;
                    //need to consider -1 and one node condition
                    //The first step for AGV should have a initial time
                    if (numberOfNode == 0) {
                        drivingDistance = DistanceCalculation.calculateDrivingDistance(graph, path.get(numberOfNode),
                                path.get(numberOfNode + 1),minDistance);
                        timeToGetToNode = drivingDistance/AGVSpeed - timeAlreadyPassing[AGVIndex];

                        timeNode = new TimeNode(time,)
                    }
                    else {
                        double previousTime = AGVTimeWindow.get(numberOfNode-1).getTime();
                        // the node in the buffer
                        if ((graph[0].length-1) < numberOfNode) {
                            drivingDistance = minDistance;
                        }
                        else {
                            drivingDistance
                        }
                        double drivingTime =
                        time =  + graph.getSize()
                    }

                    numberOfNode++;
                }
            }

        }

        return null;

    }




}

