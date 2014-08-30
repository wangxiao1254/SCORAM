// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package test;

import org.junit.Assert;

import scoram.RecursiveSCORAM;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public class VerifySCORAM {

	public static void main(String[] args) throws InterruptedException {

		if(args.length < 8){
			System.out.println("Usage: \t\t"
					+ "java  -cp bin:lib test.VerifySCORAM port logN bucketSize payloadSize recursionFactor logCutOff writeCount readCount\n"
					+ "port:\t port number used by this program\n"
					+ "logN:\t log(#blocks in SCORAM)\n"
					+ "bucketSize:\t #blocks in each bucket\n"
					+ "payloadSize:\t #bits in each data block\n"
					+ "recursionFactor:\t #entries grouped together in recursion level\n"
					+ "logCutOff\t: log(#entries where the recursion stops)\n"
					+ "writeCount\t: #write to perform"
					+ "readCount\t: #read to perform"
					+ "Example:\n "
					+ "java  -cp bin:lib/* test.VerifySCORAM 12345 7 6 32 8 5 200 200");
			return ;
		}
		writeCount = new Integer(args[6]);
		readCount = new Integer(args[7]);
		GenRunnable	gen =  new GenRunnable(new Integer(args[0]), new Integer(args[1]), 
				new Integer(args[2]), new Integer(args[3]),
				new Integer(args[4]), new Integer(args[5]) );

		EvaRunnable eva = new EvaRunnable("localhost", 12345);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start(); Thread.sleep(10);
		tEva.start();
		tGen.join();

	}

	static int writeCount;
	static int readCount;
	public VerifySCORAM() {
	}

	static class GenRunnable extends network.Server  implements Runnable{
		int port;
		int logN;
		int N;
		int recurFactor;
		int cutoff;
		int capacity;
		int dataSize;
		int logCutoff;

		GenRunnable (int port, int logN, int capacity, int dataSize, int recurFactor, int logCutoff) {
			this.port = port;
			this.logN = logN;
			this.N = 1<<logN;
			this.recurFactor = recurFactor;
			this.logCutoff = logCutoff;
			this.cutoff = 1<<logCutoff;
			this.dataSize = dataSize;
			this.capacity = capacity;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			try {
				listen(port);

				os.write(logN);
				os.write(recurFactor);
				os.write(logCutoff);
				os.write(capacity);
				os.write(dataSize);
				os.flush();


				CompEnv env = CompEnv.getEnv(Mode.VERIFY, Party.Alice, is, os);
				RecursiveSCORAM<Boolean> client = new RecursiveSCORAM<Boolean>(env, N, dataSize, cutoff, recurFactor, capacity,  80);

				for(int i = 0; i < writeCount; ++i) {
					int element = i%N;
					Boolean[] scData = client.baseOram.env.inputOfAlice(Utils.fromInt(element, dataSize));
					Boolean[] scIndex = client.baseOram.env.inputOfAlice(Utils.fromInt(element, client.lengthOfIden));
					System.out.println("writing "+i);
					client.write(scIndex, scData);
				}

				for(int i = 0; i < readCount; ++i){
					int element = i%N;
					Boolean[] scIndex = client.baseOram.env.inputOfAlice(Utils.fromInt(element, client.lengthOfIden));
					Boolean[] scb = client.read(scIndex);
					System.out.println("reading "+i);

					boolean[] b = client.baseOram.env.outputToAlice(scb);

					if(writeCount >= N || i < writeCount) {
						if(Utils.toInt(b) != element)
							System.out.println("inconsistent: "+element+" "+Utils.toInt(b));
						Assert.assertTrue(Utils.toInt(b) == element);
					}
				}

				os.flush();


				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("All accesses are successful!");
		}
	}

	static class EvaRunnable extends network.Client implements Runnable{

		String host;		
		int port;		
		EvaRunnable (String host, int port) {
			this.host = host;
			this.port = port;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void run() {
			try {
				connect(host, port);

				int logN = is.read();
				int recurFactor = is.read();
				int logCutoff = is.read();
				int cutoff = 1<<logCutoff;
				int capacity = is.read();
				int dataSize = is.read();

				int N = 1<<logN;

				CompEnv env = CompEnv.getEnv(Mode.VERIFY, Party.Bob, is, os);
				RecursiveSCORAM<Boolean> server = new RecursiveSCORAM<Boolean>(env, N, dataSize, cutoff, recurFactor, capacity,  80);
				for(int i = 0; i < writeCount; ++i) {
					Boolean[] scData = server.baseOram.env.inputOfAlice(new boolean[dataSize]);
					Boolean[] scIndex = server.baseOram.env.inputOfAlice(new boolean[server.lengthOfIden]);
					server.write(scIndex, scData);
				}


				for(int i = 0; i < readCount; ++i) {
					Boolean[] scIndex = server.baseOram.env.inputOfAlice(new boolean[server.lengthOfIden]);
					Boolean[] scb = server.read(scIndex);
					server.baseOram.env.outputToAlice(scb);
				}

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}