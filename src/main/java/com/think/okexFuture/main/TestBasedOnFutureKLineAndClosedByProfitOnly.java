package com.think.okexFuture.main;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSONObject;
import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.openAnalysis.CloseLongStrategy;
import com.think.okexFuture.openAnalysis.CloseShortStrategy;
import com.think.okexFuture.openAnalysis.OpenLongBasedOnFutureLine;
import com.think.okexFuture.openAnalysis.OpenShortBasedOnFutureLine;
import com.think.okexFuture.rest.future.IFutureRestApi;
import com.think.okexFuture.rest.future.impl.FutureRestApiV1;
import com.think.okexFuture.util.CommonFutureMethods;

/**
 * 该测试只针对profit
 * 开多或者开空都只有一个单在，不可能同时有两个单存在，开单数量为5个
 * 开单之后判断单据是否成交，如果2分钟之内未成交则cancel掉，等待下一次下单
 * 下单价格为买一价和卖一价的中间价格，可适当上浮或者下滑
 * 平单价格为快速成交，价格无效，成交后会查询价格
 * 所有成交记录均需要记录或者输出到log中
 * type	String	是	1:开多 2:开空 3:平多 4:平空
 * match_price	String	否	是否为对手价 0:不是 1:是 ,当取值为1时,price无效
 * lever_rate	String	否	杠杆倍数，下单时无需传送，系统取用户在页面上设置的杠杆倍数。且“开仓”若有10倍多单，就不能再下20倍多单
 * @author map6
 *
 */
public class TestBasedOnFutureKLineAndClosedByProfitOnly {
	private static Logger log = Logger.getLogger(TestBasedOnFutureKLineAndClosedByProfitOnly.class);
	private static IFutureRestApi futurePostV1 = new FutureRestApiV1(OkexProperties.URL_PREX, OkexProperties.API_KEY, OkexProperties.SECRET_KEY);
	private static IFutureRestApi futureGetV1 = new FutureRestApiV1(OkexProperties.URL_PREX);

	private final static String symbol = "eos_usd";
	private final static String type = "15min";
	private final static String contractType = "this_week";
	private final static String size = "5"; //获取记录的条数，如果条数和期望值一样会买入
	private final static int expectedTimes = 4;
	private final static String amount = "3"; //委托数量
	private final static String leverageRate = "20";
	private final static String matchPrice = "0";
	private final static int timeoutForChecking = 120;

	private static int longTicketNum = 0;
	private static double longPrice;
	private static int shortTicketNum = 0;
	private static double shortPrice;


	public static void main(String[] args) throws HttpException, IOException, InterruptedException, ParseException {
		String log4jConfPath = "log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);
		while (true) {
			Thread.sleep(10000);
			log.info("Check every 10 seconds if the KLine satisfy the setting condition");
			if (longTicketNum == 0) {
				//open long
				boolean openLongFlag = OpenLongBasedOnFutureLine.isOpenLongTiming(symbol, type, contractType, size, "", expectedTimes);
				log.info("openLongFlag: " + openLongFlag);
				if (openLongFlag) {
					String tradeTicker = futureGetV1.future_ticker(symbol, contractType);
					JSONObject ticker = CommonFutureMethods.getTicker(tradeTicker);
					double buy1 = Double.parseDouble(ticker.getString("buy"));
					double sell1 = Double.parseDouble(ticker.getString("sell"));
					double avg = (buy1 + sell1) / 2; //open long price
					String openLongResult = futurePostV1.future_trade(symbol, contractType, String.valueOf(avg), amount, "1", "0", leverageRate);
					JSONObject openLongObject = JSONObject.parseObject(openLongResult);
					if (openLongObject.getBooleanValue("result")) {
						log.info("Open Long, check whether the order is totally closed, otherwise cancel it.");
						String tempOrderId = openLongObject.getString("order_id");
						int waitTime = 0;
						while (waitTime < timeoutForChecking) {
							String orderResult = futurePostV1.future_order_info(symbol, contractType, tempOrderId, "", "", "");
							JSONObject orderObject = JSONObject.parseObject(orderResult);
							String status = orderObject.getJSONArray("orders").getJSONObject(0).getString("status");
							if (status.equals("2")) {
								log.info("Open Long successfully, the order is closed");
								log.error("open long record, open long price: " + avg);
								longTicketNum = 1;
								longPrice = avg;
								//为了避免开多的时候，还有空单，需要在这个时候平掉空单
								//出现这个的原因是由于在连续4次下跌后，仍然没有达到盈利门限值的情况
								if (shortTicketNum == 1) {
									log.info("It's time to open long, we need to close the short ticket!");
									String closeLongResult = futurePostV1.future_trade(symbol, contractType, "", amount, "4", "1", leverageRate);
									JSONObject closeShortObject = JSONObject.parseObject(closeLongResult);
									if (closeShortObject.getBooleanValue("result")) {
										log.info("close short successfully!!!");
										String closeShortOrderId = closeShortObject.getString("order_id");
										String closedOrderInfo = futurePostV1.future_order_info(symbol, contractType, closeShortOrderId, "", "", "");
										JSONObject tempObject = JSONObject.parseObject(closedOrderInfo);
										String closeprice = tempObject.getJSONArray("orders").getJSONObject(0).getString("price_avg");
										log.error("close short record, close short price: " + closeprice);
										shortTicketNum = 0;
										shortPrice = 0;
									} else {
										log.info("close short failed after opening long!!!");
									}
								}

								break;
							}
							Thread.sleep(1000);
							waitTime++;
						}
						if (waitTime == timeoutForChecking) {
							log.info("The order is not closed until timeout, now cancel it");
							String cancelResult = futurePostV1.future_cancel(symbol, contractType, tempOrderId);
							log.info("cancel result: " + cancelResult);
						}
					} else {
						log.info("open long failed");
						continue;
					}
				}
			} else {
				boolean closeLongFlag = CloseLongStrategy.isCloseLongTimingBasedOnProfit(symbol, contractType, longPrice, "");
				log.info("close long flag: " + closeLongFlag);
				if (closeLongFlag) {
					log.info("close long condition is satisfied，close long now to sell without price limit");
					String closeLongResult = futurePostV1.future_trade(symbol, contractType, "", amount, "3", "1", leverageRate);
					JSONObject closeLongObject = JSONObject.parseObject(closeLongResult);
					if (closeLongObject.getBooleanValue("result")) {
						log.info("close long successfully!!!");
						String closeLongOrderId = closeLongObject.getString("order_id");
						String closedOrderInfo = futurePostV1.future_order_info(symbol, contractType, closeLongOrderId, "", "", "");
						JSONObject tempObject = JSONObject.parseObject(closedOrderInfo);
						String closeprice = tempObject.getJSONArray("orders").getJSONObject(0).getString("price_avg");
						log.error("close long record, close long price: " + closeprice);
						longTicketNum = 0;
						longPrice = 0;
					} else {
						continue;
					}
				}
			}

			if (shortTicketNum == 0) {
				//open short
				boolean openShortFlag = OpenShortBasedOnFutureLine.isOpenShortTiming(symbol, type, contractType, size, "", expectedTimes);
				log.info("open short flag: " + openShortFlag);
				if (openShortFlag) {
					String tradeTicker = futureGetV1.future_ticker(symbol, contractType);
					JSONObject ticker = CommonFutureMethods.getTicker(tradeTicker);
					double buy1 = Double.parseDouble(ticker.getString("buy"));
					double sell1 = Double.parseDouble(ticker.getString("sell"));
					double avg = (buy1 + sell1) / 2; //open long price
					String openShortResult = futurePostV1.future_trade(symbol, contractType, String.valueOf(avg), amount, "2", "0", leverageRate);
					JSONObject openShortObject = JSONObject.parseObject(openShortResult);
					if (openShortObject.getBooleanValue("result")) {
						log.info("Open short, check whether the order is totally closed, otherwise cancel it.");
						String tempOrderId = openShortObject.getString("order_id");
						int waitTime = 0;
						while (waitTime < timeoutForChecking) {
							String orderResult = futurePostV1.future_order_info(symbol, contractType, tempOrderId, "", "", "");
							JSONObject orderObject = JSONObject.parseObject(orderResult);
							String status = orderObject.getJSONArray("orders").getJSONObject(0).getString("status");
							if (status.equals("2")) {
								log.info("Open short successfully, the order is closed");
								shortTicketNum = 1;
								shortPrice = avg;
								if (longTicketNum == 1) {
									//为了避免开空的时候，还有多单，需要在这个时候平掉多单,虽然这种情况比较少，但是如果出现对造成比较大的损失
									//出现这个的原因是由于在连续4次上次上涨伴随一次下跌，仍然没有达到盈利门限值的情况
									log.info("It's time to open short, we need to close the long ticket!");
									String closeLongResult = futurePostV1.future_trade(symbol, contractType, "", amount, "3", "1", leverageRate);
									JSONObject closeLongObject = JSONObject.parseObject(closeLongResult);
									if (closeLongObject.getBooleanValue("result")) {
										log.info("close long successfully!!!");
										String closeLongOrderId = closeLongObject.getString("order_id");
										String closedOrderInfo = futurePostV1.future_order_info(symbol, contractType, closeLongOrderId, "", "", "");
										JSONObject tempObject = JSONObject.parseObject(closedOrderInfo);
										String closeprice = tempObject.getJSONArray("orders").getJSONObject(0).getString("price_avg");
										log.error("close long record, close long price: " + closeprice);
										longTicketNum = 0;
										longPrice = 0;
									} else {
										log.info("close long failed after opening short!!!");
									}
								}
								break;
							}
							Thread.sleep(1000);
							waitTime++;
						}
						if (waitTime == timeoutForChecking) {
							log.info("The order is not closed until timeout, now cancel it");
							String cancelResult = futurePostV1.future_cancel(symbol, contractType, tempOrderId);
							log.info("cancel result: " + cancelResult);
						}
					} else {
						log.info("open short failed");
						continue;
					}
				}
			} else {
				//close short
				boolean closeShortFlag = CloseShortStrategy.isCloseShortTimingBasedOnProfit(symbol, contractType, shortPrice, "");
				log.info("close short flag: " + closeShortFlag);
				if (closeShortFlag) {
					log.info("close short condition is satisfied，close short now to sell without price limit");
					String closeShortResult = futurePostV1.future_trade(symbol, contractType, "", amount, "4", "1", leverageRate);
					JSONObject closeShortObject = JSONObject.parseObject(closeShortResult);
					if (closeShortObject.getBooleanValue("result")) {
						log.info("close short successfully!!!");
						String closeShortOrderId = closeShortObject.getString("order_id");
						String closedOrderInfo = futurePostV1.future_order_info(symbol, contractType, closeShortOrderId, "", "", "");
						JSONObject tempObject = JSONObject.parseObject(closedOrderInfo);
						String closeprice = tempObject.getJSONArray("orders").getJSONObject(0).getString("price_avg");
						log.error("close short record, close short price: " + closeprice);
						shortTicketNum = 0;
						shortPrice = 0;
					} else {
						continue;
					}
				}
			}
		}
	}
}
