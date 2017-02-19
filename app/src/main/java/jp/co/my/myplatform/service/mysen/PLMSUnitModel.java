package jp.co.my.myplatform.service.mysen;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.model.PLBaseModel;
import jp.co.my.myplatform.service.model.PLDatabase;

@Table(database = PLDatabase.class)
public class PLMSUnitModel extends PLBaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;
	@Column
	private String name;
	@Column
	private int hitPoint;
	@Column
	private int attackPoint;
	@Column
	private int speedPoint;
	@Column
	private int defensePoint;
	@Column
	private int magicDefensePoint;
	@Column
	private int totalPoint;

	@Column
	private int groupType;
	@Column
	private int colorType;
	@Column
	private int weaponType;
	@Column
	private int branchType;

	public PLMSUnitModel() {
		super();
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
		totalPoint = jsonObject.getInt("total_point");
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

	// getter and setter

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getHitPoint() {
		return hitPoint;
	}

	public void setHitPoint(int hitPoint) {
		this.hitPoint = hitPoint;
	}

	public int getAttackPoint() {
		return attackPoint;
	}

	public void setAttackPoint(int attackPoint) {
		this.attackPoint = attackPoint;
	}

	public int getSpeedPoint() {
		return speedPoint;
	}

	public void setSpeedPoint(int speedPoint) {
		this.speedPoint = speedPoint;
	}

	public int getDefensePoint() {
		return defensePoint;
	}

	public void setDefensePoint(int defensePoint) {
		this.defensePoint = defensePoint;
	}

	public int getMagicDefensePoint() {
		return magicDefensePoint;
	}

	public void setMagicDefensePoint(int magicDefensePoint) {
		this.magicDefensePoint = magicDefensePoint;
	}

	public int getTotalPoint() {
		return totalPoint;
	}

	public void setTotalPoint(int totalPoint) {
		this.totalPoint = totalPoint;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public int getColorType() {
		return colorType;
	}

	public void setColorType(int colorType) {
		this.colorType = colorType;
	}

	public int getWeaponType() {
		return weaponType;
	}

	public void setWeaponType(int weaponType) {
		this.weaponType = weaponType;
	}

	public int getBranchType() {
		return branchType;
	}

	public void setBranchType(int branchType) {
		this.branchType = branchType;
	}
}
