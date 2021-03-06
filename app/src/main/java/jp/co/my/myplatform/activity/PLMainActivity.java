package jp.co.my.myplatform.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessaging;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLApplication;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLDeviceSetting;

public class PLMainActivity extends Activity {

	public static final String KEY_DO_NOT_START_SERVICE = "KEY_DO_NOT_START_SERVICE";
	public static final String KEY_LOGIN_TWITTER = "KEY_LOGIN_TWITTER";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setButtonEvent();

		FirebaseMessaging.getInstance().subscribeToTopic("MyPlatformFCM");

		Intent intent = getIntent();
		if (intent.getBooleanExtra(KEY_LOGIN_TWITTER, false)) {
			loginToTwitter();
		} else if (!intent.getBooleanExtra(KEY_DO_NOT_START_SERVICE, false)) {
			startServiceIfEnable();
			finish();
		}
	}

	private boolean enablePermission() {
		boolean result = true;
		// 権限管理画面を介す必要のある許可確認
		if (!Settings.canDrawOverlays(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, 1234);
			result = false;
		}
		if (!Settings.System.canWrite(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, 1235);
			result = false;
		}

		String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
		if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, permissions, 1);
			result = false;
		}

		return result;
	}

	public boolean canGetUsage(Context context) {
		AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
		int mode = appOpsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
				context.getPackageName());
		if (mode != AppOpsManager.MODE_DEFAULT) {
			return mode == AppOpsManager.MODE_ALLOWED;
		}
		// AppOpsの状態がデフォルトなら通常のpermissionチェックを行う。
		return context.checkPermission("android.permission.PACKAGE_USAGE_STATS",
				Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
	}

	private void startServiceIfEnable() {
		if (!canGetUsage(this)) {
			startActivity(new Intent("android.settings.USAGE_ACCESS_SETTINGS"));
		} else if (enablePermission()) {
			PLApplication.startCoreService();
		}
	}

	private void setButtonEvent() {
		findViewById(R.id.start_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startServiceIfEnable();
			}
		});
		findViewById(R.id.stop_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLApplication.stopCoreService();

				PLDeviceSetting.revertAllSetting();
			}
		});
	}

	private void loginToTwitter() {
		TwitterLoginButton button = new TwitterLoginButton(this);
		button.setCallback(new Callback<TwitterSession>() {
			@Override
			public void success(Result<TwitterSession> result) {
				TwitterSession session = result.data;
				MYLogUtil.showToast("Login success " +session.getUserName());
			}
			@Override
			public void failure(TwitterException exception) {
				MYLogUtil.showErrorToast("Login error of twitter");
			}
		});
		button.performClick();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// ツイッター登録画面からの復帰
		TwitterLoginButton button = new TwitterLoginButton(this);
		button.onActivityResult(requestCode, resultCode, data);
		PLCoreService.getNavigationController().displayNavigationIfNeeded();
		finish();
	}
}
