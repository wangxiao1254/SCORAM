// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package test;

import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;

import scoram.SCORAM;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;
import gc.GCSignal;


public class TestSCORAMBasic {
	final int N = 1<<7;
	final int capacity = 6;
	int[] posMap = new int[N];
	int writecount = N;
	int readcount = N;
	int dataSize = 32;
	
	public TestSCORAMBasic() {
		SecureRandom rng = new SecureRandom();
		for(int i = 0; i < posMap.length; ++i)
			posMap[i] = rng.nextInt(N);
	}
	SecureRandom rng = new SecureRandom();
	
	class GenRunnable extends network.Server implements Runnable {
		int port;
		GenRunnable (int port) {
			this.port = port;
		}
		public int[][] idens;
		public boolean[][] du;
		public int[] stash;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			try {
				listen(port);

				int data[] = new int[N+1];
				CompEnv env = CompEnv.getEnv(Mode.REAL, Party.Alice, is, os);
				SCORAM<GCSignal> client = new SCORAM<GCSignal>(env, N, dataSize,capacity, 80);
				System.out.println("logN:"+client.logN+", N:"+client.N);
				
				
				for(int i = 0; i < writecount; ++i) {
					int element = i%N;
					int oldValue = posMap[element];
					int newValue = rng.nextInt(1<<client.lengthOfPos);
					System.out.println("writing at "+element);
					data[element] = 2*element+1;
					
					GCSignal[] scNewValue = client.env.inputOfAlice(Utils.fromInt(newValue, client.lengthOfPos));
					GCSignal[] scData = client.env.inputOfAlice(Utils.fromInt(data[element], client.lengthOfData));
					GCSignal[] scIndex = client.env.inputOfAlice(Utils.fromInt(element, client.lengthOfIden));
					client.write(scIndex, Utils.fromInt(oldValue, client.lengthOfPos), scNewValue, scData);

//					os.write(0);
					posMap[element] = newValue;
//					os.flush();
				}

				for(int i = 0; i < readcount; ++i){
					int element = i%N;
					int oldValue = posMap[element];
					int newValue = rng.nextInt(1<<client.lengthOfPos);
					System.out.println("reading at "+element);

					GCSignal[] scNewValue = client.env.inputOfAlice(Utils.fromInt(newValue, client.lengthOfPos));
					GCSignal[] scIndex = client.env.inputOfAlice(Utils.fromInt(element, client.lengthOfIden));
					
					GCSignal[] scb = client.read(scIndex, Utils.fromInt(oldValue, client.lengthOfPos), scNewValue);

					boolean[] b = client.env.outputToAlice(scb);
//					os.write(0);
					posMap[element] = newValue;
//					os.flush();


					if(Utils.toInt(b) != data[element]) {
						System.out.println("inconsistent: "+element+" "+Utils.toInt(b) + " "+data[element]+" "+posMap[element]);
					}
					Assert.assertTrue(Utils.toInt(b) == data[element]);
					
				}
				
				idens = new int[client.tree.length][];
				du = new boolean[client.tree.length][];

				for(int j = 1; j < client.tree.length; ++j){
					idens[j] = new int[client.tree[j].length];
					for(int i = 0; i < client.tree[j].length; ++i)
						idens[j][i]=Utils.toInt(client.tree[j][i].iden);
					}

				for(int j = 1; j < client.tree.length; ++j){
					du[j] = new boolean[client.tree[j].length];
					for(int i = 0; i < client.tree[j].length; ++i)
						du[j][i]=client.tree[j][i].isDummy;
				}
				
				stash = new int[client.queue.length];
				for(int j = 0; j < client.queue.length; ++j)
						stash[j]=Utils.toInt(client.queue[j].iden);

				os.flush();

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	class EvaRunnable extends network.Client implements Runnable {
		String host;
		int port;
		public int[][] idens;
		public boolean[][] du;
		public int[] stash;

		EvaRunnable (String host, int port) {
			this.host =  host;
			this.port = port;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void run() {
			try {
				connect(host, port);
				
				CompEnv env = CompEnv.getEnv(Mode.REAL, Party.Bob, is, os);
				SCORAM<GCSignal> server = new SCORAM<GCSignal>(env, N, dataSize,capacity, 80);


				for(int i = 0; i < writecount; ++i) {
					int element = i%N;
					int oldValue = posMap[element];
					GCSignal[] scNewValue = server.env.inputOfAlice(new boolean[server.lengthOfPos]);
					GCSignal[] scData = server.env.inputOfAlice(new boolean[server.lengthOfData]);
					GCSignal[] scIndex = server.env.inputOfAlice(new boolean[server.lengthOfIden]);

					server.write(scIndex, Utils.fromInt(oldValue, server.lengthOfPos), scNewValue, scData);
//					is.read();
				}

				for(int i = 0; i < readcount; ++i){
					int element = i%N;
					int oldValue = posMap[element];
					GCSignal[] scNewValue = server.env.inputOfAlice(new boolean[server.lengthOfPos]);
					GCSignal[] scIndex = server.env.inputOfAlice(new boolean[server.lengthOfIden]);
					GCSignal[] scb = server.read(scIndex, Utils.fromInt(oldValue, server.lengthOfPos), scNewValue);

					server.env.outputToAlice(scb);
//					is.read();
				}
				
				idens = new int[server.tree.length][];
				du = new boolean[server.tree.length][];
				for(int j = 1; j < server.tree.length; ++j){
					idens[j] = new int[server.tree[j].length];
					for(int i = 0; i < server.tree[j].length; ++i)
						idens[j][i]=Utils.toInt(server.tree[j][i].iden);
					}

				for(int j = 1; j < server.tree.length; ++j){
					du[j] = new boolean[server.tree[j].length];
					for(int i = 0; i < server.tree[j].length; ++i)
						du[j][i]=server.tree[j][i].isDummy;
				}
				
				
				stash = new int[server.queue.length];
				for(int j = 0; j < server.queue.length; ++j)
					stash[j]=Utils.toInt(server.queue[j].iden);
				os.flush();

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	GenRunnable gen = new GenRunnable(12345);
	EvaRunnable eva = new EvaRunnable("localhost", 12345);
	@Test
	public void runThreads() throws Exception {
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start(); Thread.sleep(10);
		tEva.start();
		tGen.join();
		
//		printTree(gen,eva);
//		System.out.println(Arrays.toString(xor(gen.stash, eva.stash)));
//		System.out.print("\n");

		System.out.println();
	}
	
	public void printTree(GenRunnable gen, EvaRunnable eva) {
		int k = 1;
		int i = 1;
		for(int j = 1; j < gen.idens.length; ++j) {
			System.out.print("[");
			int[] a = xor(gen.idens[j], eva.idens[j]);
			boolean[] bb = xor(gen.du[j], eva.du[j]);
			for(int p = 0; p < eva.idens[j].length; ++p)
				if(bb[p])
					System.out.print("d,");
				else
					System.out.print(a[p]+",");
			System.out.print("]");
			if(i == k ){
				k = k*2;
				i = 0;
				System.out.print("\n");
			}
			++i;
		}
		System.out.print("\n");
	}
	
	public boolean[] xor(boolean[]a, boolean[] b) {
		boolean[] res = new boolean[a.length];
		for(int i = 0; i <res.length; ++i)
			res[i] = a[i]^b[i];
		return res;

	}

	public int[] xor(int[]a, int[] b) {
		int[] res = new int[a.length];
		for(int i = 0; i <res.length; ++i)
			res[i] = a[i]^b[i];
		return res;

	}

}