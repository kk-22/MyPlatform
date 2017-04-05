package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.my.common.util.MYImageUtil;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

public class PLMSUnitView extends FrameLayout {

	private ImageView mUnitImageView;
	private PLMSHitPointBar mHPBar;
	private View mAlreadyActionView;

	private PLMSUnitData mUnitData;
	private PLMSLandView mLandView;
	private Point mCurrentPoint;

	public PLMSUnitView(Context context, PLMSUnitData unitData) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_view, this);
		mUnitImageView = (ImageView) findViewById(R.id.image_view);
		mHPBar = (PLMSHitPointBar) findViewById(R.id.hp_bar);
		mAlreadyActionView = findViewById(R.id.already_action_view);

		mUnitData = unitData;
		mUnitData.getArmyStrategy().addUnitView(this);

		initChildView();
	}

	// ターン開始時
	public void resetForNewTurn(int numberOfTurn) {
		mUnitData.resetParamsForNewTurn();
		for (PLMSSkillData skillData : mUnitData.getPassiveSkillArray()) {
			skillData.executeStartTurnSkill(this, numberOfTurn);
		}
	}

	// ターン終了時
	public void resetForFinishTurn(int numberOfTurn) {
		if (!isAlreadyAction()) {
			standby();
		}
		mAlreadyActionView.setVisibility(GONE);
	}

	public void moveToLand(PLMSLandView landView) {
		if (mLandView != null && equals(mLandView.getUnitView())) {
			// 初回配置以外
			mLandView.removeUnitView();
		}

		mLandView = landView;
		mCurrentPoint = landView.getPoint();
		landView.putUnitView(this);
	}

	// 待機
	public void standby() {
		mUnitData.resetDebuffParams();
		didAction();
	}

	// 行動終了
	public void didAction() {
		mAlreadyActionView.setVisibility(VISIBLE);
	}

	// マップからの離脱
	public void removeFromField() {
		mLandView.removeUnitView();
		MYViewUtil.removeFromSuperView(this);
	}

	public boolean isAlreadyAction() {
		return (mAlreadyActionView != null && mAlreadyActionView.getVisibility() == VISIBLE);
	}

	private void initChildView() {
		ImageView weaponImage = (ImageView) findViewById(R.id.weapon_image);
		String weaponPath = mUnitData.getWeapon().getWeaponImagePath();
		weaponImage.setImageBitmap(MYImageUtil.getBitmapFromImagePath(weaponPath, getContext()));

		// Army による設定
		PLMSArmyStrategy army = mUnitData.getArmyStrategy();
		FrameLayout.LayoutParams layoutParams = (LayoutParams) weaponImage.getLayoutParams();
		layoutParams.gravity = army.getIconGravity();

		loadImage();
		mHPBar.initWithUnitView(this);
	}

	private void loadImage() {
		String path = mUnitData.getSmallImagePath();
		mUnitImageView.setImageBitmap(MYImageUtil.getBitmapFromImagePath(path, getContext()));
	}

	// 便利メソッド
	public boolean isEnemy(PLMSUnitView targetUnitView) {
		if (targetUnitView == null) {
			return false;
		}
		PLMSArmyStrategy myArmy = mUnitData.getArmyStrategy();
		PLMSArmyStrategy targetArmy = targetUnitView.getUnitData().getArmyStrategy();
		return !myArmy.equals(targetArmy);
	}

	// Debug
	public String debugLog() {
		PLMSUnitModel unitModel = mUnitData.getUnitModel();
		String debugText = " UnitView " + unitModel.getName();
		MYLogUtil.outputLog(debugText);
		return debugText;
	}

	// getter and setter
	public PLMSUnitData getUnitData() {
		return mUnitData;
	}

	public Point getCurrentPoint() {
		return mCurrentPoint;
	}

	public PLMSLandView getLandView() {
		return mLandView;
	}

	public ImageView getUnitImageView() {
		return mUnitImageView;
	}

	public PLMSHitPointBar getHPBar() {
		return mHPBar;
	}
}
