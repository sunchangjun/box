package framework.controller.back;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import framework.common.JSONExt;
import framework.common.JSONText;
import framework.common.TianDianPage;
import framework.common.jpa.Query;
import framework.common.jpa.Select;
import framework.common.result.Code;
import framework.common.result.Message;
import framework.common.result.Result;
import framework.dao.BoxPortCateRepository;
import framework.dao.BoxRepository;
import framework.dao.CommodityBoxPortCateRelRepository;
import framework.dao.CommodityCateRepository;
import framework.dao.CommodityRepository;
import framework.dao.ImageResourceRepository;
import framework.entity.dto.CommodityDTO;
import framework.entity.po.Box;
import framework.entity.po.BoxPortCate;
import framework.entity.po.Commodity;
import framework.entity.po.CommodityBoxPortCateRel;
import framework.entity.po.CommodityCate;
import framework.entity.po.CommodityRes;
import framework.entity.po.ImageResource;
import framework.enums.IsDeleteEnum;
import framework.mqtt.MQttUtil;
import framework.pojo.DataType;
import framework.pojo.Request;
import framework.service.interf.CommodityBoxPortCateRelService;
import framework.service.interf.CommodityService;

@Controller
@RequestMapping("/back/commodity")
public class CommodityController {

	private static Logger log = LoggerFactory.getLogger(CommodityController.class);

	@Autowired
	private CommodityRepository commodityRepository;

	@Autowired
	private CommodityService commodityService;

	@Autowired
	private CommodityCateRepository commodityCateRepository;

	@Autowired
	private MQttUtil mQttUtil;

	@Autowired
	private BoxRepository boxRepository;

	@Autowired
	CommodityBoxPortCateRelService commodityBoxPortCateRelService;

	@Autowired
	BoxPortCateRepository boxPortCateRepository;

	@Autowired
	ImageResourceRepository imageResourceRepository;

	@Autowired
	CommodityBoxPortCateRelRepository commodityBoxPortCateRelRepository;

	/**
	 * 添加或修改商品
	 * 
	 * @param commodity_id
	 * @param commodity_name
	 * @param price
	 * @param stocks
	 * @param commodity_desc
	 * @return
	 */
	@RequestMapping(value = "/createOrUpdateCommodity")
	@ResponseBody
	public Result createOrUpdateCommodity(Commodity commodity, String bocCate) {
		// 参数判断
		if (null == commodity || StringUtils.isBlank(commodity.getCommodityName())
				|| null == commodity.getCommodityCateId() || StringUtils.isBlank(commodity.getSpecification())
				|| StringUtils.isBlank(commodity.getContent()) || null == commodity.getPrice()
				|| null == commodity.getBiPrice() || null == commodity.getDianBi()
				|| null == commodity.getImageResourceId()) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		double price = commodity.getPrice();
		double biPrice = commodity.getBiPrice() + commodity.getDianBi();
		if (price != biPrice) {
			Message message = new Message();
			message.setErrno(Code.CNUM003);
			message.setErrmsg("必须:价格=点币+点币价");
			return Result.setData(false, message);
		}
		JSONObject jo = new JSONObject();
		// 保存
		Commodity dbReturnReturnCommodity = null;
		List<CommodityBoxPortCateRel> returnRelList = null;
		try {
			if (null != commodity.getCommodityId()) {// 修改
				Commodity dbReturnCommodity = commodityRepository.findOne(commodity.getCommodityId());
				if (null != dbReturnCommodity) {
					BeanUtils.copyProperties(dbReturnCommodity, commodity);
					dbReturnCommodity.setImageResource(imageResourceRepository.findOne(commodity.getImageResourceId()));
					dbReturnCommodity.setCommodityCate(commodityCateRepository.findOne(commodity.getCommodityCateId()));
					dbReturnReturnCommodity = commodityService.saveCommodity(dbReturnCommodity);
					jo = JSONText.JavaBeanToJsonObject(dbReturnReturnCommodity);
				}

				List<CommodityBoxPortCateRel> cbpcList = new ArrayList<CommodityBoxPortCateRel>();
				// 修改商品存储分类关系
				Query<CommodityBoxPortCateRel> query = new Query<CommodityBoxPortCateRel>();
				query.add(Select.eq("commodity", dbReturnCommodity.getCommodityId()));
				List<CommodityBoxPortCateRel> commodityBoxPortCateRelList = commodityBoxPortCateRelRepository
						.findAll(query);

				// 数据库状态
				List<Long> dbLong = new ArrayList<>();
				if (CollectionUtils.isNotEmpty(commodityBoxPortCateRelList)) {
					for (CommodityBoxPortCateRel c : commodityBoxPortCateRelList) {
						dbLong.add(c.getBoxPortCateId());
					}
				}

				// 最新状态
				List<Long> nowlong = new ArrayList<Long>();
				if (StringUtils.isNotBlank(bocCate)) {
					String[] strArray = bocCate.split(",");
					for (String string : strArray) {
						if (StringUtils.isNotBlank(string)) {
							nowlong.add(Long.valueOf(string));
						}
					}
				}

				// 去最新状态与数据库状态差集
				nowlong.removeAll(dbLong);
				if (CollectionUtils.isNotEmpty(nowlong)) {
					for (Long long1 : nowlong) {
						CommodityBoxPortCateRel commodityBoxPortCateRel = new CommodityBoxPortCateRel();
						BoxPortCate boxPortCate = boxPortCateRepository.findOne(long1);
						commodityBoxPortCateRel.setBoxPortCate(boxPortCate);
						commodityBoxPortCateRel.setBoxPortCateId(long1);
						commodityBoxPortCateRel.setCommodityId(commodity.getCommodityId());
						commodityBoxPortCateRel.setCommodity(commodity);
						cbpcList.add(commodityBoxPortCateRel);
					}
					returnRelList = commodityBoxPortCateRelService.saveCommodityBoxPortCateRelList(cbpcList);
				}

				// 库存状态与最近状态的差集
				dbLong.removeAll(nowlong);
				if (CollectionUtils.isNotEmpty(dbLong)) {
					for (Long long1 : nowlong) {
						for (CommodityBoxPortCateRel d : commodityBoxPortCateRelList) {
							if (long1.equals(d.getBoxPortCateId())) {
								commodityBoxPortCateRelRepository.delete(d);
							}
						}
					}

				}

			} else {// 新增

				commodity.setCreateTime(new Date().getTime());
				commodity.setEnabled(IsDeleteEnum.UN_DELETE.getType());
				commodity.setCommodityCate(commodityCateRepository.findOne(commodity.getCommodityCateId()));
				commodity.setImageResource(imageResourceRepository.findOne(commodity.getImageResourceId()));
				dbReturnReturnCommodity = commodityService.saveCommodity(commodity);
				jo = JSONText.JavaBeanToJsonObject(dbReturnReturnCommodity);
				if (null != dbReturnReturnCommodity) {

					if (StringUtils.isNotBlank(bocCate)) {
						// 添加商品制冷制热常温类型分类
						String[] strArray = bocCate.split(",");
						List<CommodityBoxPortCateRel> relList = new ArrayList<CommodityBoxPortCateRel>();
						for (String string : strArray) {
							if (StringUtils.isNotBlank(string)) {
								CommodityBoxPortCateRel commodityBoxPortCateRel = new CommodityBoxPortCateRel();
								BoxPortCate boxPortCate = boxPortCateRepository.findOne(Long.valueOf(string));
								commodityBoxPortCateRel.setBoxPortCate(boxPortCate);
								commodityBoxPortCateRel.setBoxPortCateId(Long.valueOf(string));
								commodityBoxPortCateRel.setCommodityId(commodity.getCommodityId());
								commodityBoxPortCateRel.setCommodity(commodity);
								relList.add(commodityBoxPortCateRel);
							}
						}

						returnRelList = commodityBoxPortCateRelService.saveCommodityBoxPortCateRelList(relList);
					}

				}
			}

			// 发送到柜机客户端
			if (null != dbReturnReturnCommodity) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("commodity", dbReturnReturnCommodity);
				map.put("commodityBoxPortCateRelList", returnRelList);
				// 发送MQ到所有柜机
				List<String> boxCodeList = getAllBoxCode();
				if (CollectionUtils.isNotEmpty(boxCodeList)) {
					for (String string : boxCodeList) {
						if (StringUtils.isNotBlank(string)) {
							List<Long> commodityIdList = getBoxCommodityIdList(string);
							if (commodityIdList.contains(dbReturnReturnCommodity.getCommodityId())) {
								Request bind = new Request();
								bind.setDataType(DataType.COMMODITY);
								bind.setDevice(string);
								bind.setData(Request.Object2Bytes(map));
								mQttUtil.sendP2P(bind);
							}
						}

					}

				}
			}

		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}
		return Result.setData(true, jo);
	}

	public List<CommodityCate> findCommodityCateList() {
		List<CommodityCate> list = commodityCateRepository.findAll();
		return list;
	}

	// 查询售货机下有的商品id
	public List<Long> getBoxCommodityIdList(String boxCode) {

		List<Long> list = commodityRepository.getBoxCommodityIdList(boxCode);
	
		List<Long> idList = new ArrayList<Long>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Long long1 : list) {
				idList.add(long1);
			}
		}
		return idList;
	}

	// 查询所有柜机的boxcode
	public List<String> getAllBoxCode() {
		List<String> list = new ArrayList<String>();
		List<Box> boxList = boxRepository.findAll();
		if (CollectionUtils.isNotEmpty(boxList)) {
			for (Box box : boxList) {
				list.add(box.getBoxCode());
			}
		}
		return list;
	}

	// -------------------------------------------------------------------------------------------------

	/**
	 * 查询商品列表
	 * 
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping(value = "/findCommodity")
	@ResponseBody
	public Result findCommodity(Integer page, Integer rows, Short isEnabled, Long classification,
			String commodityName) {
		// 参数判断
		if (null == page || null == rows) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		JSONArray js = new JSONArray();
		// 查询商品
		Page<Commodity> pages = null;
		try {
			Query<Commodity> query = new Query<Commodity>();
			if (null != isEnabled) {
				query.add(Select.eq("enabled", isEnabled));
			}
			if (null != classification) {
				query.add(Select.eq("commodityCate", classification));
			}
			if (StringUtils.isNotBlank(commodityName)) {
				query.add(Select.eq("commodityName", commodityName));
			}
			Pageable pageable = new PageRequest(page - 1, rows);
			pages = commodityRepository.findAll(query, pageable);
			if (CollectionUtils.isNotEmpty(pages.getContent())) {
				for (Commodity commodity : pages.getContent()) {
					// 加载分类名称
					CommodityDTO copyCommodityDTO = new CommodityDTO();
					BeanUtils.copyProperties(copyCommodityDTO, commodity);
					// 分类名称
					copyCommodityDTO.setClassificationStr(commodity.getCommodityCate().getCommodityCateName());
					copyCommodityDTO
							.setIsDeleteStr(IsDeleteEnum.getEnumByNumber(commodity.getEnabled()).getDescription());
					JSONObject jo = JSONExt.toJSON(copyCommodityDTO);
					// 图片

					jo.put("imageResource", JSONText.JavaBeanToJsonObject(
							null == commodity.getImageResource() ? null : commodity.getImageResource()));
					js.add(jo);
				}
			}
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}
		TianDianPage tp = new TianDianPage();
		tp.setContent(js);
		tp.setCurrentPage(pages.getNumber());
		tp.setPageCount(pages.getTotalPages());
		tp.setTotal(pages.getTotalElements());

		return Result.setData(true, tp);
	}

	// --------------------------------------------------------------------------------
	/**
	 * 查询单个商品信息
	 * 
	 * @param commodityId
	 * @return
	 */
	@RequestMapping(value = "/getOneCommodity", method = RequestMethod.POST)
	@ResponseBody
	public Result getOneCommodity(Long commodityId) {
		if (null == commodityId) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		JSONObject jo = null;
		CommodityDTO dbCommodityDTO = new CommodityDTO();
		try {
			Commodity dbCommodity = commodityRepository.findOne(commodityId);
			if (null != dbCommodity) {
				BeanUtils.copyProperties(dbCommodityDTO, dbCommodity);
				List<CommodityRes> commodityResList = dbCommodity.getCommodityResList();
				List<ImageResource> imageResourceList = new ArrayList<ImageResource>();
				if (CollectionUtils.isNotEmpty(commodityResList)) {
					for (CommodityRes commodityRes : commodityResList) {
						imageResourceList.add(commodityRes.getImageResource());
					}
				}
				dbCommodityDTO.setClassificationStr(dbCommodity.getCommodityCate().getCommodityCateName());
				dbCommodityDTO.setImageResources(imageResourceList);
				jo = JSONExt.toJSON(dbCommodityDTO);
			}
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}
		return Result.setData(true, jo);
	}

	// ---------------------------------------------------------------------------------------------------

	/**
	 * 删除商品
	 * 
	 * @param commodity_id
	 * @return
	 */
	@RequestMapping(value = "/deleteCommodity", method = RequestMethod.POST)
	@ResponseBody
	public Result deleteCommodity(Long commodity_id) {
		// 参数判断
		if (null == commodity_id || commodity_id < 0) {
			return Result.setData(false, Code.getMessage(Code.CNUM003));
		}
		boolean bool = false;
		try {
			Commodity commodity = commodityRepository.findOne(commodity_id);
			if (null != commodity) {
				commodity.setEnabled(IsDeleteEnum.DELETE.getType());
				Commodity dbReturnCommodity = commodityService.saveCommodity(commodity);
				if (null != dbReturnCommodity) {
					bool = true;

					// 发送MQ到所有柜机
					List<String> boxCodeList = getAllBoxCode();
					if (CollectionUtils.isNotEmpty(boxCodeList)) {
						for (String string : boxCodeList) {
							List<Long> commodityIdList = getBoxCommodityIdList(string);
							if (commodityIdList.contains(dbReturnCommodity.getCommodityId())) {
								Request bind = new Request();
								bind.setDataType(DataType.COMMODITY);
								bind.setDevice(string);
								bind.setData(Request.Object2Bytes(dbReturnCommodity));
								mQttUtil.sendP2P(bind);
							}
						}

					}
				}

			}
		} catch (Exception e) {
			return Result.setData(false, Code.getMessage(Code.CNUM001));
		}

		return Result.setData(true, bool);
	}

}
