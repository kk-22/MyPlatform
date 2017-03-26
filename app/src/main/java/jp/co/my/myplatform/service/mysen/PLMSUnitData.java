package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillModel;

public class PLMSUnitData {

	public static final int PARAMETER_ATTACK = 0;
	public static final int PARAMETER_SPEED = 1;
	public static final int PARAMETER_DEFENSE = 2;
	public static final int PARAMETER_MAGIC_DEFENSE = 3;
	public static final int PARAMETER_NUMBER = 4; // パラメータ数

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
	private int[] mBaseParams; // バフ適用前の値
	private int[] mCurrentParams; // バフ適用後の値
	private int[] mBuffParams; // バフによる加算値

	private int mMoveCount; // 同一ターン内での移動回数

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

		mBaseParams = new int[PARAMETER_NUMBER];
		mBaseParams[PARAMETER_ATTACK] = mUnitModel.getAttackPoint();
		mBaseParams[PARAMETER_SPEED] = mUnitModel.getSpeedPoint();
		mBaseParams[PARAMETER_DEFENSE] = mUnitModel.getDefensePoint();
		mBaseParams[PARAMETER_MAGIC_DEFENSE] = mUnitModel.getMagicDefensePoint();
		mCurrentParams = new int[PARAMETER_NUMBER];
		mBuffParams = new int[PARAMETER_NUMBER];
		
		mMaxHP = mUnitModel.getHitPoint();
		mCurrentHP = mMaxHP;
		resetParams();
		setAllStatus();
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mUnitModel.getNo() +".png";
	}

	public void resetParams() {
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			mBuffParams[i] = 0;
		}

		mMoveCount = 0;
	}

	public void setAllStatus() {
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			mCurrentParams[i] = mBaseParams[i] + mBuffParams[i];
		}
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
		return mCurrentParams[PARAMETER_ATTACK];
	}

	public int getCurrentSpeed() {
		return mCurrentParams[PARAMETER_SPEED];
	}

	public int getCurrentDefense() {
		return mCurrentParams[PARAMETER_DEFENSE];
	}

	public int getCurrentMagicDefense() {
		return mCurrentParams[PARAMETER_MAGIC_DEFENSE];
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

	public void setBuffNumber(int parameterNo, int buff) {
		if (mBuffParams[parameterNo] < buff) {
			mBuffParams[parameterNo] = buff;
		}
	}
}
