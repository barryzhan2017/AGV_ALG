package org.spring.springboot.controller;

import org.spring.springboot.Algorithm.GeneticAlgorithm.FeasiblePathGrowth;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.ujmp.core.Matrix;

@RestController
public class GeneticAlogrithmControll {

    @RequestMapping(value = "/api/genetic", method = RequestMethod.GET)
    public String findOneCity() {
        int[] priorityChromosome = {1,9,7,4,5};
        FeasiblePathGrowth feasiblePathGrowth = new FeasiblePathGrowth();
        Matrix testGraph = Matrix.Factory.ones(5,5);
        testGraph.setAsInt(0,0,0);
        testGraph.setAsInt(0,1,1);
        testGraph.setAsInt(0,2,2);
        testGraph.setAsInt(0,3,3);
        testGraph.setAsInt(0,4,4);
        testGraph.setAsInt(0,0,3);
        testGraph.setAsInt(0,1,4);
        testGraph.setAsInt(0,2,4);
        testGraph.setAsInt(0,3,0);
        testGraph.setAsInt(0,4,1);
        testGraph.setAsInt(0,4,2);
    //    testGraph.setAsInt(0,4,3);
    //    testGraph.setAsInt(0,3,4);

        feasiblePathGrowth.feasiblePathGrowth(testGraph,0,3,priorityChromosome);
        return "hello";
    }
}
