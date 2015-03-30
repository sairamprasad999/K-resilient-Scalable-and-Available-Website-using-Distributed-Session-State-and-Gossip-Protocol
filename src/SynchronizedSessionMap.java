import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//Defines the SynchronizedSessionMap used to store session table
public class SynchronizedSessionMap {
	
	public static ConcurrentHashMap<String,MySession> map;
	
	public SynchronizedSessionMap(){
		map = new ConcurrentHashMap<String,MySession>();
	}
	
	//remove expired sessions
	public static synchronized void removeExpiredSessions(){
		
		Iterator<Map.Entry<String,MySession>> it = map.entrySet().iterator();
		UUID key;
		while(it.hasNext()){
			Map.Entry<String, MySession> e = it.next();
			if((e.getValue().getdiscardTime()) - System.currentTimeMillis() < 0 )
				map.remove(e.getKey());
				
		}
		
	}
	
	//add session entry
	public static synchronized void addSession(MySession obj){
		//this.removeExpiredSessions();
		map.put(obj.getSessionId(),obj);
		
	}
	
	//remove session entry
	public static synchronized void removeSession(String id){
		//this.removeExpiredSessions();
		map.remove(id);
		
	}
	
	//get session object
	public static synchronized MySession getSessionById(String id){
		//this.removeExpiredSessions();
		return map.get(id.trim());
	}
	
	//update session entry
	public static synchronized void updateSessionEntryById(String id, MySession obj){
		//this.removeExpiredSessions();
		map.put(id,obj);
	}
	
	//toString
	public static  synchronized  String asString(){
		StringBuilder s = new StringBuilder();
		Iterator<Map.Entry<String,MySession>> it = map.entrySet().iterator();
		UUID key;
		while(it.hasNext()){
			//SessionTracker.out.print("   In while with key");
			Map.Entry<String, MySession> e = it.next();
			s.append(e.toString());
			//SessionTracker.out.print("   " + e.toString());
			s.append("\n");
		}
		//SessionTracker.out.print("   Coming out of while now");
		return s.toString();
	}

}
