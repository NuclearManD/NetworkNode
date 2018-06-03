

import javax.swing.JFrame;
import javax.swing.JList;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

public class WebUtil extends JFrame {

	private static final long serialVersionUID = -3594380707506917724L;
	private JTextField domainName;

	public WebUtil(){
		setTitle("Website Builder");
		getContentPane().setLayout(null);
		
		JList<String> fileList = new JList<String>();
		fileList.setBounds(150, 50, 200, 100);
		getContentPane().add(fileList);
		
		JComboBox<String> domainSelect = new JComboBox<String>();
		domainSelect.setBounds(150, 11, 130, 20);
		getContentPane().add(domainSelect);
		
		domainName = new JTextField();
		domainName.setBounds(10, 79, 130, 20);
		getContentPane().add(domainName);
		domainName.setColumns(10);
		
		JButton btnClaimDomain = new JButton("Claim Domain");
		btnClaimDomain.setBounds(7, 110, 133, 23);
		getContentPane().add(btnClaimDomain);
		
		JButton btnReleaseSelectedDomain = new JButton("Release selected domain");
		btnReleaseSelectedDomain.setBounds(150, 161, 200, 23);
		getContentPane().add(btnReleaseSelectedDomain);
		
		JLabel lblNetworkStatus = new JLabel("Network Status");
		lblNetworkStatus.setBounds(10, 11, 130, 14);
		getContentPane().add(lblNetworkStatus);
		
		JButton btnUpload = new JButton("Upload");
		btnUpload.setBounds(365, 10, 89, 23);
		getContentPane().add(btnUpload);
		
	}
}
