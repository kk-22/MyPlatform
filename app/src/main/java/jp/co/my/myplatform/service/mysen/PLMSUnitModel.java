package jp.co.my.myplatform.service.mysen;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.model.PLBaseModel;

public class PLMSUnitModel extends PLBaseModel {

	int no;
	private String name;
	private String mSmallImageName;
	int hitPoint;
	int attackPoint;
	int speedPoint;
	int defensePoint;
	int magicDefensePoint;

	int groupType;
	int colorType;
	int weaponType;
	int branchType;

	public PLMSUnitModel() {
		super();
		// ダミー
		name = "ルキナ";
		mSmallImageName = "rukina.png";
	}

	public String getSmallImageName() {
		return mSmallImageName;
	}

	@Override
	public void initFromJson(JSONObject jsonObject) throws JSONException {
		no = jsonObject.getInt("no");
		name = jsonObject.getString("name");
		hitPoint = jsonObject.getInt("hit_point");
		attackPoint = jsonObject.getInt("attack_point");
		speedPoint = jsonObject.getInt("speed_point");
		defensePoint = jsonObject.getInt("defense_point");
		magicDefensePoint = jsonObject.getInt("magic_point");
		groupType = jsonObject.getInt("group_type");
		colorType = jsonObject.getInt("color_type");
		weaponType = jsonObject.getInt("weapon_type");
		branchType = jsonObject.getInt("branch_type");
		debugLog(null);
	}

	@Override
	public String getSheetUrl() {
		return "https://script.google.com/macros/s/AKfycby42Gh5M6Qkc30-KFfsHEncNAUhG-f2B3EhoIj44T--u7hbUoti/exec?sheet=unit";
	}

	// Debug
	public void debugLog(String message) {
		if (message == null) {
			message = "";
		}
		MYLogUtil.outputLog(" UnitModel " +no +" " +name +" " +message);
	}
}
