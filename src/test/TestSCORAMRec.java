// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package test;

import org.junit.Assert;

import scoram.RecursiveSCORAM;
import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.Party;
import gc.GCSignal;
public class TestSCORAMRec {

	static int writeCount = 10;
	static int readCount = 10;
	public TestSCORAMRec() {
	}

	class GenRunnable extends network.Server  implements Runnable{
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

				System.out.println("\nlogN recurFactor  cutoff capacity dataSize");
				System.out.println(logN+" "+recurFactor +" "+cutoff+" "+capacity+" "+dataSize);

				System.out.println("connected");		
				CompEnv env = CompEnv.getEnv(Mode.REAL, Party.Alice, is, os);
				RecursiveSCORAM<GCSignal> client = new RecursiveSCORAM<GCSignal>(env, N, dataSize, cutoff, recurFactor, capacity,  80);

				for(int i = 0; i < writeCount; ++i) {
					int element = i%N;

					Flag.sw.ands = 0;
					GCSignal[] scData = client.baseOram.env.inputOfAlice(Utils.fromInt(element, dataSize));
					GCSignal[] scIndex = client.baseOram.env.inputOfAlice(Utils.fromInt(element, client.lengthOfIden));

					double t1 = System.nanoTime();
					client.write(scIndex, scData);
					double t = System.nanoTime() - t1;
					Flag.sw.addCounter();
					Runtime rt = Runtime.getRuntime(); 
					double usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0;

					System.out.println("#AND gates (K): "+Flag.sw.ands/1000+
							"\nClock Time(sec) : "+t/1000000000.0 + 
							"\n#AND(K gates/second) : " +Flag.sw.ands/t*1000*1000 +
							"\nMemeory Usage(MB) : "+usedMB+
							"\n----------------------------------"
							);
				}

				for(int i = 0; i < readCount; ++i){
					int element = i%N;
					GCSignal[] scIndex = client.baseOram.env.inputOfAlice(Utils.fromInt(element, client.lengthOfIden));
					GCSignal[] scb = client.read(scIndex);
					boolean[] b = client.baseOram.env.outputToAlice(scb);


					if(Utils.toInt(b) != element)
						System.out.println("inconsistent: "+element+" "+Utils.toInt(b));
					Assert.assertTrue(Utils.toInt(b) == element);
					Flag.sw.addCounter();
				}

				os.flush();


				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	class EvaRunnable extends network.Client implements Runnable{

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
				System.out.println("\nlogN recurFactor  cutoff capacity dataSize");
				System.out.println(logN+" "+recurFactor +" "+cutoff+" "+capacity+" "+dataSize);
				System.out.println("connected");
				CompEnv env = CompEnv.getEnv(Mode.REAL, Party.Bob, is, os);
				RecursiveSCORAM<GCSignal> server = new RecursiveSCORAM<GCSignal>(env, N, dataSize, cutoff, recurFactor, capacity,  80);
				for(int i = 0; i < writeCount; ++i) {

					GCSignal[] scData = server.baseOram.env.inputOfAlice(new boolean[dataSize]);
					GCSignal[] scIndex = server.baseOram.env.inputOfAlice(new boolean[server.lengthOfIden]);

					server.write(scIndex, scData);
					Flag.sw.addCounter();
				}


				for(int i = 0; i < readCount; ++i) {
					GCSignal[] scIndex = server.baseOram.env.inputOfAlice(new boolean[server.lengthOfIden]);
					GCSignal[] scb = server.read(scIndex);
					server.baseOram.env.outputToAlice(scb);
					Flag.sw.addCounter();
				}

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}