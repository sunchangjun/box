package framework.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import framework.common.jpa.BaseRepository;
import framework.entity.po.Box;
import framework.entity.po.BoxPort;
import framework.entity.po.BoxPortCate;
import framework.entity.po.Commodity;
import framework.entity.po.CommodityRoad;

/**
 * 货道
 * 
 * @author Administrator
 *
 */
public interface CommodityRoadRepository extends BaseRepository<CommodityRoad, Long> {

	//根据柜机编号和货道编号查询货道
//	@Query("select r    from Box b,BoxPort p,CommodityRoad r  where b.boxId=p.boxId and p.boxPortId=r.boxPortId and b.boxCode= ?1 and r.code= ?2")
//	CommodityRoad getCommodityRoadByBoxCodeAndCode(String boxCode,String  code);
	
	
	//根据柜机编号查询货道列表
//	@Query("select r    from Box b,BoxPort p,CommodityRoad r  where b.boxId=p.boxId and p.boxPortId=r.boxPortId and b.boxCode= ?1 ")
//	List<CommodityRoad> getCommodityRoadListByBoxCode(String boxCode);
	
	//根据柜机编号查询货道列表
//	@Query("select count(r.commodityNum)    from Box b,BoxPort p,CommodityRoad r  where b.boxId=p.boxId and p.boxPortId=r.boxPortId and b.boxCode= ?1 and r.commodityNum<5")
//	Integer getCommodityRoadCountByBoxCode(String boxCode);
	
	
//	@Query(value = "select `c`.`commodity_road_id` AS `commodity_road_id`,`c`.`commodity_id` AS `commodity_id`,`c`.`box_port_id` AS `box_port_id`,`c`.`commodity_num` AS `commodity_num`,`c`.`code` AS `code`,`c`.`code_img_url` AS `code_img_url`,`c`.`is_enable` AS `is_enable` from ((`t_commodity_road` `c` left join `t_box_port` `b` on((`b`.`box_port_id` = `c`.`box_port_id`))) left join `t_box_new` `a` on((`b`.`box_id` = `a`.`box_id`))) where (`a`.`box_id` = ?1) group by `c`.`commodity_road_id`", nativeQuery = true)
//	List<CommodityRoad> findAllByBoxId(Long boxId);
//
//
//	@Query(value = "select `a`.`commodity_road_id` AS `commodity_road_id`,`a`.`box_id` AS `box_id`,`a`.`box_port_id` AS `box_port_id`,`a`.`commodity_id` AS `commodity_id`,`a`.`commodity_num` AS `commodity_num`,`a`.`code` AS `code`,`a`.`code_img_url` AS `code_img_url`,`a`.`is_enable` AS `is_enable` from `t_commodity_road` `a` where ((`a`.`box_id` = ?2) and (`a`.`commodity_id` = ?1) and (`a`.`commodity_num` > 0)) order by `a`.`commodity_num` desc", nativeQuery = true)
//	List<CommodityRoad> findByBoxIdAndCommodityId(Long commodityId,Long box_id);


	CommodityRoad findByCodeAndBoxPort(String _code, BoxPort boxPort);

	List<CommodityRoad> findAllByBoxAndCommodity(Box box, Commodity commodity);

	CommodityRoad findByBoxAndCode(Box box, String code);
	
	@Query(value = "select a from CommodityRoad a where a.box=?1 and a.boxPort.boxPortCate=?2 and a.commodity=?3")
	List<CommodityRoad> findAllByBoxAndBoxPortCateAndCommodity(Box box, BoxPortCate boxPortCate, Commodity commodity);

}
