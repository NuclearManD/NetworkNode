package com.nuclaer.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nuclear.slitherge.top.io;
import nuclear.slithernet.Client;

public class PrivateDatabaseClient extends Client {

	public PrivateDatabaseClient(int port, String host) throws IOException {
		super(port, host);
	}
	
	public byte[] loadFile(byte[] name){
		byte[] in=new byte[name.length+1];
		in[0]=PrivateDatabaseHandler.CMD_READFILE;
		for(int i=0;i<name.length;i++)
			in[i+1]=name[i];
		byte[] o;
		try {
			o = poll(in);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if(o[0]!=PrivateDatabaseHandler.MARK_TX_)
			return null;
		byte[] ret=new byte[o.length-1];
		for(int i=1;i<o.length;i++){
			ret[i-1]=o[i];
		}
		return ret;
	}
	public static void main(String[] args){
		PrivateDatabaseClient x;
		try {
			x = new PrivateDatabaseClient(6609, "localhost");
			io.println(Arrays.toString(x.loadFile("bios.lua".getBytes(StandardCharsets.UTF_8))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
