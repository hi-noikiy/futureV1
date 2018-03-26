package com.think.okexFuture.util;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.rest.future.IFutureRestApi;
import com.think.okexFuture.rest.future.impl.FutureRestApiV1;

public class CommonFutureMethods {
	private static Logger log = Logger.getLogger(CommonFutureMethods.class);
	private static IFutureRestApi futurePostV1 = new FutureRestApiV1(OkexProperties.URL_PREX, OkexProperties.API_KEY, OkexProperties.SECRET_KEY);
	private static IFutureRestApi futureGetV1 = new FutureRestApiV1(OkexProperties.URL_PREX);
	private final static int timeoutForChecking = 60;

	public static JSONObject getTicker(String result) {
		return getJsonObject(result, "ticker");
	}


	public static JSONObject getJsonObject(String result, String targetKey) {
		JSONObject object = JSONObject.parseObject(result);
		String targetValue = object.getString(targetKey);
		return JSONObject.parseObject(targetValue);
	}

	public static boolean checkRestResult(String result) {
		JSONObject object = JSONObject.parseObject(result);
		return object.getBooleanValue("result");
	}


	/**
	 * 下单并且确定是否成功，如果成功返回true，否则返回false，timeout时间为2分钟
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @param price  价格
	 * @param amount  委托数量
	 * @param type   1:开多   2:开空   3:平多   4:平空
	 * @param matchPrice  是否为对手价 0:不是    1:是   ,当取值为1时,price无效
	 * @param lever_rate 杠杆倍数，下单时无需传送，系统取用户在页面上设置的杠杆倍数。且“开仓”若有10倍多单，就不能再下20倍多单
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws HttpException
	 */
	public static boolean tradeWithChecking(String symbol, String contractType, String price, String amount, String type, String matchPrice,
		String leverRate) throws HttpException, IOException, InterruptedException {

		String openResult = futurePostV1.future_trade(symbol, contractType, price, amount, type, matchPrice, leverRate);
		JSONObject openLongObject = JSONObject.parseObject(openResult);
		if (openLongObject.getBooleanValue("result")) {
			log.info("Open type: " + type + ", check whether the order is totally closed, otherwise cancel it.");
			String tempOrderId = openLongObject.getString("order_id");
			int waitTime = 0;
			while (waitTime < timeoutForChecking) {
				String orderResult = futurePostV1.future_order_info(symbol, contractType, tempOrderId, "", "", "");
				JSONObject orderObject = JSONObject.parseObject(orderResult);
				String status = orderObject.getJSONArray("orders").getJSONObject(0).getString("status");
				if (status.equals("2")) {
					return true;
				}
				Thread.sleep(1000);
				waitTime++;
			}
			if (waitTime == timeoutForChecking) {
				log.info("The order is not closed until timeout, now cancel it");
				String cancelResult = futurePostV1.future_cancel(symbol, contractType, tempOrderId);
				log.info("cancel result: " + cancelResult);
			}
		}
		return false;
	}

	public static double getAVGPriceOfCurrentFuture(String symbol, String contractType) throws HttpException, IOException, InterruptedException {
		String tradeTicker = futureGetV1.future_ticker(symbol, contractType);
		JSONObject ticker = CommonFutureMethods.getTicker(tradeTicker);
		double buy1 = Double.parseDouble(ticker.getString("buy"));
		double sell1 = Double.parseDouble(ticker.getString("sell"));
		double avg = (buy1 + sell1) / 2;
		return avg;
	}
}
