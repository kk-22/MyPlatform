package jp.co.my.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class MYImageUtil {

	private MYImageUtil() {
	}

	public static ImageView getImageViewFromImagePath(String imagePath, Context context) {
		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(getBitmapFromImagePath(imagePath, context));
		return imageView;
	}

	public static Bitmap getBitmapFromImagePath(String imagePath, Context context) {
		try {
			InputStream inputStream = context.getResources().getAssets().open(imagePath);
			return BitmapFactory.decodeStream(inputStream);
		} catch (IOException e) {
			MYLogUtil.showExceptionToast(e);
			return null;
		}
	}
}
