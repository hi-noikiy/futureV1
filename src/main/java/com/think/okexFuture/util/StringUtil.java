package com.think.okexFuture.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		}
		String tempStr = str.trim();
		if (tempStr.length() == 0) {
			return true;
		}
		if (tempStr.equals("null")) {
			return true;
		}
		return false;
	}

	//不获取最近一条记录，因为最近一条记录是用来判断最近一次15分钟的走势，会在后面重新获取
	public static List<String[]> formatKlineDataWithoutLatest(String kLineData) {
		List<String[]> result = new ArrayList<>();
		String[] formatList = kLineData.split("],");
		for (int i = 0; i < formatList.length - 1; i++) {
			String tempString = formatList[i].replaceAll("\\[", "").replaceAll("]", "");
			String[] tempArray = tempString.split(",");
			result.add(tempArray);
		}
		return result;
	}

	//不获取最近一条记录，因为最近一条记录是用来判断最近一次15分钟的走势，会在后面重新获取
	public static List<String[]> formatKlineDataWithoutLatestAndReplaceQuota(String kLineData) {
		kLineData = kLineData.replaceAll("\"", "");
		List<String[]> result = new ArrayList<>();
		String[] formatList = kLineData.split("],");
		for (int i = 0; i < formatList.length - 1; i++) {
			String tempString = formatList[i].replaceAll("\\[", "").replaceAll("]", "");
			String[] tempArray = tempString.split(",");
			result.add(tempArray);
		}
		return result;
	}

	public static List<String[]> formatKlineDataAndReplaceQuota(String kLineData) {
		kLineData = kLineData.replaceAll("\"", "");
		List<String[]> result = new ArrayList<>();
		String[] formatList = kLineData.split("],");
		for (int i = 0; i < formatList.length; i++) {
			String tempString = formatList[i].replaceAll("\\[", "").replaceAll("]", "");
			String[] tempArray = tempString.split(",");
			result.add(tempArray);
		}
		return result;
	}

	public static List<String[]> formatKlineData(String kLineData) {
		List<String[]> result = new ArrayList<>();
		String[] formatList = kLineData.split("],");
		for (int i = 0; i < formatList.length; i++) {
			String tempString = formatList[i].replaceAll("\\[", "").replaceAll("]", "");
			String[] tempArray = tempString.split(",");
			result.add(tempArray);
		}
		return result;
	}

}
