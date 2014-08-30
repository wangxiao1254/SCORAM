// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package scoram;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public class SCORAM<T> extends TreeBasedOramParty<T> {
	public SCORAMLib<T> lib;
	Block<T>[] scQueue;
	public PlainBlock[] queue;
	public int queueCapacity;
	int[] stash15 = new int[]{4, 5, 8, 13, 18, 23, 34, 50};
	@SuppressWarnings("unchecked")
	public SCORAM(InputStream is, OutputStream os, int N, int dataSize,
			Party p, int cap, Mode m, int sp) throws Exception {
		super(CompEnv.getEnv(m, p, is, os), N, dataSize, cap);
		init(env, N, dataSize, cap, sp);
	}
	
	public SCORAM(CompEnv<T> env, int N, int dataSize,
			 int cap, int sp) throws Exception {
		super(env, N, dataSize, cap);
		init(env, N, dataSize, cap, sp);
	}
	
	void init(CompEnv<T> env, int N, int dataSize,
			 int cap, int sp) throws Exception{
		lib = new SCORAMLib<T>(lengthOfIden, lengthOfPos, lengthOfData, logN, capacity, env);
		
		int l = logN;
		if(logN <= 24 && logN >= 10) {
			if(logN %2 == 1)
				l++;
			queueCapacity = (int) ((sp-15)+stash15[(l-10)/2]);
		}
		else 
			queueCapacity = (int) (sp+0.32589*logN*logN -8.7411*logN+40);//(int) ((2*t-10)/20.0*sp);

		
		queue = new PlainBlock[queueCapacity];

		for(int i = 0; i < queue.length; ++i) 
			queue[i] = getDummyBlock(p == Party.Alice);

		scQueue = prepareBlocks(queue, queue);		
	}

	protected void ControlEviction() throws Exception {
		for(int i = 0 ; i < 4; ++i)
			flushOneTime(getRandomPath());
	}


	public void flushOneTime(boolean[] pos) throws Exception {
		PlainBlock[][] blocks = getPath(pos);
		Block<T>[][] scPath = preparePath(blocks, blocks);
		
		lib.putFromQueueToPath(scPath, scQueue, pos);
		lib.flush(scPath, pos);

		blocks = preparePlainPath(scPath);
		putPath(blocks, pos);
	}


	public T[] readAndRemove(T[] scIden, boolean[] pos, boolean RandomWhenNotFound) throws Exception {
		PlainBlock[][] blocks = getPath(pos);
		Block<T>[][] scPath = preparePath(blocks, blocks);


		Block<T> res = lib.readAndRemove(scPath, scIden);
		Block<T> res2 = lib.readAndRemove(scQueue, scIden);
		res = lib.mux(res, res2, res.isDummy);
		
		blocks = preparePlainPath(scPath);
		putPath(blocks, pos);

		if(RandomWhenNotFound) {
			T[] randBooleans = lib.randBools(rng, lengthOfData);
			return lib.mux(res.data, randBooleans, res.isDummy);
		}
		else{
			return lib.mux(res.data, lib.zeros(res.data.length),res.isDummy);
		}
	}

	public void putBack(T[] scIden, T[] scNewPos, T[] scData) throws Exception {
		Block<T> b = new Block<T>(scIden, scNewPos, scData, lib.SIGNAL_ZERO);
		lib.add(scQueue, b);

		os.flush();
		ControlEviction();
	}

	public T[] read(T[] scIden, boolean[] pos, T[] scNewPos) throws Exception {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		T[] r = readAndRemove(scIden, pos, false);
		putBack(scIden, scNewPos, r);
		return r;
	}
	
	public void write(T[] scIden, boolean[] pos, T[] scNewPos, T[] scData) throws Exception {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		readAndRemove(scIden, pos, true);
		putBack(scIden, scNewPos, scData);
	}
}
