package jp.co.my.myplatform.service.mysen;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

public class PLMSArgument {

	private PLMSTurnManager mTurnManager;
	private PLMSAnimationManager mAnimationManager;
	private PLMSAreaManager mAreaManager;

	private PLMSFieldView mFieldView;
	private PLMSInformationView mInformationView;

	private MYArrayList<PLMSArmyStrategy> mArmyArray;
	private MYArrayList<PLMSUnitView> mAllUnitViewArray;

	public PLMSArgument() {
		mAreaManager = new PLMSAreaManager(this);
	}

	// getter and setter
	public PLMSAnimationManager getAnimationManager() {
		return mAnimationManager;
	}

	public void setAnimationManager(PLMSAnimationManager animationManager) {
		mAnimationManager = animationManager;
	}

	public PLMSTurnManager getTurnManager() {
		return mTurnManager;
	}

	public void setTurnManager(PLMSTurnManager turnManager) {
		mTurnManager = turnManager;
	}

	public PLMSFieldView getFieldView() {
		return mFieldView;
	}

	public void setFieldView(PLMSFieldView fieldView) {
		mFieldView = fieldView;
		mAreaManager.setField(fieldView);
	}

	public PLMSInformationView getInformationView() {
		return mInformationView;
	}

	public void setInformationView(PLMSInformationView informationView) {
		mInformationView = informationView;
	}

	public MYArrayList<PLMSUnitView> getAllUnitViewArray() {
		return mAllUnitViewArray;
	}

	public void setAllUnitViewArray(MYArrayList<PLMSUnitView> allUnitViewArray) {
		mAllUnitViewArray = allUnitViewArray;
	}

	public PLMSAreaManager getAreaManager() {
		return mAreaManager;
	}

	public void setAreaManager(PLMSAreaManager areaManager) {
		mAreaManager = areaManager;
	}

	public MYArrayList<PLMSArmyStrategy> getArmyArray() {
		return mArmyArray;
	}

	public void setArmyArray(MYArrayList<PLMSArmyStrategy> armyArray) {
		mArmyArray = armyArray;
	}
}
