package jp.co.my.myplatform.service.mysen;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYImageUtil;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

public class PLMSUnitView extends FrameLayout implements PLMSUnitInterface {

	private ImageView mUnitImageView;
	private ImageView mBuffImageView;
	private PLMSHitPointBar mHPBar;
	private View mAlreadyActionView;

	private PLMSUnitData mUnitData;
	private PLMSLandView mLandView;
	private Point mCurrentPoint;

	public PLMSUnitView(Context context, PLMSUnitData unitData) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_view, this);
		mUnitImageView = (ImageView) findViewById(R.id.image_view);
		mBuffImageView = (ImageView) findViewById(R.id.buff_image);
		mHPBar = (PLMSHitPointBar) findViewById(R.id.hp_bar);
		mAlreadyActionView = findViewById(R.id.already_action_view);

		mUnitData = unitData;
		mUnitData.getArmyStrategy().addUnitView(this);

		initChildView();
	}

	// ターン開始時
	public void resetForNewTurn(int numberOfTurn) {
		mUnitData.resetParamsForNewTurn();

		int totalBuff = 0;
		for (int i = 0; i < PLMSUnitData.PARAMETER_NUMBER; i++) {
			totalBuff += mUnitData.getBuffParameterOfNo(i);
			totalBuff -= mUnitData.getDebuffParameterOfNo(i);
		}
		updateBuffImage(totalBuff);
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

	private void updateBuffImage(int buffPoint) {
		if (buffPoint == 0) {
			mBuffImageView.setAlpha(0.f);
		} else if (buffPoint > 0) {
			mBuffImageView.setImageBitmap(MYImageUtil.getBitmapFromImagePath("icon/up_arrow.png", getContext()));
		} else {
			mBuffImageView.setImageBitmap(MYImageUtil.getBitmapFromImagePath("icon/up_arrow.png", getContext()));
		}
	}

	// 便利メソッド
	public boolean isEnemy(PLMSUnitInterface targetUnit) {
		if (targetUnit == null) {
			return false;
		}
		PLMSArmyStrategy myArmy = mUnitData.getArmyStrategy();
		PLMSArmyStrategy targetArmy = targetUnit.getUnitData().getArmyStrategy();
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
	@Override
	public PLMSUnitView getUnitView() {
		return this;
	}

	@Override
	public PLMSUnitData getUnitData() {
		return mUnitData;
	}

	@Override
	public PLMSLandView getLandView() {
		return mLandView;
	}

	@Override
	public int getRemainingHP() {
		return mUnitData.getCurrentHP();
	}

	@Override
	public boolean isAlive() {
		return (getRemainingHP() > 0);
	}

	@Override
	public PLMSUnitInterface getAnotherUnit() {
		return null;
	}

	public Point getCurrentPoint() {
		return mCurrentPoint;
	}

	public ImageView getUnitImageView() {
		return mUnitImageView;
	}

	public PLMSHitPointBar getHPBar() {
		return mHPBar;
	}

	public Animator getBuffAnimator(int buffPoint) {
		if (buffPoint == 0) {
			return null;
		}
		updateBuffImage(buffPoint);

		MYArrayList<Animator> animatorArray = new MYArrayList<>();
		float currentY = mBuffImageView.getY();
		float topY = currentY - getHeight() / 4;
		// 表示
		ObjectAnimator showAnimation = ObjectAnimator.ofFloat(mBuffImageView, "alpha", 1);
		showAnimation.setDuration(1);
		animatorArray.add(showAnimation);

		// 上へ
		ObjectAnimator upAnimation = ObjectAnimator.ofFloat(mBuffImageView, "y", currentY, topY);
		upAnimation.setDuration(100);
		animatorArray.add(upAnimation);

		// 元の位置へ下げる
		ObjectAnimator downAnimation = ObjectAnimator.ofFloat(mBuffImageView, "y", topY, currentY);
		downAnimation.setDuration(100);
		animatorArray.add(downAnimation);

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playSequentially(animatorArray);
		return animatorSet;
	}
}
