package org.spring.springboot.algorithmn.GA;


/*
 * Find the possible path around the terminal vertex, which will not form a loop.
 * Do not consider dead node, use penalty to remove that.
 * */


import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;

public class FeasiblePathGrowth {
    //实际的邻接矩阵
    private Matrix matrix;
    //0，1表示的邻接矩阵
    private Matrix graph;

    public FeasiblePathGrowth(Matrix matrix, Matrix graph) {
        this.matrix = matrix;
        this.graph = graph;
    }

    //graph is the connected graph with value 0,1, matrix is the actual graph with weight
    //path为节点个数加一的数组，最后一位作为该路线的距离,-1用来判断是否路径结束
    public double[] feasiblePathGrowth(int startNode, int terminalNode, Integer[] priorityChromosome) {
        //如果起点和终点重复，直接返回path
        if (startNode==terminalNode) {
            double[] path =  {startNode,startNode,0};
            return path;
        }
        int sizeOfNode = priorityChromosome.length;
        int k = startNode;
        double distance = 0;
        double[] path =  new double[sizeOfNode+1];
        for (int i = 0; i < path.length; i ++ ){
            path[i] = -1;
        }
        int index = 0;
        path[index] = startNode;
        index++;
        Matrix dynamicMatrix = graph;
        while (k!=terminalNode) {
            /*create mesh matrix*/
            Matrix meshMatrix = Matrix.Factory.ones(sizeOfNode, sizeOfNode);
            for (double existedNode : path) {
                if (existedNode != -1) {
                    for (int i = 0; i < sizeOfNode; i++) {
                        meshMatrix.setAsInt(0, i, (int) existedNode);
                    }
                }
            }
            dynamicMatrix = graph.and(Calculation.Ret.NEW, meshMatrix).toIntMatrix();
            Matrix eligibleEdges = dynamicMatrix.selectRows(Calculation.Ret.NEW, (int) path[index - 1]);
            /*dead node*/
            //penalty就是MAX_EDGE
            if (!eligibleEdges.containsInt(1)) {
                path[index] = terminalNode;
                distance += matrix.getAsDouble((int) path[index - 1], terminalNode);
                //为了便于赋-1，index和外层统一大小
                index++;
                break;
            }
            Integer nextNode = findNextNode(priorityChromosome, eligibleEdges);
            // System.out.println(nextNode);
            path[index] = nextNode;
            distance += matrix.getAsDouble((int) path[index - 1], nextNode);
            index++;
            k = nextNode;
        }
        path[sizeOfNode] = distance;
        return path;
    }

    /*Based on priority chromosome, finds the next node in the path*/
    private Integer findNextNode(Integer[] priorityChromosome, Matrix eligibleEdges) {
        int count = 0;
        for (int[] edge : eligibleEdges.toIntArray()) {
            for (int j = 0; j < priorityChromosome.length; j++) {
                if (edge[j] == 1) {
                    count++;
                }
            }
        }
        //候选的边
        int[] sortEdge = new int[count];
        count = 0;
        for (int[] edge : eligibleEdges.toIntArray()) {
            for (int j = 0; j < priorityChromosome.length; j++) {
                if (edge[j] == 1) {
                    sortEdge[count] = j;
                    count++;
                }
            }
        }
   //     System.out.println(Matrix.Factory.importFromArray(sortEdge));
        insertSort(sortEdge,priorityChromosome);
        return sortEdge[sortEdge.length-1];
    }


    private void insertSort(int[] a, Integer[] priorityChromosome) {
        int i, j, insertNote;// 要插入的数据
        for (i = 1; i < a.length; i++) {// 从数组的第二个元素开始循环将数组中的元素插入
            insertNote = a[i];// 设置数组中的第2个元素为第一次循环要插入的数据
            j = i - 1;
            while (j >= 0 && priorityChromosome[insertNote] < priorityChromosome[a[j]]) {
                a[j + 1] = a[j];// 如果要插入的元素小于第j个元素,就将第j个元素向后移动
                j--;
            }
            a[j + 1] = insertNote;// 直到要插入的元素不小于第j个元素,将insertNote插入到数组中
        }
    }

}