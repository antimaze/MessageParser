package org.messages.messagemodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.messages.messageparser.MessageParser;

public class MessageDocument {
	
	private List<Message> messages;
	private MessageDate lastParsedDate;
	
	public MessageDocument() 
	{
		messages = new ArrayList<Message>();
		lastParsedDate = null;
	}
	
	public static MessageDocument load(File cFile) throws FileNotFoundException
	{
		MessageParser parser = new MessageParser(cFile);
		MessageDocument doc = new MessageDocument();
		
		try 
		{
			parser.parse();
			doc = parser.getMessageDocument();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public boolean addMessage(Message message)
	{
		// Updating last parsed message date...
		lastParsedDate = message.getDate();
		return messages.add(message);
	}
	
	public List<Message> getMessages()
	{
		return this.messages;
	}
	
	public MessageDate getLastParsedMessageDate()
	{
		return this.lastParsedDate;
	}
}
