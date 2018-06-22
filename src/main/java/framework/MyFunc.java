package framework;

public class MyFunc {
	
	public static void main(String[] args) {
		System.out.println(ByteArrToHex("1".getBytes()));
	}
	
	public static int isOdd(int num) {
		return num & 1;
	}

	public static int HexToInt(String inHex) {
		return Integer.parseInt(inHex, 16);
	}

	public static byte HexToByte(String inHex) {
		return (byte) Integer.parseInt(inHex, 16);
	}

	public static String Byte2Hex(Byte inByte) {
		return String.format("%02x", new Object[] { inByte }).toUpperCase();
	}

	public static String ByteArrToHex(byte[] inBytArr) {
		StringBuilder strBuilder = new StringBuilder();
		for (byte valueOf : inBytArr) {
			strBuilder.append(Byte2Hex(Byte.valueOf(valueOf)));
		}
		return strBuilder.toString();
	}

	public static String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {
		StringBuilder strBuilder = new StringBuilder();
		int j = byteCount;
		for (int i = offset; i < j; i++) {
			strBuilder.append(Byte2Hex(Byte.valueOf(inBytArr[i])));
		}
		return strBuilder.toString();
	}

	public static byte[] HexToByteArr(String inHex) {
		byte[] result;
		int hexlen = inHex.length();
		if (isOdd(hexlen) == 1) {
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {
			result = new byte[(hexlen / 2)];
		}
		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = HexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}
}
