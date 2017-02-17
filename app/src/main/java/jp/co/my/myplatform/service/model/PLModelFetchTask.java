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

public class PLModelFetchTask<T extends PLBaseModel> extends AsyncTask<Void, Void, ArrayList<T>> {

	private Class<T> mKlass;

	public PLModelFetchTask(Class<T> klass) {
		super();
		mKlass = klass;
	}

	@Override
	protected ArrayList<T> doInBackground(Void... params) {
		T dummyModel = createModel();
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
		return createModelArray(responseArray);
	}

	@Override
	protected void onPostExecute(ArrayList<T> modelArray) {

	}

	private T createModel() {
		try {
			String className = mKlass.getName();
			return (T) Class.forName(className).getConstructor().newInstance();
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

	private ArrayList<T> createModelArray(ByteArrayOutputStream responseArray) {
		try {
			ArrayList<T> modelArray = new ArrayList<>();
			JSONArray jsonArray = new JSONArray(new String(responseArray.toByteArray()));
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				// TODO: Execute break if id is null
				T model = createModel();
				model.initFromJson(jsonObject);
			}
			return modelArray;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public interface PLModelFetchTaskListener<T> {
		void finishedFetchModels(ArrayList<T> modelArray);
	}
}
