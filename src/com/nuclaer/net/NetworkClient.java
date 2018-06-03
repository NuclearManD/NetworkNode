package com.nuclaer.net;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import nuclear.slithernet.Client;

public class NetworkClient extends Client {
	private static final byte[] GOOGLE_DNS = {0x7F,0x4D,53,0,8,8,8,8};
	byte[] ourAdr;
	public NetworkClient(String host,byte[] address){
		super(1153, host);
		ourAdr=address;
	}
	public String httpGetRemote(String url){
		return "null";
	}
	public String dns(String url){
		byte[] request={(byte) 0xAA,(byte) 0xAA,1,0,0,1,0,0,0,0,0,0};
		String uri=URI.create(url).getHost();
		byte[] question=new byte[uri.length()+2];
		int idx=0;
		int n=0;
		int len=0;
		for(int i=0;i<question.length-2;i++){
			question[i+1]=(byte) uri.charAt(n);
			n++;
			if(question[i+1]=='.'){
				question[idx]=(byte) len;
				len=0;
				idx=i+1;
			}else
				len++;
		}
		question[question.length-1]=0;
		int v=request.length;
		request=Arrays.copyOf(request, request.length+question.length+4);
		for(int i=v;i<request.length-4;i++){
			request[i]=question[i-v];
		}
		request[request.length-3]=1;
		request[request.length-1]=1;
		byte[] response=req(GOOGLE_DNS,request);
		return Arrays.toString(response);
	}
	public byte[] req(byte[] adr, byte[] data){
		byte[] packet=Arrays.copyOf(adr, data.length+16);
		for(int i=8;i<16;i++)
			packet[i]=ourAdr[i-8];
		for(int i=16;i<packet.length;i++)
			packet[i]=data[i-16];
		
		try {
			byte[] response=poll(packet);
			byte[] out=new byte[response.length-16];
			for(int i=0;i<out.length;i++)
				out[i]=response[i+16];
			return out;
		} catch (IOException e) {
			return new byte[0];
		}
	}
}
