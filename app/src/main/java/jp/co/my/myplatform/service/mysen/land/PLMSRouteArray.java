package jp.co.my.myplatform.service.mysen.land;

import jp.co.my.common.util.MYArrayList;

/*
1つの PLMSLandView につき、本クラスのインスタンスを1つ生成する。
その PLMSLandView までのルートを管理する。
 */
public class PLMSRouteArray extends MYArrayList<PLMSLandRoute> {

	private boolean isAlreadySearched; // 本インスタンスが担当する PLMSLandView が1度でも探索済みのならtrue

	public PLMSRouteArray() {
	}

	public void didSearch() {
		isAlreadySearched = true;
	}

	// getter
	public boolean isAlreadySearched() {
		return isAlreadySearched;
	}
}
