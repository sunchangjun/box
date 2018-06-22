package framework.mqtt.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import framework.mqtt.util.IMqttHandler;
import framework.pojo.Request;

@Component
public class SaveCommodityNumChangedHandler implements IMqttHandler {
	@Override
	public void exec(Request bean) {
		String rep = new String(bean.getData());
		JSONObject jparam = JSON.parseObject(rep);
		Long boxId = jparam.getLong("boxId");
		Long boxPortId = jparam.getLong("boxPortId");
		JSONArray ret = jparam.getJSONArray("ret");
		System.out.println(boxId);
		System.out.println(boxPortId);
		System.out.println(ret);
	}
}
