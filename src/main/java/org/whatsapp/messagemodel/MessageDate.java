package org.whatsapp.messagemodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class MessageDate {
	
	private static final String STRING_FORMAT = "h:mm aa, M/d/yyyy";
	private String strDate;
	private Date date;
	
	private int month;
	private int day;
	private String monthString;
	
	private boolean isValidDate;
	
	public static final String[] months = new String[] {
		"",
		"Jan",
		"Feb",
		"Mar",
		"Apr",
		"May",
		"Jun",
		"Jul",
		"Aug",
		"Sep",
		"Oct",
		"Nov",
		"Dec"
	};
	
	public static final int[] monthDays = new int[] {
		0,
		31,
		29,
		31,
		30,
		31,
		30,
		31,
		31,
		30,
		31,
		30,
		31
	};
	
	public MessageDate(String strDate) 
	{
		this.strDate = strDate;
		SimpleDateFormat df = new SimpleDateFormat(STRING_FORMAT);
		try 
		{
			date = df.parse(strDate);
			
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			month = localDate.getMonthValue();
			day = localDate.getDayOfMonth();
			monthString = months[month];
			
			isValidDate = true;
		} catch (ParseException e) 
		{
			e.printStackTrace();
			isValidDate = false;
		}
	}
	
	public String getMonthString(){
		return monthString;
	}
	
	public int getMonth(){
		return month;
	}
	
	public int getDay(){
		return day;
	}

	public int compareTo(MessageDate date){
		return this.date.compareTo(date.getDate());
	}
	
	public Date getDate(){
		return this.date;
	}
	
	public String getDateString(){
		return strDate;
	}

	public boolean isValidDate() {
		return isValidDate;
	}

	public void setValidDate(boolean isValidDate) {
		this.isValidDate = isValidDate;
	}
}
