package org.spring.springboot.algorithmn.GA;

import java.util.List;

public interface ConflictAvoidStrategy {
    void conflictAvoidance(List<List<List<Integer>>> AGVPaths, List<double[]> AGVFitness);
}
