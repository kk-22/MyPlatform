package jp.co.my.myplatform.service.explorer;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.popover.PLPopoverView;
import jp.co.my.myplatform.service.view.PLLoadImageView;

public class PLImagePopover extends PLPopoverView {

	private int mCurrentIndex;
	private PLLoadImageView mLoadImage;
	private List<File> mImageFileList;
	private LruCache<String, Bitmap> mCache;
	private PLOnSetImageListener mListener;

	public PLImagePopover(List<File> fileList, File imageFile, LruCache<String, Bitmap> imageCache) {
		super(R.layout.popover_full_image);
		mCache = imageCache;
		mLoadImage = (PLLoadImageView) findViewById(R.id.load_image_view);

		mImageFileList = new ArrayList<>();
		for (File file : fileList) {
			String fileName = file.getName();
			if (MYStringUtil.isImageFileName(fileName)) {
				mImageFileList.add(file);
			}
		}
		int index = mImageFileList.indexOf(imageFile);
		if (index == -1) {
			MYLogUtil.showErrorToast(imageFile.getName() + " is not image");
			return;
		}
		setImage(index);
		initClickEvent();
	}

	private void setImage(int index) {
		mCurrentIndex = index;
		File file = mImageFileList.get(index);
		mLoadImage.loadImageFile(file, mCache);

		if (mListener != null) {
			mListener.onSetImage(file);
		}
	}

	private void initClickEvent() {
		mLoadImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLImagePopover.this.removeFromContentView();
				if (mListener != null) {
					mListener.onSetImage(null);
				}
			}
		});
		findViewById(R.id.left_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = mCurrentIndex - 1;
				if (index < 0) {
					index = mImageFileList.size() - 1;
				}
				setImage(index);
			}
		});
		findViewById(R.id.right_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = mCurrentIndex + 1;
				if (index >= mImageFileList.size()) {
					index = 0;
				}
				setImage(index);
			}
		});	}

	public interface PLOnSetImageListener {
		void onSetImage(File file);
	}

	public void setListener(PLOnSetImageListener listener) {
		mListener = listener;
		if (mListener != null) {
			// コンストラクタで設定した画像パスを保存
			File file = mImageFileList.get(mCurrentIndex);
			mListener.onSetImage(file);
		}
	}
}
