package org.spring.springboot.Algorithm.GeneticAlgorithm;



import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.ujmp.core.Matrix;

import java.util.*;

//前端传的ongoingAGVPaths不要有个-1位
//前端传的timeAlreadyPassing用0表示空闲
public class AGV_GA {
    private Matrix graph;
    private int populationGen;
    private FeasiblePathGrowth feasiblePathGrowth;
    private Integer[][] tasks; // 每一行表示一个未完成任务，第一列表示起始节点，第二列是终止节点
    private Double[] timeAlreadyPassing; //表示每个车已经运行了多长时间在这个道路上，-1表示车已经闲置
    private Double[] timeForFinishingTasks;//用来存储车辆还需要多久完成当前任务
    private List<List<Integer>> ongoingAGVPaths; //正在运行的AGV小车路径
    private List<List<Integer>> bufferSet; //所有的buffer，以及buffer的路径
    private Integer[] bufferForAGV; //每个小车应在的buffer编号
    private List<Double[]> taskDistribution;//任务分配的个体
    private List<Integer[][]> priorityChromosomeSet;//详细路径的个体，从当前位置到取货点，以及从取货点到放货点,每一个task都有一个染色体
    private Random random = new Random();
    private int taskNumber;
    private int AGVNumber;
    private double AGVSpeed;
    private int nodeSize;// 节点个数
    private double crossoverProbability = 0.7;
    private double mutationProbability = 0.2;
    private final int MAX_EDGE = 999999;
    private double min_distance;//最小距离精度
    private double min_time; //最小时间精度
    //用来统一所有的不同类型染色体的交叉和变异概率
    private double[] crossoverProbabilityArray;
    private double[] mutateProbabilityArray;
    private double currentMeanFitness = 0;//当代的适应度
    private double previousMeanFitness = 0;//下一代的适应度

    private final int MIN_GENERATION = 40;
    private final int MAX_GENERATION = (int)(MIN_GENERATION*1.5);
    private final double PENALTY_FOR_CONFLICT = 999;//小车要是碰撞一次就有距离上的惩罚
    private final double SAFE_DISTANCE = 1;//1m为小车间的安全距离

    public AGV_GA(Matrix graph,int populationGen, Integer[][] tasks, Double[] timeAlreadyPassing, List<List<Integer>> ongoingAGVPaths,
                  double AGVSpeed, double min_distance, List<List<Integer>> bufferSet, Integer[] bufferForAGV) {
        this.graph = graph;
        this.populationGen = populationGen;
        this.tasks = tasks;
        this.timeAlreadyPassing = timeAlreadyPassing;
        this.ongoingAGVPaths = ongoingAGVPaths;
        this.AGVSpeed = AGVSpeed;
        this.bufferForAGV = bufferForAGV;
        this.bufferSet = bufferSet;
        taskNumber = tasks.length;
        AGVNumber  = ongoingAGVPaths.size();
        nodeSize = graph.toIntArray()[0].length;
        this.min_distance = min_distance;
        priorityChromosomeSet = new ArrayList<Integer[][]>();
        taskDistribution = new ArrayList<Double[]>();
        timeAlreadyPassing = new Double[AGVNumber];
        timeForFinishingTasks = new Double[AGVNumber];
        feasiblePathGrowth = new FeasiblePathGrowth(graph,transferGraphToConnectedGraph(graph,nodeSize));
        //初始化时间剩余列表
        initiateTimeLeftForFinishingTasks();
        System.out.println("已经经过的时间"+Matrix.Factory.importFromArray(timeForFinishingTasks));
        min_time = min_distance/AGVSpeed;
    }

    //任务分配的任务优先级按传人的先后顺序
    public void hibridMultiObjectGenericAlgorithm() {

        //记录有多少次前后子代平均适应度函数稳定
        int stableTimes = 0;
        //记录进化了多少轮
        int evolveTimes = 0;

        //初始子代的建立
        //encode 任务分配
        //encode 详细路径的优先级染色体，从当前位置到取货点，以及从取货点到放货点,从返货点到闲置buffer入口点(最多有AGV车数目个)，每一个task都有一个染色体
        for (int i = 0; i < populationGen; i++) {
            Double[] task = new Double[taskNumber];
            Integer[][] priChromosome = new Integer[taskNumber*2+AGVNumber][nodeSize];
            for (int j = 0; j < taskNumber; j++) {
                //j代表第几个任务，值对应哪一个车
                task[j] = random.nextDouble();
            }
            for (int j = 0; j < priChromosome.length; j++) {
                for (int k = 0; k < nodeSize; k++) {
                    priChromosome[j][k] = random.nextInt(nodeSize);
                }
            }
            priorityChromosomeSet.add(priChromosome);
            taskDistribution.add(task);
        }

//        for (Double[] taskDis : taskDistribution) {
//            System.out.println("任务分配矩阵" + Matrix.Factory.importFromArray(taskDis));
//        }
//        for (Integer[][] priChromosome : priorityChromosomeSet) {
//            System.out.println("优先染色体集合" + Matrix.Factory.importFromArray(priChromosome));
//        }


        //每一个子代的每一个车的路径
        List<List<List<Integer>>> AGVPaths;

        //开始进化
        //当连续10代适应度平均值没变化且循环次数大于最小代数，或者到达最大代数收敛结束进化
        while (! ((stableTimes >= 10 && evolveTimes > MIN_GENERATION) || evolveTimes > MAX_GENERATION )) {
            System.out.println("进化第"+evolveTimes+"轮啦");
            //更新概率矩阵
            initializeCrossoverProbability();
            //交叉 taskDistribution
            crossoverScheduling();
            //交叉 PriorityChromosomeSet
            crossoverPrioritySet();

            //更新人口数目
            populationGen = priorityChromosomeSet.size();
            //更新概率矩阵
            initializeMutateProbability();


            //变异 taskDistribution
            mutateScheduling();
            //变异 PriorityChromosomeSet
            mutatePrioritySet();

            //更新人口数目
            populationGen = taskDistribution.size();



            //初始化每一个子代的每个车的适应度
            List<double[]> AGVFitness = new ArrayList<double[]>();
            for (int i = 0; i < populationGen; i++) {
                double[] AGVFitnessGeneration = new double[AGVNumber];
                for (int j = 0; j < AGVNumber; j++) {
                    AGVFitnessGeneration[j] = 0;
                }
                AGVFitness.add(AGVFitnessGeneration);
            }


            //初始化该次循环下的路径，每一个子代的每一个车的路径
            AGVPaths = new ArrayList<List<List<Integer>>>();

            //初始化车辆路径
            for (int i = 0; i < populationGen; i++) {
                List<List<Integer>> generationForAGVPaths = new ArrayList<List<Integer>>();
                for (List<Integer> path : ongoingAGVPaths) {
                    //按顺序拷贝每一辆车的路径。直接浅拷贝进去，生成地址不同但是元素指向相同的AGV，不改变原有元素所以可以这么做
                    List<Integer> AGV = new ArrayList<Integer>(path);
                    generationForAGVPaths.add(AGV);
                }
                AGVPaths.add(generationForAGVPaths);
            }


            //每一个子代的已经过去的时间
            List<double[]> AGVTimes = new ArrayList<double[]>();
            for (int i = 0; i < populationGen; i++) {
                double[] time = new double[timeForFinishingTasks.length];
                for (int j = 0; j < timeForFinishingTasks.length; j++) {
                    time[j] = timeForFinishingTasks[j];
                }
                AGVTimes.add(time);
            }

            //decode 任务分配 同时 decode 路径规划
            // taskSequence所有子代的任务顺序 list存每个个体的任务顺序，数组存储每个个体的任务顺序，如3，2，0，1，表示先做第3个任务
            List<Integer[]> taskSequence = new ArrayList<Integer[]>();
            getTaskSequence(taskSequence);

            //第count个任务
            int count;
            //第i代
            int i = 0;


            for (List<List<Integer>> generationForAGVPaths : AGVPaths) {
                //未分配完所有任务时，分配任务, 小车
                count = 0;
                while (count < taskNumber) {
                    //记录是第几个任务
                    //获取最早的AGV空闲小车
                    int AGVIndex = getEarliestAGV(AGVTimes.get(i));
                    //如果小车还未分配了任务，则获取小车的出发点
//                if (timeAlreadyPassing[AGVIndex]==-1) {
                    int startPoint = generationForAGVPaths.get(AGVIndex).get(generationForAGVPaths.get(AGVIndex).size() - 1);
                    List<Integer> buffer = bufferSet.get(bufferForAGV[AGVIndex]);
                    //如果当前车子在buffer第一位的话，起始点就是buffer和graph交点, 并且直接加入path中，为了统一性,更新下fitness
                    if (startPoint == buffer.get(buffer.size() - 2)) {
                        startPoint = buffer.get(buffer.size() - 1);
                        generationForAGVPaths.get(AGVIndex).add(startPoint);
                        AGVFitness.get(i)[AGVIndex] += min_distance;
                    }
                    //获取第i个子代的第个taskSequence.get(i)[count]个（排在最前面的）任务的确切路径以及该路径的距离，用对应的优先度染色体
                    //从tasks中获取子代的最前的任务的第一个移动点（buffer和graph的交点）和第二移动个点
                    double[] path1 = feasiblePathGrowth.feasiblePathGrowth(
                            startPoint, tasks[taskSequence.get(i)[count]][0], priorityChromosomeSet.get(i)[taskSequence.get(i)[count] * 2]);
                    double[] path2 = feasiblePathGrowth.feasiblePathGrowth(
                            tasks[taskSequence.get(i)[count]][0], tasks[taskSequence.get(i)[count]][1], priorityChromosomeSet.get(i)[taskSequence.get(i)[count] * 2 + 1]);
                    double distance = path1[path1.length - 1] + path2[path2.length - 1];
                    //更新小车的任务完成时间表
                    AGVTimes.get(i)[AGVIndex] += distance / AGVSpeed;
                    //更新该小车的路径，不要重复了startPoint,索引从1开始
//                System.out.println("taskSequence: " +startPoint+","+tasks[taskSequence.get(i)[count]][0]+","+tasks[taskSequence.get(i)[count]][1]);
//                System.out.println("path1:"+Matrix.Factory.importFromArray(path1));
//                System.out.println("path2:"+Matrix.Factory.importFromArray(path2));
                    //第一个点是buffer和graph的交点之前就先放入
                    for (int j = 1; j < path1.length - 1; j++) {
                        //结束了路径
                        if ((int) path1[j] == -1) {
                            break;
                        }
                        generationForAGVPaths.get(AGVIndex).add((int) path1[j]);
                    }
                    //从一开始，避免重复path1的最后一个节点
                    for (int j = 1; j < path2.length - 1; j++) {
                        if ((int) path2[j] == -1) {
                            break;
                        }
                        generationForAGVPaths.get(AGVIndex).add((int) path2[j]);
                    }
                    //更新该子代该小车的适应度 最后一位存了距离，加上车子开出buffer的1点距离
                    AGVFitness.get(i)[AGVIndex] += (path1[path1.length - 1] + path2[path2.length - 1]);
                    //调整其他还在buffer里头在该辆车后头的车子的位置，让他们都同时前进一步
                    adjustOtherAGVPositions(bufferForAGV[AGVIndex], generationForAGVPaths, AGVFitness.get(i));
                    count++;
                }
                //将在地图上闲置的小车遣返回buffer
                returnAGVToBuffer(generationForAGVPaths, AGVFitness.get(i), priorityChromosomeSet.get(i), 2 * taskNumber, AGVTimes.get(i));
                i++;
            }

//            for (double[] fitness : AGVFitness) {
//                System.out.println("AGV的适应度" + Matrix.Factory.importFromArray(fitness));
//            }
//            for (Integer[] taskOrder : taskSequence) {
//                System.out.println("任务顺序" + Matrix.Factory.importFromArray(taskOrder));
//            }
//            for (List<List<Integer>> generationForAGVPaths : AGVPaths) {
//                System.out.println("next generation");
//                for (List<Integer> path : generationForAGVPaths) {
//                    System.out.println(path);
//                }
//            }
//            for (int p = 0; p < populationGen; p++) {
//                System.out.println("已过去的时间" + Matrix.Factory.importFromArray(AGVTimes.get(p)));
//            }


            //删除掉走了非法路径的子代
            deleteIllegalGeneration(AGVFitness, AGVPaths, AGVTimes);
            //更新人口
            populationGen = priorityChromosomeSet.size();
//
//            for (double[] fitness : AGVFitness) {
//                System.out.println("删除掉走了非法路径的子代的AGV的适应度" + Matrix.Factory.importFromArray(fitness));
//            }
//
//            for (List<List<Integer>> generationForAGVPaths : AGVPaths) {
//                System.out.println("删除掉走了非法路径的子代的next generation");
//                for (List<Integer> path : generationForAGVPaths) {
//                    System.out.println(path);
//                }
//            }
//            for (int p = 0; p < populationGen; p++) {
//                System.out.println("已过去的时间" + Matrix.Factory.importFromArray(AGVTimes.get(p)));
//            }

            //计算fitness，对出现碰撞的规划增加penalty，碰撞越多penalty越大
            conflictAvoid(AGVPaths, AGVTimes, AGVFitness);


//            for (double[] fitness : AGVFitness) {
//                System.out.println("碰撞计算后的AGV的适应度" + Matrix.Factory.importFromArray(fitness));
//            }


            //为了选取优秀的子代，将以距离为适应度的列表转换为每个车的1/distance的和的数列来进行轮盘法
            double[] adjustFitness = new double[populationGen];
            currentMeanFitness = 0;
            for (int j = 0; j < populationGen; j++) {
                for (int k = 0; k < AGVNumber; k++) {
                    adjustFitness[j] += AGVFitness.get(j)[k];
                }
                adjustFitness[j] = 1/adjustFitness[j];
                //计算当代的适应度
                currentMeanFitness += adjustFitness[j];
            }

            //当前子代求平均值
            currentMeanFitness /= populationGen;

            System.out.println("当前适应度" + currentMeanFitness + "  之前的适应度" + previousMeanFitness + " 稳定次数"+stableTimes);

            //更新适应度稳定次数，如果不连续则重新开始计算
            if (currentMeanFitness - previousMeanFitness < currentMeanFitness*0.1) {
                stableTimes++;
            }
            else {
                stableTimes = 0;
            }

            for (double[] fitness : AGVFitness) {
                System.out.println("当前AGV的适应度" + Matrix.Factory.importFromArray(fitness));
            }
//            for (List<List<Integer>> generationForAGVPaths : AGVPaths) {
//                System.out.println("当前子代的路径");
//                for (List<Integer> path : generationForAGVPaths) {
//                    System.out.println(path);
//                }
//            }

            //选取过程,获得存活下来的群体,用set不重复选取index
            Set<Integer> survival = new HashSet<Integer>();
            for (int j = 0; j < populationGen; j++) {
                survival.add(rouletteSelect(adjustFitness));
            }
            //倒着删除掉没存活的染色体以及任务以及适应度
            for (int j = populationGen-1; j >= 0; j--) {
                if (!survival.contains(j)) {
                    priorityChromosomeSet.remove(j);
                    taskDistribution.remove(j);
                    AGVFitness.remove(j);
                    AGVPaths.remove(j);
                }
            }
            //更新populationGen
            populationGen = priorityChromosomeSet.size();

//            for (double[] fitness : AGVFitness) {
//                System.out.println("AGV的适应度" + Matrix.Factory.importFromArray(fitness));
//            }
//            for (List<List<Integer>> generationForAGVPaths : AGVPaths) {
//                System.out.println("子代的路径");
//                for (List<Integer> path : generationForAGVPaths) {
//                    System.out.println(path);
//                }
//            }
            previousMeanFitness = currentMeanFitness;
            evolveTimes++;
        }
//        GenericSortAlgorithm.mergesort();
//        for (List<List<Integer>> generationForAGVPaths :AGVPaths)
    }


































































    //初始化这个子代的所有交叉概率
    private void initializeCrossoverProbability() {
        crossoverProbabilityArray = new double[populationGen];
        for (int i = 0; i < populationGen; i++) {
            crossoverProbabilityArray[i] = random.nextDouble();
        }
    }

    //初始化这个子代的所有变异概率
    private void initializeMutateProbability() {
        mutateProbabilityArray = new double[populationGen];
        for (int i = 0; i < populationGen; i++) {
            mutateProbabilityArray[i] = random.nextDouble();
        }
    }


    //删除掉所有走了非法路径的子代
    private void deleteIllegalGeneration(List<double[]> AGVFitness, List<List<List<Integer>>> AGVPaths, List<double[]> AGVTimes) {
        boolean[] legalGeneration = new boolean[populationGen];
        for (int j = 0; j < populationGen; j++) {
            legalGeneration[j] = true;
        }
        int count1 = 0;
        for (double[] fitness: AGVFitness) {
            boolean legal = true;
            //有一辆车的路径非法则该子代非法
            for (int j = 0 ; j < AGVNumber; j++) {
                if (fitness[j] >= MAX_EDGE) {
                    legal = false;
                }
            }
            if (!legal) {
                legalGeneration[count1] = false;
            }
            count1++;
        }
        int currentPop = populationGen;
        //倒着删除，防止退位造成index错误
        for (int j = currentPop-1; j >= 0; j--) {
            if (!legalGeneration[j]) {
                AGVFitness.remove(j);
                AGVPaths.remove(j);
                AGVTimes.remove(j);
                priorityChromosomeSet.remove(j);
                taskDistribution.remove(j);
                populationGen--;
            }
        }
    }


    //将闲置在搬货区域的车强制开回buffer中，并且计算额外的适应度，startIndex是从priChromosome的什么位置开始
    private void returnAGVToBuffer(List<List<Integer>> generationForAGVPaths,
                                   double[] fitness, Integer[][] priChromosome, int startIndex, double[] AGVtime) {
        int bufferNumber = bufferSet.size();
        //找到每个buffer中没有回去和回去的AGV存到下面，初始化全是-1
        int[][] unreturnedAGVs = new int[bufferNumber][AGVNumber];
        for (int i = 0; i < bufferNumber; i++) {
            for (int j = 0; j < AGVNumber; j++) {
                unreturnedAGVs[i][j] = -1;
            }
        }
        //记录第几个车
        int count = 0;
        //记录，每个buffer被占用的个数
        Integer[] occupiedNumberForBuffer = new Integer[bufferNumber];
        for (int i = 0; i < bufferNumber; i++) {
            occupiedNumberForBuffer[i] = 0;
        }
        for (List<Integer> path : generationForAGVPaths) {
            List<Integer> buffer = bufferSet.get(bufferForAGV[count]);
            int endPoint = path.get(path.size() - 1);
            //如果小车没有停到了buffer里头,给该buffer中记录该小车没回去,反之也是记录进去
            if (buffer.contains(endPoint) && buffer.get(buffer.size() - 1) != endPoint && buffer.get(0) != endPoint) {
               occupiedNumberForBuffer[bufferForAGV[count]]++;
            }
            else {
                int k = 0;
                while (unreturnedAGVs[bufferForAGV[count]][k] != -1 && k < AGVNumber) {
                    k++;
                }
                unreturnedAGVs[bufferForAGV[count]][k] = count;
            }
            count++;
        }
        //转换类型成Double数组
        Double [] DoubleAGVTime = new Double[AGVtime.length];
        for (int i = 0; i < AGVtime.length; i++) {
            DoubleAGVTime[i] = AGVtime[i];
        }
        //先将每个车开回他们的buffer的入口点，计算时间加入DoubleAGVtime，也计算了fitness
        for (int i = 0; i < bufferNumber; i++) {
            for (int j = 0; j < AGVNumber; j++) {
                //如果没车了
                if (unreturnedAGVs[i][j] == -1) {
                    break;
                }
                else {
                    //找到该AGV位置处的优先染色体（startIndex+AGVIndex）
                    int AGVIndex = unreturnedAGVs[i][j];
                    double[] path = feasiblePathGrowth.feasiblePathGrowth(
                            generationForAGVPaths.get(AGVIndex).get(generationForAGVPaths.get(AGVIndex).size()-1),
                            bufferSet.get(i).get(0),priChromosome[startIndex+AGVIndex]);
                    for (int k = 1; k < path.length - 1; k++) {
                        //结束了路径
                        if ((int) path[k] == -1) {
                            break;
                        }
                        generationForAGVPaths.get(AGVIndex).add((int) path[k]);
                    }
                    double distance = path[path.length-1];
                    DoubleAGVTime[AGVIndex] += distance/AGVSpeed;
                    fitness[AGVIndex] += distance;
                }
            }
        }

        //依据时间较小的车在数组前面给每个小车排序，排除了-1的问题
        for (int i = 0; i < bufferNumber; i++) {
            GenericSortAlgorithm.insertSort(unreturnedAGVs[i],DoubleAGVTime);
        }
        //时间较小的车开到在buffer的靠出口位置
        for (int i = 0; i < bufferNumber; i++) {
            for (int j = 0; j < AGVNumber; j++) {
                //如果没车了
                if (unreturnedAGVs[i][j] == -1) {
                    break;
                }
                //把车开总点数-（已经占用个数+2）次,增加占用个数, 调整fitness
                else {
                    int AGVIndex = unreturnedAGVs[i][j];
                    for (int k = 1; k <= bufferSet.get(i).size() - occupiedNumberForBuffer[i] -2; k++ ) {
                        generationForAGVPaths.get(AGVIndex).add(bufferSet.get(i).get(k));
                        fitness[AGVIndex] += min_distance;
                    }
                    occupiedNumberForBuffer[i]++;
                }
            }
        }

    }



    //按照最小时间精度遍历，出现碰撞给该方案的距离和加上一次惩罚值，二次则两倍
    private void conflictAvoid(List<List<List<Integer>>> AGVPaths, List<double[]> AGVTimes, List<double[]> AGVFitness) {
        //首先，将当前小车的timeAlreadyPassing列表，和路径一块存。
        //每一个子代的每个车的前两个路径加上时间，如 [3][0,2,3.2] 表示车3在0到2的路上，已经开了3.2秒了
        List<double[][]> futurePaths = new ArrayList<double[][]>();
        for (int i = 0; i < populationGen; i++) {
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
        //记录是第几个子代
        int count = 0;
        //对每个子代进行conflict检测
        for (double[][] futurePath: futurePaths) {
            //记录到路径上的第几个位置（index),初始是从第二个点开始
            Matrix index = Matrix.Factory.ones(1,AGVNumber);
            index = index.times(2);
            //判断是否所有车辆都在跑
            boolean AGVrunning = true;
            //最小的时间步长
            double timeStep = min_time;
            while (AGVrunning) {
                AGVrunning = false;
                //对每个小车的后面排列的小车进行路径对比，分为线上会车和点上会车
                for (int j = 0; j < AGVNumber; j++) {
                    //如果小车是停滞的小车就不管它
                    if (futurePath[j][0]==-1) {
                        continue;
                    }
                    //有车子在跑
                    AGVrunning = true;
                    //j车正在走的路的长度
                    double ongoingPathLength = getAGVOngoingPathLength(j,futurePath);
                    for (int k = j+1; k < AGVNumber; k++) {
                        //如果小车是停滞的小车就不管它
                        if (futurePath[k][0]==-1) {
                            continue;
                        }
                        //k车正在走的路的长度
                        double ongoingPathLength1 = getAGVOngoingPathLength(k,futurePath);
                        //对线上回车情况进行讨论，若两车在相向而走，且两车距离之和经历小于路长到大于路长，则判定为一次碰撞,增加惩罚值
                        //第一个等式取等 会造成二次计算 所以不取
                        if (futurePath[j][0] == futurePath[k][1] && futurePath[j][1] == futurePath[k][0]
                                && ongoingPathLength > futurePath[j][2]*AGVSpeed + futurePath[k][2]*AGVSpeed
                                && ongoingPathLength <= futurePath[j][2]*AGVSpeed + futurePath[k][2]*AGVSpeed + min_distance*2) {
                            AGVFitness.get(count)[j] += PENALTY_FOR_CONFLICT;
                            AGVFitness.get(count)[k] += PENALTY_FOR_CONFLICT;
                            //用来检测碰撞情况是否属实
                       //     printErrorOnLine(j,k,futurePath,ongoingPathLength,ongoingPathLength1);
                        }

                        //对在点附近撞车的情况考虑，若两车朝点而开，两车对点的距离都不得小于安全距离
                        //若是一车朝点开，一车离开点，两车与点的距离之和小于安全距离，为碰撞,给上惩罚
                        //两车都离开点的情况已经被第一条考虑过了，若两个车一直紧密挨着走会被多次计算惩罚
                        else if ((futurePath[j][0] == futurePath[k][1] &&
                                ongoingPathLength1 - futurePath[k][2]*AGVSpeed + futurePath[j][2]*AGVSpeed < SAFE_DISTANCE)

                                || (futurePath[j][1] == futurePath[k][0]
                                && ongoingPathLength - futurePath[j][2]*AGVSpeed + futurePath[k][2]*AGVSpeed < SAFE_DISTANCE)

                                || (futurePath[j][1] == futurePath[k][1] && ongoingPathLength - futurePath[j][2]*AGVSpeed < SAFE_DISTANCE
                                && ongoingPathLength1 - futurePath[k][2]*AGVSpeed < SAFE_DISTANCE)) {

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

//    private void printErrorOnLine(int j, int k, double[][] futurePath, double ongoingPathLength, double ongoingPathLength1) {
//        System.out.println("第"+j+"辆车和"+"第"+k+"辆车发生在线上的碰撞");
//        System.out.println("第"+j+"辆车路径"+Matrix.Factory.importFromArray(futurePath[j])+"路径长度为"+ongoingPathLength);
//        System.out.println("第"+k+"辆车路径"+Matrix.Factory.importFromArray(futurePath[k])+"路径长度为"+ongoingPathLength1);
//    }
//
//    private void printErrorOnPoint(int j, int k, double[][] futurePath, double ongoingPathLength, double ongoingPathLength1) {
//        System.out.println("第"+j+"辆车和"+"第"+k+"辆车发生在点上的碰撞");
//        System.out.println("第"+j+"辆车路径"+Matrix.Factory.importFromArray(futurePath[j])+"路径长度为"+ongoingPathLength);
//        System.out.println("第"+k+"辆车路径"+Matrix.Factory.importFromArray(futurePath[k])+"路径长度为"+ongoingPathLength1);
//    }

    //根据第k车以及所有车的路径找出该车当前路径的长度,分为buffer里头的和graph中的
    private double getAGVOngoingPathLength(int k, double[][] futurePath) {
        double ongoingPathLength;
        if (bufferSet.get(bufferForAGV[k]).contains((int)futurePath[k][0])
                && bufferSet.get(bufferForAGV[k]).contains((int)futurePath[k][1])) {
            ongoingPathLength = min_distance;
        }
        else {
            ongoingPathLength = graph.getAsDouble((long) futurePath[k][0], (long) futurePath[k][1]);
        }
//        System.out.println("第"+k+"车当前运行路径："+futurePath[k][0]+" "+futurePath[k][1]);
        return ongoingPathLength;
    }

    //由于排头车已经开走，调整该buffer中小车的位置,将AGVPaths上增加一个位置，也增加一个最小距离给适应度函数值
    private void adjustOtherAGVPositions(int bufferIndex, List<List<Integer>> generationForAGVPaths, double fitness[]) {
        for (List<Integer> path :generationForAGVPaths) {
            int endPosition = path.get(path.size()-1);
            //如果该车停靠在该buffer中,而不包括交接点,在其路径上添加下一个节点,并且增加其适应度
            if (bufferSet.get(bufferIndex).contains(endPosition) && endPosition!=bufferSet.get(bufferIndex).get(0)
                    && endPosition!=bufferSet.get(bufferIndex).get(bufferSet.get(bufferIndex).size()-1)) {
                path.add(bufferSet.get(bufferIndex).get(bufferSet.get(bufferIndex).indexOf(endPosition)+1));
                fitness[generationForAGVPaths.indexOf(path)] += min_distance;
            }
        }
    }

    // 初始化每个AGV计算还需要多长时间小车完成任务
    // 计算时间包括AGV到buffer中停靠点已经从停靠点到buffer和graph交点的时间
    private void initiateTimeLeftForFinishingTasks() {
        //初始化剩余时间数组
        for (int i = 0; i < timeForFinishingTasks.length; i++) {
            timeForFinishingTasks[i] = (double)-1;
        }
        //如果小车闲置(为-1)，给数组赋值0
        for (int i = 0; i < timeAlreadyPassing.length; i++) {
            if (timeAlreadyPassing[i]==-1) {
                timeForFinishingTasks[i] = (double)0;
            }
        }

        for (int i = 0; i < AGVNumber; i++) {
            //对于没有闲置的车,计算他们还要多长时间完成任务，整个路程的时间减去已经开了多久
            double distance = 0;
            if (timeForFinishingTasks[i]!=0)  {
                for (int j = 0; j+1 < ongoingAGVPaths.get(i).size(); j++) {
                    //该车正在回闲置位置
                    if (bufferSet.get(bufferForAGV[i]).contains(ongoingAGVPaths.get(i).get(j)) &&
                            bufferSet.get(bufferForAGV[i]).contains(ongoingAGVPaths.get(i).get(j+1))) {
                        //i是入口点，求出剩下的路程
                        distance += (ongoingAGVPaths.get(i).size() - j - 1)*min_distance;
                        break;
                    }
                    distance += graph.getAsDouble(ongoingAGVPaths.get(i).get(j),ongoingAGVPaths.get(i).get(j+1));
                }
                timeForFinishingTasks[i] = distance/AGVSpeed - timeAlreadyPassing[i];
            }
        }

        for (int i = 0; i < AGVNumber; i++) {
            //计算每个车从闲置点到buffer和地图的交接点的时间,最小距离乘多少条边除掉速度
            List<Integer> bufferPath = bufferSet.get(bufferForAGV[i]);
                timeForFinishingTasks[i] += (double)(min_distance)*(bufferPath.size() - 1 -
                        bufferPath.indexOf(ongoingAGVPaths.get(i).get(ongoingAGVPaths.get(i).size()-1)))/(AGVSpeed);
        }


    }

    //给每个子代一个任务的序列
    private void getTaskSequence(List<Integer[]> taskSequence) {
        for (Double[] tasksCode : taskDistribution) {
            Double[] sortArray = new Double[taskNumber];
            for (int i = 0; i < taskNumber; i++) {
                sortArray[i] = tasksCode[i];
            }
            GenericSortAlgorithm.mergesort(sortArray);
            Integer[] sequence = new Integer[taskNumber];
            //最小的最早开始做，任务序号存在sequence中
            for (int i = 0; i < taskNumber; i++) {
                for (int j = 0; j < taskNumber; j++) {
                    if (tasksCode[j] == sortArray[i]) {
                        sequence[i] = j;
                        break;
                    }
                }
            }
            taskSequence.add(sequence);
        }
    }


    //找到最早的空闲小车,用的是局部变量不是全局的那个
    private Integer getEarliestAGV(double[] timeForFinishingTasks) {
        Double[] sortTimeArray = new Double[timeForFinishingTasks.length];
        for (int i = 0; i < timeForFinishingTasks.length; i++) {
            sortTimeArray[i] = timeForFinishingTasks[i];
        }
        GenericSortAlgorithm.mergesort(sortTimeArray);
        //找到剩余完成任务时间最少的小车
        for (int i = 0; i < timeForFinishingTasks.length; i++) {
            if (timeForFinishingTasks[i] == sortTimeArray[0]) {
                return i;
            }
        }
        //出问题
        return -1;
    }

    //直接生成新的个体给人口作为mutation
    private void mutateScheduling() {
        int currentPopulationSize = taskDistribution.size();
        for (int i = 0; i < currentPopulationSize; i++) {
            if (mutateProbabilityArray[i] <= mutationProbability) {
                Double[] newGeneration = new Double[taskNumber];
                for (int j = 0; j < taskNumber; j++) {
                    //j代表第几个任务，值对应哪一个车
                    newGeneration[j] = random.nextDouble();
                }
                taskDistribution.add(newGeneration);
            }
        }
    }

    //用 insertion mutation来处理 路线优先度染色体
    private void mutatePrioritySet() {
        //  int currentPopulationSize = priorityChromosomeSet.size();
        // 防止边查边改list，先加入，然后在外头加进去
        List<Integer[][]> newGenerationList = new ArrayList<Integer[][]>();
        int count = 0;
        for (Integer[][] parent: priorityChromosomeSet) {
            if (mutateProbabilityArray[count] <= mutationProbability) {
                Integer[][] newGeneration = new Integer[parent.length][nodeSize];
                for (int k = 0; k < parent.length; k++) {
                    //从point1位置取出一个数字插入到point2的位置上,空位有node个
                    //对所有的该方法下的染色体都进行随机变异，不是同一点的变异
                    int point1 = random.nextInt(nodeSize);
                    int point2 = random.nextInt(nodeSize);
                    int mutatedPoint = parent[k][point1];
                    for (int i = 0; i < nodeSize; i++) {
                        newGeneration[k][i] = parent[k][i];
                    }
                    //删掉该点
                    for (int i = point1; i + 1 < nodeSize; i++) {
                        newGeneration[k][i] = newGeneration[k][i + 1];
                    }
                    //加入该点
                    for (int i = nodeSize - 1; i - 1 >= point2; i--) {
                        newGeneration[k][i] = newGeneration[k][i - 1];
                    }
                    newGeneration[k][point2] = mutatedPoint;
                }
                newGenerationList.add(newGeneration);
            }
            count++;
        }
        for (Integer[][] newGeneration : newGenerationList) {
            priorityChromosomeSet.add(newGeneration);
        }
    }


    //2点的 crossover 用于分配任务， 不用partially mapped crossover
    private void crossoverScheduling() {
        for (int i = 0; i+1 < populationGen; i=i+2) {
            //如果概率值达到要求，进行交叉
            if (crossoverProbabilityArray[i] <= crossoverProbability) {
                //从point1（包括）到point2（包括）的父代1和父代2交叉获取子代
                int point1 = random.nextInt(taskNumber - 1);
                //从point到最后一位
                int point2 = random.nextInt(taskNumber-point1)+point1;
            //    System.out.println(point2);
           //     System.out.println(taskNumber);
                Double[] newGeneration1 = new Double[taskNumber];
                Double[] newGeneration2 = new Double[taskNumber];
                Double[] parent1 = taskDistribution.get(i);
                Double[] parent2 = taskDistribution.get(i + 1);
//                Map<Double,Double> mappingSet = new HashMap<Double,Double>();
                for (int j = 0; j < point1; j++) {
                    newGeneration1[j] = parent1[j];
                    newGeneration2[j] = parent2[j];
                }
                for (int k = point2 + 1; k < taskNumber; k++) {
                    newGeneration1[k] = parent1[k];
                    newGeneration2[k] = parent2[k];
                }
                for (int j = point1; j <= point2; j++) {
//                    mappingSet.put(parent1[j],parent2[j]);
                    newGeneration1[j] = parent2[j];
                    newGeneration2[j] = parent1[j];
                }

//                //检查是否出现冲突（相同）等位基因
//                for (int j = 0; j < point1; j++) {
//                    while (true) {
//                        boolean checkAgain = false;
//                        for (Double randomKey: mappingSet.keySet()) {
//                            //查变了的值是否需要再查一遍
//                            if (newGeneration1[i] == randomKey.doubleValue()) {
//                                newGeneration1[i] = randomKey;
//                                checkAgain = true;
//                            }
//                        }
//                    }
//                }

                taskDistribution.add(newGeneration1);
                taskDistribution.add(newGeneration2);
            }
        }
    }

    //用weight mapping crossover， 用于路线优先度染色体
    private void crossoverPrioritySet() {
        int priChromosomeLength = priorityChromosomeSet.get(0).length;
        for (int i = 0; i+1 < populationGen; i=i+2) {
            //如果概率值达到要求，进行交叉
            if (crossoverProbabilityArray[i] <= crossoverProbability) {
                //从0到point（包括）的父代1和point+1到任务数目的父代2获取子代
                //一刀切，所有该方法下的染色体从一个切口处切开和另个进行交叉
                int point = random.nextInt(nodeSize - 1);
                Integer[][] newGeneration1 = new Integer[priChromosomeLength][nodeSize];
                Integer[][] newGeneration2 = new Integer[priChromosomeLength][nodeSize];
                Integer[][] parent1 = priorityChromosomeSet.get(i);
                Integer[][] parent2 = priorityChromosomeSet.get(i + 1);
                Integer[][] sortArray1 = new Integer[priChromosomeLength][nodeSize-point-1];
                Integer[][] sortArray2 = new Integer[priChromosomeLength][nodeSize-point-1];
                for (int k = 0; k < priChromosomeLength; k++) {
                    for (int j = 0; j <= point; j++) {
                        newGeneration1[k][j] = parent1[k][j];
                        newGeneration2[k][j] = parent2[k][j];
                    }
                }
                for (int j = 0; j < priChromosomeLength; j++) {
                    int count = 0;
                    for (int k = point + 1; k < nodeSize; k++) {
                        sortArray1[j][count] = parent1[j][k];
                        sortArray2[j][count] = parent2[j][k];
                        newGeneration1[j][k] = parent2[j][k];
                        newGeneration2[j][k] = parent1[j][k];
                        count++;
                    }
                }
                for (int j = 0; j < priChromosomeLength; j++) {
                    GenericSortAlgorithm.mergesort(sortArray1[j]);
                    GenericSortAlgorithm.mergesort(sortArray2[j]);
                }
                for (int p = 0; p < priChromosomeLength; p++) {
                    for (int l = 1; l < nodeSize-point; l++) {
                        for (int j = 0; j < nodeSize-point-1; j++) {
                            if (newGeneration1[p][point + l] == sortArray2[p][j]) {
                                newGeneration1[p][point + l] = sortArray1[p][j];
                            }
                            if (newGeneration2[p][point + l] == sortArray1[p][j]) {
                                newGeneration2[p][point + l] = sortArray2[p][j];
                            }
                        }
                    }
                }
//                System.out.println("parent1"+Matrix.Factory.importFromArray(parent1));
//                System.out.println("parent2"+Matrix.Factory.importFromArray(parent2));
//                System.out.println(point);
//                System.out.println("newGeneration"+Matrix.Factory.importFromArray(newGeneration1));
                priorityChromosomeSet.add(newGeneration1);
                priorityChromosomeSet.add(newGeneration2);
            }
        }
    }





    private Matrix transferGraphToConnectedGraph(Matrix graph, int size) {
        Matrix connectedGraph = Matrix.Factory.zeros(size,size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (graph.getAsInt(i,j)==MAX_EDGE) {
                    connectedGraph.setAsInt(0,i,j);
                }
                else {
                    connectedGraph.setAsInt(1,i,j);
                }
            }
        }
//        System.out.println("connect:"+ connectedGraph);
        return connectedGraph;
    }


//    private void insertSort(Integer[] array) {
//        for (int j = 1; j < array.length; j++) {
//            //从第二位开始遍历数组
//            int key = array[j];
//            int i = j - 1;
//            //遍历数组下标j之前的数据，如果大于当前值，则后移一位
//            while (i >= 0 && array[i] > key) {
//                array[i + 1] = array[i];
//                i--;
//            }
//            //直至遍历到最前一位，或者当前值小于遍历值，将当前值插入当前位置的后一个位置
//            array[i+1]=key;
//        }
//    }

    // Returns the selected index based on the weights(probabilities)
    int rouletteSelect(double[] weight) {
        // calculate the total weight
        double weightSum = 0;
        for(int i = 0; i < weight.length; i++) {
            weightSum += weight[i];
        }
        // get a random value
        double value = randUniformPositive() * weightSum;
        // locate the random value based on the weights
        for(int i = 0; i < weight.length; i++) {
            value -= weight[i];
            if(value < 0) return i;
        }
        // when rounding errors occur, we return the last item's index
        return weight.length - 1;
    }

    // Returns a uniformly distributed double value between 0.0 and 1.0
    double randUniformPositive() {
        // easiest implementation
        return new Random().nextDouble();
    }

}
