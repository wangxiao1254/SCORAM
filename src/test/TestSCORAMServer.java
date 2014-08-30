// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package test;

import test.TestSCORAMRec.GenRunnable;
import flexsc.Flag;


public class TestSCORAMServer {
	public static void main(String [ ] args) throws InterruptedException {
		TestSCORAMRec c = new TestSCORAMRec();
		GenRunnable gen ;
		if(args.length < 6){
//			gen = c.new GenRunnable(12345, 20, 6, 32,  8, 10);
			System.out.println("Usage: \t\t"
					+ "java  -cp bin:lib test.TestSCORAMServer port logN bucketSize payloadSize recursionFactor logCutOff\n"
					+ "port:\t port number used by this program\n"
					+ "logN:\t log(#blocks in SCORAM)\n"
					+ "bucketSize:\t #blocks in each bucket\n"
					+ "payloadSize:\t #bits in each data block\n"
					+ "recursionFactor:\t #entries grouped together in recursion level\n"
					+ "logCutOff\t: log(#entries where the recursion stops)\n\n"
					+ "Example:\n "
					+ "java  -cp bin:lib/* test.TestSCORAMServer 12345 20 6 32  8 10");
			return ;
		}
		else
			gen = c.new GenRunnable(new Integer(args[0]), new Integer(args[1]), 
					new Integer(args[2]), new Integer(args[3]),
					new Integer(args[4]), new Integer(args[5]) );


		gen.run();
		Flag.sw.print();
	}
}