package com.think.okexFuture.main;

import java.io.IOException;

import org.apache.http.HttpException;

import com.think.okexFuture.Kline.future.KlineAnalysisBasedOnFutureData;
import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.rest.future.IFutureRestApi;
import com.think.okexFuture.rest.future.impl.FutureRestApiV1;

public class FutureFunctionTest {
	private static IFutureRestApi futurePostV1 = new FutureRestApiV1(OkexProperties.URL_PREX, OkexProperties.API_KEY, OkexProperties.SECRET_KEY);

	public static void main(String[] args) throws HttpException, IOException, InterruptedException {
		// TODO Auto-generated method stub

		//String closeLongResult = futurePostV1.future_trade("eos_usd", "this_week", "", "1", "3", "1", "20");
		//System.out.println(closeLongResult);

		//{"result":true,"order_id":456681552432129}

		//		String closedOrderInfo = futurePostV1.future_order_info("eos_usd", "this_week", "456681552432129", "", "", "");
		//		JSONObject object = JSONObject.parseObject(closedOrderInfo);
		//
		//		String closeprice = object.getJSONArray("orders").getJSONObject(0).getString("price_avg");
		//
		//		System.out.println(closeprice);

		String result = futurePostV1.future_kLine("eos_usd", "15min", "this_week", "5", "");
		boolean flag = KlineAnalysisBasedOnFutureData.historyGoingUpExpectedTimes(result, 4);
		System.out.println(flag);
	}

}
