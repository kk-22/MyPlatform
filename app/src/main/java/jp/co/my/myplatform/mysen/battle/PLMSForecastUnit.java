package jp.co.my.myplatform.mysen.battle;

import jp.co.my.myplatform.mysen.PLMSLandView;
import jp.co.my.myplatform.mysen.PLMSUnitData;
import jp.co.my.myplatform.mysen.PLMSUnitView;
import jp.co.my.myplatform.mysen.unit.PLMSUnitInterface;

public class PLMSForecastUnit implements PLMSUnitInterface {

	PLMSUnitView mUnitView;
	PLMSLandView mLandView;
	int mRemainingHP;

	public PLMSForecastUnit(PLMSUnitView unitView, PLMSLandView landView) {
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
		return (mRemainingHP > 0) ;
	}

	@Override
	public PLMSForecastUnit getAnotherUnit() {
		// サブクラスで実装
		return null;
	}
}
