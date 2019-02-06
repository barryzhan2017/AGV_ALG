//package org.spring.springboot.algorithmn.genetic_algorithm;
//
//import java.util.List;
//
//
///*
//入口点是地图与buffer的交点
// */
//public class ReturnPathPlanning {
//    //0.如果有车子是闲置在buffer中，则先给buffer扣去位置，并且在后续不考虑这些车
//    //1.将未规划入buffer的车子先规划到buffer入口，调整对应time，path，record，fitness等
//    //2.计算出每个buffer中到达入口的时间，主要是把已经进入buffer的时间扣掉那些进入部分的时间，不考虑闲置车
//    //3.选取最小的时间的车， 把它规划到buffer的最深处（将当前路径延伸过去），以此类推，先前规划的已经有进入buffer的路径只有可能增加，之前的规划的是对的
//    //将闲置在搬货区域的车强制开回buffer中，并且计算额外的适应度，startIndex是从priChromosome的什么位置开始, i表示第几代
//    public void returnAGVToBuffer(List<List<Integer>> generationForAGVPaths,
//                                   double[] fitness, Integer[][] priChromosome, int startIndex, double[] AGVTime, List<List<AGVRecord>> AGVRecord
//    ,FeasiblePathGrowth feasiblePathGrowth, Integer[] bufferForAGV,List<List<Integer>> bufferSet, double AGVSpeed, double min_distance) {
//        //如果有
//        int AGVNumber = generationForAGVPaths.size();
//        int bufferNumber = bufferSet.size();
//        //记录第几个车
//        int count = 0;
//        //把时间数组转换类型成Double数组,不需要真正改变数组 后续不再用到time数组
//        Double [] DoubleAGVTime = new Double[AGVTime.length];
//        for (int i = 0; i < AGVTime.length; i++) {
//            DoubleAGVTime[i] = AGVTime[i];
//        }
//        //记录，每个buffer被占用的个数,初始都是0
//        Integer[] occupiedNumberForBuffer = new Integer[bufferNumber];
//        for (int i = 0; i < bufferNumber; i++) {
//            occupiedNumberForBuffer[i] = 0;
//        }
//
//        //找到每个buffer对应的除了闲置在buffer里头的AGV存到下面，初始化全是-1
//        int[][] unreturnedAGVs = new int[bufferNumber][AGVNumber];
//        for (int i = 0; i < bufferNumber; i++) {
//            for (int j = 0; j < AGVNumber; j++) {
//                unreturnedAGVs[i][j] = -1;
//            }
//        }
//        //将非闲置的未规划进入buffer的小车规划到buffer入口点
//        for (List<Integer> AGVPath : generationForAGVPaths) {
//            //没有所谓的闲置的小车，闲置小车视为路径点为当前点的小车
//            //小车所在buffer号
//            int bufferNum = bufferForAGV[count];
//            //获取第count俩车的buffer图
//            List<Integer> buffer = bufferSet.get(bufferForAGV[count]);
//            //小车的最后一步
//            int endPoint = AGVPath.get(AGVPath.size() - 1);
//            //如果小车没有停到了buffer里头(包括入口点，不包括出口点),则让小车先开到buffer入口点,反之则跳过
//            if (buffer.contains(endPoint) && buffer.get(buffer.size() - 1) != endPoint) {
//                //计算出从运行的时候到最后到达入口点的时间,此时有同一个buffer假设点是连续的序号,如进入点是5，当前点是9，则路径为5，6，7，8，9
//                int numberOfNodeToEntrance = endPoint - buffer.get(0);
//                double timeForArrivingEntrance = numberOfNodeToEntrance * min_distance / AGVSpeed;
//                //扣掉这部分的时间，大家都是从到入口点开始计算时间
//                DoubleAGVTime[count]  -= timeForArrivingEntrance;
//            }
//
//            else {
//                //小车最后一个点在buffer之外，也在buffer的入口点之外，规划出该车回到buffer的路径
//                int pathStartIndex = AGVPath.size()-1;
//                int AGVStartPoint = AGVPath.get(pathStartIndex);
//                int bufferEntrancePoint = bufferSet.get(bufferNum).get(0);
//                double[] path = feasiblePathGrowth.feasiblePathGrowth(
//                        AGVStartPoint,
//                        bufferEntrancePoint,priChromosome[startIndex + count]);
//                int pathLength = path.length;
//                for (int k = 1; (int) path[k] != -1 && k < pathLength - 1; k++) {
//                    //结束了路径
//                    AGVPath.add((int) path[k]);
//                }
//                int pathEndIndex = AGVPath.size()-1;
//                double distance = path[pathLength-1];
//                AGVRecord.get(count).add(new AGVRecord(pathStartIndex,pathEndIndex, AGVStartPoint,
//                        bufferEntrancePoint,distance,startIndex+count,true,AGVSpeed));
//                DoubleAGVTime[count] += distance/AGVSpeed;
//                fitness[count] += distance;
//            }
//            //将在回去的或者还没回去的buffer的车的编号存入unreturnedAGV对应的buffer行中
//            int k = 0;
//            while (unreturnedAGVs[bufferForAGV[count]][k] != -1 && k < AGVNumber) {
//                k++;
//            }
//            unreturnedAGVs[bufferNum][k] = count;
//            count++;
//        }
//
//        //依据时间较小的车在数组前面给每个小车排序，如果unreturnedAGVs里头存在-1，排序方法自动跳过该buffer循环
//        for (int i = 0; i < bufferNumber; i++) {
//            GenericSortAlgorithm.insertSort(unreturnedAGVs[i],DoubleAGVTime);
//        }
//
//
//        //根据AGV到达时间把AGV开入所在buffer的最深处
//        navigateAGVToInnerBuffer(bufferNumber,AGVNumber,unreturnedAGVs,generationForAGVPaths,bufferSet,occupiedNumberForBuffer,fitness,min_distance);
//
//
//    }
//
//
//
//
//
//    //根据AGV到达时间把AGV开入所在buffer的最深处
//    public void navigateAGVToInnerBuffer(int bufferNumber, int AGVNumber, int[][] unreturnedAGVs,
//                                          List<List<Integer>> generationForAGVPaths, List<List<Integer>> bufferSet,
//                                          Integer[] occupiedNumberForBuffer, double[] fitness, double min_distance) {
//        //时间较小的车开到在buffer的靠出口位置
//        for (int i = 0; i < bufferNumber; i++) {
//            for (int j = 0; j < AGVNumber; j++) {
//                //如果该buffer的车都排好位置了
//                if (unreturnedAGVs[i][j] == -1) {
//                    break;
//                }
//                //注意有的车子可能是已经规划到了里头的车，从当前位置规划到最里头
//                //把车开总点数-（已经占用个数+2）次,增加占用个数, 调整fitness
//                else {
//                    int AGVIndex = unreturnedAGVs[i][j];
//                    //找到当前也就是最后一个点的位置
//                    List<Integer> AGVPath = generationForAGVPaths.get(AGVIndex);
//                    int currentIndex = AGVPath.get(AGVPath.size()-1);
//                    //当前车所在buffer路径
//                    List<Integer> currentBufferSet = bufferSet.get(i);
//                    //找到当前位置在buffer中的index
//                    int indexInBuffer = currentBufferSet.indexOf(currentIndex);
//                    for (int k = indexInBuffer+1; k <= currentBufferSet.size() - occupiedNumberForBuffer[i] -2; k++ ) {
//                        generationForAGVPaths.get(AGVIndex).add(currentBufferSet.get(k));
//                        fitness[AGVIndex] += min_distance;
//                    }
//                    occupiedNumberForBuffer[i]++;
//                }
//            }
//        }
//
//
//
//    }
//
//
//}
