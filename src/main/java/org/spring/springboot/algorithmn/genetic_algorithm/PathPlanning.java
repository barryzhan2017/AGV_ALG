package org.spring.springboot.algorithmn.genetic_algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.exception.NoAGVInTheBuffer;
import org.spring.springboot.algorithmn.exception.NoPathFeasibleException;

import java.util.ArrayList;
import java.util.List;


/**

 * Used for routing AGVs to their desired positions
 */
public class PathPlanning {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private boolean[] returningAGV;
    private double penaltyForConflict;
    private double speedOfAGV;
    private double distanceOfBuffer;

    public PathPlanning(int numberOfAGV, double penaltyForConflict, double speedOfAGV, double distanceOfBuffer) {
        returningAGV = new boolean[numberOfAGV];
        for (int i = 0; i < numberOfAGV; i++) {
            returningAGV[i] = false;
        }
        this.penaltyForConflict = penaltyForConflict;
        this.speedOfAGV = speedOfAGV;
        this.distanceOfBuffer = distanceOfBuffer;
    }

    /**
     * Get the path of the AGV and adjust the time and fitness for them
     * @param routing Algorithm Used for routing
     * @param endNode The terminal node the AGV wants to go
     * @param startNode The start node of AGV
     * @param indexOfAGV Number of AGV
     * @param currentAGVsFitness Fitness of all AGVs
     * @param currentAGVsTime Time of all AGVs
     * @return Path of the routing from start node to end node
     */
    List<Path> getPath(Routing routing, int endNode, int startNode, int indexOfAGV,
                                  double[] currentAGVsFitness, double[] currentAGVsTime) {
        List<Path> paths;
        double leastTimeGetHere = currentAGVsTime[indexOfAGV];
        try {
            paths = routing.getPath(startNode, leastTimeGetHere, indexOfAGV, endNode);
        }
        // No Path feasible so quit the routing
        catch (NoPathFeasibleException e) {
            currentAGVsFitness[indexOfAGV] = penaltyForConflict;
            logger.info(e.getMessage());
            return new ArrayList<>();
        }
        double time2 = getTimeFromPath(paths, speedOfAGV);
        currentAGVsTime[indexOfAGV] += time2;
        currentAGVsFitness[indexOfAGV] += time2;
        return paths;
    }

    private double getTimeFromPath(List<Path> paths, double speed) {
        double time = 0;
        for (Path path : paths) {
            time += path.getTime() + CommonConstant.CROSSING_DISTANCE / speed;
        }
        return time;
    }

    /**
     * Move the other AGVs in the buffer when some AGV gets out of the buffer. Adjust the fitness and time window corresponding
     * @param buffer The path of the buffer
     * @param generationForAGVPaths Path of all the AGVs
     * @param fitness Fitness of all the AGVs
     * @param routing Used for routing AGVs
     */
    void adjustOtherAGVPositions(List<Integer> buffer, List<List<Path>> generationForAGVPaths, double fitness[], Routing routing) {
        for (List<Path> path :generationForAGVPaths) {
            Path endPath = path.get(path.size()-1);
            int endPosition = endPath.getEndNode();
            //If the AGV stops in the buffer area, move it forwards
            if (buffer.contains(endPosition) && endPosition != buffer.get(0)
                    && endPosition != buffer.get(buffer.size() - 1)) {
                int nextNode = buffer.get(buffer.indexOf(endPosition) + 1);
                Path nextPath = new Path(endPosition, nextNode, distanceOfBuffer / speedOfAGV, false);
                path.add(nextPath);
                int indexOfAGV = generationForAGVPaths.indexOf(path);
                fitness[indexOfAGV] += (distanceOfBuffer + CommonConstant.CROSSING_DISTANCE) / speedOfAGV;
                //Release the first node in buffer
                int secondPositionInBuffer = buffer.get(1);
                if (endPosition == secondPositionInBuffer) {
                    routing.releaseBufferFirstPosition(secondPositionInBuffer);
                }
                int secondToLastPositionInBuffer = buffer.get(buffer.size() - 2);
                if (nextNode == secondToLastPositionInBuffer) {
                    routing.createReservedTimeWindowForEndPosition(secondToLastPositionInBuffer, indexOfAGV);
                }
            }
        }
    }


    /**
     * Navigate all of the AGVs to their buffer according to their time to arrive the start node to enter the buffer
     * @param generationForAGVPaths All of the paths of AGVs in one generation
     * @param bufferSet All of the paths information for all the buffers
     * @param bufferForAGVs The mapping from AGV index to buffer index
     * @param fitness Fitness of all the AGVs
     * @param time Current time for all the AGVs
     * @throws NoAGVInTheBuffer Find no AGV is returning to specific buffer
     */
    public void navigateAGVsToInnerBuffer(List<List<Path>> generationForAGVPaths, List<List<Integer>> bufferSet,
                                          Integer[] bufferForAGVs, double[] time) throws NoAGVInTheBuffer {
        int numberOfBuffers = bufferSet.size();
        int numberOfAGVs = returningAGV.length;
        //Each buffer has it's AGV's index
        int [][] indexOfAGVsForBuffer = new int[numberOfBuffers][numberOfAGVs];
        for (int i = 0; i < numberOfBuffers; i++) {
            for (int j = 0; j < numberOfAGVs; j++) {
                indexOfAGVsForBuffer[i][j] = -1;
            }
        }
        for (int i = 0; i < numberOfBuffers; i++) {
            int numberOfAGVsInBuffer = 0;
            for (int j = 0; j < numberOfAGVs; j++) {
                if (returningAGV[j] && bufferForAGVs[j] == i) {
                    indexOfAGVsForBuffer[i][numberOfAGVsInBuffer] = j;
                    numberOfAGVsInBuffer++;
                }
            }
        }
        //Sort all the AGVs according to their time arriving the buffer
        for (int i = 0; i < numberOfBuffers; i++) {
            MergeSortAlgorithm.mergesort(indexOfAGVsForBuffer[i], time);
        }
        for (int i = 0; i < numberOfBuffers; i++) {
            List<Integer> buffer = bufferSet.get(i);
            int sizeOfBuffer = buffer.size();
            int occupiedSpaceInBuffer = 0;
            for (int j = 0; j < numberOfAGVs; j++) {
                int indexOfAGV = indexOfAGVsForBuffer[i][j];
                if (indexOfAGV != -1) {
                    returnAGVToBuffer(indexOfAGV, buffer,
                            generationForAGVPaths.get(indexOfAGV),
                            sizeOfBuffer - 2 - occupiedSpaceInBuffer);
                    occupiedSpaceInBuffer++;
                }
            }
        }
    }


    public void setBackingAGV(int backingAGV) {
        returningAGV[backingAGV] = true;
    }

    public boolean isReturning(int AGVIndex) {
        return returningAGV[AGVIndex];
    }

    /**
     * Add the path to the some index of the buffer
     * @param indexOfAGV AGV index
     * @param buffer Buffer where the AGV locates
     * @param earliestAGVPath The current path for this AGV
     * @param indexOfBuffer The end position the AGV will go to
     */
    public void returnAGVToBuffer(int indexOfAGV, List<Integer> buffer, List<Path> earliestAGVPath, int indexOfBuffer) {
        Path lastPath = earliestAGVPath.get(earliestAGVPath.size() - 1);
        //Get rid of the first and last element in the buffer to be considered
        for (int i = 1; i <= indexOfBuffer; i++) {
            int bufferNode = buffer.get(i);
            int startNode = lastPath.getEndNode();
            Path path = new Path(startNode, bufferNode, distanceOfBuffer, false);
            earliestAGVPath.add(path);
            lastPath = path;
        }
        returningAGV[indexOfAGV] = false;
    }
}