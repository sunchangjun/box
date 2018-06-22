package framework.controller.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
import framework.dao.AdvRepository;
import framework.dao.AreaRepository;
import framework.dao.ImageResourceRepository;
import framework.entity.po.Adv;
import framework.service.interf.AdvService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//@Api(value= "Adv操作api", description ="广告api")
@RestController
@RequestMapping("/back/adv")
public class AdvController {

	@Autowired
	AdvService advService;
	@Autowired
	ImageResourceRepository imageResourceRepository;
	@Autowired
	AreaRepository areaRepository;
	@Autowired
	AdvRepository advRepository;
	

	@RequestMapping("/addOrUpdateAdv")
	public Result addOrUpdateAdv(Adv adv) {
		if (null == adv || null == adv.getAreaId() || null == adv.getImageResourceId() || null == adv.getSortOrder()
				|| null == adv.getType()) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}
		JSONObject jo = new JSONObject();
		try {
	
			adv.setImageResource(imageResourceRepository.findOne(adv.getImageResourceId()));
			adv.setArea(areaRepository.findOne(adv.getAreaId()));

			jo = JSONText.JavaBeanToJsonObject(advService.saveAdv(adv));

		} catch (Exception e) {
			Message message = new Message();
			message.setErrno(Code.CNUM001);
			message.setErrmsg(Code.CSTR001);
			return Result.setData(false, message);
		}
		return Result.setData(true, jo);
	}
//	@ApiOperation(value="条件查询广告分页", notes="查询")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "areaId", value = "区域id", required = false, dataType = "Long",paramType = "path"),
//		@ApiImplicitParam(name = "type", value = "类型", required = false, dataType = "Integer"),
//		@ApiImplicitParam(name = "pages", value = "当前页", required = true, dataType = "Integer"),
//		@ApiImplicitParam(name = "rows", value = "每页记录数", required = true, dataType = "Integer")
//})
	@RequestMapping("/getAllAdv")
	public Result getAllAdv(Long areaId, Integer type, Integer pages, Integer rows) {
		// 参数判断
		if (null == pages || null == rows) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}
		// 查询
		JSONArray ja = new JSONArray();
		Page<Adv> page=null;
		try {
			Query<Adv> query=new  Query<Adv>();
			if(null != areaId) {
				query.add(Select.eq("areaId", areaId));
			}
			if(null != type) {
				query.add(Select.eq("type", type));
			}
			Sort sort=new  Sort(Direction.ASC, "sortOrder");
			Pageable pageable=new  PageRequest(pages-1, rows,sort);
			 page=advRepository.findAll(query,pageable);
			 if(CollectionUtils.isNotEmpty(page.getContent())) {
				 for (Adv adv : page.getContent()) {
					 JSONObject jo=JSONText.JavaBeanToJsonObject(adv);
					 jo.put("imageResource", JSONText.JavaBeanToJsonObject(adv.getImageResource()));
				
					 ja.add(jo);					
					}
			 }
			

		} catch (Exception e) {
			Message message = new Message();
			message.setErrno(Code.CNUM001);
			message.setErrmsg(Code.CSTR001);
			return Result.setData(false, message);
		}
		TianDianPage tp=new  TianDianPage();
		if(null != page) {
			tp.setContent(ja);
			tp.setCurrentPage(page.getNumber());
			tp.setPageCount(page.getTotalPages());
			tp.setTotal(page.getTotalElements());
		}
		
		return Result.setData(true, tp);
	}
//	@ApiOperation(value="获取单个广告实体", notes="查询单个")
//	@ApiImplicitParam(name = "advId", value = "广告id", required = true, dataType = "Long")
	@PostMapping("/getOneAdv")
	public  Result getOneAdv(Long advId) {
		if(null == advId) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}
		JSONObject jo=new   JSONObject();
		try {
			jo=JSONText.JavaBeanToJsonObject(advRepository.findOne(advId));		
		} catch (Exception e) {
			Message message = new Message();
			message.setErrno(Code.CNUM001);
			message.setErrmsg(Code.CSTR001);
			return Result.setData(false, message);
		}	
		return Result.setData(true, jo);
	}
	
	@PostMapping("/deleteAdv")
	public  Result  deleteAdv(Long advId) {
		if(null == advId) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg(Code.CSTR003);
			return Result.setData(false, message);
		}
		boolean bool=false;
		try {
			advRepository.delete(advId);
			bool=true;
		} catch (Exception e) {
			Message message = new Message();
			message.setErrno(Code.CNUM001);
			message.setErrmsg(Code.CSTR001);
			return Result.setData(false, message);
		}
		
		return  Result.setData(true, bool);
	}

}
