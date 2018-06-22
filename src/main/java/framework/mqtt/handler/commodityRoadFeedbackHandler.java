package framework.mqtt.handler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import framework.dao.OrderRepository;
import framework.entity.po.Box;
import framework.mqtt.util.IMqttHandler;
import framework.pojo.Request;
/** 
 * 货道出货反馈通知
 * @author Administrator
 *
 */
@Component("commodityRoadFeedbackHandler")
public class commodityRoadFeedbackHandler implements IMqttHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(commodityRoadFeedbackHandler.class);
	@Autowired
	OrderRepository orderRepository;

	@Override
	public void exec(Request bean) {
		String response = new String(bean.getData());
		logger.info(response);
		Map<String, Map<String, Object>> mp = JSON.parseObject(response, Map.class);
		for (Iterator<String> it = mp.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Map<String, Object> rt = mp.get(key);
			
			
		}
	}
}
