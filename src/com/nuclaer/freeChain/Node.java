package com.nuclaer.freeChain;

import java.io.File;
import java.util.List;

import com.nuclaer.net.NetworkRelay;
import com.nuclaer.net.PrivateDatabaseHandler;

import nuclear.blocks.node.ExternalNode;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slitherge.top.io;

public class Node implements Runnable {
	NodeServer server;
	List<ExternalNode> nodes;
	ECDSAKey key;
	private byte[] auth=new byte[32];
	String keypath=System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/keys/main.key";
	public Node() {
		if(new File(keypath).exists())
			key=new ECDSAKey(keypath);
		else{
			new File(keypath).getParentFile().mkdirs();
			key=new ECDSAKey();
			key.save(keypath);
		}
		server=new NodeServer(30000, key.getPublicKey());
		new Thread(new NetworkRelay(1153)).start();
		new Thread(new PrivateDatabaseHandler(6609, key, server.blockchain, auth)).start();
	}
	public Node(int bt) {
		if(new File(keypath).exists())
			key=new ECDSAKey(keypath);
		else{
			new File(keypath).getParentFile().mkdirs();
			key=new ECDSAKey();
			key.save(keypath);
		}
		server=new NodeServer(bt, key.getPublicKey());
		new Thread(new NetworkRelay(1153)).start();
		new Thread(new PrivateDatabaseHandler(6609, key, server.blockchain, auth)).start();
	}
	public void run() {
		new Node();
	}

	public static void main(String[] args) {
		io.println("Node preinit...");
		new Node();
	}

}
