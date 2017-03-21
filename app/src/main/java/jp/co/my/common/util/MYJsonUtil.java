package jp.co.my.common.util;

import org.json.JSONException;
import org.json.JSONObject;

public class MYJsonUtil {

	private MYJsonUtil() {}

	public static int parseIntIfNonNull(JSONObject jsonObject, String key) throws JSONException {
		Object obj = jsonObject.get(key);
		String str = MYOtherUtil.castObject(obj, String.class);
		if (str == null || str.equals("")) {
			return 0;
		}
		return Integer.parseInt(str);
	}
}
