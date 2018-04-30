package com.nuclear.modem;

public class Packet {
	public byte[] target;
	public byte[] next;
	public byte[] data;
	public byte[] pack(){
		byte[] o=new byte[data.length+16];
		int n=0;
		for(byte i:target){
			o[n]=i;
			n++;
		}
		for(byte i:next){
			o[n]=i;
			n++;
		}
		for(byte i:data){
			o[n]=i;
			n++;
		}
		return o;
	}
	public static Packet unpack(byte[] data){
		int n=0;
		Packet o=new Packet();
		o.target=new byte[8];
		o.next=new byte[8];
		o.data=new byte[data.length-16];
		for(int i=0;i<8;i++){
			o.target[i]=data[n];
			n++;
		}
		for(int i=0;i<8;i++){
			o.next[i]=data[n];
			n++;
		}
		for(int i=0;i<o.data.length;i++){
			o.data[i]=data[n];
			n++;
		}
		return o;
	}
}
