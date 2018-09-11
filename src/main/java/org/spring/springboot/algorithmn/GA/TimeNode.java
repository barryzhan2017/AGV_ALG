package org.spring.springboot.algorithmn.GA;

import java.util.Objects;

public class TimeNode {
    //Follow 0-start convention
    private int numberOfStep;
    private double time;
    private int nodeId;

    public TimeNode(double time, int nodeId, int numberOfStep) {
        this.numberOfStep = numberOfStep;
        this.time = time;
        this.nodeId = nodeId;
    }

    public int getNumberOfStep() {
        return numberOfStep;
    }

    public void setNumberOfStep(int numberOfStep) {
        this.numberOfStep = numberOfStep;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    //Two time nodes are equal when they have same nodeId and time
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeNode timeNode = (TimeNode) o;
        return Double.compare(timeNode.time, time) == 0 &&
                nodeId == timeNode.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, nodeId);
    }
}
