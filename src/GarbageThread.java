

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class GarbageThread implements Runnable{
	 
    @Override
    public void run() {
        while(true){
            killSessions();
        }
    }
 
    private void killSessions() {
        try {
        		SynchronizedSessionMap.removeExpiredSessions();
        		System.out.println(SynchronizedSessionMap.map.toString());
        		Thread.sleep(12000);
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	}
    }
    
    
	
	   	
   
}



