package edu.iscas.tcse.faultfuzz;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import edu.iscas.tcse.faultfuzz.FaultSequence;
import edu.iscas.tcse.faultfuzz.QueueEntry;
import edu.iscas.tcse.faultfuzz.Stat;
import edu.iscas.tcse.faultfuzz.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.selection.OldQueueEntrySelector;
import edu.iscas.tcse.faultfuzz.selection.SelectionInfo;
import edu.iscas.tcse.faultfuzz.selection.SelectionInfo.QueuePair;

public class QueueManagerNewTest {

    public static List<QueueEntry> prepareEntryAndTestFaultIdSet() {
        List<QueueEntry> result = new ArrayList<QueueEntry>();
        SelectionInfo.tested_fault_id.add(1);
        SelectionInfo.tested_fault_id.add(3);
        FaultPoint fp1 = Mockito.mock(FaultPoint.class);
        when(fp1.getFaultID()).thenReturn(1);
        FaultPoint fp2 = Mockito.mock(FaultPoint.class);
        when(fp2.getFaultID()).thenReturn(2);
        FaultPoint fp3 = Mockito.mock(FaultPoint.class);
        when(fp3.getFaultID()).thenReturn(3);
        FaultSequence fs1 = new FaultSequence();
        fs1.seq.add(fp1);
        fs1.seq.add(fp2);
        QueueEntry entry1 = new QueueEntry();
        entry1.faultSeq = fs1;
        FaultSequence fs2 = new FaultSequence();
        fs2.seq.add(fp3);
        QueueEntry entry2 = new QueueEntry();
        entry2.faultSeq = fs2;
        result.add(entry1);
        result.add(entry2);
        return result;
    }

    @Test
    public void testTryToGetAQueueEntryWithGlobalNewPoint() {
        List<QueueEntry> l = prepareEntryAndTestFaultIdSet();
        QueueEntry entry1 = l.get(0);
        QueueEntry entry2 = l.get(1);
        QueueEntry seedEntry1 = new QueueEntry();
        seedEntry1.mutates.add(entry1);
        QueueEntry seedEntry2 = new QueueEntry();
        seedEntry2.mutates.add(entry2);
        List<QueueEntry> list = new ArrayList<QueueEntry>();
        list.add(seedEntry1);
        list.add(seedEntry2);
        SelectionInfo.QueuePair pair = OldQueueEntrySelector.tryToGetAQueueEntryWithGlobalNewPoint(list);
        Stat.log(pair.mutate.faultSeq.seq.get(0).getFaultID());
        Assert.assertEquals(entry1.faultSeq, pair.mutate.faultSeq);
        Assert.assertEquals(seedEntry1.faultSeq, pair.seed.faultSeq);
    }
}
