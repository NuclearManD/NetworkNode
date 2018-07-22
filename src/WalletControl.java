import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.nuclaer.nnutil.Logger;

import nuclear.blocks.client.ClientIface;
import nuclear.blocks.wallet.ui.RemoteFileSelector;
import nuclear.blocks.wallet.ui.WalletGUI;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.Transaction;

public class WalletControl extends WalletGUI {

	private static final long serialVersionUID = -403147653826986273L;


	Logger log=new Logger("Wallet");


	public WalletControl(BlockchainBase man1, ECDSAKey key1, ClientIface iface1) {
		super(man1, key1, iface1);
		btnReconnect.setText("Change Node");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}


	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand()=="UPLOAD") {
			JFileChooser jfc = new JFileChooser(System.getProperty("user.home"));
			int retval=jfc.showOpenDialog(this);
			if(retval==JFileChooser.APPROVE_OPTION) {
				file=jfc.getSelectedFile();
				path=file.getPath();
				long length=file.length();
				try {
					FileInputStream stream=new FileInputStream(path);
					buffer=new byte[(int) length];
					for(int i=0;i<length;i++) {
						buffer[i]=(byte) stream.read();
					}
					stream.close();
					lasthash=new byte[32];
					if(man.length()>0)
						lasthash=man.getBlockByIndex(man.length()-1).getHash();
					new Thread(new Runnable() {
					     public void run() {
					    	 iface.uploadPair(Transaction.makeFile(key.getPublicKey(), key.getPrivateKey(), buffer, lasthash, file.getName()));
					     }
					}).start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else if(e.getActionCommand()=="DOWNLOAD") {
			new Thread(new Runnable() {
			    public void run() {
			    	RemoteFileSelector selector=new RemoteFileSelector(man,key.getPublicKey());
					if(selector.selection==null)
						return;
					byte[] sel=selector.selection.getDaughterHash();
					log.println(selector.selection.toString());
					int us = fc.showSaveDialog(null);
					if(us==JFileChooser.APPROVE_OPTION) {
						File file=fc.getSelectedFile();
						log.println("Saving File...");
						try(FileOutputStream f=new FileOutputStream(file)){
							Block bk=iface.downloadDaughter(sel);
							byte[] data=bk.getData();
							f.write(data);
							f.close();
						}catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, "Error: Download failed!");
						}
						log.println("Done writing file.");
					}
			    }
			}).start();
		}else if(e.getActionCommand()=="RECONNECT"){
			selReconnect=true;
		}
	}

}
