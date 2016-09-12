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
				FileInputStream stream = new FileInputStream(file);
				Bitmap bitmap = BitmapFactory.decodeStream(stream);
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
