package org.spring.springboot.algorithmn.genetic_algorithm;

import java.util.List;

public interface ConflictAvoidStrategy {

    void conflictAvoidance(List<List<Integer>> AGVPaths, double[] AGVFitness);

}
