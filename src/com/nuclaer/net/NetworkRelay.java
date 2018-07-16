package com.nuclaer.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import com.nuclaer.nnutil.Logger;

import nuclear.slithernet.Client;
import nuclear.slithernet.RoutingTable;
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
		byte sender[]=Arrays.copyOfRange(in,ADDRESS_LENGTH,ADDRESS_LENGTH*2);
		String target=router.next(endpoint);
		log.println("Route: "+target);
		if(target==null){
			// error!  no route to the destination
			return ERROR_NO_ROUTE;
		}
		boolean conv=router.convert(endpoint);
		if(!conv){
			Client client;
			if(!target.contains(":"))
				client = new Client(port,target);
			else{
				int i=target.indexOf(":");
				String s=target.substring(i+1);
				int port=Integer.parseInt(s);
				String sustr=target.substring(0, i);
				client = new Client(port,sustr);
			}
			try {
				log.println("Relaying to "+target);
				in=client.poll(in);
				log.println("Got response from "+target);
				return in;
			} catch (IOException e) {
				e.printStackTrace();
				log.println("Error connecting!");
				return ERROR_IN_TX;
			}
		}else{
			String host=target;
			int port=this.port;
			DatagramSocket socket = null;
			if(target.contains(":")){
				int i=target.indexOf(":");
				String s=target.substring(i+1);
				port=Integer.parseInt(s);
				host=target.substring(0, i);
			}
			try {
				in=Arrays.copyOfRange(in,ADDRESS_LENGTH*2,in.length);
				log.println("Connecting to "+target);
				socket = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(in, in.length, InetAddress.getByName(host), port);
				socket.send(packet);
				byte[] buf = new byte[1024];
		        packet = new DatagramPacket(buf, buf.length);
		        socket.receive(packet);
		        buf=Arrays.copyOf(buf,packet.getLength());
				log.println("Got response from "+target);
				in=Arrays.copyOf(buf, buf.length+ADDRESS_LENGTH*2);
				for(int i=in.length-1;i>=ADDRESS_LENGTH*2;i--){
					in[i]=in[i-ADDRESS_LENGTH*2];
				}
				for(int i=0;i<8;i++){
					in[i]=sender[i];
				}
				for(int i=8;i<16;i++){
					in[i]=endpoint[i-8];
				}
				log.println("closing connection with "+Arrays.toString(sender));
				return in;
			} catch (IOException e) {
				e.printStackTrace();
				log.println("Error connecting!");
				return ERROR_IN_TX;
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				if(socket!=null)
					socket.close();
			}
		}
		return ERROR_IN_TX;
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
