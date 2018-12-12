package org.spring.springboot.algorithmn.genetic_algorithm;



//记录某一子代的某个AGV的某个任务的完成情况
public class AGVRecord {

    private int pathStartIndex = -1;//该任务在路径的第几个点开始（从0开始）
    private int pathEndIndex = -1;//该任务在路径的第几个点结束
    private int startNode = -1;//该任务在第几个节点序号开始（从0开始）
    private int endNode = -1;//该任务在第几个节点序号结束（从0开始）
    private double distance = -1;//该路径的距离
    private int indexInPriorityChromosome = -1;//在priorityChromosome中的位置
    private boolean isFirstStep= true;//是该任务的从当前位置到出发位置（第一步）还是从出发位置到结束位置（第二步，false）
    private double speed = 2;//AGV在这段路上的速度，用来记录避障时的减速

    public AGVRecord(int pathStartIndex, int pathEndIndex, int startNode, int endNode,
                     double distance, int indexInPriorityChromosome, boolean isFirstStep, double speed) {
        this.pathStartIndex = pathStartIndex;
        this.pathEndIndex = pathEndIndex;
        this.startNode = startNode;
        this.endNode = endNode;
        this.distance = distance;
        this.indexInPriorityChromosome = indexInPriorityChromosome;
        this.isFirstStep = isFirstStep;
        this.speed = speed;
    }

    public AGVRecord(int pathStartIndex, int pathEndIndex, int startNode, int endNode,
                     double distance, int indexInPriorityChromosome, boolean isFirstStep) {
        this.pathStartIndex = pathStartIndex;
        this.pathEndIndex = pathEndIndex;
        this.startNode = startNode;
        this.endNode = endNode;
        this.distance = distance;
        this.indexInPriorityChromosome = indexInPriorityChromosome;
        this.isFirstStep = isFirstStep;
    }

    public AGVRecord(int pathStartIndex, int pathEndIndex, int startNode, int endNode,
                      double speed) {
        this.pathStartIndex = pathStartIndex;
        this.pathEndIndex = pathEndIndex;
        this.startNode = startNode;
        this.endNode = endNode;
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getPathStartIndex() {
        return pathStartIndex;
    }

    public void setPathStartIndex(int pathStartIndex) {
        this.pathStartIndex = pathStartIndex;
    }

    public int getPathEndIndex() {
        return pathEndIndex;
    }

    public void setPathEndIndex(int pathEndIndex) {
        this.pathEndIndex = pathEndIndex;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getIndexInTasksDistribution() {
        return indexInPriorityChromosome;
    }

    public void setIndexInTasksDistribution(int indexInTasksDistribution) {
        this.indexInPriorityChromosome = indexInTasksDistribution;
    }

    public boolean isFirstStep() {
        return isFirstStep;
    }

    public void setFirstStep(boolean firstStep) {
        isFirstStep = firstStep;
    }

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

    public int getIndexInPriorityChromosome() {
        return indexInPriorityChromosome;
    }

    public void setIndexInPriorityChromosome(int indexInPriorityChromosome) {
        this.indexInPriorityChromosome = indexInPriorityChromosome;
    }

    @Override
    public String toString() {
        return "AGVRecord{" +
                "pathStartIndex=" + pathStartIndex +
                ", pathEndIndex=" + pathEndIndex +
                ", startNode=" + startNode +
                ", endNode=" + endNode +
                ", distance=" + distance +
                ", indexInPriorityChromosome=" + indexInPriorityChromosome +
                ", isFirstStep=" + isFirstStep +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AGVRecord agvRecord = (AGVRecord) o;
        return startNode == agvRecord.startNode &&
                endNode == agvRecord.endNode;
    }

    @Override
    public int hashCode() {
        return String.valueOf(startNode).hashCode() + String.valueOf(endNode).hashCode();
    }
}
