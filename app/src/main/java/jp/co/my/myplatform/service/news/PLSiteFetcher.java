package jp.co.my.myplatform.service.news;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import jp.co.my.common.util.MYLogUtil;

public class PLSiteFetcher {

	private PLSiteFetcher(){}

	public static void fetchGroup(Context context) {
		String url = "https://script.google.com/macros/s/AKfycbzI7PZcERIwZeDeE-7RXthqfQZZP4JU_T5Imcg12tOBoIdu_yVq/exec?sheet=group";
		RequestQueue requestQueue = Volley.newRequestQueue(context);
		//キューにリクエストを追加
		requestQueue.add(new JsonArrayRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try{
							//Log.v("tama",response.getString("price"));
							//取得した値を表示
							MYLogUtil.outputLog(response.getString(1));
						} catch(JSONException e) {
							MYLogUtil.showExceptionToast(e);
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error){
						MYLogUtil.showErrorToast("Fetch group error " + error.toString());
					}
				}
		));
	}
}
