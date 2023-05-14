package edu.iscas.CCrashFuzzer.traditionalworkflow;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.QueueManagerNewTest;
import edu.iscas.CCrashFuzzer.selection.SelectionInfo;

public class MutationTest {

    @Test
    public void testIntergerHashSet() {
        SelectionInfo.tested_fault_id.add(1);
        SelectionInfo.tested_fault_id.add(3);
        Assert.assertTrue(SelectionInfo.tested_fault_id.contains(new Integer(1)));
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
        
        TranditionalFuzzingMutationSelector.appendGlobalNewIOSocre(mutates, scores);
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
        TranditionalFuzzingMutationSelector.appendGlobalNewIOSocre(mutates, scores);
        Assert.assertEquals(1, scores[0]);
        Assert.assertEquals(0, scores[1]);
    }

    @Test
    public void testEntryAndScoreComparable() {
        QueueEntry entry1 = new QueueEntry();
        QueueEntry entry2 = new QueueEntry();
        TranditionalFuzzingMutationSelector.EntryAndScore e1 = new TranditionalFuzzingMutationSelector.EntryAndScore(entry1, 1);
        TranditionalFuzzingMutationSelector.EntryAndScore e2 = new TranditionalFuzzingMutationSelector.EntryAndScore(entry2, 2);
        Assert.assertTrue(e1.compareTo(e2) < 0);
        List<TranditionalFuzzingMutationSelector.EntryAndScore> list = new ArrayList<TranditionalFuzzingMutationSelector.EntryAndScore>();
        list.add(e2);
        list.add(e1);
        list.sort(null);
        Assert.assertEquals(e1, list.get(0));
    }
}
