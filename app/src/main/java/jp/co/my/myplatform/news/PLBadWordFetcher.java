package jp.co.my.myplatform.news;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.core.PLCoreService;

public class PLBadWordFetcher {

	private PLWordCallbackListener mListener;

	private ArrayList<PLBadWordModel> mWordArray;

	public PLBadWordFetcher() {
		super();
	}

	public void startRequest(PLWordCallbackListener listener) {
		mListener = listener;
		fetchWord();
	}

	public void cancelAllRequest() {
		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
	}

	private void fetchWord() {
		String url = "https://script.google.com/macros/s/AKfycbzI7PZcERIwZeDeE-7RXthqfQZZP4JU_T5Imcg12tOBoIdu_yVq/exec?sheet=bad_word";
		JsonArrayRequest request = new JsonArrayRequest(url,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						finishFetch(parseWord(response));
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error){
				MYLogUtil.showErrorToast("Fetch word error " + error.toString());
				finishFetch(null);
			}
		}
		);
		PLCoreService.getVolleyHelper().addRequest(request, this.getClass());
	}

	private ArrayList<ArrayList<PLBadWordModel>> parseWord(JSONArray response) {
		ArrayList<ArrayList<PLBadWordModel>> array = new ArrayList<>();
		try {
			for (int i = 0; i < response.length(); i++) {
				JSONObject jsonObject = response.getJSONObject(i);
				PLBadWordModel model = new PLBadWordModel();
				model.setNo(jsonObject.getInt("word_no"));
				model.setWord(jsonObject.getString("word"));

				int groupNo = jsonObject.getInt("group_no");
				ArrayList<PLBadWordModel> siteArray;
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

	private void finishFetch(ArrayList<ArrayList<PLBadWordModel>> wordListArray) {
		mListener.finishedBadWordRequest(wordListArray);
		mListener = null;
	}

	public static abstract class PLWordCallbackListener {
		public abstract void finishedBadWordRequest(ArrayList<ArrayList<PLBadWordModel>> wordListArray);
	}
}
