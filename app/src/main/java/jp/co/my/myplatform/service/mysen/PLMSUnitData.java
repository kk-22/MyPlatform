package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillModel;

public class PLMSUnitData {

	private PLMSUnitModel mUnitModel;
	private Point mFirstPoint;				// 初期位置
	private PLMSArmyStrategy mArmyStrategy;

	private PLMSColorData mColor;
	private PLMSWeaponData mWeapon;
	private PLMSBranchData mBranch;
	private PLMSSkillData mSupportSkill;
	private PLMSSkillData mSecretSkill;
	private PLMSSkillData mPassiveASkill;
	private PLMSSkillData mPassiveBSkill;
	private PLMSSkillData mPassiveCSkill;

	private int mMaxHP;
	private int mCurrentHP;
	private int mCurrentAttack;
	private int mCurrentSpeed;
	private int mCurrentDefense;
	private int mCurrentMagicDefense;

	private int mMoveCount;					// 同一ターン内での移動回数

	public PLMSUnitData(PLMSUnitModel unitModel, Point firstPoint, PLMSArmyStrategy armyStrategy) {
		mUnitModel = unitModel;
		mFirstPoint = firstPoint;
		mArmyStrategy = armyStrategy;

		mColor = new PLMSColorData((mUnitModel.getColorType()));
		mWeapon = new PLMSWeaponData(mUnitModel.getWeaponType());
		mBranch = new PLMSBranchData(mUnitModel.getBranchType());
		mSupportSkill = createSkillData(mUnitModel.getSupportSkillForeign());
		mSecretSkill = createSkillData(mUnitModel.getSecretSkillForeign());
		mPassiveASkill = createSkillData(mUnitModel.getPassiveASkillForeign());
		mPassiveBSkill = createSkillData(mUnitModel.getPassiveBSkillForeign());
		mPassiveCSkill = createSkillData(mUnitModel.getPassiveCSkillForeign());

		resetAllStatus();
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mUnitModel.getNo() +".png";
	}

	public void resetAllStatus() {
		mMaxHP = mUnitModel.getHitPoint();
		mCurrentHP = mMaxHP;
		mCurrentAttack = mUnitModel.getAttackPoint();
		mCurrentSpeed = mUnitModel.getSpeedPoint();
		mCurrentDefense = mUnitModel.getDefensePoint();
		mCurrentMagicDefense = mUnitModel.getMagicDefensePoint();

		mMoveCount = 0;
	}

	public PLMSSkillData createSkillData(ForeignKeyContainer<PLMSSkillModel> foreign) {
		if (foreign == null) {
			return new PLMSSkillData(null);
		}
		return new PLMSSkillData(foreign.load());
	}

	// getter
	public PLMSUnitModel getUnitModel() {
		return mUnitModel;
	}

	public PLMSBranchData getBranch() {
		return mBranch;
	}

	public int getMaxHP() {
		return mMaxHP;
	}

	public int getCurrentHP() {
		return mCurrentHP;
	}

	public int getCurrentAttack() {
		return mCurrentAttack;
	}

	public int getCurrentSpeed() {
		return mCurrentSpeed;
	}

	public int getCurrentDefense() {
		return mCurrentDefense;
	}

	public int getCurrentMagicDefense() {
		return mCurrentMagicDefense;
	}

	public PLMSArmyStrategy getArmyStrategy() {
		return mArmyStrategy;
	}

	public int getMoveCount() {
		return mMoveCount;
	}

	public Point getFirstPoint() {
		return mFirstPoint;
	}

	public PLMSWeaponData getWeapon() {
		return mWeapon;
	}

	public PLMSColorData getColor() {
		return mColor;
	}

	public PLMSSkillData getSupportSkill() {
		return mSupportSkill;
	}

	public PLMSSkillData getSecretSkill() {
		return mSecretSkill;
	}

	public PLMSSkillData getPassiveASkill() {
		return mPassiveASkill;
	}

	public PLMSSkillData getPassiveBSkill() {
		return mPassiveBSkill;
	}

	public PLMSSkillData getPassiveCSkill() {
		return mPassiveCSkill;
	}

	// setter
	public void setCurrentHP(int currentHP) {
		mCurrentHP = currentHP;
	}
}
