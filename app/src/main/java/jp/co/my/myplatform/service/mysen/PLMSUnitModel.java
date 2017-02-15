package jp.co.my.myplatform.service.mysen;

public class PLMSUnitModel extends Object {

	private String mName;
	private String mSmallImageName;
	private PLMSABranchData mBranch;

	public PLMSUnitModel() {
		// ダミー
		mName = "ルキナ";
		mSmallImageName = "rukina.png";
		mBranch = new PLMSABranchData();
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mSmallImageName;
	}

	public PLMSABranchData getBranch() {
		return mBranch;
	}
}
