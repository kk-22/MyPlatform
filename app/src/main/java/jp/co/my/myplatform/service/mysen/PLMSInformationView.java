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


public class PLMSInformationView extends LinearLayout {

	private PLMSUnitView mLeftUnitView;
	private PLMSUnitView mRightUnitView;

	private LinearLayout mUnitDataLinear;
	private FrameLayout mBattleDataFrame;

	private ImageView mLeftImageView;
	private TextView mLeftNameTextView;
	private TextView mHPTextView;
	private TextView mAttackTextView;
	private TextView mSpeedTextView;
	private TextView mDefenseTextView;
	private TextView mMagicDefenseTextView;

	private View mRightBackgroundView;
	private ImageView mRightImageView;

	public PLMSInformationView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_information, this);
		mUnitDataLinear = (LinearLayout) findViewById(R.id.unit_data_linear);
		mBattleDataFrame = (FrameLayout) findViewById(R.id.battle_data_frame);

		mLeftImageView = (ImageView) findViewById(R.id.left_image);
		mLeftNameTextView = (TextView) findViewById(R.id.name_text);
		mHPTextView = (TextView) findViewById(R.id.hp_text);
		mAttackTextView = (TextView) findViewById(R.id.attack_text);
		mSpeedTextView = (TextView) findViewById(R.id.speed_text);
		mDefenseTextView = (TextView) findViewById(R.id.defense_text);
		mMagicDefenseTextView = (TextView) findViewById(R.id.magic_defense_text);

		mRightBackgroundView = findViewById(R.id.right_background_view);
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
		setIntToText(unitView.getUnitData().getCurrentHP(), mHPTextView);
		setIntToText(unitView.getUnitData().getCurrentAttack(), mAttackTextView);
		setIntToText(unitView.getUnitData().getCurrentSpeed(), mSpeedTextView);
		setIntToText(unitView.getUnitData().getCurrentDefense(), mDefenseTextView);
		setIntToText(unitView.getUnitData().getCurrentMagicDefense(), mMagicDefenseTextView);

		// 背景色設定
		setBackgroundColor(unitView.getUnitData().getArmyStrategy().getInformationColor());
	}

	// TODO: バトルのダメージデータは内部で計算？ BattleResultクラスを引数で受け取る？
	public void updateForBattleData(PLMSUnitView leftUnitView, PLMSUnitView rightUnitView) {
		if (mRightUnitView == null) {
			mUnitDataLinear.setVisibility(View.GONE);
			mBattleDataFrame.setVisibility(View.VISIBLE);
		}

		if (!leftUnitView.equals(mLeftUnitView)) {
			animateUnitImage(mLeftImageView, leftUnitView);
			mLeftUnitView = leftUnitView;
		}

		mRightUnitView = rightUnitView;
		animateUnitImage(mRightImageView, rightUnitView);

		// 背景色設定
		setBackgroundColor(leftUnitView.getUnitData().getArmyStrategy().getInformationColor());
		mRightBackgroundView.setBackgroundColor(rightUnitView.getUnitData().getArmyStrategy().getInformationColor());
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
