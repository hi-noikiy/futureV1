package com.think.okexFuture.rest.stock;

import java.io.IOException;

import org.apache.http.HttpException;


/**
 * 现货行情，交易 REST API
 * @author zhangchi
 *
 */
public interface IStockRestApi {

	/**
	 * 行情
	 * @param symbol btc_usd:比特币    ltc_usd :莱特币
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String ticker(String symbol) throws HttpException, IOException, InterruptedException;

	/**
	 * 市场深度
	 * @param symbol btc_usd:比特币    ltc_usd :莱特币
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String depth(String symbol) throws HttpException, IOException, InterruptedException;

	/**
	 * 现货历史交易信息
	 * @param symbol btc_usd:比特币    ltc_usd :莱特币
	 * @param since 不加since参数时，返回最近的60笔交易
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String trades(String symbol, String since) throws HttpException, IOException, InterruptedException;

	/**
	 * 获取用户信息
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String userinfo() throws HttpException, IOException, InterruptedException;

	/**
	 * 下单交易
	 * @param symbol btc_usd: 比特币 ltc_usd: 莱特币
	 * @param type 买卖类型： 限价单（buy/sell） 市价单（buy_market/sell_market）
	 * @param price 下单价格 [限价买单(必填)： 大于等于0，小于等于1000000 |
	 *                       市价买单(必填)： BTC :最少买入0.01个BTC 的金额(金额>0.01*卖一价) / LTC :最少买入0.1个LTC 的金额(金额>0.1*卖一价)]
	 *
	 * @param amount 交易数量 [限价卖单（必填）：BTC 数量大于等于0.01 / LTC 数量大于等于0.1 |
	 *                        市价卖单（必填）： BTC :最少卖出数量大于等于0.01 / LTC :最少卖出数量大于等于0.1]
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String trade(String symbol, String type,
		String price, String amount) throws HttpException, IOException, InterruptedException;

	/**
	 * 批量下单
	 * @param symbol   btc_usd: 比特币 ltc_usd: 莱特币
	 * @param type  买卖类型： 限价单（buy/sell） 市价单（buy_market/sell_market）
	 * @param orders_data JSON类型的字符串 例：[{price:3,amount:5},{price:3,amount:3}]
	 *        最大下单量为5，price和amount参数参考trade接口中的说明
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String batch_trade(String symbol, String type,
		String orders_data) throws HttpException, IOException, InterruptedException;

	/**
	 * 撤销订单
	 * @param symbol  btc_usd: 比特币 ltc_usd: 莱特币
	 * @param order_id 订单ID(多个订单ID中间以","分隔,一次最多允许撤消3个订单)
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String cancel_order(String symbol, String order_id) throws HttpException, IOException, InterruptedException;

	/**
	 * 获取用户的订单信息
	 * @param symbol  btc_usd: 比特币 ltc_usd: 莱特币
	 * @param order_id  订单ID(-1查询全部订单，否则查询相应单号的订单)
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String order_info(String symbol, String order_id) throws HttpException, IOException, InterruptedException;

	/**
	 * 批量获取用户订单
	 * @param type  查询类型 0:未成交，未成交 1:完全成交，已撤销
	 * @param symbol  btc_usd: 比特币 ltc_usd: 莱特币
	 * @param order_id  订单ID(多个订单ID中间以","分隔,一次最多允许查询50个订单)
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String orders_info(String type, String symbol,
		String order_id) throws HttpException, IOException, InterruptedException;

	/**
	 * 获取历史订单信息，只返回最近七天的信息
	 * @param symbol btc_usd: 比特币 ltc_usd: 莱特币
	 * @param status 委托状态: 0：未成交 1：已完成(最近七天的数据)
	 * @param current_page  当前页数
	 * @param page_length  每页数据条数，最多不超过200
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String order_history(String symbol, String status,
		String current_page, String page_length) throws HttpException, IOException, InterruptedException;

	/**
	 * Get /api/v1/kline 获取OKEx币币K线数据(每个周期数据条数2000左右)
	 * @param symbol btc_usd: 比特币 ltc_usd: 莱特币
	 * @param type 1min/3min/5min/15min/30min/1day/3day/1week/1hour/2hour/4hour/6hour/12hour
	 * @param size optional  指定获取数据的条数
	 * @param since  时间戳，返回该时间戳以后的数据(例如1417536000000)
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String kline(String symbol, String type,
		String size, String since) throws HttpException, IOException, InterruptedException;

}
