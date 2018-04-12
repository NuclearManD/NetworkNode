
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.swing.JOptionPane;

import com.nuclaer.freeChain.SavedChainMod;

import nuclear.blocks.client.ClientIface;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slitherge.top.io;

public class WalletMain implements Runnable {
	String basepath=System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks";
	String keypath=basepath+"/keys/main.key";
	String blockchainStorePlace=basepath+"/blockchain/";
	
	ECDSAKey key;
	
	String nodeAdr;
	
	ClientIface iface;
	SavedChainMod chain;
	WalletControl gui;
	public WalletMain() {
		nodeAdr=nuclear.blocks.wallet.Main.nodeAdr;//JOptionPane.showInputDialog(null, "What IP is the node at?");
		if(new File(keypath).exists())
			key=new ECDSAKey(keypath);
		else{
			new File(keypath).getParentFile().mkdirs();
			key=new ECDSAKey();
			key.save(keypath);
		}
		io.println("Got key...");
		chain=new SavedChainMod(blockchainStorePlace);
		try {
			iface=new ClientIface(nodeAdr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		io.println("Loading GUI");
		gui=new WalletControl(chain,key,iface);
		gui.addressLabel.setText("Address: "+encode(key.getPublicKey()));
		gui.coinCountLabel.setText("Please wait, connecting to network...");
		gui.remove(gui.kibAmt);
		gui.remove(gui.txtrAddress);
		gui.setVisible(true);
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
			io.println("Downloading blocks...");
			int q;
			q=iface.downloadBlockchain(chain);
			if(q!=-1){
				io.println("Downloaded "+q+" new blocks.");
				gui.coinCountLabel.setText("Welcome to the database.  "+chain.length()+" main blocks loaded.");
			}else
				gui.coinCountLabel.setText("Error connecting to network.  "+chain.length()+" main blocks loaded.");
			try {
				for(int i=0;i<1000*60;i++){
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
	
}
