package jp.co.my.myplatform.service.core;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import jp.co.my.common.util.MYLogUtil;


public class PLFirebaseInstanceIdService extends FirebaseInstanceIdService {

	@Override
	public void onTokenRefresh() {
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		MYLogUtil.outputLog("Refreshed token: " + refreshedToken);
	}
}
