// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package scoram;

import flexsc.CompEnv;

public class SecureArray<T> {
	static final int threshold = 128;
	boolean useTrivialOram = false;
	TrivialPrivateOram<T> trivialOram = null;
	RecursiveSCORAM<T> circuitOram = null;
	public SecureArray(CompEnv<T> env, int N, int dataSize) throws Exception{
		useTrivialOram = N <= threshold;
		if(useTrivialOram)
			trivialOram = new TrivialPrivateOram<T>(env, N, dataSize);
		else 
			circuitOram = new RecursiveSCORAM<T>(env, N, dataSize, 256, 8, 3, 80);
	}
	
	public T[] read(T[] iden) throws Exception{
		if(useTrivialOram)
			return trivialOram.read(iden);
		else
			return circuitOram.read(iden);
	}
	
	public void write(T[] iden, T[] data) throws Exception{
		if(useTrivialOram)
			trivialOram.write(iden, data);
		else
			circuitOram.write(iden, data);
	}
}
