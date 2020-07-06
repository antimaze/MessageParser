package org.whatsapp.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.googlesheet.operations.GoogleSheetUpdater;
import org.json.simple.parser.ParseException;
import org.whatsapp.messagemodel.MessageDocument;

public class Run {

	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException 
	{
		if(args.length == 1)
		{
			Configuration config = new Configuration(args[0]);
			MessageDocument sourceDoc = InitializeMessageDocument(config.getMessageFile());		
			GoogleSheetUpdater updater = new GoogleSheetUpdater(sourceDoc, config.getCredentialFile(), 
					config.getSpreadSheetId(), config.getNameRange(), config.getDateRange());
			try 
			{
				updater.update();
			} catch (GeneralSecurityException e) 
			{
				e.printStackTrace();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			usage();
		}
	}
	
	private static MessageDocument InitializeMessageDocument(File sourceFile)
	{
		MessageDocument sourceDoc = null;
		
		try 
		{
			sourceDoc = MessageDocument.load(sourceFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return sourceDoc;
	}
	
	private static void usage()
	{
		String usage = "\nAccepted Parameteres : "
				+ "\n1. <config.json file path> (Required)";
		
		System.out.println(usage);
	}
}