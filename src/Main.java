import com.nuclaer.freeChain.Node;

import nuclear.slitherge.top.io;

public class Main {
	public static int blocktime;
	public static void main(String[] args) {
		boolean startNode=false;
		boolean startWallet=false;
		blocktime=512;// in seconds
		for(String i:args){
			if(i.equalsIgnoreCase("-n"))
				startNode=true;
			else if(i.equalsIgnoreCase("-w"))
				startWallet=true;
			else if(i.startsWith("-bt:")) // in SECONDS
				blocktime=Integer.parseInt(i.substring(4));
		}
		if(startNode){
			new Thread(new Runnable() {
				public void run() {
					new Node(blocktime);
				}
			}).start();
		}
		/*try{
			Thread.sleep(12000);
		}catch(Exception e){
			
		}///*/
		if(startWallet){
			new WalletMain();
		}
		if(!(startNode||startWallet)){
			io.println("Usage: NetSys [options]");
			io.println(" OPTIONS          DESCRIPTION");
			io.println("   -n -N           Start a node");
			io.println("   -w -W           Start a wallet");
			io.println("   -bt [seconds]   Specify block time");
			io.println("                   > Default is 512");
		}
	}

}
