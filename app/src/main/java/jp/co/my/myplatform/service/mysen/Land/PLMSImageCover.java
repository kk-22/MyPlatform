package jp.co.my.myplatform.service.mysen.Land;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import jp.co.my.common.util.MYLogUtil;

public class PLMSImageCover extends PLMSAbstractCover {

	private String mImagePath;

	public PLMSImageCover(String imagePath) {
		super();
		mImagePath = imagePath;
	}

	@Override
	protected View createCoverView(Context context) {
		ImageView imageView = new ImageView(context);
		try {
			InputStream inputStream = context.getResources().getAssets().open(mImagePath);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			imageView.setImageBitmap(bitmap);
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
		}
		return imageView;
	}
}
