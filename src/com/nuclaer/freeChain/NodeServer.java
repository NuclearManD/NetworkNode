package com.nuclaer.freeChain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import com.nuclaer.nnutil.Logger;

import nuclear.blocks.node.SavedList;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.DaughterPair;
import nuclear.slithercrypto.blockchain.SavedChain;
import nuclear.slithercrypto.blockchain.Transaction;
import nuclear.slitherge.top.io;
import nuclear.slitherio.SlitherS;
import nuclear.slithernet.Server;

public class NodeServer extends Server {
	public static final byte CMD_ADD_PAIR = 5;
	public static final byte CMD_ADD_TRANS = 1;
	public static final byte CMD_GET_BLOCK = 2;
	public static final byte[] RESULT_SUCCESS = "OK".getBytes(StandardCharsets.UTF_8);
	public static final byte CMD_GET_DAUGHTER = 3;
	public static final byte CMD_GET_BLOCKS = 4;
	public static final byte CMD_ADD_NODE = 5;
	public static final byte CMD_GET_NODES= 6;
	private static final int RESPONSE_SIZE_LIMIT = 1024*1024;
	public BlockchainBase blockchain;
	byte[] pubkey;
	private ECDSAKey key;
	Logger log=new Logger("Public Node");
	
	SavedList nodes;
	
	public NodeServer(ECDSAKey key) {
		super(1152);
		pubkey=key.getPublicKey();
		this.key=key;
		log.println("Loading blockchain...");
		blockchain=new SavedChain(System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/blockchain");
		nodes = new SavedList(System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/nodes.txt");
		log.println("Loaded; blockchain contains "+blockchain.length()+" normal blocks.");
		log.println("Node public key: "+Base64.getEncoder().encodeToString(pubkey));
		log.println("Node balance: "+blockchain.getCoinBalance(pubkey)+" KiB ");
		if(blockchain.getPriority(pubkey)==100){
			log.println("Node needs to register!  Registering now...");
			blockchain.addTransaction(Transaction.register(pubkey, key.getPrivateKey()));
		}
		try {
			start();
		} catch (IOException e) {
			log.println("Could not bind port");
		}
	}
	public byte[] easyServe(byte[] in, String ip) {
		byte cmd=in[0];
		byte[] response="OK".getBytes(StandardCharsets.UTF_8);
		byte data[]=Arrays.copyOfRange(in,1,in.length);
		//double secs=System.currentTimeMillis();
		if(cmd==CMD_ADD_PAIR) {
			log.println("Request to add Daughter Pair...");
			DaughterPair pair=DaughterPair.deserialize(data);
			log.println("Deserialized...");
			if(pair.verify()) {
				blockchain.addPair(pair);
				log.println("Added Daughter Pair:");
				log.println(pair.toString());
			}else {
				response="INVALID".getBytes(StandardCharsets.UTF_8);
				log.println("Invalid Pair Submission for daughter "+pair.block.toString());
				log.println(pair.tr.toString());
			}
		}else if(cmd==CMD_ADD_TRANS) {
			Transaction t=new Transaction(data);
			if(t.verify()) {
				blockchain.addTransaction(t);
				log.println("Added Transaction:");
				log.println(t.toString());
			}else {
				response="INVALID".getBytes(StandardCharsets.UTF_8);
				log.println("Invalid Transaction: "+t.toString());
			}
		}else if(cmd==CMD_ADD_NODE) {
			log.println("Node reporting: IP address "+ip);
			int l=nodes.length();
			boolean go=!ip.equals("68.4.23.94");
			for(int i=0;i<l;i++){
				if(nodes.get(i).equals(ip))
					go=false;
			}
			if(go)nodes.add(ip);
		}else if(cmd==CMD_GET_NODES) {
			response=nodes.serialize();
		}else if(cmd==CMD_GET_BLOCK&&data.length==8){
			int index=(int)SlitherS.bytesToLong(data);
			log.println("Sending block "+index+" to client...");
			if(index<blockchain.length())
				response=blockchain.getBlockByIndex(index).pack();
			else{
				log.println("Blockchain is not as long as "+index+" blocks.");
				response=new byte[1];
				response[0]=0x55;
			}
		}else if(cmd==CMD_GET_BLOCKS&&data.length==8){
			int index=(int)SlitherS.bytesToLong(data);
			if(index>=blockchain.length()){
				log.println("Client requested block "+index+" onward.  Those blocks do not exist.");
				response=new byte[1];
				response[0]=0x55;
			}else{
				int num = index;
				ByteArrayOutputStream stream =new ByteArrayOutputStream();
				for(int i=index;i<blockchain.length();i++){
					byte[] packed=blockchain.getBlockByIndex(i).pack();
					if(packed.length+stream.size()>RESPONSE_SIZE_LIMIT)
						break;
					num++;
					try {
						stream.write(SlitherS.longToBytes(packed.length));
						stream.write(packed);
					} catch (Exception e) {
						e.printStackTrace();
						response=new byte[1];
						response[0]=0x55;
					}
				}
				log.println("Sending blocks "+index+"-"+num+" to client...");
				response=stream.toByteArray();
			}
		}else if(cmd==CMD_GET_DAUGHTER&&data.length==32){
			log.println("Sending daughter block "+Base64.getEncoder().encodeToString(data)+" to client...");
			Block block=blockchain.getDaughter(data);
			if(block==null){
				log.println("Daughter block not found!");
				response="ERR".getBytes(StandardCharsets.UTF_8);
			}else
				response=block.pack();
		}else
			response="UREC".getBytes(StandardCharsets.UTF_8);
		//secs=System.currentTimeMillis()-secs;
		//secs=secs/1000.0;
		return response;
	}
	public void start() throws IOException{
		try{
			new Thread(new Runnable(){
				@Override
				public void run() {
					if(blockchain.length()==0){
						Block block=new Block(new byte[32],new byte[0]);
						block.sign(key);
						blockchain.addBlock(block);
					}
					while(true){
						int bt=(int) (System.currentTimeMillis()/1000-blockchain.getBlockByIndex(blockchain.length()-1).getTimestamp());
						if(blockchain.getPriority(pubkey)<bt){
							blockchain.getCurrent().setLastBlockHash(blockchain.getBlockByIndex(blockchain.length()-1).getHash());
							blockchain.getCurrent().sign(key);
							if(blockchain.commit())
								log.println("Signed block "+Base64.getEncoder().encodeToString(blockchain.getBlockByIndex(blockchain.length()-1).getHash()));
							else
								log.println("ERROR COMMITING BLOCK!");
						}
						io.waitMillis(15000);
							
					}
				}
				
			}).start();
		}catch(Exception e){
			log.println("ERROR STARTING VERIFIER THREAD!");
			e.printStackTrace();
		}
		log.println("Opening port "+port);
		sok = new ServerSocket(port);
		log.println("Starting Thread...");
		NodeServer q=this;
		new Thread(new Runnable() {
			public void run() {
				log.println("Node started on port "+port);
				while(true) {
					try {
						tmpsok = sok.accept();
						new Thread(q).start();
					}   
					catch (Exception e) {
						log.println(e.getMessage());
					}
				}
			}
		}).start();
	}
	
	protected void onError(Exception e) {
		log.println("ERROR: "+e.getMessage());
		e.printStackTrace();
	}
}
