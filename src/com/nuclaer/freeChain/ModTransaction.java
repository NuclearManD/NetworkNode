package com.nuclaer.freeChain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.DaughterPair;
import nuclear.slithercrypto.blockchain.Transaction;
import nuclear.slitherio.uint256_t;

public class ModTransaction extends Transaction {

	public ModTransaction(byte[] packed) {
		super(packed);
	}

	public ModTransaction(byte[] publicKey, byte[] descriptr, byte t) {
		super(publicKey, descriptr, t);
	}
	
	public double getTransactionCost(){
		return 0;
	}
	public static DaughterPairMod makeFile(byte[] publickey,byte[] priKey, byte[] program_data,byte[] lastBlockHash,String meta) {
		ECDSAKey key=new ECDSAKey(publickey,priKey);
		byte data[]=new byte[TRANSACTION_LENGTH];
		BlockMod tmp=new BlockMod(publickey,lastBlockHash,new uint256_t("771947261582107967251640281103336579920368336826869405186543784860581888"),program_data);
		tmp.CPUmine(publickey);
		int n=0;
		for(byte i:tmp.getHash()) {
			data[n]=i;
			n++;
		}
		byte[] byte_meta=meta.getBytes(StandardCharsets.UTF_8);
		data[32]=(byte) byte_meta.length;
		n=33;
		for(byte i:byte_meta){
			data[n]=i;
			n++;
		}
		n=data.length-SIG_LEN;
		byte[] sig=key.sign(Arrays.copyOf(data, TRANSACTION_LENGTH-SIG_LEN));
		for(byte i:sig) {
			data[n]=i;
			n++;
		}
		return new DaughterPairMod(new ModTransaction(publickey,data,TRANSACTION_STORE_FILE),tmp);
	}

}
