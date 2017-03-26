package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.common.util.MYArrayList;
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
	private PLMSSkillData mWeaponSkill;
	private PLMSSkillData mSupportSkill;
	private PLMSSkillData mSecretSkill;
	private PLMSSkillData mPassiveASkill;
	private PLMSSkillData mPassiveBSkill;
	private PLMSSkillData mPassiveCSkill;
	private MYArrayList<PLMSSkillData> mPassiveSkillArray;

	private int mMaxHP;
	private int mCurrentHP;
	private int mCurrentAttack;
	private int mCurrentSpeed;
	private int mCurrentDefense;
	private int mCurrentMagicDefense;

	private int mBuffAttack;
	private int mBuffSpeed;
	private int mBuffDefense;
	private int mBuffMagicDefense;

	private int mMoveCount;					// 同一ターン内での移動回数

	public PLMSUnitData(PLMSUnitModel unitModel, Point firstPoint, PLMSArmyStrategy armyStrategy) {
		mUnitModel = unitModel;
		mFirstPoint = firstPoint;
		mArmyStrategy = armyStrategy;

		mColor = new PLMSColorData((mUnitModel.getColorType()));
		mWeapon = new PLMSWeaponData(mUnitModel.getWeaponType());
		mBranch = new PLMSBranchData(mUnitModel.getBranchType());
		mWeaponSkill = createSkillData(null);
		mSupportSkill = createSkillData(mUnitModel.getSupportSkillForeign());
		mSecretSkill = createSkillData(mUnitModel.getSecretSkillForeign());
		mPassiveASkill = createSkillData(mUnitModel.getPassiveASkillForeign());
		mPassiveBSkill = createSkillData(mUnitModel.getPassiveBSkillForeign());
		mPassiveCSkill = createSkillData(mUnitModel.getPassiveCSkillForeign());

		mPassiveSkillArray = new MYArrayList<>(3);
		mPassiveSkillArray.add(mPassiveASkill);
		mPassiveSkillArray.add(mPassiveBSkill);
		mPassiveSkillArray.add(mPassiveCSkill);

		initAllStatus();
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mUnitModel.getNo() +".png";
	}

	public void resetParams() {
		mBuffAttack = 0;
		mBuffSpeed = 0;
		mBuffDefense = 0;
		mBuffMagicDefense = 0;

		mMoveCount = 0;
	}

	public void setAllStatus() {
		mCurrentAttack = mUnitModel.getAttackPoint() + mBuffAttack;
		mCurrentSpeed = mUnitModel.getSpeedPoint() + mBuffSpeed;
		mCurrentDefense = mUnitModel.getDefensePoint() + mBuffDefense;
		mCurrentMagicDefense = mUnitModel.getMagicDefensePoint() + mBuffMagicDefense;
	}

	private void initAllStatus() {
		mMaxHP = mUnitModel.getHitPoint();
		mCurrentHP = mMaxHP;
		resetParams();
		setAllStatus();
	}

	private PLMSSkillData createSkillData(ForeignKeyContainer<PLMSSkillModel> foreign) {
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

	public PLMSSkillData getWeaponSkill() {
		return mWeaponSkill;
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

	public MYArrayList<PLMSSkillData> getPassiveSkillArray() {
		return mPassiveSkillArray;
	}

	// setter
	public void setCurrentHP(int currentHP) {
		mCurrentHP = currentHP;
	}

	public void setBuffAttack(int buffAttack) {
		if (mBuffAttack < buffAttack) {
			mBuffAttack = buffAttack;
		}
	}

	public void setBuffSpeed(int buffSpeed) {
		if (mBuffSpeed < buffSpeed) {
			mBuffSpeed = buffSpeed;
		}
	}

	public void setBuffDefense(int buffDefense) {
		if (mBuffDefense < buffDefense) {
			mBuffDefense = buffDefense;
		}
	}

	public void setBuffMagicDefense(int buffMagicDefense) {
		if (mBuffMagicDefense < buffMagicDefense) {
			mBuffMagicDefense = buffMagicDefense;
		}
	}
}
