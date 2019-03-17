package org.spring.springboot.algorithmn.preprocess;

import org.spring.springboot.algorithmn.common.Path;
import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//用来管理数据的预处理，转换json数组成为可利用的的数组或者链表
public class PreprocessData {

    private String pathStartNodeName = "startNode";
    private String pathEndNodeName = "endNode";

    //按照对应顺序从json中取出对应的点和距离，点的名字从1开始构造邻接矩阵
    public Matrix createGraphFromJson (List<Map> startNode, List<Map> endNode, List<Map> distance, int numberOfNode, int max_edge) {
        double [][] graph = new double[numberOfNode][numberOfNode];
        //先给这些图附上无穷大边
        for (int i = 0; i < numberOfNode; i++) {
            for (int j = 0; j < numberOfNode; j++) {
                graph[i][j] = max_edge;
            }
        }
        int sizeOfNode = endNode.size();
        for (int i = 0; i < sizeOfNode; i++) {

            Double doubleDistance = getDoubleData(distance.get(i).get("nodeDistance"));
            //注意要减1来换成java数组的index
            graph[(Integer) startNode.get(i).get(pathStartNodeName) - 1][(Integer) endNode.get(i).get(pathEndNodeName) - 1]
                    = doubleDistance;
            graph[(Integer) endNode.get(i).get(pathEndNodeName) - 1][(Integer) startNode.get(i).get(pathStartNodeName) - 1]
                    = doubleDistance;
        }
        return Matrix.Factory.importFromArray(graph);
    }

    //从json中拿出任务，任务点用‘,’分开，所以需要还原(如["1,2,20"]),前两位代表任务点，后一位是次数，点是从1开始的，要更改
    public Integer[][] getTasksFromJson(List<Map> tasksList) {
        int size = tasksList.size();
        List<Integer[]> tasks = new ArrayList<Integer[]>();
        for (int i = 0; i < size; i++) {
            String pathString = (String)tasksList.get(i).get("tasks");
            //对将限定的数量的task放入tasks中
            //多少个任务
            int taskNumber = Integer.parseInt(pathString.split(",")[2]);
            for (int j = 0; j < taskNumber; j++) {
                Integer[] subTask = new Integer[2];
                subTask[0] = Integer.valueOf(pathString.split(",")[0])-1;
                subTask[1] = Integer.valueOf(pathString.split(",")[1])-1;
                tasks.add(subTask);
            }
        }
        Integer[][] taskArray = new Integer[tasks.size()][2];
        //把tasks列表转化为array
        for (int i = 0; i < tasks.size(); i++) {
            taskArray[i][0] = tasks.get(i)[0];
            taskArray[i][1] = tasks.get(i)[1];
        }
        return taskArray;
    }

    //从json中拿出小车以及运行的时间
    public Double[] getTimeFromJson(List<Map> timeList) {
        int size = timeList.size();
        Double[] timeArray = new Double[size];
        for (int i = 0; i < size; i++) {
            Double time = getDoubleData(timeList.get(i).get("time"));
            timeArray[i] = time;
        }
        return timeArray;
    }

    //从json中拿出每个小车所归属的buffer信息
    public Integer[] getBufferForAGVFromJson(List<Map> bufferList) {
        int size = bufferList.size();
        Integer[] bufferForAGV = new Integer[size];
        for (int i = 0; i < size; i++) {
            int bufferIndex = (Integer) bufferList.get(i).get("bufferForAGV");
            bufferForAGV[i] = bufferIndex-1;
        }
        return bufferForAGV;
    }

    //Get Buffer path from AGV
    public List<List<Integer>> getBufferPaths(List<List<Map>> paths) {
        List<List<Integer>> ongoingPaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            List<Integer> AGVPath = new ArrayList<>();
            //对每条路径增加节点循环
            for (int j = 0; j < paths.get(i).size(); j++) {
                //前端是从1开始的节点
                AGVPath.add((Integer) paths.get(i).get(j).get("paths") - 1);
            }
            ongoingPaths.add(AGVPath);
        }
        return ongoingPaths;
    }


    //Get AGV path from AGV
    public List<List<Path>> getAGVPaths(List<List<Map>> paths) {
        List<List<Path>> ongoingPaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            List<Path> AGVPath = new ArrayList<>();
            //对每条路径增加节点循环
            for (int j = 0; j < paths.get(i).size(); j++) {
                //Node number starts at 1 in front end
                int startNode = (int) paths.get(i).get(j).get(pathStartNodeName) - 1;
                int endNode = (int) paths.get(i).get(j).get(pathEndNodeName) - 1;
                double time = Double.parseDouble(String.valueOf(paths.get(i).get(j).get("time")));
                int isLoop = (int) paths.get(i).get(j).get("isLoop");
                Path path = new Path(startNode, endNode, time, isLoop == 1);
                AGVPath.add(path);
            }
            ongoingPaths.add(AGVPath);
        }
        return ongoingPaths;
    }

    //把number类型的前端数据转化为double类型
    public Double getDoubleData(Object numberObject) {
        Double doubleData;
        if (numberObject instanceof Integer) {
            int castedDistance = (Integer) numberObject;
            doubleData = (double) castedDistance;
        } else if (numberObject instanceof Float) {
            float castedDistance = (Float) numberObject;
            doubleData = (double) castedDistance;
        } else {
            doubleData = (Double) numberObject;
        }
        return doubleData;
    }

    //将真实的任务序号和生成的子任务序号建立map联系
    public Map<Integer,Integer> getTaskMap(List<Map> tasks) {
        HashMap<Integer,Integer> taskMap = new HashMap<Integer, Integer>();
        int size = tasks.size();
        int currentTaskNumber = 0;
        for (int i = 0; i < size; i++) {
            String pathString = (String)tasks.get(i).get("tasks");
            //多少个任务
            int taskNumber = Integer.parseInt(pathString.split(",")[2]);
            for (int j = 0; j < taskNumber; j++) {
                //任务编号得减一
                taskMap.put(currentTaskNumber+j, i);
            }
            currentTaskNumber += taskNumber;
        }
        return taskMap;
    }
}
