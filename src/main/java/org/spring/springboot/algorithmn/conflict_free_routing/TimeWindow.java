package org.spring.springboot.algorithmn.conflict_free_routing;

public class TimeWindow {
    private int nodeNumber;
    private double startTime;
    private double endTime;
    private int AGVNumber;


    public TimeWindow(int nodeNumber, double startTime, double endTime, int AGVNumber) {
        this.nodeNumber = nodeNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.AGVNumber = AGVNumber;
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