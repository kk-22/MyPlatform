package jp.co.my.myplatform.service.explorer;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLImagePopover extends PLPopoverView {

	public PLImagePopover(File file) {
		super(R.layout.popover_full_image);
		ImageView imageView = (ImageView) findViewById(R.id.image_view);

		Uri uri = Uri.fromFile(file);
		imageView.setImageURI(uri);

		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLImagePopover.this.removeFromNavigation();
			}
		});
	}
}
