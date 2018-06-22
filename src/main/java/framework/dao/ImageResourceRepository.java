package framework.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import framework.common.jpa.BaseRepository;
import framework.entity.po.ImageResource;

/**
 * 图片资源表
 * @author sunchangjunn
 *
 */
public interface ImageResourceRepository extends BaseRepository<ImageResource,Long>{
	
	
	@Query(value="select a.commodity.imageResource from CommodityRoad a where a.box.boxCode=?1 group by a.commodity.imageResource.imageResourceId")
	List<ImageResource> findAllByDevice(String device);
	
//	@Query("select i from ImageResource i where i. commodityId in (?1)")
//	List<ImageResource> getImageResourceListByCommodity(List<Long> commodityIdList);
//	
//	@Query("select i from ImageResource i where i. commodityId = ?1")
//	List<ImageResource> getImageResourceByCommodity(Long commodityId);
//
//	
//	@Query(value="select `d`.`image_resource_id` AS `image_resource_id`,`d`.`commodity_id` AS `commodity_id`,`d`.`url` AS `url`,`d`.`res_type` AS `res_type`,`d`.`enabled` AS `enabled`,`d`.`thump` AS `thump`,`d`.`remarks` AS `remarks` from (((`t_image_resource` `d` left join `t_commodity_road` `c` on((`c`.`commodity_id` = `d`.`commodity_id`))) left join `t_box_port` `b` on((`b`.`box_port_id` = `c`.`box_port_id`))) left join `t_box_new` `a` on((`b`.`box_id` = `a`.`box_id`))) where (`a`.`box_code` = ?1) group by `d`.`image_resource_id`",nativeQuery=true)
//	List<ImageResource> findAllByDevice(String device);

}
