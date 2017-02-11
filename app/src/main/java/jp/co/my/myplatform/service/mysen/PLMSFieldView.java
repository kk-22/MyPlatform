package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import jp.co.my.myplatform.R;


public class PLMSFieldView extends FrameLayout {

	public PLMSFieldView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_field, this);
	}

	public PLMSFieldView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSFieldView(Context context) {
		this(context, null);
	}
}
