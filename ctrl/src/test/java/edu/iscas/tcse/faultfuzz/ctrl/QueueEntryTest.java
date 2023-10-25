package edu.iscas.tcse.faultfuzz.ctrl;

import org.junit.Test;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;

public class QueueEntryTest {
    @Test
    public void getPerfScore() {
        QueueEntry entry = new QueueEntry();
        entry.exec_s = 10;
        entry.bitmap_size = 50;
        entry.handicap = 2;
        entry.faultSeq = new FaultSequence();
        entry.faultSeq.seq.add(new FaultPoint());
        entry.faultSeq.seq.add(new FaultPoint());
        FuzzInfo.exec_us = 20;
        FuzzInfo.total_bitmap_size = 100;
        FuzzInfo.total_bitmap_entries = 4;
        FuzzInfo.total_execs = 4;
        Stat.log(entry.getPerfScore());
    }
}
