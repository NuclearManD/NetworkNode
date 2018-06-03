package com.nuclaer.nnutil;
import java.text.SimpleDateFormat;
import java.util.Date;

import nuclear.slitherge.top.io;
import nuclear.slitherio.SlitherLog;

public class Logger extends SlitherLog {
	private String name;
	public Logger(String name) {
		this.name="[ "+name+" ]";
		for(int i=name.length();i<16;i++)
			this.name+=" ";
	}

	@Override
	public void print(String s) {
		io.print(name+new SimpleDateFormat("YYMMddHHmmss").format(new Date())+" : "+s);
	}

}
