import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.RecordsHandler;

public class TestCollection {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<FAVEntry> entries = new ArrayList<FAVEntry>();
		entries.add(new FAVEntry(1, 1, 1, "1", Taint.emptyTaint(), new ArrayList<String>()));
		entries.add(new FAVEntry(2, 2, 2, "2", Taint.emptyTaint(), new ArrayList<String>()));
		entries.add(new FAVEntry(3, 3, 3, "3", Taint.emptyTaint(), new ArrayList<String>()));
        int range = entries.size();
        List<FAVEntry> tmp = entries.subList(0, 2);
        ArrayList<FAVEntry> fin = new ArrayList<FAVEntry>();
        fin.addAll(tmp);
        tmp.clear();
        System.out.println(fin);
        System.out.println(entries);
	}

}
