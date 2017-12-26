package jp.co.my.myplatform.news;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

/**
 * VolleyライブラリXML用リクエストクラス
 * base from http://y-anz-m.blogspot.jp/2013/07/volley-xml.html
 * custom priority  from http://www.vagrantup.jp/entry/2014/01/01/224849
 */
public class PLInputStreamRequest extends Request<InputStream> {

	private final Listener<InputStream> mListener;
	private Priority mPriority;

	public PLInputStreamRequest(int method, String url,
								Listener<InputStream> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
		mPriority = Priority.NORMAL;
	}

	public PLInputStreamRequest(String url, Listener<InputStream> listener, ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}

	@Override
	protected void deliverResponse(InputStream response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<InputStream> parseNetworkResponse(NetworkResponse response) {
		InputStream inputStream = new ByteArrayInputStream(response.data);
		return Response.success(inputStream, HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	public Priority getPriority() {
		return mPriority;
	}

	public void setPriority(Priority priority) {
		this.mPriority = priority;
	}
}