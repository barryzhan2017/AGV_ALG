package org.spring.springboot.algorithmn.GA.common;

public class DistanceCalculation {



    //calculate the distance to drive from one node to the other by judging the kind of road
    public static double calculateDrivingDistance(double[][] graph, Integer startNode, Integer endNode, double minDistance) {
        int maxIndexOfNodeInGraph = graph[0].length-1;
        //it must be a road in buffer because it contains the node in buffer
        if (startNode > maxIndexOfNodeInGraph || endNode > maxIndexOfNodeInGraph) {
            return minDistance;
        }
        //return the distance in the graph
        else {
            return graph[startNode][endNode];
        }
    }

}
