
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;


public class ServerThread implements Runnable{
	
    public void run() {
      System.out.println("I will start my RPC server now!!!");
    	RPCServer server = new RPCServer();
    	server.ServerResponse();
   
    }
}
