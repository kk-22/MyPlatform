package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.my.common.util.MYImageUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

public class PLMSUnitView extends FrameLayout {

	private ImageView mUnitImageView;
	private PLMSHitPointBar mHPBar;

	private PLMSUnitData mUnitData;
	private PLMSLandView mLandView;
	private Point mCurrentPoint;

	public PLMSUnitView(Context context, PLMSUnitData unitData) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_view, this);
		mUnitImageView = (ImageView) findViewById(R.id.image_view);
		mHPBar = (PLMSHitPointBar) findViewById(R.id.hp_bar);

		mUnitData = unitData;

		initChildView();
	}

	public void moveToLand(PLMSLandView landView) {
		if (mLandView != null) {
			mLandView.removeUnitView();
		}

		mLandView = landView;
		mCurrentPoint = landView.getPoint();
		landView.putUnitView(this);
	}

	public void updateHitPoint(int diffHP) {
		int prevHP = mUnitData.getCurrentHP();
		int nextHP = Math.max(0, prevHP + diffHP);
		mUnitData.setCurrentHP(nextHP);
		mHPBar.updateFromUnitView(this, diffHP);
	}

	private void initChildView() {
		loadImage();
		mHPBar.initFromUnitView(this);
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
}
