package org.messages.messageparser;

import java.io.IOException;
import java.util.ArrayList;

import org.messages.messagemodel.Message;
import org.messages.messagemodel.MessageDate;
import org.messages.messagemodel.MessageDocument;



public class BaseParser
{
	protected MessageDocument doc;
	private RandomAccessFile sourceFile;
	public BaseParser(RandomAccessFile sourceFile) 
	{
		this.sourceFile = sourceFile;
		doc = new MessageDocument();
	}
	
	protected static final byte ASCII_LF 			= 10;
    /**
     * ASCII code for carriage return.
     */
    protected static final byte ASCII_CR 			= 13;
    protected static final int ASCII_SPACE			= 32;
	
	
	public void parse() throws IOException 
	{
		skipSpaces();
		int counter = 0;
		while(!sourceFile.isEOF())
		{			
			if(hasMessage())
			{
				counter++;
				Message message = parseMessage();
				doc.addMessage(message);
				
				System.out.println("--------------------------------------------------------------------");
				System.out.println(message.isParsed() + " " + counter);
				if(!message.isParsed())
				{
					System.out.println();
					System.out.println(message.getMessageBody());
				}
				System.out.println("--------------------------------------------------------------------");
			}
			else
			{
				break;
			}
		}
	}

	private Message parseMessage() throws IOException 
	{
		skipSpaces();
		long position = sourceFile.getFilePosition();
		if(sourceFile.isEOF()) 
		{
			return null;
		}
		
		String date = readMessageDate();
		String name = readName();
		int partNum = -1;
		int from = -1;
		int to = -1;
		
		boolean shouldParse = true;
		while(!sourceFile.isEOF())
		{
			char c = (char) sourceFile.read();
			if(c == '[') 
			{
				sourceFile.rewind(1);
				shouldParse = false;
				if(hasMessage())
				{	
					long newPosition = sourceFile.getFilePosition();
					int length = (int)(newPosition - position);
					byte[] byteArray = new byte[length];
					
					sourceFile.seek(position);
					sourceFile.read(byteArray, 0, length);
					sourceFile.seek(newPosition);
					
					// Create Message...
					if(partNum == -1 || from == -1 || to == -1)
					{
						return new Message(new MessageDate(date), name, partNum, from, to, new String(byteArray), false);
					}
					else
						return new Message(new MessageDate(date), name, partNum, from, to, new String(byteArray), true);
				}
				else
				{
					sourceFile.read();
				}
			}
			if(shouldParse)
			{
				skipSpaces();
				shouldParse &= readJaySwaminarayan();
				if(shouldParse)
				{
					shouldParse &= !isForOrFromPresent();
				}
				
				if(shouldParse)
				{
					shouldParse &= readBapashrinivaato();
				}
				
				if(shouldParse)
				{
					// Extract numbers...
					ArrayList<Integer> numbers = extractNumbers();
					if(numbers.size() == 2 || numbers.size() == 3)
					{
						partNum = numbers.get(0);
						from = numbers.get(1);
						if(numbers.size() == 3)
						{
							to = numbers.get(2);
						}
						else
						{
							to = from;
						}
					}
				}
			}
		}
		
		long newPosition = sourceFile.getFilePosition();
		int length = (int)(newPosition - position);
		byte[] byteArray = new byte[length];
		
		sourceFile.seek(position);
		sourceFile.read(byteArray, 0, length);
		sourceFile.seek(newPosition);
		if(partNum == -1 || from == -1 || to == -1)
		{
			return new Message(new MessageDate(date), name, partNum, from, to, new String(byteArray), false);
		}
		else
			return new Message(new MessageDate(date), name, partNum, from, to, new String(byteArray), true);
	}

	private boolean isForOrFromPresent() throws IOException 
	{
		long position = sourceFile.getFilePosition();
		skipSpaces();
		while(!sourceFile.isEOF())
		{
			char c = (char) sourceFile.read();
			if(c == '[')
			{
				sourceFile.seek(position);
				return false;
			}
			if(Character.toLowerCase(c) == 'f')
			{
				StringBuilder sb = new StringBuilder();
				sb.append(c);
				
				char c1 = (char) sourceFile.read();
				if(c1 == '[' || Character.toLowerCase(c1) == 'b') { sourceFile.rewind(1); return false; }
				sb.append(c1);
				
				char c2 = (char) sourceFile.read();
				if(c2 == '[' || Character.toLowerCase(c2) == 'b') { sourceFile.rewind(1); return false; }
				sb.append(c2);
				
				char c3 = (char) sourceFile.read();
				if(c3 == '[' || Character.toLowerCase(c3) == 'b') { sourceFile.rewind(1); return false; }
				sb.append(c3);
				
				String[] array = new String[] {
						"for",
						"from"
				};
				
				for(int i=0; i<array.length; i++) 
				{
					if(sb.toString().toLowerCase().contains(array[i]))
					{
						return true;
					}
				}
			}
		}
		
		sourceFile.seek(position);
		return false;
	}

	private ArrayList<Integer> extractNumbers() throws IOException 
	{
		skipSpaces();
		
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		
		int num = 0;
		while(!sourceFile.isEOF())
		{
			char c = (char) sourceFile.read();
			if(c == '[')
			{
				sourceFile.rewind(1);
				return numbers;
			}
			
			if(Character.isDigit(c))
			{
				num = (num*10) + Character.getNumericValue(c);
			}
			else
			{
				if(num != 0)
				{
					numbers.add(num);
					num = 0;
				}
			}
		}
		
		return numbers;
	}

	

	private boolean readBapashrinivaato() throws IOException 
	{
		skipSpaces();
		
		while(!sourceFile.isEOF())
		{
			char c = (char) sourceFile.read();
			if(c == '[')
			{
				sourceFile.rewind(1);
				return false;
			}
			
			boolean skipRead = false;
			if(Character.toLowerCase(c) == 'b')
			{
				long position = sourceFile.getFilePosition();
				StringBuilder sb = new StringBuilder();
				sb.append(c);
				
				for(int i=0; i<17; i++)
				{
					if(!skipRead)
					{
						c = (char) sourceFile.read();
					}
					if(c == '[')
					{
						sourceFile.rewind(1);
						return false;
					}
					
					if(!skipRead)
					{
						if(Character.isDigit(c))
						{
							sourceFile.rewind(1);
							skipRead = true;
						}
						else
						{
							sb.append(c);
						}
					}
				}
				
				String[] array = new String[] {
						"bapashree vato",
						"bapashree vaato",
						"bapashree ni vato",
						"bapashreeni vato",
						"bapashree ni vaato",
						"bapashreeni vaato",
						"bapashri ni vato",
						"bapashrini vato",
						"bapashri ni vaato",
						"bapashrini vaato",
						"bapashri vato",
						"bapashri vaato",
						"bapa ni vaato",
						"bapani vaato",
						"bapa ni vato",
						"bapani vato",
						"bapa vaato",
						"bapa vato",
						"bapashri",
						"bapashree",
						"bapa shree",
						"bapa shri"
				};
				
				for(int i=0; i<array.length; i++) 
				{
					if(sb.toString().toLowerCase().contains(array[i]))
					{
						return true;
					}
				}
				
				sourceFile.seek(position);
			}
		}
		return false;
	}

	private boolean readJaySwaminarayan() throws IOException {
		
		skipSpaces();
		
		StringBuilder sb = new StringBuilder();
		boolean skipRead = false;
		
		// Jay Swaminarayan (16 characters expected)...
		for(int i=0; i<16; i++)
		{
			char c = 0;
			if(!skipRead)
			{
				c = (char) sourceFile.read();
			}
			if(c == '[')
			{
				sourceFile.rewind(1);
				return false;
			}
			
			if(!skipRead)
			{
				if(Character.isDigit(c))
				{
					sourceFile.rewind(1);
					skipRead = true;
				}
				else
				{
					sb.append(c);
				}
			}
		}
		
		String[] array = new String[] {
				"jay swaminarayan",
				"jai swaminarayan",
				"jay swami",
				"jai swami",
				"jai shree swami",
				"jay shree swami"
		};
		
		for(int i=0; i<array.length; i++) 
		{
			if(sb.toString().toLowerCase().contains(array[i]))
			{
				return true;
			}
		}
				
		return false;
	}

	private String readName() throws IOException 
	{
		skipSpaces();
		StringBuilder name = new StringBuilder();
		while(!sourceFile.isEOF())
		{
			char c = (char) sourceFile.read();
			if(c == ':')
			{
				return name.toString();
			}
			
			name.append(c);
		}
		
		return null;
	}

	private String readMessageDate() throws IOException 
	{
		StringBuilder date = new StringBuilder();
		while(!sourceFile.isEOF())
		{
			char c = (char) sourceFile.read();
			if(c == ']')
			{
				return date.toString();
			}
			
			if(c != '[')
			{
				date.append(c);
			}
		}
		
		return null;
	}

	private boolean hasMessage() throws IOException 
	{
		long position = sourceFile.getFilePosition();
		if(sourceFile.isEOF()) 
		{
			sourceFile.seek(position);
			return false;
		}
		
		char c = (char) sourceFile.read();
		if(c != '[')
		{
			sourceFile.seek(position);
			return false;
		}
		
		StringBuilder strDate = new StringBuilder();
		while(!sourceFile.isEOF() && c != ']')
		{
			c = (char) sourceFile.read();
			if(c != '[' && c != ']') strDate.append(c);
		}
		
		if(c == ']') 
		{
			MessageDate date = new MessageDate(strDate.toString());
			sourceFile.seek(position);
			return date.isValidDate();
		}
		sourceFile.seek(position);
		return false;
	}
	
	private int skipSpaces() throws IOException
	{
		int spaces = 0;
    	int c = sourceFile.read();
        while( isWhitespace(c))
        {
        	spaces++;
    		c = sourceFile.read();
        }

        if (c != -1)
        {
            sourceFile.rewind(1);
        }
        
        return spaces;
	}
	
	protected boolean isWhitespace( int c )
    {
        return c == 0 || c == 9 || c == 12  || c == ASCII_LF
        || c == ASCII_CR || c == ASCII_SPACE;
    }
}
