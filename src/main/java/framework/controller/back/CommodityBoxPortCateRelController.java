package framework.controller.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import framework.common.CollectionUtils;
import framework.common.JSONText;
import framework.common.TianDianPage;
import framework.common.jpa.Query;
import framework.common.jpa.Select;
import framework.common.result.Code;
import framework.common.result.Message;
import framework.common.result.Result;
import framework.dao.CommodityBoxPortCateRelRepository;
import framework.entity.po.CommodityBoxPortCateRel;
import framework.service.interf.CommodityBoxPortCateRelService;

@RestController
@RequestMapping("/back/commodityBoxPortCateRel")
public class CommodityBoxPortCateRelController {
	@Autowired
	CommodityBoxPortCateRelRepository commodityBoxPortCateRelRepository;
	
	@Autowired
	CommodityBoxPortCateRelService commodityBoxPortCateRelService;
	
	/**
	 * 查询分页
	 * @param boxPortCateId
	 * @param commodityId
	 * @param pages
	 * @param rows
	 * @return
	 */
	@PostMapping("/getAllCommodityBoxPortCateRel")
	public Result getAllCommodityBoxPortCateRel(Long boxPortCateId,Long commodityId,Integer pages,Integer rows) {
		//参数判断
		if(null == pages || null == rows) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		Query<CommodityBoxPortCateRel> query=new Query<CommodityBoxPortCateRel>();
		if(null != boxPortCateId) {
			query.add(Select.eq("boxPortCateId", boxPortCateId));
		}
		if(null !=commodityId ) {
			query.add(Select.eq("commodity", commodityId));
		}
		JSONArray ja=new JSONArray();
		Page<CommodityBoxPortCateRel> page=null;
		try {
			Pageable pageable=new PageRequest(pages-1, rows);
			 page=commodityBoxPortCateRelRepository.findAll(query,pageable);
			if(CollectionUtils.isNotEmpty(page.getContent())) {
				for (CommodityBoxPortCateRel commodityBoxPortCateRel : page.getContent()) {
					JSONObject jo=JSONText.JavaBeanToJsonObject(commodityBoxPortCateRel);
//					jo.remove("commodity");
					ja.add(jo);
				}		
			}
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}
		TianDianPage tp=new TianDianPage();
		if(null != page) {
			tp.setContent(ja);
			tp.setCurrentPage(page.getNumber());
			tp.setCurrentPage(page.getTotalPages());
			tp.setTotal(page.getTotalElements());
		}
		
		
		return Result.setData(true, tp);
	}
	/**
	 * 删除
	 * @param boxPortCateRelId
	 * @return
	 */
	@PostMapping("/deleteCommodityBoxPortCateRel")
	public Result deleteCommodityBoxPortCateRel(Long boxPortCateRelId) {
		//参数验证
		if(null ==  boxPortCateRelId) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		boolean boo=false;
		try {
			commodityBoxPortCateRelRepository.delete(boxPortCateRelId);
			boo=true;
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}	
		return Result.setData(true, boo);			
	}
	
	
	/**
	 * 修改
	 * @param commodityBoxPortCateRel
	 * @return
	 */
	@PostMapping("/updateCommodityBoxPortCateRel")
	public Result updateCommodityBoxPortCateRel(CommodityBoxPortCateRel commodityBoxPortCateRel) {
		//参数验证
		if(null == commodityBoxPortCateRel || null == commodityBoxPortCateRel.getBoxPortCateRelId()  || null ==commodityBoxPortCateRel.getBoxPortCateId() || null == commodityBoxPortCateRel.getCommodityId()) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		JSONObject jo=null;
		try {
			CommodityBoxPortCateRel dbCommodityBoxPortCateRel=	commodityBoxPortCateRelService.saveCommodityBoxPortCateRel(commodityBoxPortCateRel);
			if(null != dbCommodityBoxPortCateRel) {
				jo=JSONText.JavaBeanToJsonObject(dbCommodityBoxPortCateRel);
			}
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}
		
		
		return Result.setData(true, jo);
	}
	
	/**
	 * 查询单个
	 * @param boxPortCateRelId
	 * @return
	 */
	@PostMapping("/getOneCommodityBoxPortCateRel")
	public Result  getOneCommodityBoxPortCateRel(Long boxPortCateRelId) {
		//参数验证
		if(null ==  boxPortCateRelId) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		JSONObject jo=null;
		try {
			CommodityBoxPortCateRel dbCommodityBoxPortCateRel=	commodityBoxPortCateRelRepository.findOne(boxPortCateRelId);
			if(null !=dbCommodityBoxPortCateRel ) {
				jo=JSONText.JavaBeanToJsonObject(dbCommodityBoxPortCateRel);
			}	
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}	
		
		return Result.setData(true, jo);
	}

}
