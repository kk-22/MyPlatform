package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;


public class PLMSLandView extends FrameLayout {

	private ImageView mLandImage;
	private View mMoveAreaView;				// 移動可能マスを表す

	private Point mPoint;
	private PLMSUnitView mUnitView;
	private PLMSLandData mLandData;

	public PLMSLandView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_land, this);
		mLandImage = (ImageView) findViewById(R.id.image_view);

		mLandData = new PLMSLandData();

		loadImage();
	}

	public void putUnitView(PLMSUnitView unitView) {
		mUnitView = unitView;
	}

	private void loadImage() {
		try {
			String path = "land/" +getImageName();
			InputStream inputStream = getResources().getAssets().open(path);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			mLandImage.setImageBitmap(bitmap);
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	public void showMoveArea() {
		if (mMoveAreaView == null) {
			mMoveAreaView = new View(getContext());
			mMoveAreaView.setBackgroundColor(Color.argb(128, 0, 0, 255));
			addView(mMoveAreaView, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		}
		mMoveAreaView.setVisibility(VISIBLE);
	}

	public void hideMoveArea() {
		if (mMoveAreaView != null) {
			mMoveAreaView.setVisibility(GONE);
		}
	}

	public boolean isShowingMoveArea() {
		return (mMoveAreaView != null && mMoveAreaView.getVisibility() == View.VISIBLE);
	}

	protected String getImageName() {
		return "grassland.gif";
	}

	public Point getPoint() {
		return mPoint;
	}

	public void setPoint(Point point) {
		mPoint = point;
	}

	public PLMSLandData getLandData() {
		return mLandData;
	}

	public PLMSUnitView getUnitView() {
		return mUnitView;
	}
}
