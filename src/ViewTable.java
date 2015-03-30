
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ViewTable {
	public static final String delimiter = ";"; 
	ConcurrentHashMap<String,ViewTuple> map = null;
	public static final String STATUS_DOWN = "D";
	public static final String STATUS_UP = "U";

	public int getViewSize(){
		return map.size();
	}
	
	public ViewTable(){
	map = new ConcurrentHashMap<String,ViewTuple>();
	}
	
	//add view tuple  entry
	public void addTuple(ViewTuple obj){
//REMOVE
	   if(obj.getSvrId()!=null && obj !=null)
		map.put(obj.getSvrId(),obj);
		
	}
	
	//get session object
	public ViewTuple getTupleById(String id){
		return map.get(id);
	}
	
	//update session entry
	public void updateTupleById(String id, ViewTuple obj){
		map.put(id,obj);
	}
	
	//merge two view tables
	public static ViewTable mergeViews(ViewTable local,ViewTable remote){
		System.out.println("In merge View: Just entered");
		ViewTable result = new ViewTable();
		for(String key: local.map.keySet()){
			if(!remote.map.keySet().contains(key))
				result.addTuple(local.map.get(key));
			else{
				if(local.map.get(key).getTime() > remote.map.get(key).getTime())
					result.addTuple(local.map.get(key));
				else
					result.addTuple(remote.map.get(key));
			}
		}
		for(String key1: remote.map.keySet()){
			if(!local.map.keySet().contains(key1))
				result.addTuple(remote.map.get(key1));
			
		}
		
		System.out.println("In merge view : The result set is: " + result.toString());
		return result;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		for (String key : map.keySet()) {
		    result.append(map.get(key).toString());
		    result.append(delimiter);
		}
		result.setLength(Math.max(result.length() - 1, 0));
		return result.toString();
	}
	
	public static ViewTable stringToTuple(String View){
		System.out.println("In viewTable : The view string is" + View);
		ViewTable result = new ViewTable();
		if(!(View == null || View.equals(""))){
			 result = new ViewTable();
			String[] viewArray = View.split(delimiter);
			for(String s: viewArray){
				String[] ViewTuple_parts = s.split(ViewTuple.tupledelimeter);
				ViewTuple tuple = new ViewTuple(ViewTuple_parts[0], ViewTuple_parts[1],Long.parseLong(ViewTuple_parts[2].trim()));
				result.addTuple(tuple);
			}
		}
		
		
		
		return result;
	}
	
	public void modifyMap(ViewTable v){
		if(v != null && v.map.size() != 0){
		this.map = v.map;
		}
	}
	
	//Get n random servers from local ViewTable
    public static HashSet<String> getRandomServers(int n)
    {
    	HashSet<String> randServers = new HashSet<String>();
    	ArrayList<String> allUpServers = getUpViewKeys();
    	
    	//Now randomly choose n
    	Random generator = new Random();
    	if(allUpServers.size() < n)
    	{
        	for(String upServer : allUpServers)
        	{
        		randServers.add(upServer);
        		System.out.println("Constant server chosen : " + upServer);
        	}
    	}
    	else
    	{
    		int i = 0;
    		while(i<n)
    		{
    			String serverID = allUpServers.get(generator.nextInt(allUpServers.size()));
    			if(!randServers.contains(serverID))
    			{
    				randServers.add(serverID);
    				System.out.println("Random server chosen : " + serverID);
    				i++;
    			}
    		}
    	}
    	
    	return randServers;
    }
    
    //Returns all UP servers from ViewTuple, except for my own IP
	public static ArrayList<String> getUpViewKeys(){
		ArrayList<String> upServers = new ArrayList<String>();
		
		for(Map.Entry<String, ViewTuple> entry : SessionTracker.LocalView.map.entrySet())
		{
			//Check only up servers, not myself
			if(entry.getValue().getStatus().equals(STATUS_UP) &&
					!entry.getValue().getSvrId().equals(SessionTracker.SvrLocal))
			{
				upServers.add(entry.getKey());
			}
		}
		return upServers;
	}
}
	

