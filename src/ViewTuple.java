
public class ViewTuple{
   public final static String tupledelimeter = "/";
   
	private String SvrId;
	private String status;
	private long time;
	
	public ViewTuple(String id,String stat,long t){
		SvrId = id;
		status = stat;
		time = t;
	}

	public String getSvrId() {
		return SvrId;
	}

	public void setSvrId(String svrId) {
		SvrId = svrId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public String toString()
	{
	   StringBuilder str = new StringBuilder(SvrId);
	   str.append(tupledelimeter);
	   str.append(status);
	   str.append(tupledelimeter);
	   str.append(time);
	   return str.toString();
	}
	
}
