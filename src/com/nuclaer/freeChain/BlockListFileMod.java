package com.nuclaer.freeChain;

import java.io.FileInputStream;

import nuclear.slithercrypto.blockchain.BlockListFile;

public class BlockListFileMod extends BlockListFile {

	public BlockListFileMod(String pathname) {
		super(pathname);
	}

	synchronized public BlockMod get(int index) {
		try {
			long start=blockStarts[index];
			int length=(int)blockLengths[index];
			FileInputStream chainBlocks=new FileInputStream(dir+".dat");
			chainBlocks.skip(start);
			byte[] data=new byte[length];
			for(int i=0;i<length;i++) {
				int q=chainBlocks.read();
				if(q==-1)
					i--;
				else
					data[i]=(byte)q;
			}
			chainBlocks.close();
			return new BlockMod(data);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
