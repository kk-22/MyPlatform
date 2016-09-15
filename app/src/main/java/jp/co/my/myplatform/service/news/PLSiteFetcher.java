package jp.co.my.myplatform.service.news;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;

public class PLSiteFetcher {

	private int mFinishCount;
	private ArrayList<PLNewsGroupModel> mGroupArray;
	private ArrayList<PLNewsGroupModel> mSiteArray;

	public PLSiteFetcher() {
		super();
	}

	public void startRequest() {
		cancelAllRequest();
		mFinishCount = 0;

		fetchGroup();
	}

	public void cancelAllRequest() {
		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
	}

	private void finishFetch() {
		mFinishCount++;
		if (mFinishCount < 2) {
			return;
		}
	}

	private void fetchGroup() {
		String url = "https://script.google.com/macros/s/AKfycbzI7PZcERIwZeDeE-7RXthqfQZZP4JU_T5Imcg12tOBoIdu_yVq/exec?sheet=group";
		JsonArrayRequest request = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						mGroupArray = parseGroup(response);
						finishFetch();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error){
						MYLogUtil.showErrorToast("Fetch group error " + error.toString());
						finishFetch();
					}
				}
		);
		PLCoreService.getVolleyHelper().addRequest(request, this.getClass());
	}

	private ArrayList<PLNewsGroupModel> parseGroup(JSONArray response) {
		ArrayList<PLNewsGroupModel> array = new ArrayList<>();
		try {
			for (int i = 0; i < response.length(); i++) {
				JSONObject jsonObject = response.getJSONObject(i);
				int no = jsonObject.getInt("group_no");
				String color = jsonObject.getString("color");
				String title = jsonObject.getString("title");
				PLNewsGroupModel model = new PLNewsGroupModel(no, color, title);;
				array.add(model);
			}
		} catch(JSONException e) {
			MYLogUtil.showExceptionToast(e);
		}
		return array;
	}
}
