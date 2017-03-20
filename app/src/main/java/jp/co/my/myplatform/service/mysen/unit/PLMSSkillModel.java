package jp.co.my.myplatform.service.mysen.unit;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import jp.co.my.myplatform.service.model.PLBaseModel;
import jp.co.my.myplatform.service.model.PLDatabase;

@Table(database = PLDatabase.class)
public class PLMSSkillModel extends PLBaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;
	@Column
	private String name;
	@Column
	private String description;

	@Column
	private int timingType;
	@Column
	private int requirementType;
	@Column
	private int requirementValue;
	@Column
	private int scopeType;
	@Column
	private int scopeValue;
	@Column
	private int effectType;
	@Column
	private int effectValue;

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

	public int getScopeType() {
		return scopeType;
	}

	public void setScopeType(int scopeType) {
		this.scopeType = scopeType;
	}

	public int getScopeValue() {
		return scopeValue;
	}

	public void setScopeValue(int scopeValue) {
		this.scopeValue = scopeValue;
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
