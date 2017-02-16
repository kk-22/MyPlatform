package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;

public class PLMSUnitView extends FrameLayout {

	private PLMSUnitModel mUnitModel;
	private PLMSLandView mLandView;
	private Point mCurrentPoint;

	public PLMSUnitView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_view, this);
		mUnitModel = new PLMSUnitModel();

		// ダミー
		mCurrentPoint = new Point(1, 1);

		loadImage();
	}

	public void moveToLand(PLMSLandView landView) {
		mLandView = landView;
		mCurrentPoint = landView.getPoint();
		landView.putUnitView(this);
	}

	private void loadImage() {
		ImageView imageView = (ImageView) findViewById(R.id.image_view);
		try {
			String path = mUnitModel.getSmallImagePath();
			InputStream inputStream = getResources().getAssets().open(path);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			imageView.setImageBitmap(bitmap);
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
	}

	public PLMSUnitModel getUnitModel() {
		return mUnitModel;
	}

	public Point getCurrentPoint() {
		return mCurrentPoint;
	}

	public PLMSLandView getLandView() {
		return mLandView;
	}
}
