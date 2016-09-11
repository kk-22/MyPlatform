package jp.co.my.myplatform.service.explorer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLImagePopover extends PLPopoverView {

	private int mCurrentIndex;
	private ImageView mImageView;
	private List<File> mImageFileList;
	private LruCache<String, Bitmap> mCache;

	public PLImagePopover(List<File> fileList, File imageFile, LruCache<String, Bitmap> imageCache) {
		super(R.layout.popover_full_image);
		mCache = imageCache;
		mImageView = (ImageView) findViewById(R.id.image_view);

		mImageFileList = new ArrayList<>();
		for (File file : fileList) {
			String fileName = file.getName();
			if (MYStringUtil.isImageFileName(fileName)) {
				mImageFileList.add(file);
			}
		}
		int index = mImageFileList.indexOf(imageFile);
		if (index == -1) {
			MYLogUtil.showErrorToast(imageFile.getName() + "is not image");
			return;
		}
		setImage(index);
		initClickEvent();
	}

	private void setImage(int index) {
		mCurrentIndex = index;

		File file = mImageFileList.get(index);
		Bitmap cacheImage = mCache.get(file.getName());
		if (cacheImage != null) {
			mImageView.setImageBitmap(cacheImage);
		} else {
			Uri uri = Uri.fromFile(file);
			mImageView.setImageURI(uri);
		}
	}

	private void initClickEvent() {
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLImagePopover.this.removeFromContentView();
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
}
