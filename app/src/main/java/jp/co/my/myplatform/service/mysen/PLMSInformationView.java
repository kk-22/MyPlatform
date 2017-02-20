package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import jp.co.my.myplatform.R;


public class PLMSInformationView extends LinearLayout {

	private PLMSUnitView mLeftUnitView;
	private PLMSUnitView mRightUnitView;

	private ImageView mLeftImageView;

	public PLMSInformationView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_information, this);
		mLeftImageView = (ImageView) findViewById(R.id.left_image);
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
}
