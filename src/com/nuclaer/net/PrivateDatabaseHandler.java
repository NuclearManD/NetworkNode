package com.nuclaer.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockChainManager;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.DaughterPair;
import nuclear.slithercrypto.blockchain.Transaction;
import nuclear.slitherge.top.io;
import nuclear.slitherio.SlitherS;
import nuclear.slithernet.Server;

public class PrivateDatabaseHandler extends Server {

	public static final byte MARK_ERR = 0x76;
	public static final byte MARK_OK_ = 0x00;
	public static final byte MARK_TX_ = 0x01;

	public static final byte CMD_READFILE		= 0x01;
	public static final byte CMD_GETBYHASH		= 0x05;
	public static final byte CMD_GETBYINDEX		= 0x02;
	public static final byte CMD_UPLOADBLOCK	= 0x03;
	public static final byte CMD_UPLOADTRANS	= 0x04;
	
	private byte[] auth;
	private ECDSAKey key;
	private BlockchainBase bc;
	public PrivateDatabaseHandler(int port, ECDSAKey k, BlockchainBase c, byte[] passcode) {
		super(port);
		auth=Arrays.copyOf(passcode, 32);
		bc=c;
		key=k;
		io.println("Database IFace loaded.");
	}
	
	protected byte[] easyServe(byte[] in){
		byte[] response={MARK_ERR};
		byte[] data=decrypt(auth,in);
		byte[] args=Arrays.copyOfRange(data, 1, data.length);
		io.println("Database request: CMD="+data[0]);
		if(data[0]==CMD_READFILE){
			String filename=new String(args,StandardCharsets.UTF_8);
			io.println("Database : request for file '"+filename+"'");
			byte[] tmp=bc.readFile(filename, key.getPublicKey());
			if(tmp!=null){
				response=new byte[tmp.length+1];
				for(int i=0;i<tmp.length;i++)
					response[i+1]=tmp[i];
				response[0]=MARK_TX_;
				io.println("Database : found file, sending...");
			}else{
				io.println("Database file request : file not found!");
				response=null;
			}
		}else if(data[0]==CMD_GETBYHASH){
			Block tmp=bc.getDaughter(Arrays.copyOf(args,32));
			if(tmp!=null){
				response=new byte[tmp.getData().length+1];
				for(int i=0;i<tmp.getData().length;i++)
					response[i+1]=tmp.getData()[i];
				response[0]=MARK_TX_;
			}else
				response=null;
		}else if(data[0]==CMD_GETBYINDEX){
			Block tmp=bc.getBlockByIndex((int)SlitherS.bytesToLong(Arrays.copyOf(args, 8)));
			if(tmp!=null){
				response=new byte[tmp.getData().length+1];
				for(int i=0;i<tmp.getData().length;i++)
					response[i+1]=tmp.getData()[i];
				response[0]=MARK_TX_;
			}else
				response=null;
		}else if(data[0]==CMD_UPLOADBLOCK){
			DaughterPair pair=Transaction.makeFile(key.getPublicKey(), key.getPublicKey(),
					Arrays.copyOfRange(args, 16, args.length), bc.getCurrent().getHash(), new String(Arrays.copyOf(args, 16),StandardCharsets.UTF_8));
			bc.addPair(pair);
			response[0]=MARK_OK_;
		}else if(data[0]==CMD_UPLOADTRANS){
			Transaction t=new Transaction(key.getPublicKey(),args,args[0]);
			bc.addTransaction(t);
			response[0]=MARK_OK_;
		}
		if(response==null){
			response=new byte[1];
			response[0]=MARK_ERR;
		}
		return encrypt(auth,response);
	}

	protected static byte[] encrypt(byte[] pass, byte[] data){
		byte[] retval=new byte[data.length];
		for(int i=0;i<data.length;){
			for(int j=0;j<(pass[0]+1)&&i<data.length;j++){
				retval[i]=(byte) ((pass[(j+1)%32]+data[i])^pass[i%32]);
				i++;
			}
		}
		return retval;
	}
	protected static byte[] decrypt(byte[] pass, byte[] data){
		byte[] retval=new byte[data.length];
		for(int i=0;i<data.length;){
			for(int j=0;j<(pass[0]+1)&&i<data.length;j++){
				retval[i]=(byte) (-pass[(j+1)%32]+(data[i]^pass[i%32]));
				i++;
			}
		}
		return retval;
	}
	public static void main(String [] args){
		byte[] q={};
		new PrivateDatabaseHandler(6609,new ECDSAKey(),new BlockChainManager(),q);
	}
}
