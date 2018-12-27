package org.spring.springboot.algorithmn.conflict_free_routing;

import org.spring.springboot.algorithmn.common.CommonConstant;

public class TimeWindow {
    private int nodeNumber;
    private double startTime;
    private double endTime;
    private int AGVNumber;
    //The node the AGV goes to next
    private int nextNodeNumber;
    //The time window linked to it as a previous one
    private TimeWindow lastTimeWindow = null;
    //The path links this time window to the last time window, it should be useful when two nodes are same
    private Integer[] path = {-1, -1, -1};
    //The time needed to get to this time window at least
    private double leastTimeReachHere = CommonConstant.INFINITE;

    public TimeWindow(int nodeNumber, double startTime, double endTime, int AGVNumber, int nextNodeNumber) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = AGVNumber;
        this.nextNodeNumber = nextNodeNumber;
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

}
