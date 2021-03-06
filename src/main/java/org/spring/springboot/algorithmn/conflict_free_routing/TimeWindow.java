package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;
import java.util.Objects;

public class TimeWindow {
    private int nodeNumber;
    private double startTime;
    private double endTime;
    private int AGVNumber = -1;
    //The node the AGV goes to next
    //It should be the same node as the start node if it is a loop.
    private int nextNodeNumber = -1;
    //The time window linked to it as a previous one
    private TimeWindow lastTimeWindow = null;
    //The path links this time window to the last time window, it should be useful when two nodes are same
    private Integer[] path = {-1, -1, -1};
    //The time needed to get to this time window at least
    private double leastTimeReachHere = CommonConstant.INFINITE;
    //If it is first step, the time to start routing should be subtract the crossing time
    private boolean isFirstStep = false;

    TimeWindow(int nodeNumber, double startTime, double endTime) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    TimeWindow(int nodeNumber, double startTime, double endTime, int indexOfAGV, int nextNodeNumber, Integer[] path) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = indexOfAGV;
        this.nextNodeNumber = nextNodeNumber;
        this.path = path;
    }

    TimeWindow(int nodeNumber, double startTime, double endTime, int indexOfAGV, int nextNodeNumber) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = indexOfAGV;
        this.nextNodeNumber = nextNodeNumber;
        this.path = path;
    }

    TimeWindow(int nodeNumber, double startTime, double endTime, int indexOfAGV) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = indexOfAGV;
    }

    //Used for initialize current time window
    TimeWindow(int nodeNumber, double startTime, double endTime, int indexOfAGV, double leastTimeReachHere) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = indexOfAGV;
        this.leastTimeReachHere = leastTimeReachHere;
    }

    TimeWindow(int nodeNumber, double startTime, double endTime, int indexOfAGV, int nextNodeNumber, double leastTimeReachHere) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = indexOfAGV;
        this.nextNodeNumber = nextNodeNumber;
        this.leastTimeReachHere = leastTimeReachHere;
    }


    public boolean isFirstStep() {
        return isFirstStep;
    }

    public void setFirstStep(boolean firstStep) {
        isFirstStep = firstStep;
    }

    public int getNextNodeNumber() {
        return nextNodeNumber;
    }

    public TimeWindow getLastTimeWindow() {
        return lastTimeWindow;
    }

    public void setLastTimeWindow(TimeWindow lastTimeWindow) {
        this.lastTimeWindow = lastTimeWindow;
    }

    public Integer[] getPath() {
        return path;
    }

    public void setPath(Integer[] path) {
        this.path = path;
    }

    public void setNodeNumberInPath(Integer nodeNumber, int position) {
        path[position] = nodeNumber;
    }
    public double getLeastTimeReachHere() {
        return leastTimeReachHere;
    }

    public void setLeastTimeReachHere(double leastTimeReachHere) {
        this.leastTimeReachHere = leastTimeReachHere;
    }

    public void setNextNodeNumber(int nextNodeNumber) {
        this.nextNodeNumber = nextNodeNumber;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public int getAGVNumber() {
        return AGVNumber;
    }

    public void setAGVNumber(int AGVNumber) {
        this.AGVNumber = AGVNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeWindow that = (TimeWindow) o;
        //To Compare the reserved time window
        if (AGVNumber == -1) {
            return nodeNumber == that.nodeNumber &&
                    Double.compare(that.startTime, startTime) == 0 &&
                    Double.compare(that.endTime, endTime) == 0;
        }
        else{
                return nodeNumber == that.nodeNumber &&
                        Double.compare(that.startTime, startTime) == 0 &&
                        Double.compare(that.endTime, endTime) == 0 &&
                        AGVNumber == that.getAGVNumber();
            }

    }

    @Override
    public int hashCode() {

        return Objects.hash(nodeNumber, startTime, endTime);
    }
}
