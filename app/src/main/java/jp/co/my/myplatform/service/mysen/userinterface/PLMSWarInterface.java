package jp.co.my.myplatform.service.mysen.userinterface;

import java.util.ArrayList;

import jp.co.my.myplatform.service.mysen.PLMSAnimationManager;
import jp.co.my.myplatform.service.mysen.PLMSAreaManager;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

public abstract class PLMSWarInterface {

	PLMSArgument mArgument;
	ArrayList<PLMSUnitView> mUnitArray;
	PLMSFieldView mField;
	PLMSArmyStrategy mTargetArmy; // 操作対象のArmy
	PLMSAnimationManager mAnimationManager;
	PLMSAreaManager mAreaManager;

	boolean mIsEnable;

	public PLMSWarInterface(PLMSArgument argument, PLMSArmyStrategy armyStrategy) {
		mArgument = argument;
		mTargetArmy = armyStrategy;
		mField = argument.getFieldView();
		mUnitArray = mField.getUnitViewArray();
		mAnimationManager = argument.getAnimationManager();
		mAreaManager = argument.getAreaManager();
	}

	public void enableInterface() {
		mIsEnable = true;
	}

	public void disableInterface() {
		mIsEnable = false;
	}

	public void suspendInterface() {
		mAnimationManager.pauseAnimation();
	}

	public void resumeInterface() {
		mAnimationManager.resumeAnimation();
	}

	public void toggleAllDangerArea() {
		// 攻撃範囲表示するなら override
	}
}
