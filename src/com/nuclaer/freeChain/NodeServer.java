package com.nuclaer.freeChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import nuclear.blocks.node.NodeMiner;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.DaughterPair;
import nuclear.slithercrypto.blockchain.SavedChain;
import nuclear.slithercrypto.blockchain.Transaction;
import nuclear.slitherge.top.io;
import nuclear.slitherio.CLILogger;
import nuclear.slitherio.SlitherS;
import nuclear.slithernet.Server;

public class NodeServer extends Server {
	public static final byte CMD_ADD_PAIR = 5;
	public static final byte CMD_ADD_TRANS = 1;
	public static final byte CMD_GET_BLOCK = 2;
	public static final byte[] RESULT_SUCCESS = "OK".getBytes(StandardCharsets.UTF_8);
	public static final byte CMD_GET_DAUGHTER = 3;
	BlockchainBase blockchain;
	byte[] pubkey;
	protected Thread minerThread;
	protected NodeMiner minerObject;
	public NodeServer(byte[] Key) {
		super(1152);
		io.println("Starting...");
		io.println("Loading blockchain...");
		blockchain=new SavedChain(System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/blockchain");
		io.println("Loaded; blockchain contains "+blockchain.length()+" normal blocks.");
		io.println("Node public key: "+Base64.getEncoder().encodeToString(Key));
		io.println("Node balance: "+blockchain.getCoinBalance(Key)+" KiB ");
		pubkey=Key;
		minerObject=new NodeMiner(blockchain,new CLILogger(),true,pubkey);
		minerThread=new Thread(minerObject);
		minerThread.start();
	}
	public NodeServer(int bt, byte[] Key) {
		super(1152);
		io.println("Starting...");
		io.println("Loading blockchain...");
		blockchain=new SavedChain(System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/blockchain");
		io.println("Loaded; blockchain contains "+blockchain.length()+" normal blocks.");
		io.println("Node public key: "+Base64.getEncoder().encodeToString(Key));
		io.println("Node balance: "+blockchain.getCoinBalance(Key)+" KiB ");
		pubkey=Key;
		minerObject=new NodeMiner(blockchain,new CLILogger(),true,pubkey, bt);
		minerThread=new Thread(minerObject);
		minerThread.start();
	}
	public byte[] easyServe(byte[] in) {
		byte cmd=in[0];
		byte[] response="OK".getBytes(StandardCharsets.UTF_8);
		byte data[]=Arrays.copyOfRange(in,1,in.length);
		if(cmd==CMD_ADD_PAIR) {
			DaughterPair pair=DaughterPair.deserialize(data);
			if(pair.verify()) {
				blockchain.addPair(pair);
				io.println("Added Daughter Pair:");
				io.println(pair.toString());
			}else {
				response="INVALID".getBytes(StandardCharsets.UTF_8);
				log("CLIENT_ERR","Invalid Pair Submission for daughter "+pair.block.toString());
				log(" > ",pair.tr.toString());
			}
		}else if(cmd==CMD_ADD_TRANS) {
			Transaction t=new Transaction(data);
			if(t.verify()) {
				blockchain.addTransaction(t);
				io.println("Added Transaction:");
				io.println(t.toString());
			}else {
				response="INVALID".getBytes(StandardCharsets.UTF_8);
				log("CLIENT_ERR","Invalid Transaction: "+t.toString());
				log(" > ",t.toString());
			}
		}else if(cmd==CMD_GET_BLOCK&&data.length==8){
			int index=(int)SlitherS.bytesToLong(data);
			io.println("Sending block "+index+" to client...");
			if(index<blockchain.length())
				return blockchain.getBlockByIndex(index).pack();
			else{
				io.println("Blockchain is not as long as "+index+" blocks.");
				response=new byte[1];
				response[0]=0x55;
			}
		}else if(cmd==CMD_GET_DAUGHTER&&data.length==32){
			io.println("Sending daughter block "+Base64.getEncoder().encodeToString(data)+" to client...");
			Block block=blockchain.getDaughter(data);
			if(block==null){
				io.println("Daughter block not found!");
				response="ERR".getBytes(StandardCharsets.UTF_8);
			}else
				response=block.pack();
		}else
			response="UREC".getBytes(StandardCharsets.UTF_8);
		return response;
	}
	private void log(String string, String string2) {
		io.println(string+"  : "+string2);
	}
}
