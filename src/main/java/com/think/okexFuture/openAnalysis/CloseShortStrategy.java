package com.think.okexFuture.openAnalysis;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.rest.future.IFutureRestApi;
import com.think.okexFuture.rest.future.impl.FutureRestApiV1;
import com.think.okexFuture.util.CommonFutureMethods;

/**
 * 根据已经购买的合约类型和合约编号采用相应的策略来判断是否卖出
 * 目前考虑的策略(需要考虑的因素非常多，需要综合考虑)：
 * 如果已经买多，判断接下来15分钟，如果亏损达到15%，提示需要卖出，也就是最大回撤
 * 如果持续上涨就继续留着，如果下跌就卖掉？？
 * 如果这个15分钟为下跌，而且振幅小于0.5%，那么继续留着，并标记该15分钟的时间的数据不做参考
 * @author map6
 *
 */
public class CloseShortStrategy {

	private static Logger log = Logger.getLogger(CloseShortStrategy.class);
	private final static double lossPercentage = 0.2;
	private final static double profitPercentage = 0.2;
	private final static double leverage = 20;
	private static IFutureRestApi futureGetV1 = new FutureRestApiV1(OkexProperties.URL_PREX);


	/**
	 * 离场策略是一个非常复杂的问题，当前策略只根据盈亏比例来决定是否离场
	 * 盈利> 30% 或者亏损 >20均挂单离场
	 * @param symbol
	 * @param contractType
	 * @param openLongPrice 开仓价
	 * @param openTimeStamp 开仓时间
	 * @return
	 */
	public static boolean isCloseShortTimingBasedOnProfit(String symbol, String contractType, Double openShortPrice, String openTimeStamp)
		throws HttpException, IOException, InterruptedException, ParseException {
		String tradeTicker = futureGetV1.future_ticker(symbol, contractType);
		JSONObject ticker = CommonFutureMethods.getTicker(tradeTicker);
		double buy1 = Double.parseDouble(ticker.getString("buy"));
		double sell1 = Double.parseDouble(ticker.getString("sell"));
		double avg = (buy1 + sell1) / 2;
		double profit = (avg - openShortPrice) / openShortPrice * leverage;

		if (profit > lossPercentage) {
			log.info("loss reach: " + lossPercentage + "，need to sell " + " openShortPrice: " + openShortPrice + " current AVG: " + avg
				+ " loss percentage: ：" + profit);
			return true;
		} else if (-profit > profitPercentage) {
			log.info("profit reach: " + profitPercentage + "，need to sell " + " openShortPrice: " + openShortPrice + " current AVG: " + avg
				+ " profit percentage：" + -profit);
			return true;
		} else {
			log.info(
				"not reach selling threshhold: " + " openShortPrice: " + openShortPrice + " current AVG: " + avg + " profit percentage: " + -profit);
			return false;
		}
	}

	public static void main(String[] args) throws HttpException, IOException, InterruptedException {

	}
}
