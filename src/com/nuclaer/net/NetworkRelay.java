package com.nuclaer.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

import com.nuclaer.nnutil.Logger;

import nuclear.slithernet.Client;
import nuclear.slithernet.Server;

public class NetworkRelay extends Server implements Runnable {
	public static final int ADDRESS_LENGTH=8;
	public static final byte[] ERROR_NO_ROUTE={0x4E, 0x52, 0x45};
	public static final byte[] ERROR_IN_TX={0x54, 0x58, 0x45};
	public static final byte[] SENT_RESPONSE={0x4F, 0x4B, 0x3B};
	protected RoutingTable router=new RoutingTable();
	Logger log=new Logger("Relay");
	public NetworkRelay(int port) {
		super(port);
		log.println("Starting network relay...");
		
	}
	public byte[] easyServe(byte[] in) {
		byte endpoint[]=Arrays.copyOfRange(in,0,ADDRESS_LENGTH);
		String target=router.next(endpoint);
		log.println("Route: "+target);
		if(target==null){
			// error!  no route to the destination
			return ERROR_NO_ROUTE;
		}
		Client client;
		try {
			if(!target.contains(":"))
				client = new Client(port,target);
			else{
				int i=target.indexOf(":");
				client = new Client(Integer.parseInt(target.substring(0, i)),target.substring(i+1));
			}
			if(router.convert(endpoint)){
				in=Arrays.copyOfRange(in,ADDRESS_LENGTH,in.length);
			}
			return client.poll(in);
		} catch (IOException e) {
			return ERROR_IN_TX;
		}
	}
	public void start() throws IOException{
		sok = new ServerSocket(port);
		NetworkRelay q=this;
		new Thread(new Runnable() {
			public void run() {
				log.println("Started Network Relay on port "+port);
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
