package com.nuclaer.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.nuclaer.freeChain.ModTransaction;
import com.nuclaer.nnutil.Logger;

import nuclear.slithercrypto.blockchain.Block;
import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slithercrypto.blockchain.Transaction;
import nuclear.slitherge.top.io;
import nuclear.slithernet.Server;

public class UniversalServer extends Server {
	public static final int ADDRESS_LENGTH=8;
	private static final String ERR_MSG_NO_CONN = "Error -2: unable to connect to requested address.";
	private static final String ERR_MSG_NO_DATA = "Error -1: request returned no data.";
	private static final String ERR_MSG_BC_DNF  = "Error -3: Domain name not found on the blockchain.";
	private static final String ERR_MSG_BC_404  = "Error -4: File was not found on that domain.";
	Logger log=new Logger("U.S.");
	BlockchainBase man;
	public UniversalServer(int port,BlockchainBase base) {
		super(port);
		man=base;
		log.println("Starting universal server...");
		
	}
	public byte[] easyServe(byte[] in){
		byte endpoint[]=Arrays.copyOfRange(in,0,ADDRESS_LENGTH);
		byte sender[]=Arrays.copyOfRange(in,ADDRESS_LENGTH,ADDRESS_LENGTH*2);
		log.println("New request from "+Arrays.toString(sender));
		URI uri;
		try {
			uri=new URI(new String(Arrays.copyOfRange(in,ADDRESS_LENGTH*2,in.length),StandardCharsets.UTF_8));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return new byte[0];
		}
		String protocol=uri.getScheme();
		String access=uri.getAuthority();
		String path="";
		if(uri.getPath()!=null)
			path=uri.getPath();
		if(uri.getQuery()!=null)
			path+='?'+uri.getQuery();
		if(uri.getFragment()!=null)
			path+='#'+uri.getFragment();
		
		log.println("Request protocol: "+protocol);
		log.println("Request path:     "+path);
		
		byte[] result=ERR_MSG_NO_DATA.getBytes();
		if(protocol.equals("bc")){
			if(uri.getPath()==null||uri.getPath().isEmpty())
				path="index.html";
			else
				path=uri.getPath();
			// now we need to search every transaction in the blockchain...
			byte[] address=null;
			int len=man.length();
			for(int i=0;i<len;i++){
				Block b=man.getBlockByIndex(i);
				int transactions=b.numTransactions();
				for(int j=0;j<transactions;j++){
					Transaction t=b.getTransaction(j);
					if(t.type==ModTransaction.TRANSACTION_REG_DNS&&Arrays.equals(t.getMeta(),access.getBytes())){
						address=t.getSender();
						i=len;
						break;
					}
				}
			}
			if(address==null)
				result=ERR_MSG_BC_DNF.getBytes();
			else{
				path=path.replaceFirst("/", "");
				result=man.readFile(path, address);
				if(result==null){
					result=man.readFile("404.html", address);
					if(result==null)
						result=ERR_MSG_BC_404.getBytes();
					log.println("File "+path+" was not found.");
				}
				
			}
		}else if(protocol.equals("http")){
			String urlString = uri.toString();
			try{
				
				URL url = new URL(urlString);
				URLConnection conn = url.openConnection();
				InputStream is = conn.getInputStream();
				result=new byte[256];
				String tmp="";
				int len;
				while((len=is.read(result))>0){
					result=Arrays.copyOf(result, len);
					tmp+=new String(result,StandardCharsets.UTF_8);
					result=new byte[256];
				}
				result= tmp.getBytes();
			}catch(Exception e){
				result=ERR_MSG_NO_CONN.getBytes();
			}
		}
		byte[] q=Arrays.copyOf(sender, result.length+16);
		for(int i=0;i<8;i++){
			q[i+8]=endpoint[i];
		}
		for(int i=0;i<result.length;i++){
			q[i+16]=result[i];
		}
		return q;
	}
	public void start() throws IOException{
		sok = new ServerSocket(port);
		UniversalServer q=this;
		new Thread(new Runnable() {
			public void run() {
				log.println("Started Universal Server on port "+port);
				while(true) {
					try {
						tmpsok = sok.accept();
						new Thread(q).start();
					}   
					catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}).start();
	}

	protected void onError(Exception e) {
		log.println("ERROR: "+e.getMessage());
	}
}
