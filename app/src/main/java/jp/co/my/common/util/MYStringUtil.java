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
}
