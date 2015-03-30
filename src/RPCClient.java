
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;



public class RPCClient {
	
	public final static int portProj1bRPC = 5300;
	public final static int dataPacketSize = 512;
	private int callID = 0;
	private int timeout = 1000;
	String RPCcalldelimiter = ":";

	
	
	public String sessionReadClient(String sessionID, String versionNumber , List<String> listIp){
		
		
		/*
		SessionTracker.out.print("#####IN function RPC client : sessionReadClient #######");
		System.out.println("#####IN function RPC client : sessionReadClient #######");
		
		// Getting my session data on the basis of my session id
		MySession sessionVal = new MySession();
		
		SessionTracker.out.print("      The session Id received in read client rpc is " + sessionID);
		SessionTracker.out.print("          " + SynchronizedSessionMap.asString());
		
		System.out.println("The session Id received in read client rpc is " + sessionID);
		System.out.println(SynchronizedSessionMap.asString());
		
		
		sessionVal = SynchronizedSessionMap.map.get(sessionID);*/
		
		//currentVersion =  sessionVal.getVersion();
		
		/*MAdhuri:UNcomment and delete one line above:currnet version line*/
		/*PAYYYYYYYYYYYY ATTTTTTENTION!!!!!!!!!!!!!*/
		
		int currentVersion =  Integer.parseInt(versionNumber);
		
		List<String> listOfIps = new ArrayList<String>();
		listOfIps =listIp;
		
		
		//SessionTracker.out.print(" LIST of ips is  " + listOfIps.toString());
		
		
		
		
		//Setting a unique call ID : Just incrementing a counter by 1 
		callID += 1;
		
		byte[] outBuf = new byte[dataPacketSize];
		String message = callID + RPCcalldelimiter + "1" + RPCcalldelimiter + sessionID;
		String output = null;
		byte[] outbuf = message.getBytes();
		
		//DatagramPacket receivedPacket = null;
		
		//send data now
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.print("Error while creating socket");
			e.printStackTrace();
		}
	
		
		for(String IP : listOfIps){
			
			InetAddress IPAddress = null;
			try {
				IPAddress = InetAddress.getByName(IP);
			} catch (UnknownHostException e) {
				System.err.print("Error while getting IP address");
				e.printStackTrace();
			}
			
			DatagramPacket sendPacket = new DatagramPacket(outbuf, outbuf.length, IPAddress,portProj1bRPC);
			try {
				clientSocket.send(sendPacket);
				clientSocket.setSoTimeout(timeout);
				
				//SessionTracker.out.println("     RPC client : read: Sending packet to IP " + IP);
			
			} catch (IOException e) {
					e.printStackTrace();
			}
		}
    
			
		//Receive and check for timeout
	    byte[] inbuf = new byte[dataPacketSize];
	    DatagramPacket receivedPacket = new DatagramPacket(inbuf, inbuf.length);
			    
	    String[] responseParts = null;
	    int versionReceived=-1;
	    
	    // not found in case of timeout/wrongversion number we get not found
	    String sessionDataReceived = "notfound";
	    
	    do  {
	    	try{
		    		receivedPacket.setLength(inbuf.length);
		    		clientSocket.receive(receivedPacket);
		    		
		    		/*Madhuri:Need to check if its correct*/
		    		SessionTracker.SvrResponding = receivedPacket.getAddress().toString().replace("/","");
		    		String remoteIP = 	SessionTracker.SvrResponding;
		    		/*END*/
		    		
		    		String response = new String(receivedPacket.getData());
		    		
		    		//SessionTracker.out.println("    Response from server is " + response);
		    		
		    		responseParts = response.split(RPCcalldelimiter);
		    		sessionDataReceived = responseParts[1];
		    		//System.out.println("The value of session message in rpc client is " + sessionDataReceived);
		    		
		    		//SessionTracker.out.println("    the value of session message in rpc client is " + sessionDataReceived);
		    		
		    		
		    		//Extract the version
		    		if (!(sessionDataReceived.equals("notfound"))){
		    			String[] sessionData = sessionDataReceived.split(MySession.CookieDelimiter);
			    		versionReceived = Integer.parseInt(sessionData[3]);
			    	
		    		}
		    		ViewTuple tuple = new ViewTuple(remoteIP, ViewTable.STATUS_UP,System.currentTimeMillis());
		    		SessionTracker.LocalView.addTuple(tuple);
		    		
    		} catch(SocketTimeoutException stoe){
	    			System.out.println("IN timeout from client read RPC ");
			    	System.out.println("########Oops!!!! Socket timed out!!!!!!########");
			    	clientSocket.close();
			    	/*ViewTuple tuple = new ViewTuple(remoteIP, ViewTable.STATUS_DOWN,System.currentTimeMillis());
	    			SessionTracker.LocalView.addTuple(tuple);*/
		
		
    		} catch(IOException ioe){
			       System.out.println("######## IO Exception!!!!!!########");
			    	clientSocket.close();
			    	ioe.printStackTrace();
		    	
	    	} finally{ 
			    	clientSocket.close();
				    	System.out.println("######## In finally!!!!!!########");
	    	}
		    	
	  	} while(!(Integer.parseInt(responseParts[0]) == callID) && (currentVersion == versionReceived));
	    
	    
	   //SessionTracker.out.println("Going out of RPC Client:Read\n");
		
	return sessionDataReceived;
		
	}
	
	public ViewTable ExchangeView(ViewTable LocalView, String remoteIP){
		callID += 1;
		System.out.println("In RPC Client : Exchange View ");
		//Get String for my View
		String message = callID + RPCcalldelimiter+ "3"+ RPCcalldelimiter+LocalView.toString();
		
		//Setting a unique call ID : Just incrementing a counter by 1 
		
		
		byte[] outBuf = new byte[dataPacketSize];
		String output = null;
		byte[] outbuf = message.getBytes();
		
		//DatagramPacket receivedPacket = null;
		
		//send data now
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.print("Error while creating socket");
			e.printStackTrace();
		}
	
		

			
			InetAddress IPAddress = null;
			try {
				IPAddress = InetAddress.getByName(remoteIP);
			} catch (UnknownHostException e) {
				System.err.print("Error while getting IP address");
				e.printStackTrace();
			}
			
			DatagramPacket sendPacket = new DatagramPacket(outbuf, outbuf.length, IPAddress,portProj1bRPC);
			try {
				clientSocket.send(sendPacket);
				clientSocket.setSoTimeout(timeout);
				System.out.println("Sending packet to IP " + remoteIP);
			} catch (IOException e) {
					e.printStackTrace();
			}
		
    
			
		//Receive and check for timeout
	    byte[] inbuf = new byte[dataPacketSize];
	    DatagramPacket receivedPacket = new DatagramPacket(inbuf, inbuf.length);
			    
	    String[] responseParts;
	    int versionReceived=-1;
	    
	    // not found in case of timeout/wrongversion number we get not found
	    String sessionDataReceived = "notfound";
	    ViewTable hisTable = new ViewTable();
	    
	    try{
	    	do {
		    		receivedPacket.setLength(inbuf.length);
		    		clientSocket.receive(receivedPacket);
		    		String response = new String(receivedPacket.getData());
		    		responseParts = response.split(RPCcalldelimiter);
		    		//System.out.println("View from server is " + hisView);
		    		hisTable = ViewTable.stringToTuple(responseParts[1]);
		    		//System.out.println("The value of session message in rpc client is " + sessionDataReceived);
		    		ViewTuple tuple = new ViewTuple(remoteIP, ViewTable.STATUS_UP,System.currentTimeMillis());
		    		SessionTracker.LocalView.addTuple(tuple);
		    		
	    	}while(!(Integer.parseInt(responseParts[0]) == callID) );
	    
	    } catch(SocketTimeoutException stoe){
		    	receivedPacket = null;
		    	System.out.println("########Oops!!!! Socket timed out!!!!!!########");
		    	clientSocket.close();
		    	ViewTuple tuple = new ViewTuple(remoteIP, ViewTable.STATUS_DOWN,System.currentTimeMillis());
	    		SessionTracker.LocalView.addTuple(tuple);
		
	    } catch(IOException ioe){
	       System.out.println("######## IO Exception!!!!!!########");
	    	clientSocket.close();
	    	ioe.printStackTrace();
	    	
	    } finally{ 
	    	clientSocket.close();
	    	System.out.println("######## In finally!!!!!!########");
	    }
		
	System.out.println("In RPC Client : Exchange VIew: Exiting and final view is :" + hisTable.toString());
	System.out.println();
	return hisTable;
	}

	//check you are returning list of Ips which replied back 
	public List<String> sessionWriteClient(String sessionID , MySession ssn){
		//send a session data object to server for writing.
		//ssn will have all data in it including backup server IP
		
		callID += 1;
		byte[] outBuf = new byte[dataPacketSize];
		String message = callID + RPCcalldelimiter +"2" + RPCcalldelimiter + ssn.toString();
		byte[] outbuf = message.getBytes();
		 
		
		List<String> newIpList = new ArrayList<String>();
		//newIpList.add(SessionTracker.SvrLocal);
		
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.print("Error while creating socket");
			e.printStackTrace();
		}
	
		
		// select k ips from your LocalView and send to them.
		HashSet<String> randomIpSet = ViewTable.getRandomServers(SessionTracker.KRes);
		
		for(String randIP: randomIpSet){
			
			InetAddress IPAddress = null;
			try {
				IPAddress = InetAddress.getByName(randIP);
			} catch (UnknownHostException e) {
				System.err.print("Error while getting IP address");
				e.printStackTrace();
			}
			
			//SessionTracker.out.print("In RPC client: write : The message I am sending to server is "+message);
			
			DatagramPacket sendPacket = new DatagramPacket(outbuf, outbuf.length, IPAddress,portProj1bRPC);
			try {
				clientSocket.send(sendPacket);
				clientSocket.setSoTimeout(timeout);
				System.out.println("Sending packet to IP " + randIP);
			} catch (IOException e) {
					e.printStackTrace();
			}
		
		
		
		// now wait for the acknowledgement from the values you sent them to.
			byte[] inbuf = new byte[dataPacketSize];
			DatagramPacket receivedPacket = new DatagramPacket(inbuf, inbuf.length);
			HashSet<String> recievedAckSet = new HashSet<String>();	
			String[] responseParts;
			
			try{
		    	//DatagramPacket receivedPacket = null;
				do {
					clientSocket.setSoTimeout(timeout);
					/*Madhuri*/
					
		    		receivedPacket.setLength(inbuf.length);
		    		clientSocket.receive(receivedPacket);
		    		InetAddress returnAddress = receivedPacket.getAddress();
		    		String IpReceived = returnAddress.toString().replace("/","");
		    		recievedAckSet.add(IpReceived);
		    		
		    		// response is the acknowledgement
		    		String response = new String(receivedPacket.getData());
		    		responseParts = response.split(RPCcalldelimiter);
		    		newIpList.add(randIP);
		    	
		    		/*Madhuri*/
		    		ViewTuple tuple = new ViewTuple(IpReceived, ViewTable.STATUS_UP,System.currentTimeMillis());
		    		SessionTracker.LocalView.addTuple(tuple);
		    		/*Madhuri*/
		    	
		    		System.out.println("In rpc client : write :Call id : " + responseParts[0] + "and message recieved from the server is" + responseParts[1]);
		    		
		    		if ((Integer.parseInt(responseParts[0]) == callID)){
		    			System.out.println("CallId : parseint :These are equal");
		    		}else {
		    			System.out.println("CallId : parseint :These are not equal");
		    		}
		    		
				}while(!(Integer.parseInt(responseParts[0]) == callID) && ((responseParts[1]).equals("true")));
		    
		    } catch(SocketTimeoutException stoe){
		    		
			    	receivedPacket = null;
			    	
			    	/*##########TO DO: UNCOMMEN*/
			    	ViewTuple obj = new ViewTuple(randIP,ViewTable.STATUS_DOWN,System.currentTimeMillis());
					SessionTracker.LocalView.updateTupleById(randIP,obj);
					
					System.out.println("########Oops!!!! Socket timed out!!!!!!########");
			    	
		    } catch(IOException ioe){
		       System.out.println("######## IO Exception!!!!!!########");
		    	ioe.printStackTrace();
		    } 
		
		
		}
		
		List<String> finalIpList = new ArrayList<String>();
		int replyNumber = newIpList.size();
		finalIpList = newIpList;
		
		/*if (replyNumber <SessionTracker.KRes){
			for (int i=0;i<(SessionTracker.KRes-replyNumber);i++){
				finalIpList.add(MySession.SvrNULL);
			}
		}*/
		System.out.println("In Rpc client write : finalList returning from write is: "+finalIpList.toString());
		System.out.println();
		return finalIpList;
			
		//return sessionDataReceived;
		
	}

}