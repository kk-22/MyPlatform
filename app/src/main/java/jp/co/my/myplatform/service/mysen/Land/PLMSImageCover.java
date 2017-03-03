package jp.co.my.myplatform.service.mysen.Land;

import android.view.View;

import jp.co.my.common.util.MYImageUtil;

public class PLMSImageCover extends PLMSAbstractCover {

	private String mImagePath;

	public PLMSImageCover(String imagePath) {
		super();
		mImagePath = imagePath;
	}

	@Override
	protected View createCoverView() {
		return MYImageUtil.getImageViewFromImagePath(mImagePath, getContext());
	}
}
