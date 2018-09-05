package org.spring.springboot.algorithmn.GA;

public class TimeNode {
    private double time;
    private int nodeId;

    public TimeNode(double time, int nodeId) {
        this.time = time;
        this.nodeId = nodeId;
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
}
