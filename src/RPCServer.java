import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;

public class RPCServer {
	
	public final static int portProj1bRPC   =   5300;
	public final static int dataPacketSize = 512;
	String rpcDelimiter = ":";

	public void ServerResponse(){
		// TODO Auto-generated method stub
		
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(portProj1bRPC);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        while(true)
           {
           System.out.println("\nIn RPC Server: start ");
        	byte[] inBuf = new byte[dataPacketSize];
			DatagramPacket receivePacket = new DatagramPacket(inBuf, inBuf.length);
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              
			InetAddress returnAddress = receivePacket.getAddress();
			int returnPort = receivePacket.getPort();
			String receivedString= new String(receivePacket.getData());
			String[] parts = receivedString.split(rpcDelimiter);
			
			//SessionTracker.out.println("The value of received is "+receivedString);
			System.out.println("The value of received is "+receivedString);
			
			int opCode = Integer.parseInt(parts[1]);
			String callID = parts[0];
			
		//	SessionTracker.out.println("In RPC Server: The packet received from client is: "+receivedString);
		//	SessionTracker.out.println("In RPC Server:The return address is: " + returnAddress.toString().replace("/",""));
			System.out.println("In RPC Server: The packet received from client is: "+receivedString);
			System.out.println("In RPC Server:The return address is: " + returnAddress.toString().replace("/",""));
			
			ViewTuple vt = new ViewTuple(returnAddress.toString().replace("/",""),ViewTable.STATUS_UP,System.currentTimeMillis());
			
			//update view for this server in my table
			if(SessionTracker.LocalView.map.containsKey(returnAddress.toString())){
				SessionTracker.LocalView.updateTupleById(returnAddress.toString(), vt);
			}
			else{
				SessionTracker.LocalView.addTuple(vt);
			}
			
		//	SessionTracker.out.println("In RPC Server: view is:  "+SessionTracker.LocalView.toString());
			System.out.println("In RPC Server: view is:  "+SessionTracker.LocalView.toString());
			
			String result = callID;
			
		//	SessionTracker.out.println("In RPC Server: Request recived from ip" +returnAddress.toString().replace("/","") );
			System.out.println("In RPC Server: Request recived from ip" +returnAddress.toString().replace("/","") );
			
			switch (opCode){
			case 1 : System.out.println("IN RESULT OPCODE1");
			
					System.out.println(SessionRead(parts[0]));
					System.out.println(SessionRead(parts[1]));
					System.out.println(SessionRead(parts[2]));
					
					result= result + rpcDelimiter + SessionRead(parts[2]);
						
					 System.out.println("In RPC Server:After read call: result : "+result);
					break;
			case 2 : SessionWrite(parts);
					 result = result+rpcDelimiter+"true";
					break;
			case 3 : ViewTable ViewClient = ViewTable.stringToTuple(parts[2]);
			      	 System.out.println("In RPC server Exchange view: " + ViewClient.toString() );
					 ViewTable ViewServer = SessionTracker.LocalView;
					//ExchangeView(ViewServer,ViewClient);
					ViewTable finalView= ViewTable.mergeViews(ViewClient, ViewServer);
					
					System.out.println("In RPC server : Exchange View: "+ finalView.toString() );
					
					SessionTracker.LocalView.modifyMap(finalView);
					//code to return this view back to client
					result = result+ rpcDelimiter+finalView.toString();
					
					System.out.println("In RPC server :  Exchange View : Exiting");
					
					break;
			}
			
			   System.out.println("AFter breaks");
			   System.out.println("returnAddres is" +returnAddress);
			   System.out.println("returnport is" +returnPort);
			   
			byte[] outBuf = result.getBytes();
			DatagramPacket sendPacket =  new DatagramPacket(outBuf, outBuf.length, returnAddress, returnPort);
			try {
				System.out.println("I am trying to send in server back to RPC client ::");
				serverSocket.send(sendPacket);
				System.out.println("i have sent it: now rpc client responsibility");
			
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
           }
     
	}
	
	// Code for Reading
	public String SessionRead(String sessionID){
		System.out.println("In RPC server :: In session Read");
		String result = "notfound";
		//System.out.println("in RPC server :: sessionid passed  " + sessionID);
		//System.out.println(SynchronizedSessionMap.asString());
		if (SynchronizedSessionMap.getSessionById(sessionID.trim())!= null){
			
			System.out.println("in RPC server :: sessionid was found in map " + sessionID);
			
			result = SynchronizedSessionMap.getSessionById(sessionID.trim()).toString();
			
			
			System.out.println("The result i would be sending from RPC server " + result);
		
		}
		System.out.println(result);
		System.out.println();
		return result;
	}
	
	
	//Code for Writing
	public void SessionWrite(String[] parts){
		System.out.println("In RPC Server:: In session Write");
		
		String ssdata = parts[2];
		 System.out.println("In RPC Server :: WRITE:");
         //System.out.println("the parts is: " + parts[2]);
		MySession temp = MySession.sesFromStr(ssdata);
		/*##########3REMOVE SYSO####*/
		
         //System.out.println("The Value of session in RPC SERVER is: ");
         //System.out.println(temp.toString());
		SynchronizedSessionMap.addSession(temp);
		System.out.println();
		
	}
	
	/*public static void main(String[] args){
		SynchronizedSessionMap look = new SynchronizedSessionMap();
		RPCServer r = new RPCServer();
		String[] parts = {"dffdf","fdfdf","1-1.1.1.1_hi_4654_0_1.1.2.3"};
		r.SessionWrite(parts);
	}*/
	

}
