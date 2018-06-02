package com.nuclaer.net;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import com.nuclaer.nnutil.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/*
 * a simple static http server
*/
public class Bridge implements HttpHandler{
	Logger log=new Logger("Bridge");
	USReq req=new USReq(USReq.LOCALHOST);
	public void start(int port) {
		HttpServer server=null;
		log.println("Starting Network Bridge...");
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(server!=null){
			server.createContext("/", this);
			server.setExecutor(null); // creates a default executor
			server.start();
			log.println("Bridge started.");
		}else
			log.println("Error starting bridge!");
	}

	public void handle(HttpExchange t) throws IOException {
		URI Uri=t.getRequestURI();
		log.println("new request: "+Uri.toString());
		String[] path=Uri.getPath().replaceFirst("/", "").split("/");
		String uri=path[0]+"://"+path[1];
		for(int i=2;i<path.length;i++){
			uri+="/"+path[i];
		}
		if(Uri.getQuery()!=null)
			uri+="?"+Uri.getQuery();
		if(Uri.getFragment()!=null)
			uri+="#"+Uri.getFragment();
		log.println("Query: "+uri);
		byte[] response=req.get(uri).getBytes();
		t.sendResponseHeaders(200, response.length);
		OutputStream os = t.getResponseBody();
		os.write(response);
		os.close();
	}

	protected void onError(Exception e) {
		log.println("ERROR: "+e.getMessage());
	}
}