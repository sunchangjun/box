package framework.service.interf;

import java.util.List;

import framework.entity.po.CommodityRoad;
import framework.entity.po.OrderCommodityRoad;

public interface TransactionService {
	/**
	 * 同步保存货道库存和订单货道表
	 * @param commodityRoadList
	 * @param orderShippingLaneList
	 * @return
	 */
	boolean shippingOptions(CommodityRoad commodityRoad,OrderCommodityRoad orderShippingLane);
	boolean shippingOptionsList(List<CommodityRoad> commodityRoadList,List<OrderCommodityRoad> orderShippingLaneList);
}
