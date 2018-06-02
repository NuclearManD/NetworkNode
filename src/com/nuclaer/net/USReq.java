package com.nuclaer.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nuclear.slithernet.Client;

public class USReq extends Client {
	public static final byte[] LOCALHOST={0x7F,0x4E,0x00,0x00,0x7F,0x00,0x00,0x01};
	private static RoutingTable router=new RoutingTable();
	private byte[] endpoint;
	public USReq(byte[] endpoint) {
		super(8081, router.next(endpoint));
		this.endpoint=endpoint;
	}
	public String get(String q, byte[] adr){
		byte[] request=Arrays.copyOf(endpoint, q.length()+16);
		for(int i=0;i<8;i++){
			request[i+8]=adr[i];
		}
		byte[] data=q.getBytes();
		for(int i=0;i<q.length();i++){
			request[i+16]=data[i];
		}
		byte[] retdat;
		try {
			retdat=poll(request);
		} catch (IOException e) {
			return "[INTERNAL] Connection error.  Could not connect to Master Node.";
		}
		data=Arrays.copyOfRange(retdat, 16, retdat.length);
		return new String(data,StandardCharsets.UTF_8);
	}
	public String get(String q){
		return get(q,new byte[8]);
	}
}
