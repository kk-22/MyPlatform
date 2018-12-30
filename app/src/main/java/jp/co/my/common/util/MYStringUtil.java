package jp.co.my.common.util;

public class MYStringUtil {

	private MYStringUtil() {}

	public static String getSuffix(String fileName) {
		int point = fileName.lastIndexOf(".");
		if (point == -1) {
			return null;
		}
		return fileName.substring(point + 1);
	}

	public static boolean isImageFileName(String fileName) {
		String extension = MYStringUtil.getSuffix(fileName);
		return (extension != null &&
				(extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")));
	}

	// 2つの文字列の差分を返す。差分が無ければ空文字を返す
	public static String diffString(String string1, String string2) {
		int length1 = string1.length();
		int length2 = string2.length();
		if (length1 == length2) {
			return "";
		}
		String longString = (length1 > length2) ? string1 : string2;
		String shortString = (length1 > length2) ? string2 : string1;
		int minLength = Math.min(length1, length2);
		int maxLength = Math.max(length1, length2);
		int beginIndex = minLength;
		for (int i = 0; i < minLength; i++) {
			if (longString.charAt(i) != shortString.charAt(i)) {
				beginIndex = i;
				break;
			}
		}
		int diffLength = maxLength - minLength;
		return longString.substring(beginIndex, beginIndex + diffLength);
	}

	// 数値が0の場合は空文字、それ以外は数字文字を返す
	public static String stringFromIntegerIfIsNoZero(int value) {
		if (value == 0) {
			return "";
		}
		return String.valueOf(value);
	}
}
