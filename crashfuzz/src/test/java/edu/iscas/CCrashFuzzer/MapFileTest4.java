package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFileTest4 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int MAP_SIZE = 1000;
		CoverageCollector coverage = new CoverageCollector();
		coverage.read_bitmap("mycov");
	}

}
