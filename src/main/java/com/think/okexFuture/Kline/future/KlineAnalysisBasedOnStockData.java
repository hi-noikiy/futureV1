package com.think.okexFuture.Kline.future;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.rest.stock.IStockRestApi;
import com.think.okexFuture.rest.stock.impl.StockRestApi;
import com.think.okexFuture.util.DateUtil;
import com.think.okexFuture.util.StringUtil;


public class KlineAnalysisBasedOnStockData {

	//default 波动容忍值为0.002，也就是波动在0.2%的不考虑
	private static double tolerantRange = 0.003;
	private static IStockRestApi stockGetV1 = new StockRestApi(OkexProperties.URL_PREX);
	private static Logger log = Logger.getLogger(KlineAnalysisBasedOnStockData.class);

	//history up主要针对15min以上的数据分析
	public static boolean historyGoingUpExpectedTimes(String preAnalysisData, int expectedTimes) {
		return historyGoingUpExpectedTimes(preAnalysisData, expectedTimes, tolerantRange);
	}

	//history up主要针对15min以上的数据分析
	public static boolean historyGoingUpExpectedTimes(String preAnalysisData, int expectedTimes, double tolerantRange) {
		List<String[]> formatResult = StringUtil.formatKlineDataWithoutLatestAndReplaceQuota(preAnalysisData);
		int count = 0;
		for (String[] tempArray : formatResult) {
			if (Double.valueOf(tempArray[4]) >= Double.valueOf(tempArray[1])) {
				count++;
			} else {
				//加入平滑处理，当波动小于0.4%的时候，此次波动可以忽略，波动幅度的容忍范围后期需要不断调整
				//加入目的是分析一些大涨或大跌前期，数据不一定符合连续涨或跌的要求
				if (Math.abs(Double.valueOf(tempArray[4]) - Double.valueOf(tempArray[1])) / Double.valueOf(tempArray[4]) < tolerantRange) {
					continue;
				}
				count = 0;
			}
		}
		log.info("Stock result size:" + formatResult.size() + " historyGoingUp expected Times: " + expectedTimes + " count: " + count);
		if (count >= expectedTimes) {
			return true;
		} else {
			return false;
		}
	}

	//history down 主要针对15min以上的数据分析
	public static boolean historyGoingDownExpectedTimes(String preAnalysisData, int expectedTimes) {
		return historyGoingDownExpectedTimes(preAnalysisData, expectedTimes, tolerantRange);
	}

	public static boolean historyGoingDownExpectedTimes(String preAnalysisData, int expectedTimes, double tolerantRange) {
		List<String[]> formatResult = StringUtil.formatKlineDataWithoutLatestAndReplaceQuota(preAnalysisData);
		int count = 0;
		for (String[] tempArray : formatResult) {
			if (Double.valueOf(tempArray[4]) <= Double.valueOf(tempArray[1])) {
				count++;
			} else {
				//加入平滑处理，当波动小于0.4%的时候，此次波动可以忽略，波动幅度的容忍范围后期需要不断调整
				//加入目的是分析一些大涨或大跌前期，数据不一定符合连续涨或跌的要求
				if (Math.abs(Double.valueOf(tempArray[4]) - Double.valueOf(tempArray[1])) / Double.valueOf(tempArray[4]) < tolerantRange) {
					continue;
				}
				count = 0;
			}
		}
		log.info("Stock result size:" + formatResult.size() + " historyGoingDown expected Times: " + expectedTimes + " count: " + count);
		if (count >= expectedTimes) {
			return true;
		} else {
			return false;
		}
	}


	//获取最近一次15min K线的时间戳， 然后用当前时间减去改时间戳并获取1分钟的K线分析当前15min的数据是up还是down
	//当前的想法是： 在15分钟内，如果有9次的1min数据都比开盘价高，认为此次是反转节点，可考虑入市
	//如果在第14分钟并且有一半时间比开盘高，可考虑入市
	//分析当前时间到上次15min收盘时间，是否going up
	//当前15分钟内： 9次比前一次收盘价格高 或者 6次比收盘高，而且第14分钟的时候为up： 判断开始反转
	//时间未到会sleep
	public static boolean current15MinAnalysisWillUp(String symbol)
		throws HttpException, IOException, InterruptedException, ParseException {
		log.info("Begin to analysis for current15MinAnalysisWillUp++++++++++++++++++++");
		while (true) {
			String current15min = stockGetV1.kline(symbol, "15min", "1", "");
			List<String[]> temp = StringUtil.formatKlineDataAndReplaceQuota(current15min);
			Long latest = Long.valueOf(temp.get(0)[0]);
			double last15MinBeginPrice = Double.valueOf(temp.get(0)[1]);

			Long current = new Date().getTime();
			log.info("current time: " + current + " " + DateUtil.timeStampToDate(current));
			log.info("latest time: " + latest + " " + DateUtil.timeStampToDate(latest));
			Long timeback = (current - latest) / 1000 / 60 + 1;

			if (timeback < 10) {
				log.info("Current timeback is less than 10 min, tendency may be not clear. wait for 30 seconds");
				Thread.sleep(30000);
				continue;
			} else if (timeback >= 10 && timeback < 15) {
				int count = 0;
				log.info("Begin to analysis for current trendency: get current " + timeback + " mins data");
				String result = stockGetV1.kline(symbol, "1min", String.valueOf(timeback), "");
				List<String[]> resultList = StringUtil.formatKlineDataAndReplaceQuota(result);
				log.info("Last 15min close price " + last15MinBeginPrice);
				for (String[] tempArray : resultList) {
					double closePrice1Min = Double.valueOf(tempArray[4]);
					if (closePrice1Min >= last15MinBeginPrice) {
						count++;
					}
				}
				if (count > 9) {
					return true;
				} else {
					log.info("current count less than 9 " + count + " wait for 30 seconds to continue");
					count = 0;
					Thread.sleep(30000);
					continue;
				}
			} else if (timeback == 15) {
				int count = 0;
				log.info("Current timeback is 14 minutes, to determine another condition");
				String result = stockGetV1.kline(symbol, "1min", String.valueOf(timeback), "");
				List<String[]> resultList = StringUtil.formatKlineDataAndReplaceQuota(result);
				double lastClosePrice1Min = Double.valueOf(resultList.get(14)[4]);
				for (String[] tempArray : resultList) {
					double closePrice1Min = Double.valueOf(tempArray[4]);
					if (closePrice1Min >= last15MinBeginPrice) {
						count++;
					}
				}
				if (count >= 6 && lastClosePrice1Min > last15MinBeginPrice) {
					return true;
				} else {
					return false;
				}
			}
		}
	}


	//分析当前时间到上次15min收盘时间，是否going down,跟up的机制是一样的，只是判断条件的区别
	//当前15分钟内： 9次比前一次收盘价格低 或者 6次比收盘低，而且第14分钟的时候为down： 判断开始反转，当前15分钟为跌
	//时间未到会sleep
	public static boolean current15MinAnalysisWillDown(String symbol)
		throws HttpException, IOException, InterruptedException, ParseException {
		log.info("Begin to analysis for current15MinAnalysisWillDown-----------------------");
		while (true) {
			String current15min = stockGetV1.kline(symbol, "15min", "1", "");
			List<String[]> temp = StringUtil.formatKlineDataAndReplaceQuota(current15min);
			Long latest = Long.valueOf(temp.get(0)[0]);
			double last15MinBeginPrice = Double.valueOf(temp.get(0)[1]);
			Long current = new Date().getTime();

			log.info("current time: " + current + " " + DateUtil.timeStampToDate(current));
			log.info("latest time: " + latest + " " + DateUtil.timeStampToDate(latest));
			Long timeback = (current - latest) / 1000 / 60 + 1;

			if (timeback < 10) {
				log.info("Current timeback is less than 10 min, tendency may be not clear. wait for 30 seconds");
				Thread.sleep(30000);
				continue;
			} else if (timeback >= 10 && timeback < 15) {
				int count = 0;
				log.info("Begin to analysis for current trendency: get current " + timeback + " mins data");
				String result = stockGetV1.kline(symbol, "1min", String.valueOf(timeback), "");
				List<String[]> resultList = StringUtil.formatKlineDataAndReplaceQuota(result);
				log.info("Last 15min close price " + last15MinBeginPrice);
				for (String[] tempArray : resultList) {
					double closePrice1Min = Double.valueOf(tempArray[4]);
					if (closePrice1Min <= last15MinBeginPrice) {
						count++;
						log.info("close price 1min: " + closePrice1Min + " count:" + count + " last15MinBeginPrice" + last15MinBeginPrice);
					}
				}
				if (count > 9) {
					return true;
				} else {
					log.info("current count less than 9 " + count + " wait for 30 seconds to continue");
					count = 0;
					Thread.sleep(30000);
					continue;
				}
			} else if (timeback == 15) {
				int count = 0;
				log.info("Current timeback is 14 minutes, to determine another condition");
				String result = stockGetV1.kline(symbol, "1min", String.valueOf(timeback), "");
				List<String[]> resultList = StringUtil.formatKlineDataAndReplaceQuota(result);
				double lastClosePrice1Min = Double.valueOf(resultList.get(14)[4]);
				for (String[] tempArray : resultList) {
					double closePrice1Min = Double.valueOf(tempArray[4]);
					if (closePrice1Min <= last15MinBeginPrice) {
						count++;
						log.info("close price 1min: " + closePrice1Min + " count:" + count + " last15MinBeginPrice" + last15MinBeginPrice);
					}
				}
				if (count >= 6 && lastClosePrice1Min < last15MinBeginPrice) {
					return true;
				} else {
					return false;
				}
			}
		}
	}


	public static void main(String[] args) throws HttpException, IOException, InterruptedException, ParseException {
		String log4jConfPath = "log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);
		boolean currentUpFlag = current15MinAnalysisWillDown("eos_usdt");
		System.out.println(currentUpFlag);
		String current15min = stockGetV1.kline("eos_usdt", "15min", "1", "");
		System.out.println(current15min);
	}

}
