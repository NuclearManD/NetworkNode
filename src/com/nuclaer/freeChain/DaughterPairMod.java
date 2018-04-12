package com.nuclaer.freeChain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.DaughterPair;
import nuclear.slithercrypto.blockchain.Transaction;

public class DaughterPairMod extends DaughterPair{

	public ModTransaction tr;
	public BlockMod block;
	public DaughterPairMod(ModTransaction t, BlockMod d) {
		super(t,d);
		tr=t;
		block=d;
	}
	public boolean verify() {
		if(!block.verify()) {
		}else if(!tr.verify()) {
		}else if(Arrays.equals(Arrays.copyOf(tr.descriptor,32),block.getHash()))
			return true;
		else {
		}
		return false;
	}
	public byte[] serialize() {
		byte trp[]=tr.pack();
		byte bkp[]=block.pack();
		byte out[]=new byte[Transaction.PACKED_LEN+bkp.length];
		for(int i=0;i<Transaction.PACKED_LEN;i++)
			out[i]=trp[i];
		for(int i=Transaction.PACKED_LEN;i<out.length;i++)
			out[i]=bkp[i-Transaction.PACKED_LEN];
		return out;
	}
	public static DaughterPair deserialize(byte[] data) {
		byte trp[]=Arrays.copyOf(data, Transaction.PACKED_LEN);
		byte bkp[]=Arrays.copyOfRange(data, Transaction.PACKED_LEN,data.length);
		return new DaughterPair(new Transaction(trp),new Block(bkp));
	}
	public String toString() {
		return "Pair named '"+new String(tr.getMeta(),StandardCharsets.UTF_8)+"' created by "+Base64.getEncoder().encodeToString(tr.pubKey)+"\n with hash: "+Base64.getEncoder().encodeToString(block.getHash());
	}
}
