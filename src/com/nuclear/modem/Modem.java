package com.nuclear.modem;

public interface Modem {
	public boolean connect();
	public void disconnect();
	public int available();
	public Packet readPacket();
	public boolean sendPacket(Packet p);
}
