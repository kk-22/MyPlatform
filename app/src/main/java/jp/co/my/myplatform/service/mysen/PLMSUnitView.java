package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.my.common.util.MYImageUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

public class PLMSUnitView extends FrameLayout {

	private ImageView mUnitImageView;
	private PLMSHitPointBar mHPBar;

	private PLMSUnitData mUnitData;
	private PLMSLandView mLandView;
	private Point mCurrentPoint;
	private boolean mIsAlreadyAction;			// 現在のターン内での行動有無

	public PLMSUnitView(Context context, PLMSUnitData unitData) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_view, this);
		mUnitImageView = (ImageView) findViewById(R.id.image_view);
		mHPBar = (PLMSHitPointBar) findViewById(R.id.hp_bar);

		mUnitData = unitData;
		mUnitData.getArmyStrategy().addUnitView(this);

		initChildView();
	}

	public void resetForNewTurn() {
		mIsAlreadyAction = false;
	}

	public void moveToLand(PLMSLandView landView) {
		if (mLandView != null) {
			mLandView.removeUnitView();
			mIsAlreadyAction = true;
		}

		mLandView = landView;
		mCurrentPoint = landView.getPoint();
		landView.putUnitView(this);
	}

	// マップからの離脱
	public void removeFromField() {
		mLandView.removeUnitView();
		MYViewUtil.removeFromSuperView(this);
	}

	public void updateHitPoint(int nextHP, int diffHP) {
		mUnitData.setCurrentHP(nextHP);
		mHPBar.updateHitPoint(nextHP, diffHP);
	}

	private void initChildView() {
		ImageView weaponImage = (ImageView) findViewById(R.id.weapon_image);
		String weaponPath = mUnitData.getWeapon().getWeaponImagePath();
		weaponImage.setImageBitmap(MYImageUtil.getBitmapFromImagePath(weaponPath, getContext()));

		FrameLayout.LayoutParams layoutParams = (LayoutParams) weaponImage.getLayoutParams();
		layoutParams.gravity = mUnitData.getArmyStrategy().getIconGravity();

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
