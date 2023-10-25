package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {
    
    public List<String> ops;
    public List<String> result = new ArrayList<>();

    public int delay;

    public int totalCount;

    public Random random = new Random();

    public void generate() {
        for (int i = 0; i < totalCount; i++) {
            String op = ops.get(random.nextInt(ops.size()));
            result.add(op);
        }
    }
}
