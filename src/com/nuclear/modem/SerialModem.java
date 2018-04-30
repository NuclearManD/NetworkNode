package com.nuclear.modem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class SerialModem implements Modem, Runnable {
	ArrayList<Packet> buffer =new ArrayList<Packet>();
	byte[] ibuf=new byte[0];
	String port;
	InputStream in;
	OutputStream out;
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
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	            return false;
	        }
	        else
	        {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                SerialPort serialPort = (SerialPort) commPort;
	                serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	                
	                in = serialPort.getInputStream();
	                out = serialPort.getOutputStream();
	                
	                (new Thread(this)).start();
	
	            }
	            else
	            {
	                return false;
	            }
	        }
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	@Override
	public void disconnect() {
		
	}

	@Override
	public void run() {
		byte[] buffer=new byte[1024];
		while(true){
			int len;
			try {
				len = in.read(buffer);
			} catch (IOException e) {
				return;
			}
			if(len<0)
				return;
			if(len>0){
				byte[] tmp=new byte[ibuf.length+len];
				tmp=Arrays.copyOf(ibuf, tmp.length);
				int n=ibuf.length;
				for(byte i:buffer){
					tmp[n]=i;
					n++;
				}
			}
			if(this.buffer.size()>0){
				Packet p=this.buffer.remove(0);
				try {
					out.write(p.data);
				} catch (IOException e) {
					return;
				}
			}
		}
	}

}
