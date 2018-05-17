package org.spring.springboot.algorithmn.GA;
import org.ujmp.core.Matrix;

import java.util.*;

//前端传的ongoingAGVPaths不要有个-1位
//前端传的timeAlreadyPassing用-1表示空闲
public class AGV_GA {
    private Matrix graph;
    private int populationGen;
    private FeasiblePathGrowth feasiblePathGrowth;//用来生成路径
    private FuzzyControlLogic fuzzyControlLogic = new FuzzyControlLogic();//用来改变交叉和变异的概率
    private PathImprovement pathImprovement = new PathImprovement();//用来替换掉一些较差的路径
    private ReturnPathPlanning returnPathPlanning = new ReturnPathPlanning();//让车子运行完回归buffer的算法
    private ConflictAvoid conflictAvoid;//用来计算撞击的次数，以此来改变适应度值
    private Integer[][] tasks; // 每一行表示一个未完成任务，第一列表示起始节点，第二列是终止节点
    private Double[] timeAlreadyPassing; //表示每个车已经运行了多长时间在这个道路上，-1表示车已经闲置
    private Double[] timeForFinishingTasks;//用来存储车辆还需要多久完成当前任务
    private List<List<Integer>> ongoingAGVPaths; //正在运行的AGV小车路径
    private List<List<Integer>> bufferSet; //所有的buffer，以及buffer的路径
    private Integer[] bufferForAGV; //每个小车应在的buffer编号
    private List<Double[]> taskDistribution;//任务分配的个体
    private List<Integer[][]> priorityChromosomeSet;//详细路径的个体，从当前位置到取货点，以及从取货点到放货点,每一个task都有一个染色体
    private List<List<List<AGVRecord>>> AGVRecords = new ArrayList<List<List<AGVRecord>>>();//每个子代的每个车都有一个行车记录，！！暂时先重新开始计算新生成的部分！！
    private List<List<List<Integer>>> AGVPaths = new ArrayList<List<List<Integer>>>(); //每一个子代的每一个车的路径
    private List<double[]> AGVTimes = new ArrayList<double[]>(); //每一个子代的已经过去的时间
    private List<double[]> AGVFitness = new ArrayList<double[]>(); //初始化每一个子代的每个车的适应度
    private Double[] taskDistributionElitist;//最优的任务分配
    private Integer[][] priorityChromosomeSetElitist;//最优的染色体
    private double[] AGVFitnessElitist;
    private double[] AGVTimesElitist;
    private List<List<Integer>> AGVPathsElitist;
    private List<List<AGVRecord>> AGVRecordsElitist;
    private Random random = new Random();
    private int taskNumber;
    private int AGVNumber;
    private double AGVSpeed;
    private int nodeSize;// 节点个数
    private double crossoverProbability = 0.7;
    private double mutationProbability = 0.2;
    private double minDistance;//最小距离精度
    private double minTime; //最小时间精度
    //用来统一所有的不同类型染色体的交叉和变异概率
    private double[] crossoverProbabilityArray;
    private double[] mutateProbabilityArray;
    private double currentMeanFitness = 0;//当代的平均适应度
    private double previousMeanFitness = 0;//下一代的平均适应度
    private double currentMeanDistance = 0;
    private double previousMeanDistance = 0;
    private double currentMeanDistanceVariation = 0;//上一代平均适应度减去这一代平均适应度
    private double previousMeanDistanceVariation = 0;//上上一代平均适应度减去上一代平均适应度
    private final int MAX_EDGE = 999999;
    private final int INITIAL_POPULATION = 100;//初始子代个数
    private final int MIN_GENERATION = 50;//最少进化次数
    private final int MAX_GENERATION = (int)(MIN_GENERATION*1.5);//最多进化次数
    private final double PENALTY_FOR_CONFLICT = 999;//小车要是碰撞一次就有距离上的惩罚
    private final double SAFE_DISTANCE = minDistance*0.7;//1m为小车间的安全距离
    // 注意安全距离要小于两个车子的最小时间行驶距离之和保证相向碰撞计算可靠
    // 且保证小于最小距离*根号2，这样朝点的碰撞也不会重复计算，（两个车必须都在最小距离之内才计算）
    private final double RELATIVE_ERROR = 0.001;//收敛的相对误差，小于这个则表示稳定

    public AGV_GA() {

    }

    public AGV_GA(Matrix graph, Integer[][] tasks, Double[] timeAlreadyPassing, List<List<Integer>> ongoingAGVPaths,
                  double AGVSpeed, double minDistance, List<List<Integer>> bufferSet, Integer[] bufferForAGV) {
        this.graph = graph;
        this.tasks = tasks;
        this.timeAlreadyPassing = timeAlreadyPassing;
        this.ongoingAGVPaths = ongoingAGVPaths;
        this.AGVSpeed = AGVSpeed;
        this.bufferForAGV = bufferForAGV;
        this.bufferSet = bufferSet;
        populationGen = INITIAL_POPULATION;
        taskNumber = tasks.length;
        AGVNumber  = ongoingAGVPaths.size();
        nodeSize = graph.toIntArray()[0].length;
        this.minDistance = minDistance;
        priorityChromosomeSet = new ArrayList<Integer[][]>();
        taskDistribution = new ArrayList<Double[]>();
        timeForFinishingTasks = new Double[AGVNumber];
        feasiblePathGrowth = new FeasiblePathGrowth(graph,transferGraphToConnectedGraph(graph,nodeSize));
        //初始化时间剩余列表
        initiateTimeLeftForFinishingTasks();
        //初始化人口
        initializeAGVPopulation();
//        System.out.println("已经经过的时间"+Matrix.Factory.importFromArray(timeForFinishingTasks));
        minTime = minDistance/AGVSpeed;
        conflictAvoid = new ConflictAvoid(AGVSpeed, AGVNumber, timeAlreadyPassing,
                minTime, minDistance, PENALTY_FOR_CONFLICT, SAFE_DISTANCE, graph, bufferSet, bufferForAGV);
    }

    //任务分配的任务优先级按传人的先后顺序
    public List<List<Integer>> singleObjectGenericAlgorithm(List<List<AGVRecord>> bestGenRecords) {


        //记录有多少次前后子代平均适应度函数稳定
        int stableTimes = 0;
        //记录进化了多少轮
        int evolveTimes = 1;

        //为了选取优秀的子代，将以距离为适应度的列表转换为每个车的1/distance的和的数列来进行轮盘法
        double[] adjustFitness;

        //开始进化
        //当连续10代适应度平均值没变化且循环次数大于最小代数，或者到达最大代数收敛结束进化,下面有break语句
        while (true) {
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

            //将保留的精英加入当前代
            if (priorityChromosomeSetElitist != null && taskDistributionElitist != null) {
                priorityChromosomeSet.add(priorityChromosomeSetElitist);
                taskDistribution.add(taskDistributionElitist);
                AGVPaths.add(AGVPathsElitist);
                AGVTimes.add(AGVTimesElitist);
                AGVFitness.add(AGVFitnessElitist);
                AGVRecords.add(AGVRecordsElitist);
            }

            //更新人口数目
            populationGen = priorityChromosomeSet.size();
            int previousPopulationGen = AGVFitness.size();

            //新增的子代的适应度，路径和时间的初始化
            List<double[]> localAGVFitness = initialLocalAGVFitness(previousPopulationGen,populationGen);

            //初始化该次循环下的路径，初始时间列表，AGV行驶记录，每一个子代的每一个车的路径
            List<List<List<Integer>>> localAGVPaths = initialLocalAGVPaths(previousPopulationGen,populationGen);

            List<double[]> localAGVTimes = initialLocalAGVTimes(previousPopulationGen,populationGen);

            //初始化每个子代每辆车的记录
            List<List<List<AGVRecord>>> localAGVRecord = initializeAGVRecords(previousPopulationGen,populationGen);
//            System.out.println("fitnessSize:"+localAGVFitness.size()+
//                    "PathSize:"+localAGVPaths.size()+"TimesSize:"+localAGVTimes.size()+"recordSize:"+localAGVRecord.size());



            //decode 任务分配 同时 decode 路径规划
            // taskSequence所有子代的任务顺序 list存每个个体的任务顺序，数组存储每个个体的任务顺序，如3，2，0，1，表示先做第3个任务
            List<Integer[]> taskSequence = new ArrayList<Integer[]>();
            getTaskSequence(taskSequence);

            //第count个任务
            int count;
            //第i代
            int i = 0;

            System.out.println("当前子代数：" + populationGen);

            for (List<List<Integer>> generationForAGVPaths : localAGVPaths) {
                //未分配完所有任务时，分配任务, 小车
                count = 0;
                while (count < taskNumber) {
                    //记录是第几个任务
                    //获取最早的AGV空闲小车
                    int AGVIndex = getEarliestAGV(localAGVTimes.get(i));
                    //最早的空闲车的路径
                    List<Integer> earliestAGV = generationForAGVPaths.get(AGVIndex);
                    //第一条路径的起始点的索引
                    int path1StartIndex = earliestAGV.size()-1;


                    //如果小车还未分配了任务，则获取小车的出发点
                    int startPoint = earliestAGV.get(path1StartIndex);
                    List<Integer> buffer = bufferSet.get(bufferForAGV[AGVIndex]);
                    //第几个任务,考虑到每次都新加进来的子代，从他们那一带的开始获取
                    int numberOfTask = taskSequence.get(i + previousPopulationGen)[count];
                    //如果当前车子在buffer第一位的话，起始点就是buffer和graph交点, 并且直接加入path中，为了统一性,更新下fitness
                    if (startPoint == buffer.get(buffer.size() - 2)) {
                        //变更下startPoint
                        startPoint = buffer.get(buffer.size() - 1);
                        earliestAGV.add(startPoint);
                        localAGVFitness.get(i)[AGVIndex] += minDistance;
                        //如果在buffer内的话，开始节点索引该加一来符合推移一个点作为起点的情况
                        path1StartIndex++;
                    }
                    //小车当前位置非法
                    else if (startPoint >= nodeSize) {
                        throw new IllegalArgumentException("小车的当前"+startPoint+"位置非法");
                    }
                    //获取第i个子代的第numberOfTask个（排在最前面的）任务的确切路径以及该路径的距离，用对应的优先度染色体
                    //从tasks中获取子代的最前的任务的第一个移动点（buffer和graph的交点）和第二移动个点
                    //注意要加上之前的人口数目才可以用到新的染色体的索引
                    double[] path1 = feasiblePathGrowth.feasiblePathGrowth(
                            startPoint, tasks[numberOfTask][0],
                            priorityChromosomeSet.get(previousPopulationGen + i)[numberOfTask * 2]);
                    double[] path2 = feasiblePathGrowth.feasiblePathGrowth(
                            tasks[numberOfTask][0], tasks[numberOfTask][1],
                            priorityChromosomeSet.get(previousPopulationGen + i)[numberOfTask * 2 + 1]);
                    int path1Length = path1.length;
                    int path2Length = path2.length;
                    double path1Distance = path1[path1Length - 1];
                    double path2Distance = path2[path2Length - 1];
                    double distance = path1Distance + path2Distance;
                    //更新小车的任务完成时间表，不用更新在buffer中的时间，因为一开始初始化时间的时候已经计算了
                    localAGVTimes.get(i)[AGVIndex] += distance / AGVSpeed;
                    //更新该小车的路径，不要重复了startPoint,索引从1开始
//                System.out.println("taskSequence: " +startPoint+","+tasks[taskSequence.get(i)[count]][0]+","+tasks[taskSequence.get(i)[count]][1]);
//                System.out.println("path1:"+Matrix.Factory.importFromArray(path1));
//                System.out.println("path2:"+Matrix.Factory.importFromArray(path2));
                    //第一个点是buffer和graph的交点之前就先放入,如果是-1就结束了该路径或者到了倒数第二位，最后一位是距离
                    for (int j = 1; (int) path1[j] != -1 && j < path1Length -1 ; j++) {
                        earliestAGV.add((int) path1[j]);
                    }
                    //更新第一条路径的终止节点，第二条路径的开始节点
                    int path1EndIndex = earliestAGV.size()-1;
                    int path2StartIndex = path1EndIndex;
                    //从一开始，避免重复path1的最后一个节点
                    for (int j = 1; (int) path2[j] != -1 && j < path2Length -1 ; j++) {
                        earliestAGV.add((int) path2[j]);
                    }
                    //更新第二条路径的终止节点
                    int path2EndIndex = earliestAGV.size()-1;
                    //此时不考虑从buffer中出来的距离了，这段距离对比较每个子代的相对的路径好坏没有影响。
                    AGVRecord firstAGVRecord = new AGVRecord(path1StartIndex,path1EndIndex,startPoint,tasks[numberOfTask][0],path1Distance,
                            numberOfTask*2,true);
                    AGVRecord secondAGVRecord = new AGVRecord(path2StartIndex,path2EndIndex,tasks[numberOfTask][0],tasks[numberOfTask][1],path2Distance,
                            numberOfTask*2+1,false);
                    //给该子代的AGV增加它的路径记录
                    localAGVRecord.get(i).get(AGVIndex).add(firstAGVRecord);
                    localAGVRecord.get(i).get(AGVIndex).add(secondAGVRecord);
                    //更新该子代该小车的适应度 最后一位存了距离，加上车子开出buffer的1点距离
                    localAGVFitness.get(i)[AGVIndex] += (path1[path1.length - 1] + path2[path2.length - 1]);
                    //调整其他还在buffer里头在该辆车后头的车子的位置，让他们都同时前进一步，不考虑进入record
                    adjustOtherAGVPositions(bufferForAGV[AGVIndex], generationForAGVPaths, localAGVFitness.get(i));
                    count++;
//                    System.out.println("第一步"+firstAGVRecord);
//                    System.out.println("第二步"+secondAGVRecord);
//                    System.out.println("路径"+earliestAGV);

                }
                //将在地图上闲置的小车遣返回buffer
                returnPathPlanning.returnAGVToBuffer(generationForAGVPaths,localAGVFitness.get(i),priorityChromosomeSet.get(i + previousPopulationGen)
                ,2 * taskNumber,localAGVTimes.get(i),localAGVRecord.get(i),feasiblePathGrowth,bufferForAGV,bufferSet,AGVSpeed,minDistance);
                i++;
            }
            System.out.println("路径" + localAGVPaths);
            pathImprovement.improvePath(localAGVRecord,priorityChromosomeSet,previousPopulationGen,localAGVPaths);

            //删除掉走了非法路径的子代
            deleteIllegalGeneration(localAGVFitness, localAGVPaths, localAGVTimes, localAGVRecord);
            //更新人口

            populationGen = priorityChromosomeSet.size();
            System.out.println("当前子代数：" + populationGen);

            //计算fitness，对出现碰撞的规划增加penalty，碰撞越多penalty越大
            conflictAvoid.conflictAvoid(localAGVPaths, localAGVFitness);

            //将子代和父代和在一块
            AGVFitness.addAll(localAGVFitness);
            AGVTimes.addAll(localAGVTimes);
            AGVPaths.addAll(localAGVPaths);
            AGVRecords.addAll(localAGVRecord);
            //为了选取优秀的子代，将以距离为适应度的列表转换为每个车的1/distance的和的数列来进行轮盘法
            adjustFitness = new double[populationGen];
            for (int j = 0; j < populationGen; j++) {
                for (int k = 0; k < AGVNumber; k++) {
                    adjustFitness[j] += AGVFitness.get(j)[k];
                }
                adjustFitness[j] = 1/adjustFitness[j];
            }

//           // 前一代的适应度变化值在第二次进化开始记录，当代的再第三代开始记录
//            if (evolveTimes > 2) {
//                currentMeanDistanceVariation = previousMeanDistance - currentMeanDistance;
//            }
//
//
            //方法不太适用，因为变化值都的范围太过于离散,(有碰撞)
            //在进化到第三代时候,再用current值更新previous值之前使用逻辑控制器
            //运用逻辑控制机器修改交叉和变异概率，这里记录下每一代的适应度变化值，用连续两代的变化值作为参数进行修改
//            if (evolveTimes > 2) {
//                crossoverProbability = fuzzyControlLogic.adjustCrossoverProbability(crossoverProbability,previousMeanDistanceVariation,currentMeanDistance);
//                mutationProbability = fuzzyControlLogic.adjustMutateProbability(mutationProbability,previousMeanDistanceVariation,currentMeanDistance);
//            }
//
//            System.out.println("当前距离变化" + currentMeanDistanceVariation + "  之前的距离变化" + previousMeanDistanceVariation);
//            if (evolveTimes > 1 ) {
//                previousMeanDistanceVariation = previousMeanDistance - currentMeanDistance;
//            }


//为了选取优秀的子代，将以距离为适应度的列表转换为每个车的1/distance的和的数列来进行轮盘法



            //采用精英保留策略，获取当前的精英，跳过选择交叉和变异直接保留且替换掉最差的子代
            int index = elitistPreservation(adjustFitness);
            System.out.println("最优子代适应度："+adjustFitness[index]);
            taskDistributionElitist = taskDistribution.get(index);
            priorityChromosomeSetElitist = priorityChromosomeSet.get(index);
            AGVFitnessElitist = AGVFitness.get(index);
            AGVTimesElitist = AGVTimes.get(index);
            AGVPathsElitist = AGVPaths.get(index);
            AGVRecordsElitist = AGVRecords.get(index);


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
                    AGVTimes.remove(j);
                    AGVRecords.remove(j);
                }
            }
            //更新populationGen
            populationGen = priorityChromosomeSet.size();
            previousMeanFitness = currentMeanFitness;
            previousMeanDistance = currentMeanDistance;
            adjustFitness = new double[populationGen];
            currentMeanFitness = 0;
            currentMeanDistance = 0;

            //再次计算适应度来看看稳定程度
            for (int j = 0; j < populationGen; j++) {
                for (int k = 0; k < AGVNumber; k++) {
                    adjustFitness[j] += AGVFitness.get(j)[k];
                }
                currentMeanDistance += adjustFitness[j];
                adjustFitness[j] = 1/adjustFitness[j];
                //计算当代的适应度
                currentMeanFitness += adjustFitness[j];
            }
//            System.out.println("adjust:"+Matrix.Factory.importFromArray(adjustFitness));
            //当前子代求平均值,以及平均距离
            currentMeanFitness /= populationGen;
            currentMeanDistance /= populationGen;
            //调整变异和交叉的概率
            System.out.println("当前距离"+currentMeanDistance+"之前距离"+previousMeanDistance);
            regulateProbability(previousMeanDistance,currentMeanDistance);

            System.out.println("当前变异概率" + mutationProbability + "  当前交叉概率" + crossoverProbability);

            System.out.println("当前适应度" + currentMeanFitness + "  之前的适应度" + previousMeanFitness + " 稳定次数"+stableTimes);

            //更新适应度稳定次数，如果不连续则重新开始计算
            if (currentMeanFitness - previousMeanFitness < currentMeanFitness*RELATIVE_ERROR &&
                    currentMeanFitness - previousMeanFitness > -currentMeanFitness*RELATIVE_ERROR) {
                stableTimes++;
            }
            else {
                stableTimes = 0;
            }

            System.out.println("当前子代数：" + populationGen);

            //当连续10代适应度平均值没变化且循环次数大于最小代数，或者到达最大代数收敛结束进化
            if (((stableTimes >= 10 && evolveTimes > MIN_GENERATION) || evolveTimes > MAX_GENERATION )) {
                break;
            }



            evolveTimes++;
            for (double[] fitness : AGVFitness) {
                System.out.println("AGV的适应度" + Matrix.Factory.importFromArray(fitness));
            }
//            for (List<List<Integer>> generationForAGVPaths : AGVPaths) {
//                System.out.println("当前子代的路径");
//                for (List<Integer> path : generationForAGVPaths) {
//                    System.out.println(path);
//                }
//            }
//            break;

        }

//        System.out.println(AGVRecords);










        //根据适应度函数找到最好的path
        Double[] sortFitnessArray = new Double[populationGen];
        for (int i = 0; i < populationGen; i++) {
            sortFitnessArray[i] = adjustFitness[i];
        }
        GenericSortAlgorithm.mergesort(sortFitnessArray);
        //确定是第几个子代的路径最佳
        int maxFitnessGeneration = 0;
        for (int i = 0; i < populationGen; i++) {
            if (adjustFitness[i] == sortFitnessArray[sortFitnessArray.length-1]) {
                maxFitnessGeneration = i;
            }
        }
        bestGenRecords.addAll(AGVRecords.get(maxFitnessGeneration));
        System.out.println(AGVRecords.get(maxFitnessGeneration));
        System.out.println("最佳路径" + AGVPaths.get(maxFitnessGeneration)+" 它的适应度" + adjustFitness[maxFitnessGeneration]
                +"它的距离" + 1/adjustFitness[maxFitnessGeneration]);
        return AGVPaths.get(maxFitnessGeneration);
    }






























































    //找到最优的当前子代个体的索引
    private int elitistPreservation(double[] AGVFitness) {
        int size = AGVFitness.length;
        Double[] DoubleAGVFitness = new Double[size];
        for (int i = 0; i < size; i++) {
            DoubleAGVFitness[i] = AGVFitness[i];
        }
        GenericSortAlgorithm.mergesort(DoubleAGVFitness);
        for (int i = 0; i < size; i++) {
            if (AGVFitness[i] == DoubleAGVFitness[size - 1]) {
                return i;
            }
        }
        throw new IllegalArgumentException("找不到最优子代");
    }


    //每一个子代的已经过去的时间,生成从当前的之前的子代个数到当前的种群个数个子代
    private List<double[]> initialLocalAGVTimes(int startIndex, int endIndex) {
        List<double[]> localAGVTimes = new ArrayList<double[]>();
        for (int i = startIndex; i < endIndex; i++) {
            double[] time = new double[timeForFinishingTasks.length];
            for (int j = 0; j < timeForFinishingTasks.length; j++) {
                time[j] = timeForFinishingTasks[j];
            }
            localAGVTimes.add(time);
        }
       return localAGVTimes;
    }
    //初始化车辆路径,生成从当前的之前的子代个数到当前的种群个数个子代
    private List<List<List<Integer>>> initialLocalAGVPaths(int startIndex, int endIndex) {
        List<List<List<Integer>>> localAGVPaths = new ArrayList<List<List<Integer>>>();
        for (int i = startIndex; i < endIndex; i++) {
            List<List<Integer>> generationForAGVPaths = new ArrayList<List<Integer>>();
            for (List<Integer> path : ongoingAGVPaths) {
                //按顺序拷贝每一辆车的路径。改成传值了/直接浅拷贝进去，生成地址不同但是元素指向相同的AGV，不改变原有元素所以可以这么做/
                List<Integer> AGV = new ArrayList<Integer>();
                for (Integer node : path) {
                    AGV.add(node.intValue());
                }
                generationForAGVPaths.add(AGV);
            }
            localAGVPaths.add(generationForAGVPaths);
        }
        return localAGVPaths;
    }

    //初始化每一个新增的子代的每个车的适应度,生成从当前的之前的子代个数到当前的种群个数个子代
    private List<double[]> initialLocalAGVFitness(int startIndex, int endIndex) {
        List<double[]> localAGVFitness = new ArrayList<double[]>();
        for (int i = startIndex; i < endIndex; i++) {
            double[] localAGVFitnessGeneration = new double[AGVNumber];
            for (int j = 0; j < AGVNumber; j++) {
                localAGVFitnessGeneration[j] = 0;
            }
            localAGVFitness.add(localAGVFitnessGeneration);
        }
        return localAGVFitness;
    }

    //初始化AGV的记录
    private List<List<List<AGVRecord>>> initializeAGVRecords(int startIndex, int endIndex) {
        List<List<List<AGVRecord>>> localAGVRecord = new ArrayList<List<List<AGVRecord>>>();
        for (int i = startIndex; i < endIndex; i++) {
            List<List<AGVRecord>> AGVRecord = new ArrayList<List<AGVRecord>>();
            for (int j = 0; j < AGVNumber; j++) {
                List<AGVRecord> recordForAGV = new ArrayList<AGVRecord>();
                AGVRecord.add(recordForAGV);
            }
            localAGVRecord.add(AGVRecord);
        }
        return localAGVRecord;
    }

    //根据经验方程适当的改变交叉和变异的概率,这里头的适应度应该是越小越好的
    //根据经验结果，控制crossover在0.3到0.9之间，mutate在0.1到0.4之间
    private void regulateProbability(double previousMeanFitness, double currentMeanFitness) {
        if ((previousMeanFitness/currentMeanFitness) -1 >= 0.1) {
            crossoverProbability = Math.min(crossoverProbability + 0.05,0.9);
            mutationProbability = Math.min(mutationProbability + 0.005,0.4);
        }
        if ((previousMeanFitness/currentMeanFitness) -1 <= 0.1) {
            crossoverProbability = Math.max(crossoverProbability - 0.05,0.3);
            mutationProbability = Math.max(mutationProbability - 0.005,0.1);
        }

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
    private void deleteIllegalGeneration(List<double[]> AGVFitness, List<List<List<Integer>>> AGVPaths, List<double[]> AGVTimes,List<List<List<AGVRecord>>> AGVRecords) {
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
                AGVRecords.remove(j);
                priorityChromosomeSet.remove(j);
                taskDistribution.remove(j);
            }
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
            ongoingPathLength = minDistance;
        }
        else {
            ongoingPathLength = graph.getAsDouble((long) futurePath[k][0], (long) futurePath[k][1]);
        }
//        System.out.println("第"+k+"车当前运行路径："+futurePath[k][0]+" "+futurePath[k][1]);
        return ongoingPathLength;
    }

    //由于排头车已经开走，调整该buffer中小车的位置,将AGVPaths上增加一个位置，也增加一个最小距离给适应度函数值,不改变时间
    private void adjustOtherAGVPositions(int bufferIndex, List<List<Integer>> generationForAGVPaths, double fitness[]) {
        for (List<Integer> path :generationForAGVPaths) {
            int endPosition = path.get(path.size()-1);
            //如果该车停靠在该buffer中,而不包括交接点,在其路径上添加下一个节点,并且增加其适应度
            if (bufferSet.get(bufferIndex).contains(endPosition) && endPosition!=bufferSet.get(bufferIndex).get(0)
                    && endPosition!=bufferSet.get(bufferIndex).get(bufferSet.get(bufferIndex).size()-1)) {
                path.add(bufferSet.get(bufferIndex).get(bufferSet.get(bufferIndex).indexOf(endPosition)+1));
                fitness[generationForAGVPaths.indexOf(path)] += minDistance;
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
            //将所有小车的时间推到最小时间精度再开始计算系统路径规划，便于避撞
            //这样无论是不是第一次启动系统或者是新加任务再开始系统，每个小车移动一个最小时间都在最小精度上
            else {
                int k = 0;
                while (k*minTime < timeAlreadyPassing[i]) {
                    k++;
                }
                timeAlreadyPassing[i] = k*minTime;
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
                        distance += (ongoingAGVPaths.get(i).size() - j - 1)*minDistance;
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
                timeForFinishingTasks[i] += (double)(minDistance)*(bufferPath.size() - 1 -
                        bufferPath.indexOf(ongoingAGVPaths.get(i).get(ongoingAGVPaths.get(i).size()-1)))/(AGVSpeed);
        }
    }

    private void initializeAGVPopulation() {
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
    }



    //给每个子代一个任务的序列，找到最小的数字代表的索引，其为第一个任务，这边暂时不简化逻辑，保持全部的子代都算一遍task sequence
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
                int point1;
                //就一个任务的时候，只能这么选了
                if (taskNumber==1) {
                    point1 = 0;
                }
                else {
                    point1 = random.nextInt(taskNumber - 1);
                }
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





    public  Matrix transferGraphToConnectedGraph(Matrix graph, int size) {
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
