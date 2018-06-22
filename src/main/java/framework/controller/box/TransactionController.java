package framework.controller.box;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import framework.common.CollectionUtils;
import framework.common.result.Code;
import framework.common.result.Message;
import framework.common.result.Result;
import framework.common.util.MD5.MD5Transform;
import framework.dao.BoxPortCateRepository;
import framework.dao.BoxPortRepository;
import framework.dao.BoxRepository;
import framework.dao.CommodityRepository;
import framework.dao.CommodityRoadRepository;
import framework.dao.OrderCommodityRoadRepository;
import framework.dao.OrderRepository;
import framework.dao.OrderStateRcdRepository;
import framework.dao.OrderStateRepository;
import framework.entity.po.Box;
import framework.entity.po.BoxPort;
import framework.entity.po.BoxPortCate;
import framework.entity.po.Commodity;
import framework.entity.po.CommodityRoad;
import framework.entity.po.Order;
import framework.entity.po.OrderCommodity;
import framework.entity.po.OrderCommodityRoad;
import framework.entity.po.OrderStateRcd;
import framework.enums.IsDeleteEnum;
import framework.enums.OrderStatusEnum;
import framework.mqtt.MQttUtil;
import framework.mqtt.util.MacSignature;
import framework.pojo.DataType;
import framework.pojo.Request;
import framework.service.interf.OrderService;
import framework.service.interf.OrderStateRcdService;
import framework.service.interf.TransactionService;


@Controller
@RequestMapping("/transaction")
public class TransactionController {
	/**
	 * 业务流程: 商品管理由本服务器维护 1,柜机请求并缓存所有本柜机有的货道内商品列表
	 * 2.用户看中某个商品,点击某个商品,选择数量,点击确定,向服务器发送请求(柜机id,商品id,数量)生成二维码
	 * 3.用户扫描二维码,请求柜机服务器,生成订单,柜机服务器生成订单后,重定向到天点服务器,调用户的支付
	 * 4.支付成功后,天点服务器向柜机服务器返回数据(支付时间,支付成功失败),成功出货,失败调重试或取消订单
	 * 5.柜机服务器收到支付成功请求后,按照算法计算出货货道,调用发送命令sdk,发送命令
	 * 6.sdk成功后,返回结果给柜机服务器,柜机服务器通知天点服务器,失败同理
	 */

	private static Logger log = LoggerFactory.getLogger(TransactionController.class);

	@Autowired
	private CommodityRoadRepository commodityRoadRepository;
	@Autowired
	BoxPortCateRepository boxPortCateRepository;
	@Autowired
	private CommodityRepository commodityRepository;

	@Autowired
	private BoxPortRepository boxPortRepository;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private OrderStateRcdRepository orderStateRcdRepository;

	@Autowired
	private OrderStateRepository orderStateRepository;

	@Autowired
	private OrderStateRcdService orderStateRcdService;

	@Autowired
	private OrderCommodityRoadRepository orderCommodityRoadRepository;
	

	@Autowired
	private MQttUtil mQttUtil;

	@Autowired
	private BoxRepository boxRepository;



	/**
	 * 用户选择数量,商品,扫描二维码生成订单 然后重定向到天点服务器支付
	 * 
	 * @param box_id
	 * @param commodity_id
	 * @param commodity_num
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/createOrder")
	public String createOrder(String ids, String $key, String state) {
		StringBuilder sb = new StringBuilder("redirect:");
		log.info("用户扫描二维码,进入生成订单流程");
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", ids);
		boolean ischecked = false;
		try {
			String _key = MacSignature.doHttpSignature(params, IndexController.secKey);
			if (_key.equals(IBase64.decode($key))) {
				ischecked = true;
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (!ischecked) {
			sb.append("/pages/tip_error.html?errMsg=参数错误");
			return sb.toString();
		}

		String jsonData = IBase64.decode(ids);
		Map<String, Object> json = (Map<String, Object>) JSON.parse(jsonData);
		System.out.println(json);
		Long box_id = Long.valueOf(String.valueOf(json.get("boxId")));

		Box box = boxRepository.findOne(box_id);
		List<BoxPort> boxPorts = box.getBoxPorts();
		if (null == boxPorts || boxPorts.size() < 0) {
			sb.append("/pages/tip_error.html?errMsg=参数错误");
			return sb.toString();
		}

		List<Map<String, Object>> _ids = (List<Map<String, Object>>) json.get("ids");
		Map<String, Map<Long, Integer>> commodityList = new HashMap<String, Map<Long, Integer>>();

		for (int i = 0; i < _ids.size(); i++) {
			Map<String, Object> mp = _ids.get(i);
			Long commodityId = Long.valueOf(String.valueOf(mp.get("commodityId")));
			Long boxPortCateId = Long.valueOf(String.valueOf(mp.get("boxPortCateId")));
			
			BoxPortCate boxPortCate = boxPortCateRepository.findOne(boxPortCateId);
			Commodity commodity = commodityRepository.findOne(commodityId);
			
			Integer saleNum = Integer.valueOf(String.valueOf(mp.get("saleNum")));
			int _num = saleNum;
			List<CommodityRoad> listroad = commodityRoadRepository.findAllByBoxAndBoxPortCateAndCommodity(box,boxPortCate,commodity);
			
			Map<Long, Integer> saleRoadList = new HashMap<Long, Integer>();
			
			
			for (int j = 0; j < listroad.size(); j++) {
				CommodityRoad road = listroad.get(j);
				int __num = 0;
				if (_num > 0) {
					__num = (road.getCommodityNum() - 1);
					if (_num >= __num) {
						_num -= __num;
					} else if (_num < __num) {
						__num = _num;
						_num -= __num;
					}
				}
				if (__num > 0) {
					saleRoadList.put(road.getCommodityRoadId(), __num);
				}
			}
			if (_num > 0) {
				for (int j = 0; j < listroad.size(); j++) {
					CommodityRoad road = listroad.get(j);
					int __num = 0;
					if (_num > 0) {
						__num = 1;
						_num -= __num;
					}
					if (__num > 0) {
						Integer __ = saleRoadList.get(road.getCommodityRoadId());
						if (__ == null) {
							__ = 0;
						}
						__num += __;
						saleRoadList.put(road.getCommodityRoadId(), __num);
					}
				}
			}
			commodityList.put(commodityId+"_"+boxPortCateId, saleRoadList);
		}

		System.out.println(JSON.toJSONString(commodityList));
		Order od = orderService.createOrder(commodityList, box);
		box = boxRepository.findOne(box.getBoxId());
		Map<String, Object> rt = new HashMap<String, Object>();
		rt.put("box", box);
		rt.put("BoxPort", box.getBoxPorts());
		rt.put("CommodityRoad", box.getCommodityRoads());
		rt.put("boxPortCate", boxPortCateRepository.findAll());
		Request bind = new Request();
		bind.setDataType(DataType.BINDBACK);
		bind.setDevice(box.getBoxCode());
		bind.setData(Request.Object2Bytes(rt));
		mQttUtil.sendP2P(bind);
		String KEY = "WKuier_234390_dkjoDH_03-dfQz";
		StringBuilder sbx = new StringBuilder();
		sbx.append(od.getOrderId());
		sbx.append(state);
		sbx.append(KEY);
		// key不一致，不响应
		String key1 = MD5Transform.EncoderByMd5(sbx.toString(), "utf-8");
		System.out.println(JSON.toJSONString(orderRepository.findOne(od.getOrderId())));
		sb.append("http://api.tiand99.net/box/box_order_pay.action?order=" + od.getOrderId() + "&state=" + state
				+ "&$key=" + key1);
		return sb.toString();
	}

	@RequestMapping(value = "/orderInfo", method = RequestMethod.GET)
	@ResponseBody
	public Result orderInfo(Long orderId, String $key) {
		String KEY = "WKuier_234390_dkjoDH_03-dfQz";
		StringBuilder sbx = new StringBuilder();
		sbx.append(orderId);
		sbx.append(KEY);
		// key不一致，不响应
		String key1 = MD5Transform.EncoderByMd5(sbx.toString(), "utf-8");
		if (!key1.equals($key)) {
			Result ret = new Result();
			ret.setSuccess(false);
			return ret;
		}
		Order order = orderRepository.findOne(orderId);
		if (order != null) {
			String json = JSON.toJSONString(order);
			StringBuilder sbx1 = new StringBuilder();
			sbx1.append(json);
			sbx1.append(KEY);
			// key不一致，不响应
			String key12 = MD5Transform.EncoderByMd5(sbx1.toString(), "utf-8");
			Result ret = new Result();
			ret.set$key(key12);
			ret.setResult(json.getBytes());
			ret.setSuccess(true);
			return ret;
		} else {
			Result ret = new Result();
			ret.setSuccess(false);
			return ret;
		}

	}

	/**
	 * 支付成功后天点服务器回调接口
	 * 
	 * @param orderId
	 * @param pay_time
	 * @param isTrue
	 *            1是(成功)2否(不成功)
	 */
	@RequestMapping(value = "/shippingOptions", method = RequestMethod.GET)
	@ResponseBody
	public Result shippingOptions(Long orderId, Long payTime, Short isTrue) {

		// 参数判断
		boolean bool = null == orderId || orderId < 0 || null == payTime || payTime < 0 || null == isTrue || isTrue < 0;
		if (bool) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}

		// 查询订单
		Order dbOrder = orderRepository.findOne(orderId);
		if (null == dbOrder) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}

		List<OrderCommodity> roadlist = dbOrder.getOrderCommoditys();
		if (CollectionUtils.isNotEmpty(roadlist)) {
			if (isTrue == 1) {
				OrderStateRcd orderStateRcd = new OrderStateRcd();
				orderStateRcd.setOrderStateId(OrderStatusEnum.WAITING_FOR_SHIPMENT.getType());
				orderStateRcd.setOrder(dbOrder);
				orderStateRcd.setOrderState(orderStateRepository.findOne(2L));
				orderStateRcd.setRcdTime(System.currentTimeMillis());
				orderStateRcd.setRemarks("paySuccess");
				orderStateRcdService.saveOrderStateRcd(orderStateRcd); // 保存
				dbOrder.setOrderState(orderStateRepository.findOne(2L));
				dbOrder.setPayTime(payTime);
				orderRepository.save(dbOrder);
				List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();
				for (OrderCommodity orderCommodity : roadlist) {
					List<OrderCommodityRoad> listRoad = orderCommodity.getOrderCommodityRoads();
					for (int i = 0; i < listRoad.size(); i++) {
						OrderCommodityRoad road = listRoad.get(i);
						CommodityRoad commodityRoad = road.getCommodityRoad();
						Map<String, Object> map = new HashMap<String, Object>();
						// 订单号
						map.put("orderId", orderCommodity.getOrder().getOrderId());
						map.put("orderCommodityRoadId", road.getOrderCommodityRoadId());
						map.put("devicePort", commodityRoad.getBoxPort().getDevicePort());
						// 货道编码
						map.put("code", commodityRoad.getCode());
						// 数量
						map.put("number", road.getCommodityNum());
						responseList.add(map);
					}
				}
				String ss = JSONObject.toJSONString(responseList);
				Request request = new Request();
				request.setDevice(dbOrder.getBox().getBoxCode());
				request.setDataType(DataType.CONTROL_INSTRUCTION);
				request.setData(ss.getBytes());
				mQttUtil.sendP2P(request);
			} else if (isTrue == 0) {
				for (OrderCommodity orderCommodity : roadlist) {
					List<OrderCommodityRoad> road = orderCommodity.getOrderCommodityRoads();
					for (int i = 0; i < road.size(); i++) {
						OrderCommodityRoad orderCommodityRoad = road.get(i);
						CommodityRoad commodityRoad = orderCommodityRoad.getCommodityRoad();
						commodityRoad.setCommodityNum(
								commodityRoad.getCommodityNum() + orderCommodityRoad.getCommodityNum());
//						orderCommodityRoad.setIsDelete(IsDeleteEnum.DELETE.getType());
						commodityRoadRepository.save(commodityRoad);
						orderCommodityRoadRepository.save(orderCommodityRoad);
					}
					OrderStateRcd orderStateRcd = new OrderStateRcd();
					orderStateRcd.setOrderStateId(OrderStatusEnum.TRADING_CLOSED.getType());
					orderStateRcd.setOrder(dbOrder);
					orderStateRcd.setOrderState(orderStateRepository.findOne(6L));
					orderStateRcd.setRcdTime(System.currentTimeMillis());
					orderStateRcd.setRemarks("payFail");
					orderStateRcdService.saveOrderStateRcd(orderStateRcd); // 保存
					dbOrder.setOrderState(orderStateRepository.findOne(6L));
					orderRepository.save(dbOrder);
					Box box = boxRepository.findOne(dbOrder.getBox().getBoxId());
					Map<String, Object> rt = new HashMap<String, Object>();
					rt.put("box", box);
					rt.put("boxPort", box.getBoxPorts());
					rt.put("commodityRoad", box.getCommodityRoads());
					rt.put("boxPortCate", boxPortCateRepository.findAll());
					
					Request bind = new Request();
					bind.setDataType(DataType.BINDBACK);
					bind.setDevice(box.getBoxCode());
					bind.setData(Request.Object2Bytes(rt));
					mQttUtil.sendP2P(bind);
				}
			}
		}

		return Result.setData(true, null);

	}

	/**
	 * 出货不成功,退订单 1,请求支付服务器退款 2,改回库存数量 3.记录异常日志
	 * 
	 * @param order_id
	 */
	@RequestMapping(value = "/refundOrder", method = RequestMethod.POST)
	@ResponseBody
	public Result refundOrder(Long order_id, Short isTrue) {
		// 检查参数
		if (null == order_id || order_id < 0) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}
		// 检查订单是否存在,以及支付状态
		Order order = orderRepository.findOne(order_id);
		if (null == order || null == order.getPayTime()) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}
		// 出货反馈后流程
		if (isTrue == 1) {// 成功

		} else if (isTrue == 2) {

		}

		return Result.setData(true, null);
	}

}
