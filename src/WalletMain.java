
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import javax.swing.JOptionPane;

import com.nuclaer.nnutil.Logger;

import nuclear.blocks.client.ClientIface;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.SavedChain;

public class WalletMain implements Runnable {
	private String basepath=System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks";
	private String keypath=basepath+"/keys/main.key";
	private String blockchainStorePlace=basepath+"/blockchain/";
	
	private ECDSAKey key;
	
	private String nodeAdr;
	
	private ClientIface iface;
	private SavedChain chain;
	private WalletControl gui;
	private Logger log=new Logger("Wallet");
	public WalletMain() {
		nodeAdr="68.4.23.94";//nuclear.blocks.wallet.Main.nodeAdr;//JOptionPane.showInputDialog(null, "What IP is the node at?");
		if(new File(keypath).exists())
			key=new ECDSAKey(keypath);
		else{
			new File(keypath).getParentFile().mkdirs();
			key=new ECDSAKey();
			key.save(keypath);
		}
		log.println("Got key...");
		chain=new SavedChain(blockchainStorePlace);
		iface=new ClientIface(nodeAdr);
		log.println("Loading GUI");
		gui=new WalletControl(chain,key,iface);
		gui.addressLabel.setText("Address: "+encode(key.getPublicKey()));
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
		gui.networkLabel.setText("Please wait, syncing to network...");
		while(true) {
			log.println("Downloading blocks...");
			int q;
			q=downloadBlockchain(chain);
			if(q!=-1){
				gui.networkLabel.setText("Welcome to the database.  "+chain.length()+" main blocks loaded.");
			}else
				gui.networkLabel.setText("Error connecting to network.  "+chain.length()+" main blocks loaded.");
			gui.updateBalance();
			try {
				for(int i=0;i<100;i++){
					Thread.sleep(150);
					if(gui.selReconnect){
						nodeAdr=JOptionPane.showInputDialog(null, "What IP is the node at?");
						iface.close();
						iface=new ClientIface(nodeAdr);
						gui.selReconnect=false;
						break;
					}
				}
			} catch (InterruptedException e) {
				break;
			}
			if(!gui.isDisplayable())
				return;
		}
	}

	public int downloadBlockchain(BlockchainBase manager){
		iface.setNetErr(false);
		manager.update();
		int i=manager.length();
		int n=0;
		while(true){
			log.println("Downloading blocks from #"+i);
			ArrayList<Block> blocks=iface.getBlocks(i);
			if(blocks==null)
				return -1;
			if(blocks.isEmpty())
				break;
			if(iface.isNetErr()){
				log.println("Network error!");
				iface.setNetErr(false);
				return -1;
			}
			for(Block block:blocks){
				if(manager.addBlock(block)){
					n++;
					i++;
					gui.networkLabel.setText("Downloaded "+i+" blocks so far...");
				}else
					break;
			}
			int s=blocks.size();
			if(s<32)
				break;
		}
		log.println("Downloaded "+n+" new blocks.");
		return n;
	}
}
