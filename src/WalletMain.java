
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.swing.JOptionPane;

import com.nuclaer.nnutil.Logger;

import nuclear.blocks.client.ClientIface;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.SavedChain;

public class WalletMain implements Runnable {
	String basepath=System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks";
	String keypath=basepath+"/keys/main.key";
	String blockchainStorePlace=basepath+"/blockchain/";
	
	ECDSAKey key;
	
	String nodeAdr;
	
	ClientIface iface;
	SavedChain chain;
	WalletControl gui;
	Logger log=new Logger("Wallet");
	public WalletMain() {
		nodeAdr="68.4.23.92";//nuclear.blocks.wallet.Main.nodeAdr;//JOptionPane.showInputDialog(null, "What IP is the node at?");
		if(new File(keypath).exists())
			key=new ECDSAKey(keypath);
		else{
			new File(keypath).getParentFile().mkdirs();
			key=new ECDSAKey();
			key.save(keypath);
		}
		log.println("Got key...");
		chain=new SavedChain(blockchainStorePlace);
		try {
			iface=new ClientIface(nodeAdr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.println("Loading GUI");
		gui=new WalletControl(chain,key,iface);
		gui.addressLabel.setText("Address: "+encode(key.getPublicKey()));
		gui.remove(gui.kibAmt);
		gui.remove(gui.txtrAddress);
		gui.coinCountLabel.setText("Waiting...");
		gui.setVisible(true);
		log.println("GUI loaded.");
		new Thread(this).start();
	}
	
	public static String encode(byte[] publicKey) {
		return Base64.getEncoder().encodeToString(publicKey).replaceAll("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA", "@");
	}
	public static byte[] decode(String text) {
		text=text.replaceAll("@", "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA");
		return Base64.getDecoder().decode(text);
	}
	public static void main(String[] args) {
		new WalletMain();
	}

	public void run() {
		while(true) {
			gui.coinCountLabel.setText("Please wait, connecting to network...");
			log.println("Downloading blocks...");
			int q;
			q=downloadBlockchain(chain);
			if(q!=-1){
				gui.coinCountLabel.setText("Welcome to the database.  "+chain.length()+" main blocks loaded.");
			}else
				gui.coinCountLabel.setText("Error connecting to network.  "+chain.length()+" main blocks loaded.");
			try {
				for(int i=0;i<1000;i++){
					Thread.sleep(15);
					if(gui.selReconnect){
						nodeAdr=JOptionPane.showInputDialog(null, "What IP is the node at?");
						try {
							iface=new ClientIface(nodeAdr);
						} catch (IOException e) {
							e.printStackTrace();
						}
						gui.selReconnect=false;
						break;
					}
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public int downloadBlockchain(BlockchainBase manager){
		iface.setNetErr(false);
		manager.update();
		int i=manager.length();
		int n=0;
		while(true){
			log.println("Downloading block #"+i);
			Block block=iface.downloadByIndex(i);
			if(iface.isNetErr()){
				log.println("Network error!");
				iface.setNetErr(false);
				return -1;
			}
			i++;
			if(manager.addBlock(block))
				n++;
			else{
				log.println("Downloaded "+n+" new blocks.");
				return n;
			}
		}
	}
}
