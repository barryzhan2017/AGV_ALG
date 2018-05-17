package org.spring.springboot.controller;


import org.omg.PortableInterceptor.INACTIVE;
import org.spring.springboot.algorithmn.GA.AGVRecord;
import org.spring.springboot.algorithmn.GA.AGV_GA;
import org.spring.springboot.algorithmn.preprocess.PreprocessData;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.ujmp.core.Matrix;

import java.util.*;

@RestController
public class GeneticAlogrithmControll {
    private final int MAX_EDGE = 999999;

    @RequestMapping(value = "/api/genetic", method = RequestMethod.POST)
    public ModelMap genetic(@RequestBody Map payload) {
        System.out.println(payload.size());
        Map map = (Map)payload.get("data");
        PreprocessData preprocessData = new PreprocessData();

        Matrix graph = preprocessData.createGraphFromJson
                ((List<Map>) map.get("startNode"),(List<Map>)map.get("endNode"),(List<Map>)map.get("nodeDistance"),9,
              //          ,(Integer)map.get("numberOfGraphNode"),
                        MAX_EDGE);
        Integer[][] tasks = preprocessData.getTasksFromJson((List<Map>)map.get("tasks"));
        Double[] timeAlreadyPass = preprocessData.getTimeFromJson((List<Map>)map.get("time"));
        List<List<Integer>> AGVs = preprocessData.getPaths((List<List<Map>>)map.get("paths"));
        List<List<Integer>> bufferSet = preprocessData.getPaths((List<List<Map>>)map.get("bufferSet"));
        Integer [] bufferForAGV = preprocessData.getBufferForAGVFromJson((List<Map>)map.get("bufferForAGV"));
        double AGVSpeed = preprocessData.getDoubleData(map.get("speed"));
        double min_distance = preprocessData.getDoubleData(map.get("precision"));

        //构建一个任务集合的映射，从任务编号映射到真实任务序号，因为真实任务根据任务数量拆成了子任务，编号就乱了
        HashMap<Integer,Integer> taskMap = null;
        taskMap = preprocessData.getTaskMap((List<Map>)map.get("tasks"));


        System.out.print("测试图"+graph);
        System.out.print("节点数目"+map.get("numberOfGraphNode"));
        System.out.println("任务路线"+Matrix.Factory.importFromArray(tasks));
        System.out.println("时间"+Matrix.Factory.importFromArray(timeAlreadyPass));
        System.out.println("小车路径"+AGVs);
        System.out.println("buffer路径"+bufferSet);
        System.out.println("小车所归属的buffer"+Matrix.Factory.importFromArray(bufferForAGV));
        System.out.println("小车速度"+AGVSpeed+" 地图精度"+min_distance);

        AGV_GA agv_ga = new AGV_GA(graph,tasks,timeAlreadyPass,AGVs,AGVSpeed,min_distance,bufferSet,bufferForAGV);


////
//        Matrix testGraph = Matrix.Factory.ones(6,6);
//        testGraph = testGraph.times(MAX_EDGE);
//        testGraph.setAsInt(4,0,3);
//        testGraph.setAsInt(5,0,2);
//        testGraph.setAsInt(6,0,4);
//        testGraph.setAsInt(5,1,3);
//        testGraph.setAsInt(4,2,1);
//        testGraph.setAsInt(6,3,5);
//        testGraph.setAsInt(4,4,5);
//        testGraph.setAsInt(4,3,0);
//        testGraph.setAsInt(5,2,0);
//        testGraph.setAsInt(6,4,0);
//        testGraph.setAsInt(5,3,1);
//        testGraph.setAsInt(4,1,2);
//        testGraph.setAsInt(4,5,4);
//        testGraph.setAsInt(6,5,3);
//
//
//
//        List<List<Integer>> bufferSet = new ArrayList<List<Integer>>();
//        Integer[] bufferForAGV = {0,1,1};
//        List<Integer> bufferPath = new ArrayList<Integer>();
//        bufferPath.add(4);
//        bufferPath.add(6);
//        bufferPath.add(7);
//        bufferPath.add(8);
//        bufferPath.add(9);
//        bufferPath.add(5);
//        bufferSet.add(bufferPath);
//
//        List<Integer> bufferPath1 = new ArrayList<Integer>();
//        bufferPath1.add(1);
//        bufferPath1.add(10);
//        bufferPath1.add(11);
//        bufferPath1.add(12);
//        bufferPath1.add(13);
//        bufferPath1.add(2);
//        bufferSet.add(bufferPath1);
//
//
//        Integer [][] tasks = {{1,2},{2,3},{4,0}};
//        List<List<Integer>> AGVs = new ArrayList<List<Integer>>();//agv的位置, 1号车在0点，2号车在1点
//        List<Integer> position1 = new ArrayList<Integer>();
//        position1.add(0);
//        position1.add(4);
//        position1.add(6);
//        position1.add(7);
//        position1.add(8);
//        position1.add(9);
//        List<Integer> position2 = new ArrayList<Integer>();
//        position2.add(13);
//        AGVs.add(position1);
//        AGVs.add(position2);
//        List<Integer> position3 = new ArrayList<Integer>();
//        position3.add(12);
//        AGVs.add(position3);
//        //两个车都闲着
//        Double[] timeAlreadyPass = {1.2,-1.0,-1.0};



//        调用算法计算路径
//        AGV_GA agv_ga = new AGV_GA(testGraph,tasks,timeAlreadyPass,AGVs,2,1,bufferSet,bufferForAGV);



        List<List<AGVRecord>> bestGenRecords = new ArrayList<List<AGVRecord>>();
        List<List<Integer>> paths = agv_ga.singleObjectGenericAlgorithm(bestGenRecords);



        //将path组成对应的json格式
        ModelMap modelMap = new ModelMap();
        //所有小车的总路径
        ArrayList<ArrayList<ModelMap>> pathModelMap = new ArrayList<ArrayList<ModelMap>>();
        modelMap.addAttribute("path",pathModelMap);
        for (List<Integer> pathForAGV: paths) {
            //每个小车的路径
            ArrayList<ModelMap> pathsForAGV = new ArrayList<ModelMap>();
            for (Integer node: pathForAGV) {
                ModelMap modelMapForAGV = new ModelMap();
                //前端节点从1开始
                modelMapForAGV.addAttribute("path",node+1);
                pathsForAGV.add(modelMapForAGV);
            }
            //末尾添加-1作为结束标志
            ModelMap modelMapForAGV = new ModelMap();
            modelMapForAGV.addAttribute("path",-1);
            pathsForAGV.add(modelMapForAGV);
            pathModelMap.add(pathsForAGV);
        }

        ArrayList<ArrayList<ModelMap>> recordModelMap = new ArrayList<ArrayList<ModelMap>>();
        modelMap.addAttribute("record",recordModelMap);
        for (List<AGVRecord> pathRecord : bestGenRecords) {
            ArrayList<ModelMap> recordForAGV = new ArrayList<ModelMap>();
            //把所有的该车的真实任务以及它的次数放在map中
            HashMap<Integer,Integer> taskNumberMap = new HashMap<Integer, Integer>();
            for (AGVRecord record: pathRecord) {
                //保证不重复添加也不加入回buffer的记录
                if (!record.isFirstStep()) {
                    //新建该真实任务的次数或者增加其次数
                    int trueTaskNumber = taskMap.get(record.getIndexInPriorityChromosome()/2);
                    if (taskNumberMap.containsKey(trueTaskNumber)) {
                        taskNumberMap.put(trueTaskNumber,taskNumberMap.get(trueTaskNumber) + 1);
                    }
                    else {
                        taskNumberMap.put(trueTaskNumber,1);
                    }
                }
            }
            for (Integer taskNumber: taskNumberMap.keySet())  {
                ModelMap modelMapForOneTask = new ModelMap();
                ModelMap modelMapForDetailTask = new ModelMap();
                modelMapForOneTask.addAttribute("record", modelMapForDetailTask);
                modelMapForDetailTask.addAttribute("taskNum", taskNumber);
                modelMapForDetailTask.addAttribute("times", taskNumberMap.get(taskNumber));
                recordForAGV.add(modelMapForOneTask);
            }
            recordModelMap.add(recordForAGV);
        }
        return modelMap;
    }


}
