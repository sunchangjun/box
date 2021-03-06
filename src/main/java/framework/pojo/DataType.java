package framework.pojo;

public enum DataType {
	BIND(1, "bindHandler"),  //注册
	BINDBACK(5, "bindBackHandler"),  //注册
	COMMODITYROAD(2, "commodityRoadHandler"),//货道同步
	COMMODITY(3, "commodityHandler"),//商品同步
	COMMODITYROAD_FEEDBACK(4, "commodityRoadFeedbackHandler"),//货道出货反馈
	CONTROL_INSTRUCTION(6, "controlInstructionHandler"),//控制指令,出货,控制温度等
	CHANGEBOXPORTCATE(8,"changeBoxPortCateHandler"),
	SAVECOMMODITYNUMCHANGED(9,"saveCommodityNumChangedHandler"),
	PORTSCHECK(7, "portsCheckHandler");
	
	private String handler;
	private int type;

	DataType(int type, String handler) {
		this.type = type;
		this.handler = handler;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
