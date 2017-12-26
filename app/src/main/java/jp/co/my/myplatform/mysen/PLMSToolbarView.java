package jp.co.my.myplatform.mysen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import jp.co.my.myplatform.R;


public class PLMSToolbarView extends LinearLayout {

	public PLMSToolbarView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_toolbar, this);
	}

	public PLMSToolbarView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSToolbarView(Context context) {
		this(context, null);
	}
}
