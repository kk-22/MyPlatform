package jp.co.my.myplatform.service.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;

public class PLLoadImageView extends FrameLayout {

	private File mFile;
	private ImageView mImageView;
	private LruCache<String, Bitmap> mCache;
	private PLImageLoadTask mLoadTask;

	public PLLoadImageView(Context context) {
		this(context, null);
	}
	public PLLoadImageView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	public PLLoadImageView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);

		LayoutInflater.from(context).inflate(R.layout.view_load_image, this);
		mImageView = (ImageView)findViewById(R.id.image_view);
	}

	public void loadImageResource(@DrawableRes int resId) {
		cancelLoadTask();
		mImageView.setImageResource(resId);
	}

	public void loadImageFile(File imageFile, LruCache<String, Bitmap> cache) {
		mFile = imageFile;
		mCache = cache;

		cancelLoadTask();
		Bitmap image = mCache.get(mFile.getName());
		if (image != null) {
			mImageView.setImageBitmap(image);
			return;
		}

		mImageView.setImageResource(R.drawable.file);
		mLoadTask = new PLImageLoadTask();
		mLoadTask.execute(mFile);
	}

	private void cancelLoadTask() {
		if (mLoadTask == null) {
			return;
		}
		mLoadTask.cancel(true);
		mLoadTask = null;
	}

	private class PLImageLoadTask extends AsyncTask<File, Integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(File... params) {
			try {
				File file = params[0];
				// 画像サイズを取得
				InputStream inputStream = new FileInputStream(file);
				BitmapFactory.Options imageOptions = new BitmapFactory.Options();
				imageOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(inputStream, null, imageOptions);
				inputStream.close();
				// MYLogUtil.outputLog("Original Image Size: " + imageOptions.outWidth + " x " + imageOptions.outHeight);

				int maxImageSize = 500;
				float imageScaleWidth = (float)imageOptions.outWidth / maxImageSize;
				float imageScaleHeight = (float)imageOptions.outHeight / maxImageSize;
				Bitmap bitmap;
				inputStream = new FileInputStream(file);
				if (imageScaleWidth <= 2 || imageScaleHeight <= 2) {
					bitmap = BitmapFactory.decodeStream(inputStream);
				} else {
					// 縮小できるサイズならば、 縦横の小さい方のスケールに合わせて縮小して読み込む
					imageOptions.inJustDecodeBounds = false;
					int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));
					// inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下
					for (int i = 2; i <= imageScale; i *= 2) {
						imageOptions.inSampleSize = i;
					}
					bitmap = BitmapFactory.decodeStream(inputStream, null, imageOptions);
					// MYLogUtil.outputLog("Sample Size: 1/" + imageOptions2.inSampleSize);
				}
				inputStream.close();

				return bitmap;
			} catch (Exception e) {
				MYLogUtil.showExceptionToast(e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap == null) {
				return;
			}
			mImageView.setImageBitmap(bitmap);
			mCache.put(mFile.getName(), bitmap);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			MYLogUtil.outputLog("onCancelled");
		}
	}
}
