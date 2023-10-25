package edu.iscas.tcse.faultfuzz.ctrl.runtime;

import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class QueueEntryRuntime {
    public FaultSequence faultSeq;
    public List<IOPoint> ioSeq;

    public QueueEntry entry;

    public QueueEntryRuntime(QueueEntry entry) {
        this.faultSeq = entry.faultSeq;
        this.ioSeq = entry.ioSeq;
        this.entry = entry;
    }
}