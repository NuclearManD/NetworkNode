package com.nuclaer.test180409;

import nuclear.slitherge.top.io;
import nuclear.slithernet.RoutingTable;

public class RoutingTableTest {
	static byte[] a={0x7F,0x4E,0,0,(byte) 0xC0,(byte) 168,1,1};
	static byte[] d={0x7F,0x4D,0,53,8,8,8,8};
	static byte[] b={0x7F,0x00,0,0,0,0,0,0x3E};
	static byte[] c={0x7E,0x00,0,0,0,0,0,0x3E};
	public static void main(String[] args) {
		RoutingTable table=new RoutingTable();
		io.println("routing 7F:4E:00:00:C0:A8:01:01 -> "+table.next(a));
		io.println("routing 7F:00:00:00:00:00:00:3E -> "+table.next(b));
		io.println("routing 7E:00:00:00:00:00:00:3E -> "+table.next(c));
		io.println("routing 7E:4D:00:53:08:08:08:08 -> "+table.next(d));
	}

}
