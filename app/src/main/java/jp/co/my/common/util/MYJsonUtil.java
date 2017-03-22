package jp.co.my.common.util;

import org.json.JSONException;
import org.json.JSONObject;

public class MYJsonUtil {

	private MYJsonUtil() {}

	public static int parseIntIfNonNull(JSONObject jsonObject, String key) throws JSONException {
		Object obj = jsonObject.get(key);
		if (obj instanceof Integer) {
			return (int)obj;
		}
		return 0;
	}
}
