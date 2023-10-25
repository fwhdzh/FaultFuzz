package edu.iscas.tcse.faultfuzz.ctrl.utils;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class DotGraphGeneratorTest {

    @Test
    public void testGenerateGraphWithString() {
        String s = "digraph mygraph {\n";
        s += "A -> B\n";
        s += "}\n";
        String targetPath = this.getClass().getResource("DotGraphGeneratorTest.class").getPath().split("DotGraphGeneratorTest.class")[0] + "graph.dot";
        DotGraphGenerator generator = new DotGraphGenerator();
        generator.generateGraphWithString(s, targetPath);
        File f = new File(targetPath);
        Assert.assertEquals(f.exists(), true);
        f.delete();
    }
}
