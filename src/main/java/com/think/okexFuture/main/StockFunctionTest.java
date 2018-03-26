package com.think.okexFuture.main;

import java.io.IOException;

import org.apache.http.HttpException;

import com.think.okexFuture.common.OkexProperties;
import com.think.okexFuture.rest.stock.IStockRestApi;
import com.think.okexFuture.rest.stock.impl.StockRestApi;

public class StockFunctionTest {

	public static void main(String[] args) throws HttpException, IOException, InterruptedException {

		IStockRestApi stockRestApiGet = new StockRestApi(OkexProperties.URL_PREX);

		String result = stockRestApiGet.kline("eos_usdt", "15min", "5", "");
		System.out.println(result);

	}

}
