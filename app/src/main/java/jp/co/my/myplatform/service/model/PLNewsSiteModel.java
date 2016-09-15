package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = PLDatabase.class)
public class PLNewsSiteModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;

	@Column
	private int groupNo;
	@Column
	private String url;
	@Column
	private String memo;			// アプリ操作で設定可能なメモ
	@Column
	private boolean enableScript;	// WebView画面でのデフォルトスクリプト設定
	@Column
	private boolean enablePCViewr;	// WebView画面でのユーザエージェント設定

	public PLNewsSiteModel() {
		super();
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(int groupNo) {
		this.groupNo = groupNo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public boolean isEnableScript() {
		return enableScript;
	}

	public void setEnableScript(boolean enableScript) {
		this.enableScript = enableScript;
	}

	public boolean isEnablePCViewr() {
		return enablePCViewr;
	}

	public void setEnablePCViewr(boolean enablePCViewr) {
		this.enablePCViewr = enablePCViewr;
	}
}
