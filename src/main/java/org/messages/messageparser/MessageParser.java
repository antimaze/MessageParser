package org.messages.messageparser;


import java.io.File;
import java.io.FileNotFoundException;

import org.messages.messagemodel.MessageDocument;

public class MessageParser extends BaseParser
{
	public MessageParser(File sourceFile) throws FileNotFoundException 
	{
		super(new RandomAccessFile(sourceFile, "r"));
	}

	public MessageDocument getMessageDocument() 
	{
		return doc;
	}
}
