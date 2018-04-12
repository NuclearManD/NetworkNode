package com.nuclaer.freeChain;

import java.util.Arrays;

import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.Transaction;
import nuclear.slitherio.uint256_t;

public class BlockMod extends Block {

	public BlockMod(byte[] packed) {
		super(packed);
	}

	public BlockMod(byte[] miner, byte[] lastblock, uint256_t diff, byte[] data) {
		super(miner, lastblock, diff, data);
	}

	public BlockMod(byte[] lastblock, uint256_t diff, byte[] data) {
		super(lastblock, diff, data);
	}
	synchronized public ModTransaction getTransaction(int index){
		index=index*Transaction.PACKED_LEN;
		if(index>data.length)
			return null;
		return new ModTransaction(Arrays.copyOfRange(data,index,index+Transaction.PACKED_LEN));
	}
	synchronized public void addTransaction(ModTransaction t){
		int index=data.length;
		byte[] tmp=new byte[index+Transaction.PACKED_LEN];
		for(int i=0;i<data.length;i++) {
			tmp[i]=data[i];
		}
		byte[] packed=t.pack();
		for(int i=0;i<Transaction.PACKED_LEN;i++) {
			tmp[i+index]=packed[i];
		}
		data=tmp;
	}
	public double getCost() {
		return 0;
	}

}
