package org.spring.springboot.algorithmn.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.spring.springboot.Application;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({RecordTest.class, PathImprovementTest.class,
        ReturnPathPlanningTest.class, TimeWindowTest.class})
public class TestAll {

}
