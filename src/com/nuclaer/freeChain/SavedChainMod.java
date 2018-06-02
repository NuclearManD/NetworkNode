package com.nuclaer.freeChain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.DaughterPair;
import nuclear.slitherge.top.io;
import nuclear.slitherio.uint256_t;

public class SavedChainMod extends BlockchainBase{
	protected BlockListFileMod chain;
	BlockListFileMod daughters;
	private BlockMod current;
	public static final BlockMod genesis = new BlockMod(new byte[91], new byte[32], new uint256_t("771947261582107967251640281103336579920368336826869405186543784860581888"), new byte[0]);
	public SavedChainMod(String storeDir) {
		chain=new BlockListFileMod(storeDir+"/chain");
		daughters=new BlockListFileMod(storeDir+"/daugt");
		setup(chain,daughters);
		setCurrent(new BlockMod(new byte[32],new byte[32],new uint256_t("771947261582107967251640281103336579920368336826869405186543784860581888"),new byte[0]));
	}

	synchronized public void addPair(DaughterPair p) {
		DaughterPairMod pair=(DaughterPairMod)p;
		addTransaction(pair.tr);
		daughters.addBlock(pair.block);
	}
	synchronized public byte[] readFile(String meta,byte[] pubAdr) {
		for(int j=chain.length()-1;j>=0;j--) {
			Block b=chain.get(j);
			BlockMod block=(BlockMod)b;
			for(int i=block.numTransactions()-1;i>=0;i--) {
				ModTransaction t=block.getTransaction(i);
				String tmeta=new String(t.getMeta(),StandardCharsets.UTF_8);
				if(t.type==ModTransaction.TRANSACTION_STORE_FILE&&Arrays.equals(t.pubKey, pubAdr)&&tmeta.equals(meta))
					return getDaughter(Arrays.copyOf(t.descriptor,32)).getData();
			}
		}
		return null;
	}
	synchronized public Block getDaughter(byte[] hash) {
		daughters.update(); // in case another program added more
		for(Block b:daughters) {
			BlockMod i=(BlockMod)b;
			if(Arrays.equals(i.getHash(),hash))return i;
		}
		return null;
	}
	synchronized public void commit(byte[] key) {
		long hashes=0;
		long mil=System.currentTimeMillis();
		while(!getCurrent().mineOnce(key)) {
			hashes++;
			if(System.currentTimeMillis()-mil>3000) {
				io.println(hashes/(System.currentTimeMillis()-mil)+" KH/s...");
				hashes=0;
				mil=System.currentTimeMillis();
			}
		}
		commit();
	}
	synchronized public void addTransaction(ModTransaction t) {
		getCurrent().addTransaction(t);
	}
	synchronized public boolean addBlock(Block b){
		BlockMod block=(BlockMod)b;
		chain.update(); // in case some other program added to the blockchain first
		if(block==null)
			return false;
		if(block.verify()&&(chain.length()==0||Arrays.equals(chain.get(chain.length()-1).getHash(),block.getLastHash())))
			chain.addBlock(block);
		else {
			io.println("Block last hash is :           "+Base64.getEncoder().encodeToString(block.getLastHash()));
			io.println("Block last hash does not match "+Base64.getEncoder().encodeToString(chain.get(chain.length()-1).getHash()));
			return false;
		}
		getCurrent().setLastBlockHash(block.getHash());
		return true;
	}
	synchronized public int length() {
		return chain.length();
	}
	synchronized public Block getBlockByIndex(int index) {
		return chain.get(index);
	}
	synchronized public void commit() {
		chain.addBlock(getCurrent());
		setCurrent(new BlockMod(new byte[32],chain.get(chain.length()-1).getHash(),new uint256_t("771947261582107967251640281103336579920368336826869405186543784860581888"),new byte[0]));
	}
	synchronized public Block getCurrent() {
		return current;
	}
	synchronized public void setCurrent(BlockMod current) {
		this.current = current;
	}
	public void update(){
		chain.update();
		daughters.update();
	}
}
