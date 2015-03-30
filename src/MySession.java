import java.util.*;

//Defines the Session object Structure and operations
public class MySession {
	
	public static final int CookieAge = 120;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static final String CookieDelimiter = "_";
	public final String IPDelimiter = ",";
	public final static int delta=5 * SessionTracker.KRes;
	public static final String SvrNULL = "0.0.0.0";
	private String id;
	private String message;
	private Long discardTime; 
	private Integer version;
	List<String> listOfIps = new ArrayList<String>();
	
	public void generateSession(String sid, String state,int ver){
		this.id = sid;
		this.discardTime = System.currentTimeMillis()+(CookieAge*1000)+(delta*1000);
		this.message = state;
		this.version = ver;
		this.listOfIps.add(SessionTracker.SvrLocal);
		
	}
	
	//define setters
	public void setMessage(String state){
		this.message = state;
	}
	
	public void setVersion(int ver){
		this.version = ver;
	}
	public void setdiscardTime(){
		this.discardTime = System.currentTimeMillis()+(CookieAge*1000)+(delta*1000);
	}
	
	public void setlistOfIps(List<String> ipList){
		this.listOfIps= new ArrayList<String>();
		this.listOfIps.add(SessionTracker.SvrLocal);
		for(int i=0;i<ipList.size();i++){
			this.listOfIps.add(ipList.get(i));
		}
		
	}
		
	//define getters
	public Long getdiscardTime(){
		return this.discardTime;
	}
	
	public String getSessionId(){
		return this.id;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public Integer getVersion(){
		return this.version;
	}
	
	
	
	public List<String> getlistOfIps(){
		return listOfIps;
	}
	
	public String cookieValue(){
		StringBuilder s = new StringBuilder();
		s.append(id);
		s.append(CookieDelimiter);
		s.append(version);
		s.append(CookieDelimiter);
		for(String ip: listOfIps ){
			s.append(ip);
			s.append(CookieDelimiter);
		}
		s.deleteCharAt(s.length()-1);
		String cook = s.toString().trim();
		for (int i=this.getlistOfIps().size();i<(SessionTracker.KRes+1);i++){
			cook += "_" + MySession.SvrNULL;
		}
		return cook;
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		s.append(id);
		s.append(CookieDelimiter);
		s.append(message);
		s.append(CookieDelimiter);
		s.append(discardTime);
		s.append(CookieDelimiter);
		s.append(version);
		s.append(CookieDelimiter);
		for(String ip: listOfIps ){
			s.append(ip);
			s.append(CookieDelimiter);
		}
		s.deleteCharAt(s.length()-1);
		return s.toString().trim();
	}

	
	public static MySession sesFromStr(String sessionString){
		System.out.println();
		String[] sesParts = sessionString.split(MySession.CookieDelimiter);
		MySession result = new MySession();
		result.id= sesParts[0];
		result.message =sesParts[1];
		result.discardTime = Long.parseLong(sesParts[2]);
		result.version = Integer.parseInt(sesParts[3]);
		for(int i = 4; i< sesParts.length;i++){
			result.listOfIps.add(sesParts[i]);
		}
		return result;
		
	}

}
