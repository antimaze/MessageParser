package org.project.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Configuration {
	
	private String spreadSheetId;
	private String nameRange;
	private String dateRange;
	private File credentialFile;
	private File messageFile;
	
	public Configuration(String configFilePath) throws FileNotFoundException, IOException, ParseException 
	{
		// parsing file "JSONExample.json" 
		Object obj = new JSONParser().parse(new FileReader(configFilePath));
        // typecasting obj to JSONObject 
        JSONObject jo = (JSONObject) obj; 
        
        String spreadSheetId 		= (String) jo.get("spreadsheet_id");
        String nameRange 			= (String) jo.get("name_range");
        String dateRange 			= (String) jo.get("date_range");
        String credentialFilePath 	= (String) jo.get("credentials_file_path");
        String messageFilePath 		= (String) jo.get("message_file_path");
		
        if(!spreadSheetId.trim().equals(""))
        {
        	this.spreadSheetId = spreadSheetId;
        }
        else
        {
        	System.out.println("Spreadsheet Id can't be empty...");
        	System.exit(1);
        }
        
        if(!nameRange.trim().equals(""))
        {
        	this.nameRange = nameRange;
        }
        else
        {
        	System.out.println("Name range can't be empty...");
        	System.exit(1);
        }
        
        if(!dateRange.trim().equals(""))
        {
        	this.dateRange = dateRange;
        }
        else
        {
        	System.out.println("Date cell range can't be empty...");
        	System.exit(1);
        }
        
        credentialFile = new File(credentialFilePath);
        if(!credentialFile.exists())
        {
        	System.out.println("Credentials file does not exist...");
        	System.exit(1);
        }
        
        messageFile = new File(messageFilePath);
        if(!messageFile.exists())
        {
        	System.out.println("Message file does not exist...");
        	System.exit(1);
        }
	}

	public String getSpreadSheetId() {
		return spreadSheetId;
	}

	public void setSpreadSheetId(String spreadSheetId) {
		this.spreadSheetId = spreadSheetId;
	}

	public String getNameRange() {
		return nameRange;
	}

	public void setNameRange(String nameRange) {
		this.nameRange = nameRange;
	}

	public String getDateRange() {
		return dateRange;
	}

	public void setDateRange(String dateRange) {
		this.dateRange = dateRange;
	}

	public File getCredentialFile() {
		return credentialFile;
	}

	public void setCredentialFile(File credentialFile) {
		this.credentialFile = credentialFile;
	}

	public File getMessageFile() {
		return messageFile;
	}

	public void setMessageFile(File messageFile) {
		this.messageFile = messageFile;
	}
}
