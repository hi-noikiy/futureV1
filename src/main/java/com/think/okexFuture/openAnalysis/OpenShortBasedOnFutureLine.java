package com.think.okexFuture.openAnalysis;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.think.okexFuture.Kline.future.KlineAnalysisBasedOnFutureData;
import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.rest.future.IFutureRestApi;
import com.think.okexFuture.rest.future.impl.FutureRestApiV1;

/**
 * 此类用来判断买入时机 反转的苗头，此类只是判断是否购买，真实购买可能还需要其他条件或者入手策略
 * 策略：
 * 当过去4次及4次以上的走势都是down一致的时候
 * 并且当前15分钟内： 9次比前一次收盘价格高 或者 6次比收盘高，而且第14分钟的时候为up： 判断开始反转
 * note： 如果等待10分钟，可能错过买入时机，在反转的地方，需要尽快判断，这里加入一个条件，如果前5分钟都是up或者down？  这个需要数据来回测
 * 需要记录此次购买的时间，及所在15min的时间戳，如果在下一个15分钟收盘走势跟预期不一致的时候，标记此次反转为fake的
 * 并将此次记录在一个全局的map中，在买入的时候进行分析 Note： 此条件暂时不考虑
 * @author map6
 *
 */
public class OpenShortBasedOnFutureLine {
	private static Logger log = Logger.getLogger(OpenShortBasedOnFutureLine.class);
	private static IFutureRestApi futureGetV1 = new FutureRestApiV1(OkexProperties.URL_PREX);

	/**
	 * 获取过去5次记录并分析
	 * @param symbol btc_usd ltc_usd eth_usd etc_usd bch_usd
	 * @param type 1min/3min/5min/15min/30min/1day/3day/1week/1hour/2hour/4hour/6hour/12hour
	 * @param contractType 合约类型: this_week:当周 next_week:下周 quarter:季度
	 * @param size 指定获取数据的条数
	 * @param since 时间戳（eg：1417536000000）。 返回该时间戳以后的数据
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParseException
	 */
	public static boolean isOpenShortTiming(String symbol, String type, String contractType, String size, String since, int expectedTimes)
		throws HttpException, IOException, InterruptedException, ParseException {
		String result = futureGetV1.future_kLine(symbol, type, contractType, size, since);
		boolean downFlag = KlineAnalysisBasedOnFutureData.historyGoingUpExpectedTimes(result, expectedTimes);
		if (downFlag) {
			log.info("15min history data, going up " + expectedTimes + " times, If current 15min is going down. I will open short+++++");
			boolean currentTrendencyFlag = KlineAnalysisBasedOnFutureData.current15MinAnalysisWillDown(symbol, contractType);
			if (currentTrendencyFlag) {
				log.info("It's open short time, open short +++++++++++++++++++++");
				return true;
			} else {
				log.info("15min history data, going up " + expectedTimes + " times, currentTrendencyFlag is still false, waiting");
			}
		}
		return false;
	}
}
