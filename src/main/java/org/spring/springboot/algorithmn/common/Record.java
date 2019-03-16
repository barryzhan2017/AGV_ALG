package org.spring.springboot.algorithmn.common;


import java.util.Objects;

/**
 *  Record the index and times of task done by one AGV
 */
public class Record {

    private int indexOfTask = -1;
    private int times = -1;

    public Record(int indexOfTask, int times) {
        this.indexOfTask = indexOfTask;
        this.times = times;
    }

    public int getIndexOfTask() {
        return indexOfTask;
    }

    public void setIndexOfTask(int indexOfTask) {
        this.indexOfTask = indexOfTask;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return indexOfTask == record.indexOfTask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexOfTask);
    }

    @Override
    public String toString() {
        return "Record{" +
                "indexOfTask=" + indexOfTask +
                ", times=" + times +
                '}';
    }
}
