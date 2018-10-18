package org.spring.springboot.algorithmn.GA;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathImprovement {




    //previous population zhi
    //提供给当前路径优化的方法，将当前路径中出发点和终点相同的部分归类，将这一部分中路径长度高于平均值的路替换成低于本身的路，并替换相应染色体,
    public void improvePath(List<List<List<AGVRecord>>> localAGVRecord, List<Integer[][]> priorityChromosomeSet,
                            int previousPopulationGen, List<List<List<Integer>>> localAGVPaths, double AGVSpeed) {
        //没有新的子代
        if (localAGVPaths.size()==0) {
            return;
        }
        //初始化HashMap,记录每个相同record的平均距离
        HashMap<AGVRecord,Double> identicalAGVRecordSet = initialize(localAGVRecord, previousPopulationGen, AGVSpeed);
        //车的数量
        int AGVNum = localAGVRecord.get(0).size();
        for (int i = previousPopulationGen; i < localAGVRecord.size(); i++) {
            List<List<AGVRecord>> generation = localAGVRecord.get(i);
            for (int j = 0; j < AGVNum; j++) {
                //第几个record
                int count = 0;
                for (AGVRecord record: generation.get(j)) {
                    if (record.getDistance() > identicalAGVRecordSet.get(record)) {
                        //用较小的距离的其他相同路径改变其路径以及它的染色体,第i个子代和第j个车辆第count个记录
                        changeToShorterDistance(record,localAGVRecord,previousPopulationGen,
                                localAGVPaths,priorityChromosomeSet, i, j, count);
                    }
                    count++;
                }
            }
        }
    }

    //改变当前的路径成较小的路径，并且更改相应染色体
    private void changeToShorterDistance(AGVRecord record, List<List<List<AGVRecord>>> localAGVRecord,
                                         int previousPopulationGen, List<List<List<Integer>>> localAGVPaths,
                                         List<Integer[][]> priorityChromosomeSet,  int recordGen,
                                         int AGVNumber,int count) {
        int AGVNum = localAGVRecord.get(0).size();
        for (int i = previousPopulationGen; i < localAGVRecord.size(); i++) {
            for (int j = 0; j < AGVNum; j++) {
                for (AGVRecord agvRecord : localAGVRecord.get(i).get(j) ) {
                    //找到一个相同的且距离更小的路径
                    if (agvRecord.equals(record) && agvRecord.getDistance() < record.getDistance()) {
                        //修改染色体的信息
                        priorityChromosomeSet.get(recordGen)[record.getIndexInPriorityChromosome()]
                                = priorityChromosomeSet.get(i)[agvRecord.getIndexInPriorityChromosome()];
                        List<Integer> path = localAGVPaths.get(recordGen).get(AGVNumber);
                        List<Integer> newPath = localAGVPaths.get(i).get(j);
                        //删除路径
//                        System.out.println("recordGen,AGVNumber:"+recordGen+","+AGVNumber);
//                        System.out.println("path" + path);
//                        System.out.println("record" + record);
                        for (int k = record.getPathEndIndex(); k >= record.getPathStartIndex(); k--) {
                            path.remove(k);
                        }
                        //新的路径的大小
                        int newPathSize = agvRecord.getPathEndIndex()-agvRecord.getPathStartIndex();
                        //旧的路径大小
                        int oldPathSize = record.getPathEndIndex()-record.getPathStartIndex();
                        //改了路径之后的新的终点索引
                        int newEndNodeIndex = newPathSize + record.getPathStartIndex();
                        record.setPathEndIndex(newEndNodeIndex);
                        //由于添加了新路，把这些set在这个record之后的record的索引修改
                        List<AGVRecord> newRecordList = localAGVRecord.get(recordGen).get(AGVNumber);
                        //将他们后头的record的index修改
                        for (int k = count + 1; k < newRecordList.size(); k++) {
                            AGVRecord newAGVRecord = newRecordList.get(k);
                            newAGVRecord.setPathStartIndex(newPathSize - oldPathSize + newAGVRecord.getPathStartIndex());
                            newAGVRecord.setPathEndIndex(newPathSize - oldPathSize + newAGVRecord.getPathEndIndex());
                        }
                        //加上路径
                        for (int k = 0; k <= newPathSize; k++) {
                            path.add(k + record.getPathStartIndex(), newPath.get(agvRecord.getPathStartIndex()+k));
                        }
                        //修改完退出当前该record的改善
                        return;
                    }
                }
            }
        }
    }


    //将所有的子代的所有的车辆的所有record加入一个不重复的map之中,并且算出平均的距离放到value
    public HashMap<AGVRecord,Double> initialize(List<List<List<AGVRecord>>> localAGVRecord,
                                                int previousPopulationGen, double AGVSpeed) {
        //key是相同的record，value现在是出现次数
        HashMap<AGVRecord,Double> differentAGVRecordMap = new HashMap<AGVRecord,Double>();
        for (int i = previousPopulationGen; i < localAGVRecord.size(); i++) {
            List<List<AGVRecord>> generation = localAGVRecord.get(i);
            for (List<AGVRecord> AGVRecord : generation) {
                for (AGVRecord record: AGVRecord) {
                    if (!differentAGVRecordMap.containsKey(record)) {
                        //初始化重复次数,避免对象的污染，新建一个对象
                        AGVRecord agvRecord = new AGVRecord(-1,-1,record.getStartNode(),
                                record.getEndNode(),record.getDistance(),-1,false, AGVSpeed);
                        differentAGVRecordMap.put(agvRecord,1.0);
                    }
                    //如果是相同的路径，则把该路径相同的记录的路径增加上去,把重复次数加上一次
                    else {
                        differentAGVRecordMap.put(record,differentAGVRecordMap.get(record)+1);
                        for (AGVRecord differentRecord : differentAGVRecordMap.keySet()) {
                            if (differentRecord.equals(record)) {
                                differentRecord.setDistance(differentRecord.getDistance() + record.getDistance());
                            }
                        }
                    }
                }
            }
        }
        //求出key的平均距离放在value中
        for (AGVRecord differentRecord : differentAGVRecordMap.keySet()) {
            differentAGVRecordMap.put(differentRecord,differentRecord.getDistance()/differentAGVRecordMap.get(differentRecord));
        }
        return differentAGVRecordMap;
    }
}
