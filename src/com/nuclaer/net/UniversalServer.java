package com.nuclaer.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.nuclaer.nnutil.Logger;

import nuclear.slithercrypto.blockchain.BlockchainBase;
import nuclear.slitherge.top.io;
import nuclear.slithernet.Server;

public class UniversalServer extends Server {
	public static final int ADDRESS_LENGTH=8;
	private static final String ERR_MSG_NO_CONN = "Error -2: unable to connect to requested address.";
	private static final String ERR_MSG_NO_DATA = "Error -1: request returned no data.";
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
		String path=uri.getPath()+'?'+uri.getQuery()+'#'+uri.getFragment();
		
		log.println("Request protocol: "+protocol);
		log.println("Request path:     "+path);
		
		byte[] result=ERR_MSG_NO_DATA.getBytes();
		if(protocol.equals("bc")){
			
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
}
