package org.spring.springboot.test;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GeneticAlgorithmControllerTest {


    @Before
    public void setup() {
        RestAssured.port = 9090;
    }

    @Test
    public void shouldOneAGVFinishOneTaskCorrectly() {
        Map jsonMap = new HashMap<>();
        jsonMap.put("speed", 2);
        jsonMap.put("precision", 2);
        jsonMap.put("numberOfGraphNode", 9);
    }
}
