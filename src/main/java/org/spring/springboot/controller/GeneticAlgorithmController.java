package org.spring.springboot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.algorithmn.common.CommonConstant;
import org.spring.springboot.algorithmn.common.Path;
import org.spring.springboot.algorithmn.common.Record;
import org.spring.springboot.algorithmn.exception.NoAGVInTheBuffer;
import org.spring.springboot.algorithmn.genetic_algorithm.AGV_GA;
import org.spring.springboot.algorithmn.preprocess.PreprocessData;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.ujmp.core.Matrix;

import java.util.*;

@RestController
public class GeneticAlgorithmController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostMapping(value = "/api/genetic")
    public ModelMap genetic(@RequestBody Map payload) throws NoAGVInTheBuffer {
        logger.info("Size of input data: {}", payload.size());
        Map map = (Map)payload.get("data");
        PreprocessData preprocessData = new PreprocessData();

        Matrix graph = preprocessData.createGraphFromJson
                ((List<Map>) map.get("startNode"),(List<Map>)map.get("endNode"),(List<Map>)map.get("nodeDistance")
                        ,(Integer)map.get("numberOfGraphNode"),
                        (int)CommonConstant.MAX_EDGE);
        Integer[][] tasks = preprocessData.getTasksFromJson((List<Map>)map.get("tasks"));
        Double[] timeAlreadyPass = preprocessData.getTimeFromJson((List<Map>)map.get("time"));
        List<List<Path>> pathsOfAGVs = preprocessData.getAGVPaths((List<List<Map>>)map.get("paths"));
        List<List<Integer>> bufferSet = preprocessData.getBufferPaths((List<List<Map>>)map.get("bufferSet"));
        Integer [] bufferForAGV = preprocessData.getBufferForAGVFromJson((List<Map>)map.get("bufferForAGV"));
        double AGVSpeed = preprocessData.getDoubleData(map.get("speed"));
        double min_distance = preprocessData.getDoubleData(map.get("precision"));

        //Construct the mapping from tasks' spilt indexes to their original index in the data set in case that the task is a sub-task.
        Map<Integer,Integer> taskMap = preprocessData.getTaskMap((List<Map>)map.get("tasks"));
        List<List<Record>> bestGenRecords = new ArrayList<>();

        logger.info("Graph: {}", graph);
        logger.info("Node number: {}", map.get("numberOfGraphNode"));
        logger.info("Tasks paths: {}", Matrix.Factory.importFromArray(tasks));
        logger.info("Time: {}", Matrix.Factory.importFromArray(timeAlreadyPass));
        logger.info("AGV paths: {}", pathsOfAGVs);
        logger.info("Buffer paths: {}", bufferSet);
        logger.info("Buffer number for AGV: {}", Matrix.Factory.importFromArray(bufferForAGV));
        logger.info("AGV speed: {}", AGVSpeed);

        AGV_GA agv_ga = new AGV_GA(graph.toDoubleArray(), tasks, timeAlreadyPass, pathsOfAGVs, AGVSpeed, bufferSet,bufferForAGV, taskMap, min_distance, bestGenRecords);

        List<List<Path>> paths = agv_ga.singleObjectGenericAlgorithm();

        ModelMap modelMap = new ModelMap();
        ArrayList<ArrayList<ModelMap>> pathModelMap = new ArrayList<>();
        modelMap.addAttribute("paths",pathModelMap);
        for (List<Path> pathForAGV: paths) {
            ArrayList<ModelMap> pathsForAGV = new ArrayList<>();
                for (Path path: pathForAGV) {
                ModelMap modelMapForAGV = new ModelMap();
                //Add one to index of path node for front end need
                modelMapForAGV.addAttribute("startNode",path.getStartNode() + 1);
                modelMapForAGV.addAttribute("endNode",path.getEndNode() + 1);
                modelMapForAGV.addAttribute("time",path.getTime() + 1);
                modelMapForAGV.addAttribute("isLoop", path.isLoop() ? 1 : 0);
                pathsForAGV.add(modelMapForAGV);
            }
            //-1 path stands for end of path
            ModelMap modelMapForAGV = new ModelMap();
            //Add one to index of path node for front end need
            modelMapForAGV.addAttribute("startNode",-1);
            modelMapForAGV.addAttribute("endNode",-1);
            modelMapForAGV.addAttribute("time",-1);
            modelMapForAGV.addAttribute("isLoop", -1);
            pathsForAGV.add(modelMapForAGV);
            pathModelMap.add(pathsForAGV);
        }
        ArrayList<ArrayList<ModelMap>> recordModelMap = new ArrayList<>();
        modelMap.addAttribute("record", recordModelMap);
        for (List<Record> pathRecord : bestGenRecords) {
            ArrayList<ModelMap> recordForAGV = new ArrayList<>();
            for (Record record : pathRecord)  {
                ModelMap modelMapForOneTask = new ModelMap();
                ModelMap modelMapForDetailTask = new ModelMap();
                modelMapForOneTask.addAttribute("record", modelMapForDetailTask);
                modelMapForDetailTask.addAttribute("taskNum", record.getIndexOfTask());
                modelMapForDetailTask.addAttribute("times", record.getTimes());
                recordForAGV.add(modelMapForOneTask);
            }
            recordModelMap.add(recordForAGV);
        }

        logger.info("Graph: {}", graph);
        logger.info("Node number: {}", map.get("numberOfGraphNode"));
        logger.info("Tasks paths: {}", Matrix.Factory.importFromArray(tasks));
        logger.info("Time: {}", Matrix.Factory.importFromArray(timeAlreadyPass));
        logger.info("AGV paths: {}", pathsOfAGVs);
        logger.info("Buffer paths: {}", bufferSet);
        logger.info("Buffer number for AGV: {}", Matrix.Factory.importFromArray(bufferForAGV));
        logger.info("AGV speed: {}", AGVSpeed);
        return modelMap;
    }


}
