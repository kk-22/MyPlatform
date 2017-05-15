package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.my.common.util.MYImageUtil;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;


public class PLMSLandView extends FrameLayout {

	private ImageView mLandImage;

	private Point mPoint;
	private PLMSUnitView mUnitView;
	private PLMSLandData mLandData;

	public PLMSLandView(Context context, int landNumber) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_land, this);
		mLandImage = (ImageView) findViewById(R.id.image_view);

		mLandData = new PLMSLandData(landNumber);

		loadImage();
	}

	public void putUnitView(PLMSUnitView unitView) {
		mUnitView = unitView;
	}

	public void removeUnitView() {
		mUnitView = null;
	}

	private void loadImage() {
		String path = "land/" + getImageName();
		mLandImage.setImageBitmap(MYImageUtil.getBitmapFromImagePath(path, getContext()));
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

	// Debug
	public String debugLog() {
		String debugText = " LandView " + " x=" + mPoint.x + " y=" + mPoint.y;
		MYLogUtil.outputLog(debugText);
		return debugText;
	}

	public String getTextPoint() {
		return "x=" + mPoint.x + " y=" + mPoint.y;
	}
}
