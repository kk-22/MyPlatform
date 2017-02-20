package jp.co.my.myplatform.service.mysen;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;


public class PLMSInformationView extends LinearLayout {

	private PLMSUnitView mLeftUnitView;
	private PLMSUnitView mRightUnitView;

	private ImageView mLeftImageView;
	private TextView mLeftNameTextView;
	private TextView mHPTextView;
	private TextView mAttackTextView;
	private TextView mSpeedTextView;
	private TextView mDefenseTextView;
	private TextView mMagicDefenseTextView;

	public PLMSInformationView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_information, this);
		mLeftImageView = (ImageView) findViewById(R.id.left_image);
		mLeftNameTextView = (TextView) findViewById(R.id.name_text);
		mHPTextView = (TextView) findViewById(R.id.hp_text);
		mAttackTextView = (TextView) findViewById(R.id.attack_text);
		mSpeedTextView = (TextView) findViewById(R.id.speed_text);
		mDefenseTextView = (TextView) findViewById(R.id.defense_text);
		mMagicDefenseTextView = (TextView) findViewById(R.id.magic_defense_text);
	}

	public PLMSInformationView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSInformationView(Context context) {
		this(context, null);
	}

	public void updateForUnitData(PLMSUnitView unitView) {
		if (unitView.equals(mLeftUnitView)) {
			return;
		}
		mLeftUnitView = unitView;

		mLeftImageView.setImageBitmap(unitImageFromUnitView(unitView));
		animateUnitImage(mLeftImageView);
		mLeftNameTextView.setText(unitView.getUnitData().getUnitModel().getName());
		mHPTextView.setText("40  /  40");
		mAttackTextView.setText("30");
		mSpeedTextView.setText("23");
		mDefenseTextView.setText("20");
		mMagicDefenseTextView.setText("15");
	}

	// TODO: バトルのダメージデータは内部で計算？ BattleResultクラスを引数で受け取る？
	public void updateForBattleData(PLMSUnitView leftUnitView, PLMSUnitView rightUnitView) {
		mLeftUnitView = leftUnitView;
		mRightUnitView = rightUnitView;
	}

	private Bitmap unitImageFromUnitView(PLMSUnitView unitView) {
		BitmapDrawable drawable = ((BitmapDrawable)unitView.getUnitImageView().getDrawable());
		return drawable.getBitmap();
	}

	private void animateUnitImage(ImageView imageView) {
		float baseX = 0;
		int width = imageView.getWidth();
		float startX = baseX - width * 3 / 4;
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "x", startX, baseX);
		objectAnimator.setDuration(100);
		objectAnimator.start();
	}
}
