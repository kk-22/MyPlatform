package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class PLBaseModel extends BaseModel {

	public PLBaseModel() {
		super();
	}

	public String getSheetUrl() {
		return null;
	}

	public void initFromJson(JSONObject jsonObject) throws JSONException {
	}

}
