package org.spring.springboot.algorithmn.genetic_algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.conflict_free_routing.Routing;
import org.spring.springboot.algorithmn.exception.NoAGVInTheBuffer;


import java.util.*;

//前端传的ongoingAGVPaths不要有个-1位
//前端传的timeAlreadyPassing用-1表示空闲
public class AGV_GA {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private double[][] graph;
    private int populationGen;
    // Priority of tasks have maintain in the array. First element for first priority.
    private Integer[][] tasks; // Each start and end for each task
    private Double[] timeAlreadyPassing; //How long the AGV has gone. -1 means the AGV becomes idle.
    private Double[] timeForFinishingTasks;//To store how long the AGV need to finish the task
    private List<List<Path>> ongoingAGVPaths; //Ongoing AGV path
    private List<List<Integer>> bufferSet; //All of the buffer and its path
    private Integer[] bufferForAGV; //Buffer number for each AGV
    private List<Double[]> taskDistribution;//Generation for task distribution
    //path at least contain one node indicating the static position
    private List<List<List<Path>>> AGVPaths = new ArrayList<>(); //Path for each AGV in each generation
    private List<double[]> AGVTimes = new ArrayList<>(); //Gone time for each generation
    private List<double[]> AGVFitness = new ArrayList<>(); //Fitness for each generation


    private Double[] taskDistributionElitist;//Best generation for task elitist
    private double[] AGVFitnessElitist;
    private double[] AGVTimesElitist;
    private List<List<Path>> AGVPathsElitist;


    private Random random = new Random();
    private int taskNumber;
    private int sizeOfAGV;
    private double speedOfAGV;
    private double distanceOfBuffer;
    private int nodeSize;
    private double crossoverProbability = 0.7;
    private double mutationProbability = 0.2;

    //Used to unify the probability for all mutation and crossover
    private double[] crossoverProbabilityArray;
    private double[] mutateProbabilityArray;
    private double currentMeanFitness = 0;
    private double previousMeanFitness = 0;
    private final int INITIAL_POPULATION = 50;
    private final int MIN_GENERATION = 50;
    private final int MAX_GENERATION = (int)(MIN_GENERATION*1.5);
    private final double PENALTY_FOR_CONFLICT = 99999;
    private final double RELATIVE_ERROR = 0.001;//收敛的相对误差，小于这个则表示稳定
    private final int INITIAL_CAPACITY = 30;


    public AGV_GA(double[][] graph, Integer[][] tasks, Double[] timeAlreadyPassing, List<List<Path>> ongoingAGVPaths,
                  double speedOfAGV, List<List<Integer>> bufferSet, Integer[] bufferForAGV) {
        this.graph = graph;
        this.tasks = tasks;
        this.timeAlreadyPassing = timeAlreadyPassing;
        this.ongoingAGVPaths = ongoingAGVPaths;
        this.speedOfAGV = speedOfAGV;
        this.bufferForAGV = bufferForAGV;
        this.bufferSet = bufferSet;
        this.distanceOfBuffer = CommonConstant.BUFFER_PATH_LENGTH;
        populationGen = INITIAL_POPULATION;
        taskNumber = tasks.length;
        sizeOfAGV  = ongoingAGVPaths.size();
        nodeSize = graph[0].length;
        taskDistribution = new ArrayList<>();
        timeForFinishingTasks = new Double[sizeOfAGV];
        initiateTimeLeftForFinishingTasks();
        initializeAGVPopulation();
    }

    public List<List<Path>> singleObjectGenericAlgorithm() throws NoAGVInTheBuffer {
        //Record how many times the generation has been stable.
        int stableTimes = 0;
        //Record how many times it have evolved
        int evolveTimes = 1;
        // When 10 consecutive mean fitness has been stable and the looping times exceeds the min generation looping times or it
        // has reached the max generation looping times, evolution stops.
        double[] totalFitness;
        while (true) {
            logger.info("Evolve to {} Generation", evolveTimes);
            initializeCrossoverProbability();
            //crossover taskDistribution
            crossoverScheduling();
            //Update population
            populationGen = taskDistribution.size();
            initializeMutateProbability();
            //Mutate taskDistribution
            mutateScheduling();

            //Update elitist
            if (taskDistributionElitist != null) {
                taskDistribution.add(taskDistributionElitist);
                AGVPaths.add(AGVPathsElitist);
                AGVTimes.add(AGVTimesElitist);
                AGVFitness.add(AGVFitnessElitist);
            }

            //Update population
            populationGen = taskDistribution.size();
            int previousPopulationGen = AGVFitness.size();

            //新增的子代的适应度，路径和时间的初始化
            List<double[]> localAGVFitness = initialLocalAGVFitness(previousPopulationGen, populationGen);

            //初始化该次循环下的路径，初始时间列表，AGV行驶记录，每一个子代的每一个车的路径
            List<List<List<Path>>> localAGVPaths = initialLocalAGVPaths(previousPopulationGen, populationGen);

            List<double[]> localAGVTimes = initialLocalAGVTimes(previousPopulationGen, populationGen);
            // taskSequence所有子代的任务顺序 list存每个个体的任务顺序，数组存储每个个体的任务顺序，如3，2，0，1，表示先做第3个任务
            List<Integer[]> taskSequence = new ArrayList<>();
            getTaskSequence(taskSequence);
            //Record the index of ongoing task
            int countOfGeneration = 0;
            logger.info("Number of Generation is {}", populationGen);
            for (List<List<Path>> generationForAGVPaths : localAGVPaths) {
                int countOfTasks = 0;
                Routing routing = new Routing(graph, bufferSet, speedOfAGV, INITIAL_CAPACITY);
                PathPlanning pathPlanning = new PathPlanning(sizeOfAGV, PENALTY_FOR_CONFLICT, speedOfAGV, distanceOfBuffer, routing);
                double[] currentAGVsTime = localAGVTimes.get(countOfGeneration);
                double[] currentAGVsFitness = localAGVFitness.get(countOfGeneration);
                // Set ongoing AGVs path take-up to the routing
                for (int i = 0; i < sizeOfAGV; i++) {
                    if (timeAlreadyPassing[i] != -1) {
                        List<Path> path = generationForAGVPaths.get(i);
                        routing.setCurrentPathsToTimeWindows(path, timeAlreadyPassing[i], i);
                        //Set this ongoing AGV as the backing AGV to check later and delete the last backing path in the buffer
                        deleteLastBufferPath(path);
                        pathPlanning.setBackingAGV(i);
                    }
                }
                routing.setFreeTimeWindow();
                while (countOfTasks < taskNumber) {
                    int indexOfAGV = getEarliestAGV(currentAGVsTime);
                    List<Integer> buffer = bufferSet.get(bufferForAGV[indexOfAGV]);
                    List<Path> earliestAGVPath = generationForAGVPaths.get(indexOfAGV);
                    //If the AGV is returning to the buffer, plan the path to the second to the last of the buffer first
                    if (pathPlanning.isReturning(indexOfAGV)) {
                        pathPlanning.returnAGVToBuffer(indexOfAGV, buffer, earliestAGVPath, buffer.size() - 2);
                    }
                    int pathStartIndex = earliestAGVPath.size() - 1;
                    Path startPath = earliestAGVPath.get(pathStartIndex);
                    int numberOfTask = taskSequence.get(countOfGeneration + previousPopulationGen)[countOfTasks];
                    logger.info("Count Of Tasks {} for AGV {}", numberOfTask, indexOfAGV);
                    List<Path> paths1 = pathPlanning.getPath(tasks[numberOfTask][0],
                            startPath.getEndNode(), indexOfAGV, currentAGVsFitness, currentAGVsTime);
                    // No Feasible path, quit the task distribution
                    if (paths1.isEmpty()) {
                        break;
                    }
                    //Get path2
                    List<Path> paths2 = pathPlanning.getPath(tasks[numberOfTask][1],
                            paths1.get(paths1.size() - 1).getEndNode(), indexOfAGV, currentAGVsFitness, currentAGVsTime);
                    if (paths2.isEmpty()) {
                        break;
                    }
                    earliestAGVPath.addAll(paths1);
                    earliestAGVPath.addAll(paths2);
                    //Adjust all the other AGVs in the buffer, to move them forward once and change the reserved and free time window corresponding
                    pathPlanning.adjustOtherAGVPositions(buffer, generationForAGVPaths, currentAGVsFitness);
                    countOfTasks++;
                    //If the next earliest AGV having jobs to get down is not the same one, drive the AGV back to start of the buffer instead of being stuck in the road
                    //Increase time to the moment the AGV comes to the second to the last node in the buffer.
                    if (!(getEarliestAGV(currentAGVsTime) == indexOfAGV && countOfTasks < taskNumber)) {
                        Path lastPath = paths2.get(paths2.size() - 1);
                        int startNode = lastPath.getEndNode();
                        int endNode = buffer.get(0);
                        List<Path> pathToStartOfBuffer = pathPlanning.getPath(endNode,
                                startNode, indexOfAGV, currentAGVsFitness, currentAGVsTime);
//                        //The reserved time window to enter the buffer should be released for other AGV to pass
//                        pathPlanning.releaseNode(endNode);
                        earliestAGVPath.addAll(pathToStartOfBuffer);
                        pathPlanning.setBackingAGV(indexOfAGV);
                        int numberOfBufferToCross = buffer.size() - 2;
                        currentAGVsTime[indexOfAGV] += timeForCrossingBuffers(numberOfBufferToCross);
                        currentAGVsFitness[indexOfAGV] += timeForCrossingBuffers(numberOfBufferToCross);
                    }
                }
                //Navigate returning AGV back to their proper position in the buffer
                pathPlanning.navigateAGVsToInnerBuffer(generationForAGVPaths, bufferSet, bufferForAGV, currentAGVsTime);
                countOfGeneration++;
            }
            logger.info("Path is {}", localAGVPaths);
            //Put children and parents generation together
            AGVFitness.addAll(localAGVFitness);
            AGVTimes.addAll(localAGVTimes);
            AGVPaths.addAll(localAGVPaths);
            // Calculate all of the fitness for each generation. Use the 1/totalFitness to express the fitness.
            totalFitness = new double[populationGen];
            for (int j = 0; j < populationGen; j++) {
                double[] fitnessForThisGeneration = AGVFitness.get(j);
                for (int k = 0; k < sizeOfAGV; k++) {
                    totalFitness[j] += fitnessForThisGeneration[k];
                }
                totalFitness[j] = 1 / totalFitness[j];
            }


            //Preserve the elitist, skip mutation and crossover
            int index = elitistPreservation(totalFitness);
            logger.info("Best generation fitness is {}", totalFitness[index]);
            taskDistributionElitist = taskDistribution.get(index);

            AGVFitnessElitist = AGVFitness.get(index);
            AGVTimesElitist = AGVTimes.get(index);
            AGVPathsElitist = AGVPaths.get(index);

            //Use set to choose the survival generation left
            Set<Integer> survival = new HashSet<>();
            for (int j = 0; j < populationGen; j++) {
                survival.add(rouletteSelect(totalFitness));
            }
            //Remove the dead generation in a reverse order to avoid index chaos
            for (int j = populationGen - 1; j >= 0; j--) {
                if (!survival.contains(j)) {
                    taskDistribution.remove(j);
                    AGVFitness.remove(j);
                    AGVPaths.remove(j);
                    AGVTimes.remove(j);
                }
            }
            //Update populationGen
            populationGen = taskDistribution.size();
            previousMeanFitness = currentMeanFitness;
            totalFitness = new double[populationGen];
            currentMeanFitness = 0;

            double currentTotalFitness = 0;
            for (int j = 0; j < populationGen; j++) {
                double[] fitnessForThisGeneration = AGVFitness.get(j);
                for (int k = 0; k < sizeOfAGV; k++) {
                    totalFitness[j] += fitnessForThisGeneration[k];
                }
                totalFitness[j] = 1 / totalFitness[j];
                //Calculate the current fitness
                currentTotalFitness += totalFitness[j];
            }
            //Current mean fitness calculation
            currentMeanFitness = currentTotalFitness / populationGen;
            //调整变异和交叉的概率
            regulateProbability(previousMeanFitness, currentMeanFitness);

            logger.info("Current mutation probability is {}, current crossover probability is {}", mutationProbability, crossoverProbability);

            logger.info("Current fitness is {}, previous fitness is {}", currentMeanFitness, previousMeanFitness);

            logger.info("Stable times is {}", stableTimes);
            //If the variation between current and previous mean fitness is less than relative error, the model is stable.
            if (currentMeanFitness - previousMeanFitness < currentMeanFitness * RELATIVE_ERROR &&
                    currentMeanFitness - previousMeanFitness > -currentMeanFitness * RELATIVE_ERROR) {
                stableTimes++;
            }
            else {
                stableTimes = 0;
            }

            logger.info("Current population is {}", populationGen);

            if (((stableTimes >= 10 && evolveTimes > MIN_GENERATION) || evolveTimes > MAX_GENERATION )) {
                break;
            }

            evolveTimes++;
            for (double[] fitness : AGVFitness) {
                logger.info("AGV Fitness is {}", fitness);
            }
        }

        Double[] sortFitnessArray = new Double[populationGen];
        for (int i = 0; i < populationGen; i++) {
            sortFitnessArray[i] = totalFitness[i];
        }
        Arrays.sort(sortFitnessArray);
        //Specify which generation is best
        int maxFitnessGeneration = 0;
        for (int i = 0; i < populationGen; i++) {
            if (totalFitness[i] == sortFitnessArray[sortFitnessArray.length-1]) {
                maxFitnessGeneration = i;
            }
        }
        logger.info("Last Stable times is {}", stableTimes);
        logger.info("Best route is {}, its fitness is {}, its time is {}", AGVPaths.get(maxFitnessGeneration), totalFitness[maxFitnessGeneration], 1 / totalFitness[maxFitnessGeneration]);
        List<List<Path>> optimalPaths = AGVPaths.get(maxFitnessGeneration);
        //Delete redundant path
        return deleteUnnecessaryPaths(optimalPaths);
    }

    private void deleteLastBufferPath(List<Path> path) {
        int sizeOfPath = path.size();
        for (int i = sizeOfPath - 1; i >= 0; i--) {
            //Find the path in buffer
            if (path.get(i).getEndNode() >= nodeSize) {
                path.remove(i);
            }
            //Get out of the buffer already so break.
            else {
                break;
            }
        }
    }

    //Delete the path with same start node and end node and 0 time to travel. (unnecessary path)
    private List<List<Path>> deleteUnnecessaryPaths(List<List<Path>> optimalPaths) {
        List<List<Path>> adjustOptimalPaths = new ArrayList<>();
        for (List<Path> paths : optimalPaths) {
            List<Path> adjustPaths = new ArrayList<>();
            for (Path path: paths) {
                if (!(path.getTime() == 0 && path.getStartNode() == path.getEndNode())) {
                    adjustPaths.add(path);
                }
            }
            adjustOptimalPaths.add(adjustPaths);
        }
        return adjustOptimalPaths;
    }

    //找到最优的当前子代个体的索引
    private int elitistPreservation(double[] AGVFitness) {
        int size = AGVFitness.length;
        Double[] AGVFitnessCopy = new Double[size];
        for (int i = 0; i < size; i++) {
            AGVFitnessCopy[i] = AGVFitness[i];
        }
        Arrays.sort(AGVFitnessCopy);
        for (int i = 0; i < size; i++) {
            if (AGVFitness[i] == AGVFitnessCopy[size - 1]) {
                return i;
            }
        }
        throw new IllegalArgumentException("找不到最优子代");
    }


    //每一个子代的已经过去的时间,生成从当前的之前的子代个数到当前的种群个数个子代
    private List<double[]> initialLocalAGVTimes(int startIndex, int endIndex) {
        List<double[]> localAGVTimes = new ArrayList<>();
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
    private List<List<List<Path>>> initialLocalAGVPaths(int startIndex, int endIndex) {
        List<List<List<Path>>> localAGVPaths = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            List<List<Path>> generationForAGVPaths = new ArrayList<>();
            for (List<Path> path : ongoingAGVPaths) {
                //直接浅拷贝进去，生成地址不同但是元素指向相同的AGV，不改变原有元素所以可以这么做
                List<Path> pathOfAGV = new ArrayList<>();
                for (Path path1 : path) {
                    pathOfAGV.add(path1);
                }
                generationForAGVPaths.add(pathOfAGV);
            }
            localAGVPaths.add(generationForAGVPaths);
        }
        return localAGVPaths;
    }

    //初始化每一个新增的子代的每个车的适应度,生成从当前的之前的子代个数到当前的种群个数个子代
    private List<double[]> initialLocalAGVFitness(int startIndex, int endIndex) {
        List<double[]> localAGVFitness = new ArrayList<double[]>();
        for (int i = startIndex; i < endIndex; i++) {
            double[] localAGVFitnessGeneration = new double[sizeOfAGV];
            for (int j = 0; j < sizeOfAGV; j++) {
                localAGVFitnessGeneration[j] = 0;
            }
            localAGVFitness.add(localAGVFitnessGeneration);
        }
        return localAGVFitness;
    }


    //According to empirical function，adjust crossover rate between 0.3 and 0.9, mutate rate between 0.1 and 0.4.
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

    // 初始化每个AGV计算还需要多长时间小车完成任务
    // 计算时间包括AGV到buffer中停靠点以及从停靠点到buffer和graph交点的前一个点的时间（后面的点需要routing所以时间不固定，无法估计）
    private void initiateTimeLeftForFinishingTasks() {
        //初始化剩余时间数组
        for (int i = 0; i < timeForFinishingTasks.length; i++) {
            timeForFinishingTasks[i] = (double)-1;
        }
        //如果小车闲置(为-1)，给数组赋值0
        for (int i = 0; i < timeAlreadyPassing.length; i++) {
            if (timeAlreadyPassing[i] == -1) {
                timeForFinishingTasks[i] = (double)0;
            }
        }
        for (int i = 0; i < sizeOfAGV; i++) {
            //对于没有闲置的车,计算他们还要多长时间完成任务，整个路程的时间减去已经开了多久
            if (timeForFinishingTasks[i] != 0)  {
                timeForFinishingTasks[i] = (double)0;
                List<Path> paths = ongoingAGVPaths.get(i);
                for (int j = 0; j < paths.size(); j++) {
                    //AGV is returning to buffer
                    timeForFinishingTasks[i] += paths.get(j).getTime() + CommonConstant.CROSSING_DISTANCE / speedOfAGV;
                }
                timeForFinishingTasks[i] = timeForFinishingTasks[i] - timeAlreadyPassing[i];
            }
        }
        for (int i = 0; i < sizeOfAGV; i++) {
            //Calculate each AGV's time used for going to the second to the last node of the buffer and add it to the time for finishing the task
            List<Path> paths = ongoingAGVPaths.get(i);
            List<Integer> bufferPath = bufferSet.get(bufferForAGV[i]);
            Path endPath = paths.get(paths.size() - 1);
            int numberOfBufferToCross = bufferPath.size() - 2 - bufferPath.indexOf(endPath.getEndNode());
            timeForFinishingTasks[i] += timeForCrossingBuffers(numberOfBufferToCross);

        }
    }

    /**
     * Calculate the time needed to cross several buffers
     * @param numberOfBufferToCross Number of buffer the AGV will cross
     * @return Time to cross these buffers
     */
    private double timeForCrossingBuffers(int numberOfBufferToCross) {
        return numberOfBufferToCross * (distanceOfBuffer + CommonConstant.CROSSING_DISTANCE) / speedOfAGV;
    }

    private void initializeAGVPopulation() {
        //初始子代的建立
        //encode 任务分配
        for (int i = 0; i < populationGen; i++) {
            Double[] task = new Double[taskNumber];
            Integer[][] priChromosome = new Integer[taskNumber*2 + sizeOfAGV][nodeSize];
            for (int j = 0; j < taskNumber; j++) {
                //j代表第几个任务，值对应哪一个车
                task[j] = random.nextDouble();
            }
            taskDistribution.add(task);
        }
    }

    //给每个子代一个任务的序列，找到最小的数字代表的索引，其为第一个任务，这边暂时不简化逻辑，保持全部的子代都算一遍task sequence
    private void getTaskSequence(List<Integer[]> taskSequence) {
        for (Double[] tasksCode : taskDistribution) {
            Double[] sortArray = new Double[taskNumber];
            System.arraycopy(tasksCode, 0, sortArray, 0, taskNumber);
            Arrays.sort(sortArray);
            Integer[] sequence = new Integer[taskNumber];
            //最小的最早开始做，任务序号存在sequence中
            for (int i = 0; i < taskNumber; i++) {
                for (int j = 0; j < taskNumber; j++) {
                    if (tasksCode[j].equals(sortArray[i])) {
                        sequence[i] = j;
                        break;
                    }
                }
            }
            taskSequence.add(sequence);
        }
    }


    //Find the earliest AGV that has finished the job randomly
     Integer getEarliestAGV(double[] timeForFinishingTasks) {
        double[] sortTimeArray = new double[timeForFinishingTasks.length];
        System.arraycopy(timeForFinishingTasks, 0, sortTimeArray, 0, timeForFinishingTasks.length);
        Arrays.sort(sortTimeArray);
        //Store all of the earliest AGV in a list.
        List<Integer> possibleIndexesOfAGV = new ArrayList<>();
        for (int i = 0; i < timeForFinishingTasks.length; i++) {
            if (timeForFinishingTasks[i] == sortTimeArray[0]) {
                possibleIndexesOfAGV.add(i);
            }
        }
        if (possibleIndexesOfAGV.isEmpty()) {
            return -1;
        }
        else {
            int index = random.nextInt(possibleIndexesOfAGV.size());
            return possibleIndexesOfAGV.get(index);
        }
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

                Double[] newGeneration1 = new Double[taskNumber];
                Double[] newGeneration2 = new Double[taskNumber];
                Double[] parent1 = taskDistribution.get(i);
                Double[] parent2 = taskDistribution.get(i + 1);
                for (int j = 0; j < point1; j++) {
                    newGeneration1[j] = parent1[j];
                    newGeneration2[j] = parent2[j];
                }
                for (int k = point2 + 1; k < taskNumber; k++) {
                    newGeneration1[k] = parent1[k];
                    newGeneration2[k] = parent2[k];
                }
                for (int j = point1; j <= point2; j++) {
                    newGeneration1[j] = parent2[j];
                    newGeneration2[j] = parent1[j];
                }
                taskDistribution.add(newGeneration1);
                taskDistribution.add(newGeneration2);
            }
        }
    }


    // Returns the selected index based on the weights(probabilities)
    int rouletteSelect(double[] weight) {
        // calculate the total weight
        double weightSum = 0;
        for(int i = 0; i < weight.length; i++) {
            weightSum += weight[i];
        }
        // Get a random value
        double value = randUniformPositive() * weightSum;
        // Locate the random value based on the weights
        for(int i = 0; i < weight.length; i++) {
            value -= weight[i];
            if(value < 0)
                return i;
        }
        // when rounding errors occur, we return the last item's index
        logger.error("Roulette select cannot get valid value");
        return weight.length - 1;
    }

    // Returns a uniformly distributed double value between 0.0 and 1.0
    double randUniformPositive() {
        return new Random().nextDouble();
    }


}
