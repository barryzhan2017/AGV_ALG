package org.spring.springboot.algorithmn.GA;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.List;

public class ConflictAvoid {

    private double AGVSpeed;
    private int AGVNumber;
    private Double[] timeAlreadyPassing;
    private double minTime;
    private double minDistance;
    private double PENALTY_FOR_CONFLICT;
    private double SAFE_DISTANCE;
    private Matrix graph;
    private List<List<Integer>> bufferSet; //所有的buffer，以及buffer的路径
    private Integer[] bufferForAGV; //每个小车应在的buffer编号
    private int nodeNumber;//地图上有多少节点
    private final int STOP_AT_ZERO_POSITION = -10000;//当当前路段只有最小精度长度时候，停在出发点一轮的标志是-10000
    private final int STOP_AGAIN = -50000;//再一次停在当前位置


    public ConflictAvoid(double AGVSpeed, int AGVNumber, Double[] timeAlreadyPassing, double minTime, double minDistance,
                         double PENALTY_FOR_CONFLICT, double SAFE_DISTANCE, Matrix graph, List<List<Integer>> bufferSet, Integer[] bufferForAGV) {
        this.AGVSpeed = AGVSpeed;
        this.AGVNumber = AGVNumber;
        this.timeAlreadyPassing = timeAlreadyPassing;
        this.minTime = minTime;
        this.minDistance = minDistance;
        this.PENALTY_FOR_CONFLICT = PENALTY_FOR_CONFLICT;
        this.SAFE_DISTANCE = SAFE_DISTANCE;
        this.graph = graph;
        this.bufferSet = bufferSet;
        this.bufferForAGV = bufferForAGV;
        nodeNumber = (int)graph.getSize()[0];
    }

    //按照最小时间精度遍历，出现碰撞给该方案的距离和加上一次惩罚值，二次则两倍
    public void conflictAvoidStrategy(List<List<List<Integer>>> AGVPaths, List<double[]> AGVFitness) {
        //每个子代的小车的当前路径和移动了多久
        List<double[][]> futurePaths = initializeFuturePath(AGVPaths);
        //记录是第几个子代
        int count = 0;
        //对每个子代进行conflict检测
        for (double[][] futurePath : futurePaths) {
            //记录到路径上的第几个位置（index),初始是从第二个点开始,（下一步是哪个索引）
            Matrix pathIndex = Matrix.Factory.ones(1, AGVNumber);
            pathIndex = pathIndex.times(2);
            //判断是否所有车辆都在跑
            boolean AGVRunning = true;
            //最小的时间步长
            double timeStep = minTime;
            while (AGVRunning) {
                AGVRunning = false;
                //对每个小车的后面排列的小车进行路径对比，分为线上会车和点上会车
                //先看看点上会车情况,对所有节点搜索看看是否有多辆车往节点开且距离都是最小距离精度
                nodeConvergeCheck(futurePath,AGVPaths.get(count),pathIndex,AGVFitness.get(count));



            }
            count++;
        }
    }

    //根据节点汇聚情况进行惩罚，停车，以及重新规划
    private void nodeConvergeCheck(double[][] futurePath, List<List<Integer>> AGVPathsGen, Matrix pathIndex, double[] AGVFitnessGen) {
        //nodeConverge记录每个节点上有哪个几个车子与它距离等于最小精度且朝着它开,每一个list是节点，每一个节点中的元素表示几号车子是这种情况
        List<List<Integer>> nodeConverge = new ArrayList<List<Integer>>();
        for (int i = 0; i < nodeNumber; i++) {
            List<Integer> node = new ArrayList<Integer>();
            nodeConverge.add(node);
        }
        //把符合距离条件的车辆加入对应节点list
        for (int j = 0; j < AGVNumber; j++) {
            //行驶的路的长度
            double ongoingPathLength = getAGVOngoingPathLength(j,futurePath);
            //如果车辆距离节点等于最小精度且车子不在buffer里头的,把车辆加入该节点的list中
            if (ongoingPathLength == futurePath[j][2]*AGVSpeed + minDistance && futurePath[j][1] < nodeNumber) {
                nodeConverge.get((int)futurePath[j][1]).add(j);
            }
        }
        for (List<Integer> node: nodeConverge) {
            int convergeSize = node.size();
            switch (convergeSize) {
                case 0: break;
                case 1: break;
                case 2: {
                    //找到一个不往有车方向行驶的agv
                   twoAGVConflictAvoid(node,futurePath,AGVPathsGen,pathIndex,AGVFitnessGen);
                    break;
                }
                case 3: {
                    //找到一个不往有车方向行驶的agv
                    int legalAGVIndex = findAGVNotDrivingToOtherAGV(node,futurePath,AGVPathsGen,pathIndex);
                    //不存在小车避开碰撞
                    if (legalAGVIndex == -1) {
                        //重新规划一个车的道路

                    }
                    //先让一个小车开走，其他两个让路
                    else {
                        //小车编号
                        int AGVIndex1 = legalAGVIndex;
                        int AGVIndex2;
                        int AGVIndex3;
                        int indexOfLegalAGV = node.indexOf(AGVIndex1);
                        //找到其他两个车在node中的索引来获取小车的索引
                        if (indexOfLegalAGV == 0) {
                            AGVIndex2 = node.get(1);
                            AGVIndex3 = node.get(2);
                        }
                        else if (indexOfLegalAGV == 1){
                            AGVIndex2 = node.get(0);
                            AGVIndex3 = node.get(3);
                        }
                        else {
                            AGVIndex2 = node.get(0);
                            AGVIndex3 = node.get(1);
                        }
                        //让小车让路
                        stopAGV(AGVIndex2,AGVPathsGen,AGVFitnessGen,futurePath,pathIndex);
                        stopAGV(AGVIndex3,AGVPathsGen,AGVFitnessGen,futurePath,pathIndex);
                        //去掉已经走掉的小车再规划剩下两个车
                        node.remove(AGVIndex1);
                        //对剩下的两个车子做避撞，需要先对两者在路径上加一个index，这样就能保证查看下一个节点的时候是拐弯后的点(由于插入了距离点在路径)
                        pathIndex.setAsInt(pathIndex.getAsInt(0,AGVIndex2) + 1,0,AGVIndex2);
                        pathIndex.setAsInt(pathIndex.getAsInt(0,AGVIndex3) + 1,0,AGVIndex3);

                        twoAGVConflictAvoid(node,futurePath,AGVPathsGen,pathIndex,AGVFitnessGen);
                        //还原他们的index
                        pathIndex.setAsInt(pathIndex.getAsInt(0,AGVIndex2) - 1,0,AGVIndex2);
                        pathIndex.setAsInt(pathIndex.getAsInt(0,AGVIndex3) - 1,0,AGVIndex3);
                    }

                    break;
                }
                case 4: {
                    //重新规划一个车的道路
                    //然后再让剩下三个车跑
                }


            }

        }
    }

    //避开两个AGV在点上的冲撞
    private void twoAGVConflictAvoid(List<Integer> node, double[][] futurePath, List<List<Integer>> AGVPathsGen, Matrix pathIndex,
                                     double[] AGVFitnessGen) {
        int legalAGVIndex = findAGVNotDrivingToOtherAGV(node,futurePath,AGVPathsGen,pathIndex);
        //不存在小车避开碰撞
        if (legalAGVIndex == -1) {
            //重新规划一个车的道路

        }
        //暂停一个小车避开碰撞
        else {
            //小车编号
            int AGVIndex1 = legalAGVIndex;
            int AGVIndex2;
            if (node.indexOf(AGVIndex1) == 1) {
                AGVIndex2 = node.get(0);
            }
            else {
                AGVIndex2 = node.get(1);
            }
            //让小车让路
            stopAGV(AGVIndex2,AGVPathsGen,AGVFitnessGen,futurePath,pathIndex);
        }
    }
    //找到合法的agv的索引号，如果不存在则返回-1
    private int findAGVNotDrivingToOtherAGV(List<Integer> node, double[][] futurePath, List<List<Integer>> AGVPathsGen, Matrix pathIndex) {
        //初始化索引号
        int legalAGVIndex = -1;
        for (Integer AGVIndex : node) {
            boolean isLegal = false;
            List<Integer> path1 = AGVPathsGen.get(AGVIndex);
            //小车下一步的节点编号
            int nextStepIndex1 = path1.get(pathIndex.getAsInt(0, AGVIndex));
            //小车编号
            for (Integer otherAGVIndex : node) {
                //跳过重复的寻找
                if (otherAGVIndex.equals(AGVIndex)) {
                    continue;
                }
                //是否第一辆车下一步不等于第二辆车起点，是的话这个车子就合法不是就非法
                if (nextStepIndex1 != futurePath[otherAGVIndex][0]) {
                    isLegal = true;
                }
                else {
                    isLegal = false;
                }
            }
            //如果和所有车比较都通过的话就返回它的值，表示这个车子可以先走
            if (isLegal) {
                return AGVIndex;
            }
        }
        return legalAGVIndex;
    }

    //停止该编号的小车，改变它的路径和适应度
    private void stopAGV(int AGVIndex2, List<List<Integer>> AGVPathsGen, double[] AGVFitnessGen, double[][] futurePath, Matrix pathIndex) {
        //行驶的路的长度
        double ongoingPathLength2 = getAGVOngoingPathLength(AGVIndex2, futurePath);
        //小车路径
        List<Integer> path2 = AGVPathsGen.get(AGVIndex2);
        //小车下一步在路径上的索引
        int nextStepIndex2 = path2.get(pathIndex.getAsInt(0, AGVIndex2));
        //第一辆车下一步不等于第二辆车起点，则停第二个车一轮(最小时间)，让第一个车先走
        AGVFitnessGen[AGVIndex2] += minDistance;
        //如果已经有了停靠的路径则再停靠加入到它后面标准值
        if (path2.get(nextStepIndex2) < 0) {
            path2.add(nextStepIndex2 + 1, STOP_AGAIN);
        }
        else {
            //将当前位置的相反数插入在路径之中表示走到该位置停靠一轮
            if (ongoingPathLength2 != minDistance) {
                path2.add(nextStepIndex2, (int) -(ongoingPathLength2 - minDistance));
            }
            //停靠在原点
            else {
                path2.add(nextStepIndex2, STOP_AT_ZERO_POSITION);
            }
        }
    }




    //按照最小时间精度遍历，出现碰撞给该方案的距离和加上一次惩罚值，二次则两倍
    public void conflictAvoid(List<List<List<Integer>>> AGVPaths, List<double[]> AGVFitness) {
        //每个子代的小车的当前路径和移动了多久
        List<double[][]> futurePaths = initializeFuturePath(AGVPaths);
        //记录是第几个子代
        int count = 0;
        //对每个子代进行conflict检测
        for (double[][] futurePath: futurePaths) {
            //记录到路径上的第几个位置（index),初始是从第二个点开始
            Matrix index = Matrix.Factory.ones(1,AGVNumber);
            index = index.times(2);
            //判断是否所有车辆都在跑
            boolean AGVRunning = true;
            //最小的时间步长
            double timeStep = minTime;
            while (AGVRunning) {
                AGVRunning = false;
                //对每个小车的后面排列的小车进行路径对比，分为线上会车和点上会车
                for (int j = 0; j < AGVNumber; j++) {
                    //如果小车是停滞的小车就不管它
                    if (futurePath[j][0]==-1) {
                        continue;
                    }
                    //有车子在跑
                    AGVRunning = true;
                    //j车正在走的路的长度
                    double ongoingPathLength = getAGVOngoingPathLength(j,futurePath);
                    //j车在这个路上已经走了多远
                    double alreadyPassedPathLength = futurePath[j][2]*AGVSpeed;
                    for (int k = j+1; k < AGVNumber; k++) {
                        //如果小车是停滞的小车就不管它
                        if (futurePath[k][0]==-1) {
                            continue;
                        }
                        //k车正在走的路的长度
                        double ongoingPathLength1 = getAGVOngoingPathLength(k,futurePath);
                        //k车在这个路上已经走了多远
                        double alreadyPassedPathLength1 = futurePath[k][2]*AGVSpeed;
                        //对线上回车情况进行讨论，若两车在相向而走，且两车距离之和处于小于路长且加上安全距离大于路长，则判定为一次碰撞,增加惩罚值
                        //第一个等式取等 会造成二次计算 所以不取
                        if (futurePath[j][0] == futurePath[k][1] && futurePath[j][1] == futurePath[k][0]
                                && ongoingPathLength > alreadyPassedPathLength + alreadyPassedPathLength1
                                && ongoingPathLength <= alreadyPassedPathLength + alreadyPassedPathLength1 + SAFE_DISTANCE) {
                            AGVFitness.get(count)[j] += PENALTY_FOR_CONFLICT;
                            AGVFitness.get(count)[k] += PENALTY_FOR_CONFLICT;
                            //用来检测碰撞情况是否属实
                            //     printErrorOnLine(j,k,futurePath,ongoingPathLength,ongoingPathLength1);
                        }

                        //对在点附近撞车的情况考虑，若两车朝点而开，两车之间的的距离不得小于安全距离
                        //若是一车朝点开，一车离开点，两车与点的距离之和小于安全距离，这种情况已经不用考虑
                        //两车都离开点的情况已经被第一条考虑过了，若两个车一直紧密挨着走会被多次计算惩罚
                        else if (
//                                (futurePath[j][0] == futurePath[k][1] &&
//                                ongoingPathLength1 - alreadyPassedPathLength1 + alreadyPassedPathLength < SAFE_DISTANCE)
//                                || (futurePath[j][1] == futurePath[k][0]
//                                && ongoingPathLength - alreadyPassedPathLength + alreadyPassedPathLength1 < SAFE_DISTANCE)||
                                 (futurePath[j][1] == futurePath[k][1] &&
                                         Math.pow((ongoingPathLength - alreadyPassedPathLength),2) +
                                                 Math.pow(ongoingPathLength1 - alreadyPassedPathLength1,2) < Math.pow(SAFE_DISTANCE,2))) {

                            AGVFitness.get(count)[j] += PENALTY_FOR_CONFLICT;
                            AGVFitness.get(count)[k] += PENALTY_FOR_CONFLICT;
                            //   printErrorOnPoint(j,k,futurePath,ongoingPathLength,ongoingPathLength1);
                        }
                    }
                    //如果小车要换路走了且小车还能往前走,则改变小车的状态,不会影响后面小车的判断
                    if ((timeStep + futurePath[j][2])*AGVSpeed >= ongoingPathLength
                            && index.getAsInt(0,j) < AGVPaths.get(count).get(j).size()) {
                        futurePath[j][0] = futurePath[j][1];
                        //如果下一步的点和当前的一样则调到下下步,路径的索引得加2
                        int nextStep = AGVPaths.get(count).get(j).get(index.getAsInt(0,j));
                        if (nextStep==futurePath[j][1]) {
                            futurePath[j][1] = AGVPaths.get(count).get(j).get(index.getAsInt(0,j)+1);
                            index.setAsInt(index.getAsInt(0,j)+2,0,j);
                        }
                        else {
                            futurePath[j][1] = nextStep;
                            index.setAsInt(index.getAsInt(0,j)+1,0,j);
                        }
                        futurePath[j][2] = (timeStep + futurePath[j][2])*AGVSpeed - ongoingPathLength;
                    }
                    //如果小车要换路走了且小车走到停靠点了，则让小车停滞
                    else if ((timeStep + futurePath[j][2])*AGVSpeed >= ongoingPathLength
                            && index.getAsInt(0,j) == AGVPaths.get(count).get(j).size()) {
                        futurePath[j][0] = -1;
                        futurePath[j][1] = -1;
                        futurePath[j][2] = -1;
                    }
                    //还在路上开，增加时间
                    else {
                        futurePath[j][2] += timeStep;
                    }
                }
            }
            count++;
        }
    }



    private List<double[][]> initializeFuturePath(List<List<List<Integer>>> AGVPaths) {
        //首先，将当前小车的timeAlreadyPassing列表，和路径一块存。
        //每一个子代的每个车的前两个路径加上时间，如 [3][0,2,3.2] 表示车3在0到2的路上，已经开了3.2秒了
        List<double[][]> futurePaths = new ArrayList<double[][]>();
        for (int i = 0; i < AGVPaths.size(); i++) {
            double[][] futurePath = new double[AGVNumber][3];
            for (int j = 0; j < AGVNumber; j++) {
                //如果小车没有分配任务且闲置状态 [-1,-1,-1]
                if (AGVPaths.get(i).get(j).size() == 1) {
                    futurePath[j][0] = -1;
                    futurePath[j][1] = -1;
                    futurePath[j][2] = -1;
                }
                else {
                    //初始化车辆的行驶信息
                    futurePath[j][0] = AGVPaths.get(i).get(j).get(0);
                    futurePath[j][1] = AGVPaths.get(i).get(j).get(1);
                    //闲置小车初始时间为0
                    if (timeAlreadyPassing[j] == -1) {
                        futurePath[j][2] = 0;
                    }
                    else {
                        futurePath[j][2] = timeAlreadyPassing[j];
                    }
                }
            }
            futurePaths.add(futurePath);
        }
        return futurePaths;
    }


    //根据第k车以及所有车的路径找出该车当前路径的长度,分为buffer里头的和graph中的
    private double getAGVOngoingPathLength(int k, double[][] futurePath) {
        double ongoingPathLength;
        if (bufferSet.get(bufferForAGV[k]).contains((int)futurePath[k][0])
                && bufferSet.get(bufferForAGV[k]).contains((int)futurePath[k][1])) {
            ongoingPathLength = minDistance;
        }
        else {
//            System.out.println(Matrix.Factory.importFromArray(futurePath));
//            System.out.println(k);
//            System.out.println(graph);
            ongoingPathLength = graph.getAsDouble((long) futurePath[k][0], (long) futurePath[k][1]);
        }
//        System.out.println("第"+k+"车当前运行路径："+futurePath[k][0]+" "+futurePath[k][1]);
        return ongoingPathLength;
    }

}
