package jp.co.my.myplatform.news;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import jp.co.my.myplatform.database.PLDatabase;

@Table(database = PLDatabase.class)
public class PLNewsSiteModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;
	@ForeignKey
	PLNewsGroupModel groupForeign;

	@Column
	private String url;
	@Column
	private String name;
	@Column
	private String memo;			// アプリ操作で設定可能なメモ
	@Column
	private boolean enableScript;	// WebView画面でのデフォルトスクリプト設定
	@Column
	private boolean enablePCViewer;	// WebView画面でのユーザエージェント設定

	public PLNewsSiteModel() {
		super();
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public PLNewsGroupModel getGroupForeign() {
		return groupForeign;
	}

	public void setGroupForeign(PLNewsGroupModel groupForeign) {
		this.groupForeign = groupForeign;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean isEnablePCViewer() {
		return enablePCViewer;
	}

	public void setEnablePCViewer(boolean enablePCViewer) {
		this.enablePCViewer = enablePCViewer;
	}
}
