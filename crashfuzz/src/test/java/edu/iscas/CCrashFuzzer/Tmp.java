package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Tmp {
	public static class Data {
		public String s;
		public Data(String s) {
			this.s = s;
		}
		public String toString() {return s;}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Data> l1 = new ArrayList<Data>();
		l1.add(new Data("a"));
		l1.add(new Data("a"));
		l1.add(new Data("a"));
		
		List<Data> l2 = new ArrayList<Data>();
		l2.addAll(l1);
		l2.remove(0);
		
		l2.get(0).s = "modify";
		
		l1.remove(l2.get(0));
		
		System.out.println(l1);
		System.out.println(l2);
		
		Integer i = 1;
		System.out.println(i.equals(new Integer(1)));
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
