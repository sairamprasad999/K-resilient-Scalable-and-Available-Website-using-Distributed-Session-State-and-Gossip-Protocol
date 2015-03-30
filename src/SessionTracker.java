

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class SessionTracker
 */
@WebServlet("/SessionTracker")
public class SessionTracker extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	String message = "Hello User";
	private static Integer count = 0;
	public static final Integer EXP_TIME = 120;
	public static String SesidDelimiter = "@";
	public static String SvrLocal;
	public static ViewTable LocalView;
	public static String SvrResponding;
	public static final int KRes = 1;
	RPCClient client = new RPCClient();
	public static PrintWriter out;
	
	public SessionTracker() {
    	super();
    	
    	
        //ip = getLocalAddr();
        //SvrLocal = "10.132.5.183";
        //String SvrMadhuri = "10.132.3.85";
        SvrLocal = GetServerIDLocal();
        
        ViewTuple newTuple = new ViewTuple(SvrLocal,ViewTable.STATUS_UP,System.currentTimeMillis());
        //ViewTuple newTuple1 = new ViewTuple(SvrMadhuri,ViewTable.STATUS_UP,System.currentTimeMillis());
       // ViewTuple newTuple2 = new ViewTuple("1.2.3.4",ViewTable.STATUS_UP,System.currentTimeMillis());
        
        LocalView = new ViewTable();
        LocalView.addTuple(newTuple);
        
        //LocalView.addTuple(newTuple1);
       
        // TODO Auto-generated constructor stub
        SynchronizedSessionMap ssmap = new SynchronizedSessionMap();
        try {
			System.out.println("IN INIt");
			Thread dt = new Thread(new GarbageThread(), "dt");
		    dt.setDaemon(true);
		    dt.start();
		    Thread.sleep(500);
		} catch(InterruptedException e) {
			System.out.println("Exception In calling daemon");
		}
        //RPCServer server= new RPCServer();
       // server.ServerResponse();
        System.out.println("IN constructor.STart the server");
        Thread d = new Thread(new ServerThread());
        //dt.setDaemon(true);
        d.start();
        
      //Start a thread for Gossip
	    Thread gossiper = new Thread(new Gossip(SvrLocal));
	    gossiper.start();	 
    }
	
	protected String GetServerIDLocal()
	{
		String localIpAddress = null;
		InetAddress localInetAddress = null;
		String command = "/opt/aws/bin/ec2-metadata --public-ipv4";
		
		try{
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			
			String inputLine = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			while((inputLine = br.readLine()) != null)
			{
				System.out.println("inputLine" + inputLine);
				String[] inputString = inputLine.split(" ");
				localIpAddress = inputString[1];
			}
			
		}catch (IOException e){
			System.out.println(e.getMessage());
			try{
				// Try to get local server ip address, if its not ec2 instance
				localInetAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
				localIpAddress = localInetAddress.toString().substring(1);
			}catch (UnknownHostException e1){
				System.out.println(e1.getMessage());
			}
			
		}
	
		return localIpAddress;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Check if visitor is accessing url without a cookie (new/expired)
		//Perform garbage handling	
			 //ssmap.removeExpiredSessions();
			 
		
		response.setContentType("text/html");
		out = response.getWriter();
		out.print(ServletUtilities.headWithTitle("CS5300PROJ1SESSION"));
		boolean newbie = true;
		Cookie[] cookies = request.getCookies();
		String oldCookieValue = null;
		if (cookies != null) {
			for(Cookie c: cookies) {
				if ((c.getName().equals("CS5300PROJ1SESSION"))) {
					newbie = false;
					oldCookieValue = c.getValue();
					break;
				}
			}
		}
		
		if (newbie) {
			//Handle new/expired session case here
			System.out.println("New request without cookie came");
			//Create new session
			int version = 0;
			
			MySession session = new MySession();
			synchronized (this){
			count++;
			String id = count+SesidDelimiter+SvrLocal;
			session.generateSession(id, message,version);
			/*ArrayList<String> test = new ArrayList();
			test.add("10.132.3.85");
			session.setlistOfIps(test);*/
			//out.println("Inititating a the RPc client write.About to call sessionWriteClien");
			
			List<String> IpList = client.sessionWriteClient(id, session);
			
			//out.println("List of IPS returned are: "+IpList.toString());
			session.setlistOfIps(IpList);
			SynchronizedSessionMap.addSession(session);
			//out.println(SynchronizedSessionMap.asString());
			}
			
			
			String debugValue = session.getSessionId()+"_Version="+session.getVersion()+"_DiscardTime="+(session.getdiscardTime())+"_ExpTime="+(session.getdiscardTime()-MySession.delta*1000)+"_Location="+SvrLocal;
			String cookieValue = session.cookieValue();
			Cookie returnVisitorCookie = new Cookie("CS5300PROJ1SESSION",cookieValue);
			returnVisitorCookie.setMaxAge(EXP_TIME);
			response.addCookie(returnVisitorCookie);
			out.print("<body>\n"
					+ "<h1>"+message+"</h1>\n"
					+ "");
			out.print("<p>\n"
					+ "<form name=\"managesession\" method=\"POST\" action=\"SessionTracker\">\n"
					+"<input type=\"submit\" value=\"replace\" name=\"replace\"/> 	<input type=\"text\" name=\"message\"/> <br/><br/>\n"
					+"<input type=\"submit\" value=\"refresh\" name=\"refresh\"/><br/><br/>\n"
					+"<input type=\"submit\" value=\"logout\" name=\"logout\"/><br/><br/>\n"
					+"</form><br/>\n");
			out.print("<p>\n"
					+debugValue+"</p>\n");
			out.print("<p>\n"
					+cookieValue+"</p>\n");
			printTables(out);
			
			} else {
			//Handle old session case here
				//out.print("old session case");
				int flag = 0;
				MySession session = new MySession();
				String[] parts = oldCookieValue.split(MySession.CookieDelimiter);
				
				String id = parts[0].trim();
				//out.print("THE ID RECIEVED IS :" + id);
				String vnum = parts[1].trim();
				//out.print("THE VERSION RECEIVED IS :" + id);
				
				for(int i = 2; i < parts.length;i++){
					//out.print("in for loop :Svrlocal is " + SvrLocal +"\\n");
					//out.print("in for loop parts[i " + parts[i] +"\\n");
					if(parts[i].equals(SvrLocal))
					{
						session = SynchronizedSessionMap.getSessionById(id);
						//out.println("Old Session Case: ID is "+id);
						//out.println("Old Session Case: Version is"+ session.getVersion());
						if(session.getVersion()==Integer.parseInt(parts[1])){
							flag = 1;
							break;
						}
					}
				}
				//out.print("The flag after for is: + " + flag);
				
				MySession newSession = new MySession();
				// I am not a primary or secondary
				if(flag != 1){
						//out.print("unhandled case");
						//out.println("Current SvrIP not found in Cookie List");
						RPCClient client = new RPCClient();
						
						//out.print("Goin for a read from RPC and cookie value is " + oldCookieValue );
						//out.print("The id i m sending is" + id);
					
						/*Madhuri*/
						List<String> newIpList = new ArrayList<String> ();
						for (int i =2 ;i <parts.length; i ++){
							if (!(parts[i].equals(MySession.SvrNULL))){
								newIpList.add(parts[i].trim());
							}
						}

						String sesString = client.sessionReadClient(id,vnum,newIpList);
						/*Madhuri*/
						
						//String sesString = client.sessionReadClient(id);
						//out.print("sesString returned by read is " +sesString);
						if(sesString.equals("notfound")){
							String newdebugValue = newSession.getSessionId()+"_Version="+newSession.getVersion()+"_DiscardTime="+(newSession.getdiscardTime()+60000)+"_ExpTime="+(newSession.getdiscardTime()+60000-MySession.delta*1000)+"_Location="+SvrLocal;
							String newcookieValue = newSession.cookieValue();
							Cookie newreturnVisitorCookie = new Cookie("CS5300PROJ1SESSION",newcookieValue);
							out.println("ERROR not found the session in server");
							newreturnVisitorCookie.setMaxAge(0);
							response.addCookie(newreturnVisitorCookie);
							out.print("<body>\n"
									+ "<h1>"+"Session Error"+"</h1>\n"
									+ "");
							out.print("<p>\n"
									+ "<form name=\"managesession\" method=\"POST\" action=\"SessionTracker\">\n"
									+"<input type=\"submit\" value=\"Login\" name=\"login\"/>\n"
									+"</form><br/>\n");
							//printTables(out);
							
						} // It is the primary or the secondary itself
						else {
							newSession = MySession.sesFromStr(sesString);
						}
							
						
				}
				
				else{
					//out.print("THE ID by which i am looking is :" + id);
					//out.print(SynchronizedSessionMap.map.toString());
					
					newSession = SynchronizedSessionMap.getSessionById(id);
					
					//out.print("The generated session is " + (SynchronizedSessionMap.getSessionById(id).toString()));
					//newSession = session;
				}
				
				if(newSession != null){
					//out.print("  New session is not null    ");
					//out.print(" the value of new session is : "+ newSession.toString() );
					
					newSession.setVersion(newSession.getVersion()+1);
					newSession.setdiscardTime();
			
					if(request.getParameter("replace") != null){
						//Handle replace case here
						String message = request.getParameter("message");
						if(message.equals("")){
							newSession.setMessage("Hello User");
						} else{
							newSession.setMessage(message);
						}
						
						synchronized (this){
							List<String> IpList = client.sessionWriteClient(id, newSession);
							newSession.setlistOfIps(IpList);
							SynchronizedSessionMap.updateSessionEntryById(id,newSession);
						}
						
					}
					if(request.getParameter("refresh") != null){
						//Handle refresh case here
						
						synchronized (this){
							List<String> IpList = client.sessionWriteClient(id, newSession);
							newSession.setlistOfIps(IpList);
							SynchronizedSessionMap.updateSessionEntryById(id,newSession);
						}
				
					}
					if(request.getParameter("logout") != null){
						//handle logout case here
						SynchronizedSessionMap.removeSession(id);
						/*
						newSession = new MySession();
						newSession.generateSession(++count, message,0);
						synchronized (this){
						ssmap.addSession(newSession);
						}
						*/
					}
				
				
					String newdebugValue = newSession.getSessionId()+"_Version="+newSession.getVersion()+"_DiscardTime="+(newSession.getdiscardTime()+60000)+"_ExpTime="+(newSession.getdiscardTime()+60000-MySession.delta*1000)+"_Location="+SvrLocal;
					String newcookieValue = newSession.cookieValue();
			
					
					
					Cookie newreturnVisitorCookie = new Cookie("CS5300PROJ1SESSION",newcookieValue);
					if(request.getParameter("logout") != null){
						newreturnVisitorCookie.setMaxAge(0);
						response.addCookie(newreturnVisitorCookie);
						out.print("<body>\n"
								+ "<h1>"+"LoggedOut"+"</h1>\n"
								+ "");
						out.print("<p>\n"
								+ "<form name=\"managesession\" method=\"POST\" action=\"SessionTracker\">\n"
								+"<input type=\"submit\" value=\"Login\" name=\"login\"/>\n"
								+"</form><br/>\n");
						
						//out.print("<p>\n"
						//		+newcookieValue+"</p>\n");
						
					}
					else{
						newreturnVisitorCookie.setMaxAge(EXP_TIME);
					
						response.addCookie(newreturnVisitorCookie);
						out.print("<body>\n"
								+ "<h1>"+newSession.getMessage()+"</h1>\n"
								+ "");
						out.print("<p>\n"
								+ "<form name=\"managesession\" method=\"POST\" action=\"SessionTracker\">\n"
								+"<input type=\"submit\" value=\"replace\" name=\"replace\"/> 	<input type=\"text\" name=\"message\"/> <br/><br/>\n"
								+"<input type=\"submit\" value=\"refresh\" name=\"refresh\"/><br/><br/>\n"
								+"<input type=\"submit\" value=\"logout\" name=\"logout\"/><br/><br/>\n"
								+"</form><br/>\n");
						out.print("<p>\n"
								+newdebugValue+"</p>\n");
						out.print("<p>\n"
								+newcookieValue+"</p>\n");
						printTables(out);
					}
			
				
			}else {
				//out.print("your session  is null");
				out.print("<body>\n"
						+ "<h1>"+"Session Error"+"</h1>\n"
						+ "");
				out.print("<p>\n"
						+ "<form name=\"managesession\" method=\"POST\" action=\"SessionTracker\">\n"
						+"<input type=\"submit\" value=\"Login\" name=\"login\"/>\n"
						+"</form><br/>\n");
			}
				
				
				//out.println("<br/><p>"+this.LocalView+"<p><br/>"+SynchronizedSessionMap.map.toString());
				//Code to print view table
				//printTables(out);
				/*
				out.println("<p>Request received at:"+ SvrLocal+"<p>");
				out.println("<table BORDER=1 CELLPADDING=0 CELLSPACING=0 WIDTH=50% >");
				Iterator it = this.LocalView.map.entrySet().iterator();
				out.println("<caption>View Table</caption>"
						+ "<tr>"
						+ "<th>Svr IP</th>"
						+ "<th>Status</th>"
						+"<th>Last Seen Time</th>"
						+"</tr>");
			    while (it.hasNext()) {
			        HashMap.Entry<String,ViewTuple> pair = (HashMap.Entry<String,ViewTuple>)it.next();
			        ViewTuple tuple = pair.getValue();
			        out.println("<tr>");
		            out.print("<td>"+tuple.getSvrId()+ "</td>");
		            out.print("<td>"+tuple.getStatus()+ "</td>");
		            out.print("<td>"+tuple.getTime()+ "</td>");
		            out.println("</tr>");
			        
			    }*/
				/*
				out.print("<body>\n"
						+ "<h1>"+newSession.getMessage()+"</h1>\n"
						+ "");
				out.print("<p>\n"
						+ "<form name=\"managesession\" method=\"POST\" action=\"SessionTracker\">\n"
						+"<input type=\"submit\" value=\"replace\" name=\"replace\"/> 	<input type=\"text\" name=\"message\"/> <br/><br/>\n"
						+"<input type=\"submit\" value=\"refresh\" name=\"refresh\"/><br/><br/>\n"
						+"<input type=\"submit\" value=\"logout\" name=\"logout\"/><br/><br/>\n"
						+"</form><br/>\n");
				out.print("<p>\n"
						+newdebugValue+"</p>\n");
				out.print("<p>\n"
						+newcookieValue+"</p>\n");
				printTables(out);*/
				
			}
		
			
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void printTables(PrintWriter out){
		out.println("<p>Request received at:"+ SvrLocal+"<p>");
		out.println("<p>Request Processed by:"+ SvrResponding+"<p>");
		out.println("<table BORDER=1 CELLPADDING=0 CELLSPACING=0 WIDTH=50% >");
		Iterator it = this.LocalView.map.entrySet().iterator();
		out.println("<caption>View Table</caption>"
				+ "<tr>"
				+ "<th>Svr IP</th>"
				+ "<th>Status</th>"
				+"<th>Last Seen Time</th>"
				+"</tr>");
	    while (it.hasNext()) {
	        HashMap.Entry<String,ViewTuple> pair = (HashMap.Entry<String,ViewTuple>)it.next();
	        ViewTuple tuple = pair.getValue();
	        out.println("<tr>");
            out.print("<td>"+tuple.getSvrId()+ "</td>");
            out.print("<td>"+tuple.getStatus()+ "</td>");
            out.print("<td>"+tuple.getTime()+ "</td>");
            out.println("</tr>");
	        
	    }
	    out.println("</table><br/>");
	    Iterator ssit = SynchronizedSessionMap.map.entrySet().iterator();
	    out.println("<table BORDER=1 CELLPADDING=0 CELLSPACING=0 WIDTH=50% >");
	    out.println("<caption>Session Table</caption>"
				+ "<tr>"
				+ "<th>Session ID</th>"
				+ "<th>Message</th>"
				+"<th> Discard Time</th>"
				+"<th> Version</th>"
				+"<th> List of IPs</th>"
				+"</tr>");
	    while (ssit.hasNext()) {
	        HashMap.Entry<String,MySession> pair = (HashMap.Entry<String,MySession>)ssit.next();
	        MySession s= pair.getValue();
	        out.println("<tr>");
            out.print("<td>"+s.getId()+ "</td>");
            out.print("<td>"+s.getMessage()+ "</td>");
            out.print("<td>"+s.getdiscardTime()+ "</td>");
            out.print("<td>"+s.getVersion()+ "</td>");
            out.print("<td>"+s.getlistOfIps().toString()+ "</td>");
            out.println("</tr>");
	        
	    }
	    out.println("</table><br/>");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(request,response);
	}

}
