package org.spring.springboot.algorithmn.conflict_free_routing;

import java.util.Comparator;

//Compare two time windows, if one starts early, it is smaller.
public class TimeWindowComparator implements Comparator<TimeWindow> {


    @Override
    public int compare(TimeWindow o1, TimeWindow o2){
        if (o1 == null || o2 == null) {
            throw new NullPointerException("Time window cannot be compared because null value exists!");
        }
        else if (o1.getNodeNumber() != o2.getNodeNumber()){
            throw new IllegalArgumentException("Not the same node to compare!");
        }
        else {
            if (o1.getStartTime() < o2.getStartTime()) {
                return -1;
            }
            else if (o1.getStartTime() > o2.getStartTime()){
                return 1;
            }
            return 0;
        }
    }
}
