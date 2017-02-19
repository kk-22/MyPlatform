package jp.co.my.myplatform.service.model;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;

public class PLModelFetchTask<T extends PLBaseModel> extends AsyncTask<Void, Void, ArrayList<PLBaseModel>> {

	private Class<T> mKlass;
	private PLModelFetchTaskListener mListener;

	public PLModelFetchTask(Class<T> klass, PLModelFetchTaskListener listener) {
		super();
		mKlass = klass;
		mListener = listener;
	}

	@Override
	protected ArrayList<PLBaseModel> doInBackground(Void... params) {
		PLBaseModel dummyModel = createModel();
		if (dummyModel == null) {
			MYLogUtil.showErrorToast("con't create instance. class=" +mKlass.getName());
			return null;
		}
		String urlStr = dummyModel.getSheetUrl();
		if (urlStr == null) {
			MYLogUtil.showErrorToast("url is null. class=" +mKlass.getName());
			return null;
		}
		ByteArrayOutputStream responseArray = fetchJsonData(urlStr);
		ArrayList<PLBaseModel> modelArray = createModelArray(responseArray);
		PLDatabase.saveModelList(modelArray, true);
		return modelArray;
	}

	@Override
	protected void onPostExecute(ArrayList<PLBaseModel> modelArray) {
		mListener.finishedFetchModels(modelArray);
	}

	private PLBaseModel createModel() {
		try {
			String className = mKlass.getName();
			return (PLBaseModel) Class.forName(className).getConstructor().newInstance();
		} catch (Exception e) {
			MYLogUtil.showExceptionToast(e);
			return null;
		}
	}

	public ByteArrayOutputStream fetchJsonData(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.connect();
			if(connection.getResponseCode() != 200) {
				MYLogUtil.showErrorToast("モデル取得通信に失敗 class=" +mKlass.getName());
				return null;
			}
			BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream responseArray = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int length;
			while((length = inputStream.read(buff)) != -1) {
				if(length > 0) {
					responseArray.write(buff, 0, length);
				}
			}
			return responseArray;
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
			return null;
		}
	}

	private ArrayList<PLBaseModel> createModelArray(ByteArrayOutputStream responseArray) {
		try {
			ArrayList<PLBaseModel> modelArray = new ArrayList<>();
			JSONArray jsonArray = new JSONArray(new String(responseArray.toByteArray()));
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.get("no") instanceof String) {
					// 未入力セル
					continue;
				}

				PLBaseModel model = createModel();
				if (model == null) {
					return null;
				}
				model.initFromJson(jsonObject);
				modelArray.add(model);
			}
			return modelArray;
		} catch (JSONException e) {
			MYLogUtil.showExceptionToast(e);
			return null;
		}
	}

	public interface PLModelFetchTaskListener {
		void finishedFetchModels(ArrayList<PLBaseModel> modelArray);
	}
}
