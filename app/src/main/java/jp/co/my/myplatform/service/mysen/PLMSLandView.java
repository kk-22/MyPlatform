package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.Land.PLMSColorCover;


public class PLMSLandView extends FrameLayout {

	private ImageView mLandImage;
	private PLMSColorCover mMoveAreaCover;                // 移動可能マス
	private PLMSColorCover mAttackAreaCover;            // 攻撃可能マス

	private Point mPoint;
	private PLMSUnitView mUnitView;
	private PLMSLandData mLandData;

	public PLMSLandView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_land, this);
		mLandImage = (ImageView) findViewById(R.id.image_view);

		mLandData = new PLMSLandData();
		mMoveAreaCover = new PLMSColorCover(this, Color.argb(128, 0, 0, 255));
		mAttackAreaCover = new PLMSColorCover(this, Color.argb(128, 255, 0, 0));

		loadImage();
	}

	public void putUnitView(PLMSUnitView unitView) {
		mUnitView = unitView;
	}

	public void removeUnitView() {
		mUnitView = null;
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

	protected String getImageName() {
		return "grassland.gif";
	}

	// getter and setter
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

	public PLMSColorCover getMoveAreaCover() {
		return mMoveAreaCover;
	}

	public PLMSColorCover getAttackAreaCover() {
		return mAttackAreaCover;
	}

	// Debug
	public void debugLog(String message) {
		if (message == null) {
			message = "";
		}
		MYLogUtil.outputLog(" LandView x=" +mPoint.x +" y=" +mPoint.y +" " +message);
	}
}
