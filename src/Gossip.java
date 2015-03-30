

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Gossip implements Runnable {
	
	final static int GOSSIP_SECS = 20000; //Delay between gossip rounds 
	
	private String serverIDLocal = null;
	
	//Database instance
	SimpleDB db = null;
	
	Gossip()
	{
		//Do some DB init stuff
		try {
			db = new SimpleDB();
			//Init the db
			//Create a view and put in view table
			db.initDBInstance();
			db.createDBDomain();				
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	Gossip(String serverIDLocal)
	{
		this.serverIDLocal = serverIDLocal;
		//Do some DB init stuff
		try {
			db = new SimpleDB();
			//Init the db
			//Create a view and put in view table
			db.initDBInstance();
			db.createDBDomain();				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("################# Gossip Thread working #######################");
		
		Random generator = new Random();
		//new RPCSender(GOSSIP_DEST_PORT).exchangeViews(ViewTable.getRandomView());

		while(true)
		{
			//Set self-view in the Views table, ensures that timeStamp is always updated
			try{
				Thread.sleep((GOSSIP_SECS/2) + generator.nextInt(GOSSIP_SECS));
			}
			catch(InterruptedException e)
			{
				System.out.println("Thread sleep was interrupted!");					
				e.printStackTrace();
			}
			
			ViewTuple view = new ViewTuple(serverIDLocal, ViewTable.STATUS_UP, System.currentTimeMillis());
			SessionTracker.LocalView.addTuple(view);
			if(SessionTracker.LocalView.getUpViewKeys().size() == 0)
			{
				
				//merge view with db and fetch view
				//get data(string) from db and convert to View
				String gotStr = db.getDatafromDB();
				System.out.println("Gossip, received view from db:"+gotStr);
				ViewTable dbView = ViewTable.stringToTuple(gotStr);
				ViewTable mergedView= ViewTable.mergeViews(SessionTracker.LocalView,dbView);
				String result = mergedView.toString();
				
				//put view back in view table
				//convert View to String and put it back to db
				System.out.println("I am trying to put in Simple DB");
				//String putStr = dbView.toString();
				String putStr = result;
				System.out.println("The putStr is : "+ putStr);
				db.putDataIntoDB(putStr);
				SessionTracker.LocalView.modifyMap(mergedView);
				System.out.println("Gossip, put view into db:"+putStr);				
			}
			else
			{
				//Pick a random index other
				 HashSet<String> randView = ViewTable.getRandomServers(1);
				if(!randView.isEmpty())
				{
					String ip = null;
					for(String s: randView){
						ip = s;
					}
					RPCClient client = new RPCClient();
					ViewTable mergedView = client.ExchangeView(SessionTracker.LocalView,ip);
					if(mergedView != null){
					SessionTracker.LocalView.modifyMap(mergedView);
					}
				}
			}
			
			
					
		}
	}
}
