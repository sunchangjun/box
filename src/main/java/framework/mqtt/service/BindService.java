package framework.mqtt.service;

public interface BindService {
	/**
	 * 解析柜机发送数据,同步band
	 * @param s
	 * @param response 
	 * @return
	 */
	void SynchronizePortsCheck(String device, String response);

	void SynchronizeBox(String device, String response);
}
