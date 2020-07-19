package org.messages.messagemodel;

public class StoryData {

	private int partNum;
	private int from;
	private int to;
	
	private boolean isValid;
	
	public StoryData(int partNum, int from, int to) 
	{
		this.partNum = partNum;
		this.from = from;
		this.to = to;
		
		if((partNum == 1 || partNum == 2) && from != -1 && to != -1)
		{
			isValid = true;
		}
		else
			isValid = false;
	}

	public int getPartNum() {
		return partNum;
	}

	public void setPartNum(int partNum) {
		this.partNum = partNum;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
}
