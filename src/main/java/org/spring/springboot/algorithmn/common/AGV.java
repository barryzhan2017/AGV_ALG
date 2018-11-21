package org.spring.springboot.algorithmn.common;

import java.util.List;

public class AGV {
    private double speed;
    private List<Integer> path;

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }
}
