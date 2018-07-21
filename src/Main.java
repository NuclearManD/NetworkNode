import nuclear.slitherge.top.io;

public class Main {
	private static Node node;
	public static void main(String[] args) {
		boolean startNode=false;
		boolean startWallet=false;
		boolean startWebDev=false;
		for(String i:args){
			if(i.equalsIgnoreCase("-n"))
				startNode=true;
			else if(i.equalsIgnoreCase("-w"))
				startWallet=true;
			else if(i.equalsIgnoreCase("-D"))
				startWebDev=true;
		}
		if(startNode){
			new Thread(new Runnable() {
				public void run() {
					node=new Node();
				}
			}).start();
		}
		if(startWebDev){
			new Thread(new Runnable() {
				public void run() {
					new WebUtil();
				}
			}).start();
		}
		if(startWallet){
			new WalletMain();
		}
		if(!(startNode||startWallet)){
			io.println("Usage: NetSys [options]");
			io.println(" OPTIONS          DESCRIPTION");
			io.println("   -n -N           Start a node");
			io.println("   -w -W           Start a wallet");
			io.println("   -d -D           Start blockchain web developer");
		}
	}

}
