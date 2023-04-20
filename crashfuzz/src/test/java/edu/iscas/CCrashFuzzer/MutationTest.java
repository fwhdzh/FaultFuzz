package edu.iscas.CCrashFuzzer;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.Mutation.EntryAndScore;

public class MutationTest {

    @Test
    public void testIntergerHashSet() {
        QueueManagerNew.tested_fault_id.add(1);
        QueueManagerNew.tested_fault_id.add(3);
        Assert.assertTrue(QueueManagerNew.tested_fault_id.contains(new Integer(1)));
    }

    @Test
    public void testAppendGlobalNewIOSocreWithList() {
        List<QueueEntry> l = QueueManagerNewTest.prepareEntryAndTestFaultIdSet();
        QueueEntry entry1 = l.get(0);
        QueueEntry entry2 = l.get(1);
        List<QueueEntry> mutates = new ArrayList<QueueEntry>();
        mutates.add(entry1);
        mutates.add(entry2);
        List<Integer> scores = new ArrayList<Integer>();
        scores.add(0);
        scores.add(0);
        
        Mutation.appendGlobalNewIOSocre(mutates, scores);
        Assert.assertEquals(1, (int) scores.get(0));
        Assert.assertEquals(0, (int) scores.get(1));
    }

    @Test
    public void testAppendGlobalNewIOSocre() {
        List<QueueEntry> l = QueueManagerNewTest.prepareEntryAndTestFaultIdSet();
        QueueEntry entry1 = l.get(0);
        QueueEntry entry2 = l.get(1);
        List<QueueEntry> mutates = new ArrayList<QueueEntry>();
        mutates.add(entry1);
        mutates.add(entry2);
        int[] scores = new int[mutates.size()];
        Mutation.appendGlobalNewIOSocre(mutates, scores);
        Assert.assertEquals(1, scores[0]);
        Assert.assertEquals(0, scores[1]);
    }

    @Test
    public void testEntryAndScoreComparable() {
        QueueEntry entry1 = new QueueEntry();
        QueueEntry entry2 = new QueueEntry();
        EntryAndScore e1 = new EntryAndScore(entry1, 1);
        EntryAndScore e2 = new EntryAndScore(entry2, 2);
        Assert.assertTrue(e1.compareTo(e2) < 0);
        List<EntryAndScore> list = new ArrayList<EntryAndScore>();
        list.add(e2);
        list.add(e1);
        list.sort(null);
        Assert.assertEquals(e1, list.get(0));
    }
}
