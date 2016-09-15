package jp.co.my.myplatform.service.core;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class PLVolleyHelper {

	private RequestQueue mRequestQueue;

	public PLVolleyHelper(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
	}

	public void addRequest(Request request, Class klass) {
		request.setTag(klass.getName());
		mRequestQueue.add(request);
	}

	public void cancelRequest(Class klass) {
		mRequestQueue.cancelAll(klass.getName());
	}

	public void destroyRequest() {
		mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
			@Override
			public boolean apply(Request<?> request) {
				return true;
			}
		});
	}

	public static boolean parseBoolean(JSONObject jsonObject,String name) throws JSONException {
		// 空文字のみfalseとする
		String value = jsonObject.getString(name);
		return (value != null && value.length() > 0);
	}
}
