package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.Arrays;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class GeneratorTest {
    @Test
    public void testGenerate() {
        Generator generator = new Generator();
        generator.ops = Arrays.asList("read", "write", "cas");
        generator.totalCount = 100;
        generator.generate();
        System.out.println(JSONObject.toJSONString(generator.result));
    }
}
