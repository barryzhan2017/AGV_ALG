package org.spring.springboot.algorithmn.GA;

import org.apache.poi.ss.formula.functions.T;
import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.List;

public class TimeWindow {

    //All AGVS Time window
    private List<List<TimeNode>> timeWindow;


    //Generate time window of AGVs by analysing each timing to get on a node
    public List<List<TimeNode>> generateTimeWindow(List<List<List<Integer>>> AGVPaths, double AGVSpeed, Double[][] graph,
                                                   Double[] timeAlreadyPassing, Double minDistance) {
        timeWindow = new ArrayList<List<TimeNode>>();

        for (List<List<Integer>> paths : AGVPaths) {
            for (List<Integer> path : paths) {
                // One AGV Time window
                List<TimeNode> AGVTimeWindow = new ArrayList<TimeNode>();
                int AGVIndex = timeWindow.size();
                int numberOfNode = 0;
                for (Integer node: path) {
                    double time = 0;
                    //The first step for AGV should have a initial time
                    if (numberOfNode == 0) {
                    time = timeAlreadyPassing[AGVIndex];
                    }
                    else {
                        double previousTime = AGVTimeWindow.get(numberOfNode-1).getTime();
                        double drivingDistance = 0;
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
                    TimeNode timeNode = new TimeNode();
                    numberOfNode++;
                }
            }

        }

        return null;

    }



}
