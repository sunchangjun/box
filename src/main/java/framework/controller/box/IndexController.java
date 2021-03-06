package framework.controller.box;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.tiand99.util.EncryptUtils;
import com.tiand99.util.EncryptUtils.AESMode;

import framework.box.Data;
import framework.box.ResponseData;
import framework.core.web.BaseController;
import framework.dao.BoxPortCateRepository;
import framework.dao.BoxPortRepository;
import framework.dao.BoxRepository;
import framework.dao.CommodityRepository;
import framework.dao.CommodityRoadRepository;
import framework.dao.ImageResourceRepository;
import framework.entity.po.Box;
import framework.entity.po.Commodity;
import framework.entity.po.ImageResource;
import framework.mqtt.MQttUtil;
import framework.mqtt.util.MacSignature;
import framework.pojo.DataType;
import framework.pojo.Request;

@Controller
@RequestMapping("/v1")
public class IndexController extends BaseController {

	public static String secKey = "c3OTyn6!csmMu2lv";

	@Value("${framework.mq.broker}")
	String broker;

	@Value("${framework.mq.AccessKey}")
	String acessKey;

	@Value("${framework.mq.SecretKey}")
	String secretKey;

	@Value("${framework.mq.topic}")
	String topic;

	@Value("${framework.mq.groupID}")
	String groupID;

	@Value("${framework.mq.SERVERID}")
	String serverGroupID;

	@Autowired
	MQttUtil mQttUtil;
	@Autowired
	BoxPortCateRepository boxPortCateRepository;
	@Autowired
	BoxPortRepository boxPortRepository;
	@Autowired
	BoxRepository boxRepository;
	@Autowired
	CommodityRoadRepository commodityRoadRepository;
	@Autowired
	ImageResourceRepository imageResourceRepository;
	
	
	@Autowired
	CommodityRepository commodityRepository;

	@RequestMapping(value = { "/registerCab" })
	@ResponseBody
	public ResponseData registerCab(String context) {
		byte[] ot = Base64.getDecoder().decode(context);
		byte[] ots = EncryptUtils.AESDecode(ot, secKey.getBytes(), AESMode.AES_ECB_NoPadding);
		String names = new String(ots);
		String[] name = names.split("\\|");
		String deviceName = name[0];
		Box box = boxRepository.findBoxbyCode_name(deviceName);
		System.out.println(box);
		ResponseData ret = new ResponseData();
		if(box==null) {
			ret.setSuccess(false);
			return ret;
		}
		System.out.println(box);
		ret.setSuccess(true);
		ret.setStatus(0);
		Data data = new Data();
		data.setDeviceName(box.getBoxCode());
		data.setDpk(box.getBoxId().intValue());
		ret.setData(data);
		ret.setResult("连接成功");
		return ret;
	}

	@RequestMapping(value = { "/auth" })
	@ResponseBody
	public ResponseData auth(String deviceName, String deviceId, String devicePrimaryKey, String $key) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("deviceName", deviceName);
		params.put("deviceId", deviceId);
		params.put("devicePrimaryKey", devicePrimaryKey);
		try {
			String _key = MacSignature.doHttpSignature(params, secKey);
			if (_key.equals($key)) {
				ResponseData ret = new ResponseData();
				ret.setSuccess(true);
				ret.setStatus(0);
				Data data = new Data();
				data.setDeviceName(deviceName);
				data.setBroker(broker);
				data.setAcessKey(acessKey);
				data.setSecretKey(secretKey);
				data.setServerGroupID(serverGroupID);
				data.setTopic(topic);
				data.setGroupID(groupID);
				ret.setData(data);
				return ret;
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		ResponseData ret = new ResponseData();
		ret.setSuccess(false);
		ret.setStatus(0);
		return ret;

	}

	@RequestMapping(value = { "/tst" })
	@ResponseBody
	public void tst() {
		Request bind = new Request();
		bind.setDataType(DataType.BIND);
		bind.setDevice("DLA364");
		bind.setData(new String("").getBytes());
		mQttUtil.sendP2P(bind);
	}

	@RequestMapping(value = { "/tst1" })
	@ResponseBody
	public void tst1(String device) {
		Map<String, Object> rt = new HashMap<String, Object>();
		Box box = boxRepository.findByBoxCode(device);
		rt.put("box", box);
		rt.put("boxPort", box.getBoxPorts());
		rt.put("commodityRoad", box.getCommodityRoads());
		rt.put("boxPortCate", boxPortCateRepository.findAll());
		Request bind = new Request();
		bind.setDataType(DataType.BINDBACK);
		bind.setDevice(device);
		bind.setData(Request.Object2Bytes(rt));
		mQttUtil.sendP2P(bind);
	}
	
	
	
	
	@RequestMapping(value = { "/box" })
	@ResponseBody
	public void box() {
	//	List<Commodity> list = commodityRepository.findAllByDevice("DLA364");
		List<ImageResource> imageList = imageResourceRepository.findAllByDevice("DLA364");
		System.out.println(JSON.toJSONString(imageList));
	}
	
	
	
	
	
	
}
