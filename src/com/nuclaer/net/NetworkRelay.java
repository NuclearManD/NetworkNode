package com.nuclaer.net;

import java.io.IOException;
import java.util.Arrays;

import nuclear.slitherge.top.io;
import nuclear.slithernet.Client;
import nuclear.slithernet.Server;

public class NetworkRelay extends Server implements Runnable {
	public static final int ADDRESS_LENGTH=8;
	public static final byte[] ERROR_NO_ROUTE={0x4E, 0x52, 0x45};
	public static final byte[] ERROR_IN_TX={0x54, 0x58, 0x45};
	public static final byte[] SENT_RESPONSE={0x4F, 0x4B, 0x3B};
	protected RoutingTable router=new RoutingTable();
	
	public NetworkRelay(int port) {
		super(port);
		io.println("Starting network relay...");
	}
	public byte[] easyServe(byte[] in) {
		byte endpoint[]=Arrays.copyOfRange(in,0,ADDRESS_LENGTH);
		//byte data[]=Arrays.copyOfRange(in,ADDRESS_LENGTH,in.length);
		String target=router.next(endpoint);
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
			return client.poll(in);
		} catch (IOException e) {
			return ERROR_IN_TX;
		}
	}

}
