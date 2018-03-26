package com.think.okexFuture.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static String timeStampToDate(Long timeStamp) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Long time = new Long(timeStamp);
		String d = format.format(time);
		return d;
	}

	public static Long dateToTimeStamp(String time) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = format.parse(time);
		return date.getTime();
	}


	public static void main(String[] args) throws ParseException {

		Long test = 1515417420000L;
		String time = DateUtil.timeStampToDate(test);
		System.out.println(time);
		System.out.println(DateUtil.dateToTimeStamp(time));


	}


}
