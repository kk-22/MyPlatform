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
	private int timingType;
	@Column
	private int requirementType;
	@Column
	private int requirementValue;
	@Column
	private int targetType;
	@Column
	private int targetValue;
	@Column
	private int effectType;
	@Column
	private int effectValue;

	@Override
	public void initFromJson(JSONObject jsonObject) throws JSONException {
		no = jsonObject.getInt("no");
		name = jsonObject.getString("name");
		description = jsonObject.getString("description_text");
		slotType = jsonObject.getInt("slot");

		timingType = MYJsonUtil.parseIntIfNonNull(jsonObject, "timing_no");
		requirementType = MYJsonUtil.parseIntIfNonNull(jsonObject, "requirement_no");
		requirementValue = MYJsonUtil.parseIntIfNonNull(jsonObject, "requirement_value");
		targetType = MYJsonUtil.parseIntIfNonNull(jsonObject, "target_no");
		targetValue = MYJsonUtil.parseIntIfNonNull(jsonObject, "target_value");
		effectType = MYJsonUtil.parseIntIfNonNull(jsonObject, "effect_no");
		effectValue = MYJsonUtil.parseIntIfNonNull(jsonObject, "effect_value");
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

	public int getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(int targetValue) {
		this.targetValue = targetValue;
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
}
