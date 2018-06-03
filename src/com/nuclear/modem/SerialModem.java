package com.nuclear.modem;

import java.util.ArrayList;

public class SerialModem implements Modem {
	ArrayList<Packet> buffer =new ArrayList<Packet>();
	String port;
	public SerialModem(String port){
		this.port=port;
	}

	@Override
	public int available() {
		return buffer.size();
	}

	@Override
	public Packet readPacket() {
		return buffer.remove(0);
	}

	@Override
	public boolean sendPacket(Packet p) {
		return false;
	}
	
	@Override
	public boolean connect() {
		try{
			
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	@Override
	public void disconnect() {
		
	}

}
