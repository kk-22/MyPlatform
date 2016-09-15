package jp.co.my.myplatform.service.core;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
}
