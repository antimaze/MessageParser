package org.googlesheet.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.messages.messagemodel.Message;
import org.messages.messagemodel.MessageDate;
import org.messages.messagemodel.MessageDocument;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;


public class GoogleSheetUpdater {
	
	MessageDocument doc;
		
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    
	/**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static File CREDENTIALS_FILE;

    private final String SPREADSHEET_ID;
    private final String NAME_RANGE;
	private final String DATE_RANGE;
    
	public GoogleSheetUpdater(MessageDocument doc, File credentialFile, String spreadSheetId, String nameRange, String dateRange) 
	{
		this.doc = doc;
		
		CREDENTIALS_FILE = credentialFile;
		SPREADSHEET_ID = spreadSheetId;
		NAME_RANGE = nameRange;
		DATE_RANGE = dateRange;
	}
	
	/**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
    	InputStream in = new FileInputStream(CREDENTIALS_FILE);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
	
	public void update() throws GeneralSecurityException, IOException
	{
		System.out.println("Getting values");
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        GoogleSheetQueries queryFormat = new GoogleSheetQueries(service);

        // Read names from spreadsheet...
        HashMap<String, Integer> personMap = getPersonMap(service);
                
        // Create date map...
        HashMap<String, Integer> dateMap = getDateMap();
        
        MessageDate lastParsedDate = getLastParsedDate(service);
        
        List<Message> newMessages = getNewMessages(lastParsedDate);
        List<Message> parsedMessages = getParsedMessages(newMessages);

        List<Message> personNotPresentInGoogleSheet = new ArrayList<Message>();
        if(!parsedMessages.isEmpty())
        {
            // get date range from parsed messages...
            List<String> dateRange = getDateRange(parsedMessages);
            
            HashMap<String, int[]> googleSheetData = getGoogleSheetData(service, dateRange, personMap, dateMap);
            personNotPresentInGoogleSheet = processGoogleSheetData(googleSheetData, personMap, parsedMessages);
            
            updateGoogleSheet(queryFormat, googleSheetData, dateMap);
        }
        else
        {
        	System.out.println("Google sheet is not updated as there are no actual messages to parse...");
        }
        
        // Update last parsed date in the google sheets...
        String updatedDate = updateLastParsedDate(queryFormat);
        System.out.println("Last Parsed Message Date: " + updatedDate);
        
        writeMessagesInFile(newMessages, personNotPresentInGoogleSheet);
	}
	
	

	private List<Message> getNewMessages(MessageDate lastParsedDate) 
	{
		List<Message> messages = doc.getMessages();
		List<Message> result = new ArrayList<Message>();
		
		int length = messages.size();
		for(int i=0; i<length; i++)
		{
			Message message = messages.get(i);
			if(message.getDate().compareTo(lastParsedDate) > 0)
			{
				result.add(message);
			}
		}
		return result;
	}

	private void writeMessagesInFile(List<Message> newMessages, List<Message> personNotPresentInGoogleSheet) throws IOException 
	{
		String path = System.getProperty("user.dir");        
        String fileName = "MessagesNotParsed.txt";

        StringBuilder sb = new StringBuilder();
        
        int totalMessages = newMessages.size();
        int notParsedMessagesCount = 0;
        
        if(!newMessages.isEmpty())
        {
        	sb.append("MESSAGES NOT PARSED\n\n");
        }
        for(Message message: newMessages)
        {
        	if(!message.isParsed())
        	{
        		sb.append("--------------------------------------------------------------------------------------------------------------------------------\n");
        		sb.append(message.getMessageBody());
        		notParsedMessagesCount++;
        	}
        }
        
        if(!personNotPresentInGoogleSheet.isEmpty())
        {
        	sb.append("\n\n\n\n");
        	sb.append("--------------------------------------------------------------------------------------------------------------------------------\n");
        	sb.append("PARSED MESSAGES (BUT PERSON DOES NOT EXIST IN GOOGLE SHEET WITH THE GIVEN NAME)\n\n");
        }
        for(Message message: personNotPresentInGoogleSheet)
        {
        	sb.append("--------------------------------------------------------------------------------------------------------------------------------\n");
    		sb.append("Person with the name \"" + message.getName() + "\" does not exist in the google sheet...\n");
    		sb.append(message.getMessageBody());
        }
        
        String str = "Out of " + totalMessages + " messages, " + (totalMessages - notParsedMessagesCount - personNotPresentInGoogleSheet.size()) + " messages are parsed...\n\n";
        sb.insert(0, str);
        
        PrintWriter writer = new PrintWriter(path + "/" + fileName);
        writer.print(sb.toString());
        writer.close();
        
        System.out.println("File named " + fileName + " is created with messages that are not parsed...");
	}

	private String updateLastParsedDate(GoogleSheetQueries queryFormat) throws IOException 
	{
		final String date = doc.getLastParsedMessageDate().getDateString();
		final List<Object> _values = new ArrayList<Object>();
		_values.add(date);
		List<List<Object>> values = new ArrayList<List<Object>>();
		values.add(_values);
		queryFormat.batchUpdateValues(SPREADSHEET_ID, DATE_RANGE, "RAW", values);
		
		return date;
	}

	private void updateGoogleSheet(GoogleSheetQueries queryFormat, HashMap<String, int[]> googleSheetData, HashMap<String, Integer> dateMap) throws IOException
	{	
		for(Map.Entry<String, int[]> entry: googleSheetData.entrySet())
		{
			String date = entry.getKey();
			int rowIndex = dateMap.get(date);
			List<Object> _values = Arrays.stream(entry.getValue()).boxed().collect(Collectors.toList());
			
			List<List<Object>> values = new ArrayList<List<Object>>();
			values.add(_values);
			String range = "Sheet1!B" + rowIndex;
			queryFormat.batchUpdateValues(SPREADSHEET_ID, range, "RAW", values);
		}
	}

	private List<Message> processGoogleSheetData(HashMap<String, int[]> googleSheetData, HashMap<String, Integer> personMap, List<Message> parsedMessages)
	{
		List<Message> personNotPresentInGoogleSheet = new ArrayList<Message>();
		for(int i=0; i<parsedMessages.size(); i++)
		{
			Message message = parsedMessages.get(i);
			String name = message.getName();
			int total = message.getTo() - message.getFrom() + 1;
			String messageDate = message.getDate().getDay() + "-" + message.getDate().getMonthString();
			
			int[] rowData = googleSheetData.get(messageDate);
			
			if(personMap.containsKey(name))
			{
				int index = personMap.get(name);
				rowData[index-1] += total;
			}
			else
			{
				System.out.println("Person dones not exist in google sheet with this name: " + name);
				System.out.println(name + " Part: " + message.getPartNum() + " From: " + message.getFrom() + " To: " + message.getTo());
				personNotPresentInGoogleSheet.add(message);
			}
		}
		
		return personNotPresentInGoogleSheet;
	}

	private HashMap<String, int[]> getGoogleSheetData(Sheets service, List<String> dateRange, HashMap<String, Integer> personMap, HashMap<String, Integer> dateMap) throws IOException 
	{
		HashMap<String, int[]> dateRowMap = new HashMap<String, int[]>();
		
		if(dateRange.size() == 0)
		{
			return dateRowMap;
		}
		
		String range = null;
		if(dateRange.size() == 1)
		{
			String date = dateRange.get(0);
			int rowIndexOfDate = dateMap.get(date);
			range = "Sheet1!B" + rowIndexOfDate + ":AL" + rowIndexOfDate;
		}
		else
		{
			String firstDate = dateRange.get(0);
			String lastDate = dateRange.get(dateRange.size()-1);
			
			int rowIndexOfFirstDate = dateMap.get(firstDate);
			int rowIndexForLastDate = dateMap.get(lastDate);
			range = "Sheet1!B" + rowIndexOfFirstDate + ":AL" + rowIndexForLastDate;
		}
		
		ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
		
		List<List<Object>> values = response.getValues();
		
		int valuesSize = 0;
		if(values != null)
		{
			valuesSize = values.size();
		}
		for(int i=0; i<dateRange.size(); i++)
		{
			int[] array = new int[personMap.size()];
			if(i < valuesSize)
			{
				List<Object> rowData = values.get(i);
				for(int j=0; j<rowData.size(); j++)
				{
					String obj = (String) rowData.get(j);
					if(obj.equals(""))
					{
						array[j] = 0;
					}
					else
					{
						array[j] = Integer.parseInt(obj);
					}
				}
			}
			
			dateRowMap.put(dateRange.get(i), array);
		}
		
		return dateRowMap;
	}

	private List<String> getDateRange(List<Message> parsedMessages) 
	{
		List<String> range = new ArrayList<String>();
		
		if(parsedMessages.size() == 0)
		{
			return null;
		}
		
		if(parsedMessages.size() == 1)
		{
			Message message = parsedMessages.get(0);
			String date = message.getDate().getDay() + "-" + message.getDate().getMonthString();
			range.add(date);
			return range;
		}
		
		int firstMessageDay = parsedMessages.get(0).getDate().getDay();
		int firstMessageMonth = parsedMessages.get(0).getDate().getMonth();
		int lastMessageDay = parsedMessages.get(parsedMessages.size()-1).getDate().getDay();
		int lastMessageMonth = parsedMessages.get(parsedMessages.size()-1).getDate().getMonth();
		
		if(firstMessageMonth == lastMessageMonth)
		{
			for(int i=firstMessageDay; i<=lastMessageDay; i++)
			{
				String date = i + "-" + MessageDate.months[firstMessageMonth];
				range.add(date);
			}
		}
		else
		{
			String date = "";
			
			// Start month range...
			for(int i= firstMessageDay; i<= MessageDate.monthDays[firstMessageMonth]; i++)
			{
				date = i + "-" + MessageDate.months[firstMessageMonth];
				range.add(date);
			}
			
			// Middle month range...
			for(int i=firstMessageMonth+1; i<lastMessageMonth; i++)
			{
				for(int j=1; j<=MessageDate.monthDays[i]; j++)
				{
					date = j + "-" + MessageDate.months[i];
					range.add(date);					
				}
			}
			
			// Last month range...
			for(int i=1; i<=lastMessageDay; i++)
			{
				date = i + "-" + MessageDate.months[lastMessageMonth];
				range.add(date);
			}
			
		}
		
		return range;
	}

	private List<Message> getParsedMessages(List<Message> messages)
	{
		List<Message> result = new ArrayList<Message>();
		
		int length = messages.size();
		for(int i=0; i<length; i++)
		{
			Message message = messages.get(i);
			if(message.isParsed())
			{
				result.add(message);
			}
		}
		
		return result;
	}

	private MessageDate getLastParsedDate(Sheets service) throws IOException 
	{
		ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, DATE_RANGE)
                .execute();
		
		
		String date = null;
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) 
        {
            System.out.println("No last parsed date found...");
            return null;
        }
        else
        {
        	date = (String) values.get(0).get(0);
        }
        
        return new MessageDate(date);
	}

	/**
	 * Creates a date map in which date ("8-June") as a Key and row index as a Value...
	 * @return hashmap...
	 */
	private HashMap<String, Integer> getDateMap() 
	{
		int rowIndex = 2;
		
		HashMap<String, Integer> dateMap = new HashMap<String, Integer>();
		
		rowIndex = dateHelper(dateMap, 8, 30, "Jun", rowIndex); // June
		rowIndex = dateHelper(dateMap, 1, 31, "Jul", rowIndex); // July
		rowIndex = dateHelper(dateMap, 1, 31, "Aug", rowIndex); // August
		rowIndex = dateHelper(dateMap, 1, 30, "Sep", rowIndex); // September
		rowIndex = dateHelper(dateMap, 1, 31, "Oct", rowIndex); // October
		rowIndex = dateHelper(dateMap, 1, 19, "Nov", rowIndex); // November
		
		return dateMap;
	}
	
	private int dateHelper(HashMap<String, Integer> dateMap, int from, int to, String monthName, int rowIndex)
	{
		for(int i=from; i<=to; i++)
		{
			String date = i + "-" + monthName;
			dateMap.put(date, rowIndex++);
		}
		
		return rowIndex;
	}

	/**
	 * Reads the spreadsheet for person names and put those names in hashmap with index starts at 1...
	 * @param service
	 * @return hashmap contains personname as a Key and index as a Value...
	 * @throws IOException
	 */
	private HashMap<String, Integer> getPersonMap(Sheets service) throws IOException 
	{
		ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, NAME_RANGE)
                .execute();
		
		HashMap<String, Integer> personMap = new HashMap<String, Integer>();
		
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) 
        {
            System.out.println("No names found...");
        }
        else
        {
        	List<Object> persons = values.get(0);
        	for(int i=0; i<persons.size(); i++) 
        	{
        		String name = (String) persons.get(i);
        		personMap.put(name, i+1);
        	}
        }
        
		return personMap;
	}
}
