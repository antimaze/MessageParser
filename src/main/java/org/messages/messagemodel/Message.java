package org.messages.messagemodel;

import java.util.List;

public class Message {

	private MessageDate date;
	private String name;
	private String messageBody;
	private boolean isParsed;
	
	List<StoryData> storiesData;
	
	private int totalStoriesRead;
	
	public Message(MessageDate date, String name, List<StoryData> storiesData, String messageBody) 
	{
		this.date 			= date;
		this.name 			= name;
		this.storiesData 	= storiesData;
		this.messageBody 	= messageBody;
		
		checkParsed(this.storiesData);
		updateTotalStoriesRead(this.storiesData);
	}

	private void updateTotalStoriesRead(List<StoryData> storiesData)
	{
		if(isParsed)
		{
			for(StoryData storyData: storiesData)
			{
				if(storyData.isValid())
				{
					totalStoriesRead += storyData.getTo() - storyData.getFrom() + 1;
				}
			}
		}
	}

	private void checkParsed(List<StoryData> storiesData)
	{
		if(!storiesData.isEmpty())
		{
			for(StoryData storyData: storiesData)
			{
				if(!storyData.isValid())
				{
					isParsed = false;
					return;
				}
			}
			
			isParsed = true;
		}
		else
		{
			isParsed = false;
		}
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
	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public boolean isParsed() {
		return isParsed;
	}

	public List<StoryData> getStoriesData() {
		return storiesData;
	}

	public void setStoriesData(List<StoryData> storiesData) {
		this.storiesData = storiesData;
	}

	public int getTotalStoriesRead() {
		return totalStoriesRead;
	}

	public void setTotalStoriesRead(int totalStoriesRead) {
		this.totalStoriesRead = totalStoriesRead;
	}
}
