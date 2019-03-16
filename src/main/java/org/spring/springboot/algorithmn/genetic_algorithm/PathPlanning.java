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
    private Routing routing;

    PathPlanning(int numberOfAGV, double penaltyForConflict, double speedOfAGV, double distanceOfBuffer, Routing routing) {
        returningAGV = new boolean[numberOfAGV];
        for (int i = 0; i < numberOfAGV; i++) {
            returningAGV[i] = false;
        }
        this.penaltyForConflict = penaltyForConflict;
        this.speedOfAGV = speedOfAGV;
        this.distanceOfBuffer = distanceOfBuffer;
        this.routing = routing;
    }

    PathPlanning(int numberOfAGV, double penaltyForConflict, double speedOfAGV, double distanceOfBuffer) {
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
     * @param endNode The terminal node the AGV wants to go
     * @param startNode The start node of AGV
     * @param indexOfAGV Number of AGV
     * @param currentAGVsFitness Fitness of all AGVs
     * @param currentAGVsTime Time of all AGVs
     * @return Path of the routing from start node to end node
     */
    List<Path> getPath(int endNode, int startNode, int indexOfAGV,
                                  double[] currentAGVsFitness, double[] currentAGVsTime) {
        List<Path> paths;
        //Time to reach the time window. If it is idle, time should not change.
        double leastTimeGetHere = currentAGVsTime[indexOfAGV];
        if (currentAGVsFitness[indexOfAGV] != 0) {
            leastTimeGetHere -= CommonConstant.CROSSING_DISTANCE / speedOfAGV;
        }
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

    /**
     * Calculate the total time during the path
     * @param paths Path of the AGV
     * @param speed Speed of AGV
     * @return Time to go through the path
     */
    private double getTimeFromPath(List<Path> paths, double speed) {
        double time = 0;
        //To deal with one path to the current position, ignore the crossing time.
        for (Path path : paths) {
            if (path.getTime() != 0 && path.getEndNode() != path.getStartNode()) {
                time += path.getTime() + CommonConstant.CROSSING_DISTANCE / speed;
            }
            //AGV is lifting goods or putting goods. Not just pass the crossing.
            else if (path.getTime() != 0 && path.getEndNode() == path.getStartNode()) {
                time += path.getTime();
            }
        }
        return time;
    }

    /**
     * Move the other AGVs in the buffer when some AGV gets out of the buffer. Adjust the fitness and time window corresponding
     * @param buffer The path of the buffer
     * @param generationForAGVPaths Path of all the AGVs
     * @param fitness Fitness of all the AGVs
     */
    void adjustOtherAGVPositions(List<Integer> buffer, List<List<Path>> generationForAGVPaths, double fitness[]) {
        for (List<Path> path :generationForAGVPaths) {
            Path endPath = path.get(path.size() - 1);
            int endPosition = endPath.getEndNode();
            //If the AGV stops in the buffer area, move it forwards
            if (buffer.contains(endPosition) && endPosition != buffer.get(0)
                    && endPosition != buffer.get(buffer.size() - 1)) {
                int nextNode = buffer.get(buffer.indexOf(endPosition) + 1);
                Path nextPath = new Path(endPosition, nextNode, distanceOfBuffer / speedOfAGV, false);
                path.add(nextPath);
                int indexOfAGV = generationForAGVPaths.indexOf(path);
                fitness[indexOfAGV] += (distanceOfBuffer + CommonConstant.CROSSING_DISTANCE) / speedOfAGV;
            }
        }
    }


    /**
     * Navigate all of the AGVs to their buffer according to their time to arrive the start node to enter the buffer
     * @param generationForAGVPaths All of the paths of AGVs in one generation
     * @param bufferSet All of the paths information for all the buffers
     * @param bufferForAGVs The mapping from AGV index to buffer index
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
            //Calculate the number of AGVs occupying the buffer
            int occupiedSpaceInBuffer = 0;
            for (int j = 0; j < numberOfAGVs; j++){
                //These are AGV staying idle in the buffer.
                if (bufferForAGVs[j] == i && !isReturning(j)) {
                    occupiedSpaceInBuffer++;
                }
            }
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

    public boolean isReturning(int indexOfAGV) {
        return returningAGV[indexOfAGV];
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
            Path path = new Path(startNode, bufferNode, distanceOfBuffer/ speedOfAGV, false);
            earliestAGVPath.add(path);
            lastPath = path;
        }
        returningAGV[indexOfAGV] = false;
    }

    /**
     * Release the time window in this node for other AGV to cross
     * @param bufferNodeNumber Node number of the buffer
     */
    public void releaseNode(Integer bufferNodeNumber) {
        routing.releaseReservedTimeWindow(bufferNodeNumber);
    }
}
