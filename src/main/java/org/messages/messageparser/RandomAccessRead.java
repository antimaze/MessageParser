package org.messages.messageparser;

import java.io.Closeable;
import java.io.IOException;

public interface RandomAccessRead extends Closeable
{
	int 	read() 				throws IOException;
	int read(byte[] b, int offset, int length) throws IOException;
	boolean isClosed();
	void 	rewind(int bytes) 	throws IOException;
//	int 	isAvailable() 		throws IOException;
	boolean isEOF() 			throws IOException;
	long 	getFilePosition() 	throws IOException;
	int 	peek() 				throws IOException;
	void	seek(long position)	throws IOException;
}