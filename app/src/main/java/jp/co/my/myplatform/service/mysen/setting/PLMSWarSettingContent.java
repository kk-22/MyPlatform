package jp.co.my.myplatform.service.mysen.setting;

import android.view.LayoutInflater;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.mysen.PLMSArgument;


public class PLMSWarSettingContent extends PLContentView {

	private PLMSArgument mArgument;

	public PLMSWarSettingContent(PLMSArgument argument) {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_war_setting, this);
		mArgument = argument;

	}
}
