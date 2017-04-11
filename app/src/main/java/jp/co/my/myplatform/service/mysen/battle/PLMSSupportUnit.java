package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;


public class PLMSSupportUnit implements PLMSUnitInterface {

	private PLMSUnitView mUnitView;
	private PLMSLandView mLandView;
	private int mRemainingHP;

	public PLMSSupportUnit(PLMSUnitView unitView, PLMSLandView landView) {
		mUnitView = unitView;
		mLandView = landView;

		mRemainingHP = mUnitView.getUnitData().getCurrentHP();
	}

	// getter
	@Override
	public PLMSUnitView getUnitView() {
		return mUnitView;
	}

	@Override
	public PLMSLandView getLandView() {
		return mLandView;
	}

	@Override
	public PLMSUnitData getUnitData() {
		return mUnitView.getUnitData();
	}

	@Override
	public int getRemainingHP() {
		return mRemainingHP;
	}

	@Override
	public boolean isAlive() {
		return true;
	}
}
