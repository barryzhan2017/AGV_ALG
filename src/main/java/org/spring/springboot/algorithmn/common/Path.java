package org.spring.springboot.algorithmn.common;

public class Path {
    public int startNode;
    public int endNode;
    //Time required to go through the path, starting when the AGV totally entered the path, ending when the AGV totally get into the crossing
    public double time;
    //To check if the path is a loop (loop means the AGV starts from start node and goes into this path and goes back to the start node)
    public boolean isLoop;

    public int getStartNode() {
        return startNode;
    }

    public void setStartNode(int startNode) {
        this.startNode = startNode;
    }

    public int getEndNode() {
        return endNode;
    }

    public void setEndNode(int endNode) {
        this.endNode = endNode;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public Path(int startNode, int endNode, double time, boolean isLoop) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.time = time;
        this.isLoop = isLoop;
    }
}
