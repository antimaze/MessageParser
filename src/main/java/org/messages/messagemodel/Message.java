package org.messages.messagemodel;

public class Message {

	private MessageDate date;
	private String name;
	private int partNum;
	private int from;
	private int to;
	private String messageBody;
	private boolean isParsed;
	
	public Message(MessageDate date, String name, int partNum, int from, int to, String messageBody, boolean isParsed) 
	{
		this.date = date;
		this.name = name;
		this.partNum = partNum;
		this.from = from;
		this.to = to;
		this.messageBody = messageBody;
		this.isParsed = isParsed;
	}
	
	public MessageDate getDate() {
		return date;
	}

	public void setDate(MessageDate date) {
		this.date = date;
	}

	public String getName(){
		String[] nameTokens = this.name.split(" ");
		return nameTokens[0];
	}
	
	public String getFullName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public boolean isParsed() {
		return isParsed;
	}

	public void setParsed(boolean isParsed) {
		this.isParsed = isParsed;
	}
}
