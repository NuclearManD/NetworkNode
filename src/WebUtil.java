import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import nuclear.blocks.client.ClientIface;
import nuclear.slithercrypto.ECDSAKey;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.SavedChain;
import nuclear.slithercrypto.blockchain.Transaction;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;


public class WebUtil extends JFrame implements ActionListener{
	private String basepath=System.getProperty("user.home")+"/AppData/Roaming/NuclearBlocks";
	private String keypath=basepath+"/keys/main.key";
	private String bcfile=basepath+"/blockchain/";
	
	private static final long serialVersionUID = -3594380707506917724L;
	private JTextField domainName;
	
	private BlockchainBase man;
	private ClientIface iface;
	private ECDSAKey key;
	private DefaultListModel<String> files=new DefaultListModel<String>();
	JList<String> fileList = new JList<String>(files);
	JComboBox<String> domainSelect = new JComboBox<String>();
	
	public WebUtil(){
		iface=new ClientIface("68.4.23.94");
		man=new SavedChain(bcfile);
		key=new ECDSAKey(keypath);
		setBounds(100,100,480,256);
		setTitle("Website Builder");
		getContentPane().setLayout(null);
		
		fileList.setBounds(150, 50, 200, 100);
		getContentPane().add(fileList);
		
		domainSelect.setBounds(150, 11, 130, 20);
		getContentPane().add(domainSelect);
		
		domainName = new JTextField();
		domainName.setBounds(10, 79, 130, 20);
		getContentPane().add(domainName);
		domainName.setColumns(10);
		
		JButton btnClaimDomain = new JButton("Claim Domain");
		btnClaimDomain.setActionCommand("claim");
		btnClaimDomain.addActionListener(this);
		btnClaimDomain.setBounds(7, 110, 133, 23);
		getContentPane().add(btnClaimDomain);
		
		JButton btnReleaseSelectedDomain = new JButton("Release selected domain");
		btnReleaseSelectedDomain.setBounds(150, 161, 200, 23);
		getContentPane().add(btnReleaseSelectedDomain);
		
		JLabel lblNetworkStatus = new JLabel("Network Status");
		lblNetworkStatus.setBounds(10, 11, 130, 14);
		getContentPane().add(lblNetworkStatus);
		
		JButton btnUpload = new JButton("Upload");
		btnUpload.setActionCommand("upload");
		btnUpload.setBounds(365, 10, 89, 23);
		btnUpload.addActionListener(this);
		getContentPane().add(btnUpload);
		
		updateLists();
		
		setVisible(true);
	}
	private void updateLists() {
		files.clear();
		for(Transaction i:man.getPagesOf(key.getPublicKey())){
			String n=new String(i.getMeta());
			boolean q=false;
			for(int j=0;j<files.size();j++){
				if(files.get(j).equals(n))
					q=true;
			}
			if(!q)files.addElement(n);
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd=e.getActionCommand();
		if(cmd.equals("upload")){
			upload();
		}else if(cmd.equals("claim")){
			iface.uploadTransaction(Transaction.takeDomain(key.getPublicKey(), key.getPrivateKey(),domainName.getText()));
		}else if(cmd.equals("release")){
			
		}
	}
	private void upload() {
		JFileChooser jfc = new JFileChooser(System.getProperty("user.home"));
		int retval=jfc.showOpenDialog(this);
		if(retval==JFileChooser.APPROVE_OPTION) {
			File file=jfc.getSelectedFile();
			String path=file.getPath();
			long length=file.length();
			try {
				FileInputStream stream=new FileInputStream(path);
				byte[] buffer=new byte[(int) length];
				for(int i=0;i<length;i++) {
					buffer[i]=(byte) stream.read();
				}
				stream.close();
				new Thread(new Runnable() {

					public void run() {
						iface.downloadBlockchain(man);
				    	 iface.uploadPair(Transaction.makePage(key.getPublicKey(), key.getPrivateKey(), buffer, man.getBlockByIndex(man.length()-1).getHash(), file.getName()));
				     }
				}).start();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
