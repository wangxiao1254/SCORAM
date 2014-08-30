// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package test;


import test.TestSCORAMRec.EvaRunnable;
import flexsc.Flag;


public class TestSCORAMClient {

	public static void main(String [ ] args) {
		TestSCORAMRec s = new TestSCORAMRec();
		EvaRunnable eva;
		if(args.length < 2)
		{
//			eva = s.new EvaRunnable("localhost", 12345);
			System.out.println("Usage: \t java  -cp bin:lib test.TestSCORAMClient ip port\n\n"
					+ "Example:\n "
					+ "java  -cp bin:lib/* test.TestSCORAMClient localhost 12345");
			return;
		}
		else 
			eva = s.new EvaRunnable(args[0], new Integer(args[1]));
		eva.run();
		Flag.sw.print();
	}
}