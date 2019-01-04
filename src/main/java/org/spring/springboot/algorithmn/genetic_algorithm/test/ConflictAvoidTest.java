package org.spring.springboot.algorithmn.genetic_algorithm.test;

import org.junit.Before;
import org.junit.Test;
import org.spring.springboot.algorithmn.genetic_algorithm.ConflictAvoid;
import org.spring.springboot.algorithmn.common.CommonTestConstant;
import org.ujmp.core.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConflictAvoidTest {

    private ConflictAvoid conflictAvoid;


   //初始化地图信息
    @Before
    public void initializeConflictAvoid() throws IOException {
        //从csv文件中读取矩阵
        File file = new File("TestGraphSet/TestGraph2.csv");
        Matrix graph = Matrix.Factory.importFrom().file(file).asDenseCSV();
//        System.out.println(graph);
        double AGVSpeed = 2;
        double minDistance = 2;
        int AGVNumber = 4;
        Integer[] bufferForAGV = {0,1,0,1};
        int penalty = 999;
        double safeDistance = 2*0.7;
        List<List<Integer>> bufferPath = CommonTestConstant.getBufferForTestGraph2();
        Double[] timeAlreadyPass = {-1.0000,-1.0000,-1.0000,-1.0000};
        conflictAvoid = new ConflictAvoid(AGVSpeed,AGVNumber,timeAlreadyPass,minDistance/AGVSpeed,
                minDistance,penalty,safeDistance,graph,bufferPath,bufferForAGV);
    }

    //测试是否撞击次数的计算正确当两个车往一个点开的时候,1号车和2号车在9号点撞击
    @Test
    public void TestConflictOccursOnceWhenTwoAGVsCrashOnAPoint() {
        List<List<List<Integer>>> localPath = new ArrayList<List<List<Integer>>>();
        List<List<Integer>> localPathGen = new ArrayList<List<Integer>>();
        localPath.add(localPathGen);
        //每一个车的路径
        List<Integer> path1 = new ArrayList<Integer>();
        List<Integer> path2 = new ArrayList<Integer>();
        List<Integer> path3 = new ArrayList<Integer>();
        List<Integer> path4 = new ArrayList<Integer>();
        //1号车往9号点走然后从2，3点回去
        path1.add(105);
        path1.add(3);
        path1.add(8);
        path1.add(1);
        path1.add(2);

        //2号车往9号点走
        path2.add(205);
        path2.add(7);
        path2.add(8);
        path2.add(1);
        path2.add(0);
        //3号车补上1号车开走的位置
        path3.add(104);
        path3.add(105);
        //4号车补上2号车开走的位置
        path4.add(204);
        path4.add(205);
        localPathGen.add(path1);
        localPathGen.add(path2);
        localPathGen.add(path3);
        localPathGen.add(path4);
        List<double[]> fitness = new ArrayList<double[]>();
        //全部的适应度从0开始，容易观察
        double[] fitnessGen = {0, 0, 0, 0};
        fitness.add(fitnessGen);

        //第一个车子停下
        conflictAvoid.conflictAvoidStrategy(localPath, fitness);
        assertEquals(105, (int) path1.get(0));
        assertEquals(3, (int) path1.get(1));
        //停在距离起点的8m的位置一轮
        assertEquals(-8, (int) path1.get(2));
        assertEquals(8, (int) path1.get(3));
        assertEquals(1, (int) path1.get(4));
        assertEquals(2, (int) path1.get(5));

        //第二个车子继续前进
        assertEquals(205, (int) path2.get(0));
        assertEquals(7, (int) path2.get(1));
        assertEquals(8, (int) path2.get(2));
        assertEquals(1, (int) path2.get(3));
        assertEquals(0, (int) path2.get(4));

       //测试适应度,等待时间用最小距离来标志，增加该车的运行距离
        assertEquals(2, (int) fitness.get(0)[0]);
        assertEquals(0, (int) fitness.get(0)[1]);


    }



}
