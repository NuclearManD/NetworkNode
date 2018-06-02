package com.nuclaer.test180409;


import com.nuclaer.net.NetworkClient;

import nuclear.slitherge.top.io;

public class TestRelay {

	public static void main(String[] args){
		NetworkClient client=new NetworkClient("localhost", new byte[8]);
		io.println(client.dns("https://google.com/"));
	}

}
