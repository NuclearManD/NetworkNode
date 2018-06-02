

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.nuclaer.freeChain.NodeServer;
import com.nuclaer.net.Bridge;
import com.nuclaer.net.NetworkRelay;
import com.nuclaer.net.UniversalServer;

import nuclear.blocks.node.ExternalNode;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slitherge.top.io;

public class Node{
	NodeServer server;
	List<ExternalNode> nodes;
	ECDSAKey key;
	String keypath=System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks/keys/main.key";
	public Node(int bt) {
		if(new File(keypath).exists())
			key=new ECDSAKey(keypath);
		else{
			new File(keypath).getParentFile().mkdirs();
			key=new ECDSAKey();
			key.save(keypath);
		}
		server=new NodeServer(bt, key.getPublicKey());
		NetworkRelay relay=new NetworkRelay(1153);
		try {
			relay.start();
		}catch(IOException e){
			io.println("Unable to start Network Relay.");
		}
		UniversalServer server=new UniversalServer(8081,this.server.blockchain);
		try {
			server.start();
		}catch(IOException e){
			io.println("Unable to start Universal Server.");
		}
		new Bridge().start(80);
	}
	public static void main(String[] args) {
		io.println("Node preinit...");
		new Node(30000);
	}

}
