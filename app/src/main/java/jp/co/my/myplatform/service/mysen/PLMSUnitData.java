package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import jp.co.my.myplatform.service.mysen.Army.PLMSArmyStrategy;

public class PLMSUnitData {

	private PLMSUnitModel mUnitModel;
	private Point mFirstPoint;				// 初期位置
	private PLMSArmyStrategy mArmyStrategy;

	private PLMSBranchData mBranch;
	private PLMSWeaponData mWeapon;

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

		mBranch = new PLMSBranchData(mUnitModel.getBranchType());
		mWeapon = new PLMSWeaponData(mUnitModel.getWeaponType());

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

	// getter and setter
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
}
