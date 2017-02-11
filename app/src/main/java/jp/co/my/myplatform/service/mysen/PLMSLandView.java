package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;


public class PLMSLandView extends FrameLayout {

	private ImageView mLandImage;

	public PLMSLandView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_land, this);
		mLandImage = (ImageView) findViewById(R.id.image_view);

		loadImage();
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
}
