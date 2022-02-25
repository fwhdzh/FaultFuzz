package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.Arrays;
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
	}
	
	public static void test() {
		for(int i =0; i<5; i++) {
			System.out.println(Arrays.asList(Thread.currentThread().getStackTrace()));
		}
	}

}
