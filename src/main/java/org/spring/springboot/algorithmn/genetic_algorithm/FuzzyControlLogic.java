package org.spring.springboot.algorithmn.genetic_algorithm;

public class FuzzyControlLogic {
    private final int[][] defuzzificationTable = {{-4,-3,-3,-2,-2,-1,-1,0,0},
                                                  {-3,-3,-2,-2,-1,-1,0,0,1},
                                                  {-3,-2,-2,-1,-1,0,0,1,1},
                                                  {-2,-2,-1,-1,0,0,1,1,2},
                                                  {-2,-1,-1,0,2,1,1,2,2},
                                                  {-1,-1,0,0,1,1,2,2,3},
                                                  {-1,0,0,1,1,2,2,3,3},
                                                  {0,0,1,1,2,2,3,3,4},
                                                  {0,1,1,2,2,3,3,4,4}};


    //通过defuzzification table来调整交叉概率参数,用arctan转化为-pi/2到pi/2
    public double adjustCrossoverProbability(double crossoverProbability, double parentFitnessVariation, double offspringFitnessVariation) {
        return defuzzify(Math.atan(parentFitnessVariation),Math.atan(offspringFitnessVariation))*0.02 + crossoverProbability;
    }


    //通过defuzzification table来调整变异概率参数
    public double adjustMutateProbability(double mutateProbability, double parentFitnessVariation, double offspringFitnessVariation) {
        return defuzzify(Math.atan(parentFitnessVariation),Math.atan(offspringFitnessVariation))*0.002 + mutateProbability ;
    }

    //把从-pi/2到pi/2映射到[-4，4]区间,oMax,oMin是目标区间，nMin,nMax是规范化后的区间
    private double normalizeVariation(double fitnessVariation,double oMin, double oMax, double nMin, double nMax) {
        return ((nMax-nMin)/(oMax-oMin))*(fitnessVariation-oMin)+nMin;
    }


    //求解出逻辑控制器的下一子代修改值
    private int defuzzify(double parentFitnessVariation, double offspringFitnessVariation) {
        //把distance的平均值的变化映射到[-4，-4]区间上，变化值是-pi/2到pi/2,(fitness = 1/distance)
        double normalizedParentFitnessVariation = normalizeVariation(parentFitnessVariation, -Math.PI/2,Math.PI/2,-4,4);
        double normalizedOffspringFitnessVariation = normalizeVariation(offspringFitnessVariation, -Math.PI/2,Math.PI/2,-4,4);
        int deuzzificationResult =
                defuzzificationTable[(int)Math.round(normalizedParentFitnessVariation)+4][(int)Math.round(normalizedOffspringFitnessVariation)+4];
        return deuzzificationResult;
    }



}
