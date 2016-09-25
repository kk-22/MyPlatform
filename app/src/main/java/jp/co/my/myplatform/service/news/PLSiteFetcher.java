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
import jp.co.my.myplatform.service.core.PLVolleyHelper;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLSiteFetcher {

	private int mFinishCount;
	private ArrayList<PLNewsGroupModel> mGroupArray;
	private ArrayList<ArrayList<PLNewsSiteModel>> mSiteListArray;
	private PLSiteCallbackListener mListener;

	public PLSiteFetcher() {
		super();
	}

	public void startRequest(PLSiteCallbackListener listener) {
		mFinishCount = 0;
		mGroupArray = null;
		mSiteListArray = null;

		mListener = listener;
		fetchGroup();
		fetchSite();
	}

	public void cancelAllRequest() {
		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
		mFinishCount = 0;
		mGroupArray = null;
		mSiteListArray = null;
	}

	private void finishFetch() {
		mFinishCount++;
		if (mFinishCount < 2) {
			return;
		}
		mListener.finishedRequest(mGroupArray, mSiteListArray);
		mListener = null;
	}

	private void fetchGroup() {
		String url = "https://script.google.com/macros/s/AKfycbzI7PZcERIwZeDeE-7RXthqfQZZP4JU_T5Imcg12tOBoIdu_yVq/exec?sheet=group";
		JsonArrayRequest request = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						MYLogUtil.showToast("Fetched group");
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
				PLNewsGroupModel model = new PLNewsGroupModel();
				model.setNo(jsonObject.getInt("group_no"));
				model.setColor(jsonObject.getString("color"));
				model.setTitle(jsonObject.getString("title"));
				model.setUpdateInterval(jsonObject.getInt("update_interval"));
				model.setAutoUpdate(PLVolleyHelper.parseBoolean(jsonObject, "auto_update"));
				array.add(model);
			}
		} catch (JSONException e) {
			MYLogUtil.showExceptionToast(e);
		}
		return array;
	}

	private void fetchSite() {
		String url = "https://script.google.com/macros/s/AKfycbzI7PZcERIwZeDeE-7RXthqfQZZP4JU_T5Imcg12tOBoIdu_yVq/exec?sheet=site";
		JsonArrayRequest request = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						MYLogUtil.showToast("Fetched site");
						mSiteListArray = parseSite(response);
						finishFetch();
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error){
				MYLogUtil.showErrorToast("Fetch site error " + error.toString());
				finishFetch();
			}
		}
		);
		PLCoreService.getVolleyHelper().addRequest(request, this.getClass());
	}

	private ArrayList<ArrayList<PLNewsSiteModel>> parseSite(JSONArray response) {
		ArrayList<ArrayList<PLNewsSiteModel>> array = new ArrayList<>();
		try {
			for (int i = 0; i < response.length(); i++) {
				JSONObject jsonObject = response.getJSONObject(i);
				PLNewsSiteModel model = new PLNewsSiteModel();
				model.setNo(jsonObject.getInt("site_no"));
				model.setUrl(jsonObject.getString("url"));
				model.setEnableScript(PLVolleyHelper.parseBoolean(jsonObject, "script"));
				model.setEnablePCViewer(PLVolleyHelper.parseBoolean(jsonObject, "pc_viewer"));

				int groupNo = jsonObject.getInt("group_no");
				ArrayList<PLNewsSiteModel> siteArray;
				if (array.size() < groupNo) {
					siteArray = new ArrayList<>();
					array.add(siteArray);
				} else {
					siteArray = array.get(groupNo - 1);
				}
				siteArray.add(model);
			}
		} catch (JSONException e) {
			MYLogUtil.showExceptionToast(e);
		}
		return array;
	}

	public static abstract class PLSiteCallbackListener {
		public abstract void finishedRequest(ArrayList<PLNewsGroupModel> groupArray, ArrayList<ArrayList<PLNewsSiteModel>> siteListArray);
	}
}
