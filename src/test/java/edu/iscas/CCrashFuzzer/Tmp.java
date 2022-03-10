package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Tmp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> l1 = new ArrayList<String>();
		l1.add("a");
		l1.add("a");
		l1.add("a");
		
		List<String> l2 = new ArrayList<String>();
		l2.addAll(l1);
		l2.remove(0);
		
		System.out.println(l1);
		System.out.println(l2);
		//[a, a, a]
		//[a, a]
		
		test();
		
		String s = "34";
		byte[] content = s.getBytes();
		Long.parseLong(new String(content));
		
		HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
//		faultsToTests.computeIfAbsent(4, key -> 5);
		faultsToTests.put(4, 5);
		faultsToTests.computeIfAbsent(4, key -> 0);
		faultsToTests.computeIfPresent(4, (key, value) -> value + 1);
		
		System.out.println(faultsToTests);
	}
	
	public static void test() {
		for(int i =0; i<5; i++) {
			System.out.println(Arrays.asList(Thread.currentThread().getStackTrace()));
		}
	}

}
