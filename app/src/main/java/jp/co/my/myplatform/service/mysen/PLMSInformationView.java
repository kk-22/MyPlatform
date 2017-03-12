package jp.co.my.myplatform.service.mysen;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleResult;
import jp.co.my.myplatform.service.mysen.information.PLMSBattleInfoView;


public class PLMSInformationView extends LinearLayout {

	private PLMSUnitView mLeftUnitView;
	private PLMSUnitView mRightUnitView;
	private PLMSBattleResult mBattleResult;

	private LinearLayout mUnitDataLinear;
	private FrameLayout mBattleDataFrame;

	private ImageView mLeftImageView;
	private TextView mLeftNameTextView;
	private TextView mCurrentHPTextView;
	private TextView mMaxHPTextView;
	private TextView mAttackTextView;
	private TextView mSpeedTextView;
	private TextView mDefenseTextView;
	private TextView mMagicDefenseTextView;

	private PLMSBattleInfoView mLeftBattleInfo;
	private PLMSBattleInfoView mRightBattleInfo;
	private ImageView mRightImageView;

	public PLMSInformationView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_information, this);
		mUnitDataLinear = (LinearLayout) findViewById(R.id.unit_data_linear);
		mBattleDataFrame = (FrameLayout) findViewById(R.id.battle_data_frame);

		mLeftImageView = (ImageView) findViewById(R.id.left_image);
		mLeftNameTextView = (TextView) findViewById(R.id.name_text);
		mCurrentHPTextView = (TextView) findViewById(R.id.current_hp_text);
		mMaxHPTextView = (TextView) findViewById(R.id.result_hp_text);
		mAttackTextView = (TextView) findViewById(R.id.attack_text);
		mSpeedTextView = (TextView) findViewById(R.id.speed_text);
		mDefenseTextView = (TextView) findViewById(R.id.defense_text);
		mMagicDefenseTextView = (TextView) findViewById(R.id.magic_defense_text);

		mLeftBattleInfo = (PLMSBattleInfoView) findViewById(R.id.left_battle_info);
		mLeftBattleInfo.initWithIsLeft(true);
		mRightBattleInfo = (PLMSBattleInfoView) findViewById(R.id.right_battle_info);
		mRightBattleInfo.initWithIsLeft(false);
		mRightImageView = (ImageView) findViewById(R.id.right_image);
	}

	public PLMSInformationView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSInformationView(Context context) {
		this(context, null);
	}

	public void updateForUnitData(PLMSUnitView unitView) {
		if (mRightUnitView != null) {
			mUnitDataLinear.setVisibility(View.VISIBLE);
			mBattleDataFrame.setVisibility(View.GONE);
			mRightUnitView = null;
		}
		if (unitView.equals(mLeftUnitView)) {
			return;
		}
		mLeftUnitView = unitView;

		animateUnitImage(mLeftImageView, unitView);
		mLeftNameTextView.setText(unitView.getUnitData().getUnitModel().getName());
		setIntToText(unitView.getUnitData().getCurrentHP(), mCurrentHPTextView);
		setIntToText(unitView.getUnitData().getMaxHP(), mMaxHPTextView);
		setIntToText(unitView.getUnitData().getCurrentAttack(), mAttackTextView);
		setIntToText(unitView.getUnitData().getCurrentSpeed(), mSpeedTextView);
		setIntToText(unitView.getUnitData().getCurrentDefense(), mDefenseTextView);
		setIntToText(unitView.getUnitData().getCurrentMagicDefense(), mMagicDefenseTextView);

		// 背景色設定
		setBackgroundColor(unitView.getUnitData().getArmyStrategy().getInformationColor());
	}

	public void updateForBattleData(PLMSBattleResult battleResult) {
		if (mRightUnitView == null) {
			mUnitDataLinear.setVisibility(View.GONE);
			mBattleDataFrame.setVisibility(View.VISIBLE);
		}
		if (battleResult.equals(mBattleResult)) {
			return;
		}
		mBattleResult = battleResult;

		// 左の設定
		PLMSUnitView leftUnitView = battleResult.getLeftUnit().getUnitView();
		if (!leftUnitView.equals(mLeftUnitView)) {
			animateUnitImage(mLeftImageView, leftUnitView);
			mLeftUnitView = leftUnitView;
			setBackgroundColor(leftUnitView.getUnitData().getArmyStrategy().getInformationColor());
		}

		// 右の設定
		mRightUnitView = battleResult.getRightUnit().getUnitView();;
		animateUnitImage(mRightImageView, mRightUnitView);
		mBattleDataFrame.setBackgroundColor(mRightUnitView.getUnitData().getArmyStrategy().getInformationColor());

		mLeftBattleInfo.updateInfo(battleResult, battleResult.getLeftUnit());
		mRightBattleInfo.updateInfo(battleResult, battleResult.getRightUnit());
	}

	private Bitmap unitImageFromUnitView(PLMSUnitView unitView) {
		BitmapDrawable drawable = ((BitmapDrawable)unitView.getUnitImageView().getDrawable());
		return drawable.getBitmap();
	}

	private void animateUnitImage(ImageView imageView, PLMSUnitView unitView) {
		imageView.setImageBitmap(unitImageFromUnitView(unitView));

		int width = imageView.getWidth();
		int moveWidth = width * 3 / 4;
		float baseX, startX;
		if (imageView.equals(mLeftImageView)) {
			baseX = 0;
			startX = baseX - moveWidth;
		} else {
			baseX = mBattleDataFrame.getWidth() - width;
			startX = baseX + moveWidth;
		}
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "x", startX, baseX);
		objectAnimator.setDuration(100);
		objectAnimator.start();
	}

	private void setIntToText(int number, TextView textView) {
		textView.setText(Integer.toString(number));
	}

	// getter and setter
	public PLMSUnitView getRightUnitView() {
		return mRightUnitView;
	}

	public PLMSUnitView getLeftUnitView() {
		return mLeftUnitView;
	}
}
