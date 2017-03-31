package jp.co.my.myplatform.service.mysen;

public class PLMSArgument {

	private PLMSTurnManager mTurnManager;
	private PLMSAnimationManager mAnimationManager;

	private PLMSFieldView mFieldView;
	private PLMSInformationView mInformationView;

	public PLMSArgument() {
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
	}

	public PLMSInformationView getInformationView() {
		return mInformationView;
	}

	public void setInformationView(PLMSInformationView informationView) {
		mInformationView = informationView;
	}
}
