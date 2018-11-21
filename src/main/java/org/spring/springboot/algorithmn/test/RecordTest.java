package org.spring.springboot.algorithmn.test;

import org.junit.Test;
import org.spring.springboot.algorithmn.genetic_algorithm.AGVRecord;


import java.util.HashSet;
import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RecordTest {

    //test if two records are equal to each other if  the start and end index are the same
    @Test
    public void testRecordWithSameStartIndexAndEndIndexEqualsTo() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,1,2,3,0,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,1,2,1,1,false);
        assertEquals(agvRecord1,agvRecord2);
    }

    //test if two records are not equal to each other if the start index is different
    @Test
    public void testDifferentStartIndexRecordNotEqualsTo() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,2,2,3,0,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,1,2,1,1,false);
        assertNotEquals(agvRecord1,agvRecord2);
    }

    //test if two records are not equal to each other if the end index is different
    @Test
    public void testDifferentEndIndexRecordNotEqualsTo() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,2,2,3,4,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,2,1,1,5,false);
        assertNotEquals(agvRecord1,agvRecord2);
    }

    //test if two records have same hash code if  the start and end index are the same
    @Test
    public void testRecordWithSameStartIndexAndEndIndexHaveSameHashCode() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,1,2,3,4,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,1,2,1,5,false);
        assertEquals(agvRecord1.hashCode(),agvRecord2.hashCode());
    }

    //test if same records can be just one record in the HashSet
    @Test
    public void testSameRecordPresentInSetOneElement() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,1,2,3,4,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,1,2,1,5,false);
        HashSet<AGVRecord> agvRecordHashSet = new HashSet<AGVRecord>();
        agvRecordHashSet.add(agvRecord1);
        agvRecordHashSet.add(agvRecord2);
        assertEquals(1,agvRecordHashSet.size());
        Iterator it = agvRecordHashSet.iterator();
        while(it.hasNext()) {
            assertEquals(it.next(), agvRecord1);
        }
    }

    //test if  records with different start index can not maintain one record in the HashSet
    @Test
    public void testRecordWithDifferentStartIndexPresentInSetTwoElement() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,2,2,3,4,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,1,2,1,5,false);
        HashSet<AGVRecord> agvRecordHashSet = new HashSet<AGVRecord>();
        agvRecordHashSet.add(agvRecord1);
        agvRecordHashSet.add(agvRecord2);
        assertEquals(2,agvRecordHashSet.size());
    }

    //test if  records with different end index can not maintain one record in the HashSet
    @Test
    public void testRecordWithDifferentEndIndexPresentInSetTwoElement() {
        AGVRecord agvRecord1 = new AGVRecord(1,2,1,2,9,4,true);
        AGVRecord agvRecord2 = new AGVRecord(1,2,1,23,1,5,false);
        HashSet<AGVRecord> agvRecordHashSet = new HashSet<AGVRecord>();
        agvRecordHashSet.add(agvRecord1);
        agvRecordHashSet.add(agvRecord2);
        assertEquals(2,agvRecordHashSet.size());
    }
}
