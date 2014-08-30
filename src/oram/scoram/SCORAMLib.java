// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package scoram;

import flexsc.CompEnv;

public class SCORAMLib<T> extends BucketLib<T> {

	int logN;
	int capacity;
	public SCORAMLib(int lengthOfIden, int lengthOfPos, int lengthOfData, int logN, int capacity, CompEnv<T> e) {
		super(lengthOfIden, lengthOfPos, lengthOfData, e);
		this.logN = logN;
		this.capacity = capacity;
	}
	
	public void flush(Block<T>[][] scPath, boolean[] path) throws Exception {
		T[] pathSignal =  env.newTArray(path.length);
		for(int i = 0; i < path.length; ++i)
			pathSignal[i] = path[i] ? SIGNAL_ONE : SIGNAL_ZERO;

		for(int i = 0; i < logN-1; ++i) {
			T[][] max = DeepestBlock(scPath[i], pathSignal);
			T[] maxIden = max[0];
			T[] maxdepth= max[1];

//			T cannotPush = leq(maxdepth, toSignals(i, maxdepth.length));
			T cannotPush = geq(maxdepth, toSignals((1<<i)-1, maxdepth.length));

			T full = SIGNAL_ONE;
			for(int j = 0; j < scPath[i+1].length; ++j)
				full = and(full, not(scPath[i+1][j].isDummy));

			Block<T> block = conditionalReadAndRemove(scPath[i], maxIden, and(not(cannotPush), not(full)));
			add(scPath[i+1], block);
		}
	}

	public void putFromQueueToPath(Block<T>[][] path, Block<T>[] stash, boolean[] pos) throws Exception{
		T[] posInSignal = env.newTArray(pos.length);//new Signal[lengthOfPos];
		for(int i = 0; i < pos.length; ++i)
			posInSignal[i] = pos[i] ? SIGNAL_ONE : SIGNAL_ZERO;


		T[][] max = DeepestBlock(stash, posInSignal);
		T[] maxIden = max[0];
		
		Block<T> b = readAndRemove(stash, maxIden);
		T pushed = b.isDummy;
		
		T[] canPushm = deepestLevelArray(b.pos, posInSignal);//,blockInSignal[k][blockInSignal[k].length-1]);

		for(int i = logN-1; i >=0; --i) {
			for(int j = 0; j < path[i].length; ++j) {
				T canPush = and(path[i][j].isDummy, canPushm[i]);
				T toPush = and(not(pushed), canPush);
				path[i][j] = mux(path[i][j], b, toPush);
				pushed = or(pushed, toPush);
			}
		}
		conditionalAdd(stash, b, not(pushed));
	}
	
	public T[] deepestLevel(T[] pos, T[] path, T isDummy) throws Exception {
		T[] xored = xor(pos, path);
		T[] deep = leadingZeros(xored);
		return mux(deep, zeros(deep.length), isDummy);
	}

	public T[][] DeepestBlock(Block<T>[] bucket, T[] pos) throws Exception {
		T[][] deepest = env.newTArray(bucket.length, 0);//;new T[nodeCapacity][];
		for(int j = 0; j < bucket.length; ++j){
			deepest[j] = deepestLevelArray(bucket[j].pos, pos);
		}

		T[] maxIden = bucket[0].iden;
		T[] maxdepth = deepest[0];
		T isDummy = bucket[0].isDummy;
		for(int j = 1; j < bucket.length; ++j) {
			T greater = geq(deepest[j], maxdepth);
			greater = and(greater, not(bucket[j].isDummy));
			maxIden = mux(maxIden, bucket[j].iden, greater);
			maxdepth = mux(maxdepth, deepest[j], greater);
			isDummy = mux(isDummy, bucket[j].isDummy, greater);
		}
		T[][] result = env.newTArray(3, 0);
		result[0] = maxIden;
		result[1] = maxdepth;
		result[2] = env.newTArray(1);
		result[2][0] = isDummy;
		return result;
	}
	
	public T[] deepestLevelArray(T[] pos, T[] path) throws Exception {
		T[] result = xor(pos, path); 
		for(int i = result.length-2; i >=0; --i) {
			result[i] = or(result[i], result[i+1]);
		}
		T[] result2 = env.newTArray(result.length+1);
		
		for(int i = 0; i < result.length; ++i)
			result2[i+1] = not(result[result.length-i-1]);
		result2[0] = SIGNAL_ONE;
		return result2;
	}
}