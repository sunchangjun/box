package framework.enums;

public enum CodeEnum {

	
		UN_DELETE(001,"启用"),
		DELETE(1,"删除"),
		;
		private Integer num;
		private String 	str;

		private CodeEnum (Integer num,String str) {
			this.num = num;
			this.str = str;
		}
		
		public Integer getType() {
			return num;
		}
		public String getDescription() {
			return str;
		}
			
		public static CodeEnum getEnumByNumber(Integer num){
			if (num == null)
	            return null;
	        for (CodeEnum codeEnum : CodeEnum.values()) {
	            if (codeEnum.getType().equals(num))
	                return codeEnum;
	        }
	        return null;
		}
		
		public static CodeEnum getEnumByDescription(String str){
			if (str == null)
	            return null;
	        for (CodeEnum codeEnum : CodeEnum.values()) {
	            if (codeEnum.getDescription().equals(str))
	                return codeEnum;
	        }
	        return null;
		}

}
