package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import jp.co.my.myplatform.R;


public class PLMSInformationView extends LinearLayout {

	public PLMSInformationView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_information, this);
	}

	public PLMSInformationView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSInformationView(Context context) {
		this(context, null);
	}
}
