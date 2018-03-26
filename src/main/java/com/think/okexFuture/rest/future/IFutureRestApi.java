package com.think.okexFuture.rest.future;

import java.io.IOException;

import org.apache.http.HttpException;


/**
 * 期货行情，交易 REST API
 * @author zhangchi
 *
 */
public interface IFutureRestApi {


	/**
	 * 期货行情
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType  合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_ticker(String symbol, String contractType) throws HttpException, IOException, InterruptedException;

	/**
	 * 期货指数
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_index(String symbol) throws HttpException, IOException, InterruptedException;

	/**
	 * 期货交易记录
	 * @param symbol    btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_trades(String symbol, String contractType) throws HttpException, IOException, InterruptedException;

	/**
	 * 期货深度
	 * @param symbol  btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType  合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_depth(String symbol, String contractType) throws HttpException, IOException, InterruptedException;

	/**
	 * 汇率查询
	 * @return
	 * @throws IOException
	 * @throws HttpException
	*/
	public String exchange_rate() throws HttpException, IOException;

	/**
	 * 取消订单
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType    合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @param orderId   订单ID
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_cancel(String symbol, String contractType, String orderId) throws HttpException, IOException, InterruptedException;

	/**
	 * 期货下单
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @param price  价格
	 * @param amount  委托数量
	 * @param type   1:开多   2:开空   3:平多   4:平空
	 * @param matchPrice  是否为对手价 0:不是    1:是   ,当取值为1时,price无效
	 * @param lever_rate 杠杆倍数，下单时无需传送，系统取用户在页面上设置的杠杆倍数。且“开仓”若有10倍多单，就不能再下20倍多单
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_trade(String symbol, String contractType, String price, String amount, String type, String matchPrice, String leverRate)
		throws HttpException, IOException, InterruptedException;


	/**
	 * 期货账户信息
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws InterruptedException
	*/
	public String future_userinfo() throws HttpException, IOException, InterruptedException;

	/**
	 * 期货逐仓账户信息
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_userinfo_4fix() throws HttpException, IOException, InterruptedException;

	/**
	 * 用户持仓查询
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_position(String symbol, String contractType) throws HttpException, IOException, InterruptedException;

	/**
	 * 用户逐仓持仓查询
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_position_4fix(String symbol, String contractType) throws HttpException, IOException, InterruptedException;

	/**
	 * 获取用户订单信息
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @param orderId   订单ID(-1查询全部未成交订单，否则查询相应单号的订单)
	 * @param status   查询状态：1:未完成(最近七天的数据)  2:已完成(最近七天的数据)
	 * @param currentPage 当前页数
	 * @param pageLength  每页获取条数，最多不超过50
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	public String future_order_info(String symbol, String contractType, String orderId, String status, String currentPage, String pageLength)
		throws HttpException, IOException, InterruptedException;

	/**
	 * 获取虚拟合约的K线数据
	 * @param symbol ,必填, btc_usd   ltc_usd    eth_usd    etc_usd    bch_usd
	 * @param type, 必填,		1min : 1分钟
							3min : 3分钟
							5min : 5分钟
							15min : 15分钟
							30min : 30分钟
							1day : 1日
							3day : 3日
							1week : 1周
							1hour : 1小时
							2hour : 2小时
							4hour : 4小时
							6hour : 6小时
							12hour : 12小时
	 * @param contractType, 必填, 合约类型。this_week：当周；next_week：下周；quarter：季度
	 * @param size, 指定获取数据的条数, 默认0
	 * @param since, 时间戳（eg：1417536000000）。 返回该时间戳以后的数据, 默认0
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String future_kLine(String symbol, String type, String contractType, String size, String since)
		throws HttpException, IOException, InterruptedException;

	/**
	 * 批量获取合约订单信息
	 * @param symbol   btc_usd:比特币    ltc_usd :莱特币
	 * @param contractType   合约类型: this_week:当周   next_week:下周   month:当月   quarter:季度
	 * @param orderId   订单ID(-1查询全部未成交订单，否则查询相应单号的订单)
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	*/
	String future_orders_info(String symbol, String contractType, String orderId) throws HttpException, IOException, InterruptedException;

}
