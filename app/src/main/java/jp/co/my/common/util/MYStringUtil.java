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
}
