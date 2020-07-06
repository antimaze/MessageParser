package org.messages.messageparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RandomAccessFile implements RandomAccessRead
{

	private java.io.RandomAccessFile raf;
	private boolean isClosed;
	
	public RandomAccessFile(File file, String mode) throws FileNotFoundException
	{
		raf = new java.io.RandomAccessFile(file, mode);
	}

	public void close() throws IOException 
	{
		checkClosed();
		raf.seek(0);
		raf.setLength(0);
		isClosed = true;
	}

	public int read() throws IOException
	{
		checkClosed();
		return raf.read();
	}
	
	public int read(byte[] b, int offset, int length) throws IOException
	{
		checkClosed();
        return raf.read(b, offset, length);
	}

	public boolean isClosed()
	{
		return isClosed;
	}
	
	private void checkClosed() throws IOException
	{
		if(isClosed)
		{
			throw new IOException("Random Access File is closed for reading...");
		}
	}

	public void rewind(int bytes) throws IOException 
	{
		checkClosed();
		raf.seek(getFilePosition() - bytes);
	}

//	public int isAvailable() throws IOException 
//	{
//		return 0;
//	}

	public boolean isEOF() throws IOException 
	{
		return peek() == -1;
	}

	public long getFilePosition() throws IOException
	{
		checkClosed();
		return raf.getFilePointer();
	}

	public int peek() throws IOException
	{
		int read = raf.read();
		if(read != -1)
		{
			rewind(1);
		}
		
		return read;
	}

	public void seek(long position) throws IOException
	{
		checkClosed();
		raf.seek(position);
	}
}
