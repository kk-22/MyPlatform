package jp.co.my.myplatform.service.mysen;

public class PLMSUnitModel extends Object {

	private String mName;
	private String mSmallImageName;

	public PLMSUnitModel() {
		// ダミー
		mName = "ルキナ";
		mSmallImageName = "rukina.gif";
	}

	public String getSmallImagePath() {
		return "unit/" +mSmallImageName;
	}
}
