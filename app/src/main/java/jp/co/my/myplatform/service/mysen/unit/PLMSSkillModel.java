package jp.co.my.myplatform.service.mysen.unit;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.my.common.util.MYJsonUtil;
import jp.co.my.myplatform.service.model.PLBaseModel;
import jp.co.my.myplatform.service.model.PLDatabase;

@ModelContainer
@Table(database = PLDatabase.class)
public class PLMSSkillModel extends PLBaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;
	@Column
	private String name;
	@Column
	private String description;
	@Column
	private int slotType;

	@Column
	private int timingType; //　発動タイミング
	@Column
	private int requirementType; // 条件種類
	@Column
	private int requirementValue; // 条件値
	@Column
	private int targetType; // 効果範囲の種類
	@Column
	private int targetRange; // 効果範囲の値
	@Column
	private int targetWeapon; // 効果対象の武器
	@Column
	private int targetBranch; // 効果対象の兵科
	@Column
	private int effectType; // 効果内容
	@Column
	private int effectValue; // 効果の値
	@Column
	private int effectSubValue; // 効果の値2
	@Column
	private int statusType;	// 効果対象のステータス
	@Column
	private int weakness; // 特効

	@Override
	public void initFromJson(JSONObject jsonObject) throws JSONException {
		no = jsonObject.getInt("no");
		name = jsonObject.getString("name");
		description = jsonObject.getString("description_text");
		slotType = MYJsonUtil.parseIntIfNonNull(jsonObject, "slot");

		timingType = MYJsonUtil.parseIntIfNonNull(jsonObject, "timing_no");
		requirementType = MYJsonUtil.parseIntIfNonNull(jsonObject, "requirement_no");
		requirementValue = MYJsonUtil.parseIntIfNonNull(jsonObject, "requirement_value");
		targetType = MYJsonUtil.parseIntIfNonNull(jsonObject, "target_no");
		targetRange = MYJsonUtil.parseIntIfNonNull(jsonObject, "target_range");
		targetWeapon = MYJsonUtil.parseIntIfNonNull(jsonObject, "target_weapon");
		targetBranch = MYJsonUtil.parseIntIfNonNull(jsonObject, "target_branch");
		effectType = MYJsonUtil.parseIntIfNonNull(jsonObject, "effect_no");
		effectValue = MYJsonUtil.parseIntIfNonNull(jsonObject, "effect_value");
		effectSubValue = MYJsonUtil.parseIntIfNonNull(jsonObject, "effect_sub_value");
		statusType = MYJsonUtil.parseIntIfNonNull(jsonObject, "status_no");
		weakness = MYJsonUtil.parseIntIfNonNull(jsonObject, "weakness");
	}

	@Override
	public String getSheetUrl() {
		return "https://script.google.com/macros/s/AKfycby42Gh5M6Qkc30-KFfsHEncNAUhG-f2B3EhoIj44T--u7hbUoti/exec?sheet=skill";
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSlotType() {
		return slotType;
	}

	public void setSlotType(int slotType) {
		this.slotType = slotType;
	}

	public int getTimingType() {
		return timingType;
	}

	public void setTimingType(int timingType) {
		this.timingType = timingType;
	}

	public int getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(int requirementType) {
		this.requirementType = requirementType;
	}

	public int getRequirementValue() {
		return requirementValue;
	}

	public void setRequirementValue(int requirementValue) {
		this.requirementValue = requirementValue;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getTargetRange() {
		return targetRange;
	}

	public void setTargetRange(int targetRange) {
		this.targetRange = targetRange;
	}

	public int getTargetWeapon() {
		return targetWeapon;
	}

	public void setTargetWeapon(int targetWeapon) {
		this.targetWeapon = targetWeapon;
	}

	public int getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(int targetBranch) {
		this.targetBranch = targetBranch;
	}

	public int getEffectType() {
		return effectType;
	}

	public void setEffectType(int effectType) {
		this.effectType = effectType;
	}

	public int getEffectValue() {
		return effectValue;
	}

	public void setEffectValue(int effectValue) {
		this.effectValue = effectValue;
	}

	public int getEffectSubValue() {
		return effectSubValue;
	}

	public void setEffectSubValue(int effectSubValue) {
		this.effectSubValue = effectSubValue;
	}

	public int getStatusType() {
		return statusType;
	}

	public void setStatusType(int statusType) {
		this.statusType = statusType;
	}

	public int getWeakness() {
		return weakness;
	}

	public void setWeakness(int weakness) {
		this.weakness = weakness;
	}
}
