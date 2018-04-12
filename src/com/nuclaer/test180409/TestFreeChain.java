package com.nuclaer.test180409;

import com.nuclaer.freeChain.DaughterPairMod;
import com.nuclaer.freeChain.ModTransaction;
import com.nuclaer.freeChain.SavedChainMod;

import nuclear.slithercrypto.ECDSAKey;
import nuclear.slitherge.top.io;

public class TestFreeChain {

	public static void main(String[] args) {
		io.println("Makaing ECDSA key...");
		ECDSAKey key=new ECDSAKey();
		byte[] pgm= {1,2,3,4,5,6,7,78,9};
		SavedChainMod chain = new SavedChainMod(System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/blockchain");
		byte[] lastBlockHash=SavedChainMod.genesis.getHash();
		io.println("Making file transaction...");
		DaughterPairMod t=ModTransaction.makeFile(key.getPublicKey(), key.getPrivateKey(), pgm, lastBlockHash, "GetRektBadKidz");
		chain.addPair(t);
		io.println(t.tr.toString());
		if(!t.tr.verify())
			io.println("Error: SlitherCrypto broken: Transaction invalidly invalid.");
		//chain.commit();
		chain.getCurrent().CPUmine(key.getPublicKey());
		io.println(chain.getCurrent().toString());
		io.println("block price: "+chain.getCurrent().getCost());
	}

}
