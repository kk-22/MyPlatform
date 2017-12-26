package jp.co.my.myplatform.mysen;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.mysen.battle.PLMSBaseForecast;
import jp.co.my.myplatform.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.mysen.battle.PLMSSupportForecast;
import jp.co.my.myplatform.mysen.information.PLMSForecastInfoView;
import jp.co.my.myplatform.mysen.information.PLMSUnitInfoView;


public class PLMSInformationView extends LinearLayout {

	private PLMSUnitView mLeftUnitView;
	private PLMSUnitView mRightUnitView;
	private PLMSBaseForecast mForecast;

	private PLMSUnitInfoView mUnitInfoView;
	private FrameLayout mForefastDataFrame;

	private ImageView mLeftImageView;
	private PLMSForecastInfoView mLeftForecastInfo;
	private PLMSForecastInfoView mRightForecastInfo;
	private ImageView mRightImageView;
	private TextView mTitleTextView;

	public PLMSInformationView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_information, this);
		mUnitInfoView = (PLMSUnitInfoView) findViewById(R.id.unit_info);
		mForefastDataFrame = (FrameLayout) findViewById(R.id.forecast_data_frame);

		mLeftImageView = (ImageView) findViewById(R.id.left_image);

		mTitleTextView = (TextView) findViewById(R.id.title_text);
		mLeftForecastInfo = (PLMSForecastInfoView) findViewById(R.id.left_forecast_info);
		mLeftForecastInfo.initWithIsLeft(true);
		mRightForecastInfo = (PLMSForecastInfoView) findViewById(R.id.right_forecast_info);
		mRightForecastInfo.initWithIsLeft(false);
		mRightImageView = (ImageView) findViewById(R.id.right_image);

		clearInformation();
	}

	public PLMSInformationView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSInformationView(Context context) {
		this(context, null);
	}

	public void clearInformation() {
		setBackgroundColor(Color.WHITE);

		mUnitInfoView.setVisibility(View.GONE);
		mForefastDataFrame.setVisibility(View.GONE);
		mRightUnitView = null;
		mLeftUnitView = null;
		mForecast = null;
		mLeftImageView.setImageBitmap(null);
		mRightImageView.setImageBitmap(null);
	}

	public void updateForUnitData(PLMSUnitView unitView) {
		if (mRightUnitView != null || mLeftUnitView == null) {
			// バトルinfo後の表示 or 初回表示時
			mUnitInfoView.setVisibility(View.VISIBLE);
			mForefastDataFrame.setVisibility(View.GONE);
			mRightUnitView = null;
		}
		if (!unitView.equals(mLeftUnitView)) {
			mLeftUnitView = unitView;
			animateUnitImage(mLeftImageView, unitView);
		}
		mUnitInfoView.updateUnitInfo(unitView);

		// 背景色設定
		setBackgroundColor(unitView.getUnitData().getArmyStrategy().getInformationColor());
	}

	public void updateForSupportData(PLMSSupportForecast supportForecast) {
		if (!updateForForecast(supportForecast)) {
			return;
		}
		mLeftForecastInfo.updateInfoForSupport(supportForecast, supportForecast.getLeftUnit());
		mRightForecastInfo.updateInfoForSupport(supportForecast, supportForecast.getRightUnit());
	}

	public void updateForBattleData(PLMSBattleForecast battleForecast) {
		if (!updateForForecast(battleForecast)) {
			return;
		}
		mLeftForecastInfo.updateInfoForBattle(battleForecast, battleForecast.getLeftUnit());
		mRightForecastInfo.updateInfoForBattle(battleForecast, battleForecast.getRightUnit());
	}

	// 更新する必要がないとき false を返す
	private boolean updateForForecast(PLMSBaseForecast forecast) {
		if (mRightUnitView == null) {
			mUnitInfoView.setVisibility(View.GONE);
			mForefastDataFrame.setVisibility(View.VISIBLE);
		}
		if (forecast.equals(mForecast)) {
			return false;
		}
		mForecast = forecast;

		// 中央の設定
		mTitleTextView.setText(forecast.getInformationTitle());

		// 左の設定
		PLMSUnitView leftUnitView = forecast.getLeftUnit().getUnitView();
		if (!leftUnitView.equals(mLeftUnitView)) {
			animateUnitImage(mLeftImageView, leftUnitView);
			mLeftUnitView = leftUnitView;
			setBackgroundColor(leftUnitView.getUnitData().getArmyStrategy().getInformationColor());
		}
		// 右の設定
		mRightUnitView = forecast.getRightUnit().getUnitView();;
		animateUnitImage(mRightImageView, mRightUnitView);
		mForefastDataFrame.setBackgroundColor(mRightUnitView.getUnitData().getArmyStrategy().getInformationColor());
		return true;
	}

	private Bitmap unitImageFromUnitView(PLMSUnitView unitView) {
		BitmapDrawable drawable = ((BitmapDrawable)unitView.getUnitImageView().getDrawable());
		return drawable.getBitmap();
	}

	private void animateUnitImage(ImageView imageView, PLMSUnitView unitView) {
		imageView.setImageBitmap(unitImageFromUnitView(unitView));

		// 初回時は mRightImageView が非表示のために width が0なので代わりにleftのサイズを使用
		int width = mLeftImageView.getWidth();
		int moveWidth = width * 3 / 4;
		float baseX, startX;
		if (imageView.equals(mLeftImageView)) {
			baseX = 0;
			startX = baseX - moveWidth;
		} else {
			baseX = getWidth() - width * 2;
			startX = baseX + moveWidth;
		}
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "x", startX, baseX);
		objectAnimator.setDuration(100);
		objectAnimator.start();
	}

	// getter and setter
	// 現在ユニット情報に表示中のユニット
	public PLMSUnitView getInfoUnitView() {
		if (mRightUnitView != null) {
			// 予測表示中のためユニット情報表示中ではない
			return null;
		}
		return mLeftUnitView;
	}

	public PLMSBattleForecast getBattleForecast() {
		if (mRightUnitView == null) {
			// ユニット情報表示中
			return null;
		}
		return MYOtherUtil.castObject(mForecast, PLMSBattleForecast.class);
	}

	public PLMSSupportForecast getSupportForecast() {
		if (mRightUnitView == null) {
			// ユニット情報表示中
			return null;
		}
		return MYOtherUtil.castObject(mForecast, PLMSSupportForecast.class);
	}
}
