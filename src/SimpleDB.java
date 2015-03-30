

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;

public class SimpleDB {

	public AmazonSimpleDB sdb = null;
	public String myDomain = "ViewTable";

	public void initDBInstance()  {
		
		AWSCredentials credentials = null;
		try {
			credentials=new BasicAWSCredentials(Credentials.aws_user_id, Credentials.aws_password);
			
			System.out.println(credentials);
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location and is in valid format.",
							e);
		}	
		
		sdb = new AmazonSimpleDBClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sdb.setRegion(usWest2);
	}

	public void createDBDomain() throws IOException{
		
		try {
			// Create a domain
			sdb.createDomain(new CreateDomainRequest(myDomain));
		}

		catch (AmazonServiceException ase) {
			System.out
			.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SimpleDB, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out
			.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SimpleDB, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public void putDataIntoDB(String insertValue)
	{	//put Data in DB
		//String data = "Test_value_3";
		System.out.println("In Simple DB: Entered putData");
		List<ReplaceableAttribute> replaceableAttributes = new ArrayList<ReplaceableAttribute>();
		replaceableAttributes.add(new ReplaceableAttribute("myAttr",insertValue, true));
		sdb.putAttributes(new PutAttributesRequest(myDomain, "myItem",	replaceableAttributes));
		System.out.println("In Simple DB: Exiting putData");
	}


	public String getDatafromDB()
	{	
		String final_value = "";
		String selectExpression = "select myAttr from `" + myDomain + "` where itemName() = 'myItem'";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		System.out.println("In Simple DB: Entered getData");
		
		for (Item item : sdb.select(selectRequest).getItems()) {
			for (Attribute attribute : item.getAttributes()) {
				final_value = attribute.getValue();
				System.out.println("Simple DB, Read attribute:" + final_value);
			}
		}
		System.out.println("In Simple DB: Exiting getData");
		return final_value;
	}
}






