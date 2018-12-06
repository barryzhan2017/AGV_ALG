package org.spring.springboot.algorithmn.common;

import java.util.ArrayList;
import java.util.List;

public class CommonGraphOperation {


    /**
     * calculate the distance to drive from one node to the other by judging the kind of road (buffer or main graph)
     * @param graph
     * @param startNode
     * @param endNode
     * @param minDistance
     * @return The distance
     */
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

    /**
     * Find the nearby node by checking the graph. No consideration for buffer node currently.
     * @param node
     * @param graph
     * @return A list of nearby node
     */
    public static List<Integer>  findPossiblePath(int node, double[][] graph) {
        List<Integer> possibleListOfNode = new ArrayList<Integer>();
        for (int i = 0; i < graph[0].length; i++) {
            if (i != node && graph[node][i] != CommonConstant.INFINITE) {
                possibleListOfNode.add(i);
            }
        }
        return possibleListOfNode;
    }


}
