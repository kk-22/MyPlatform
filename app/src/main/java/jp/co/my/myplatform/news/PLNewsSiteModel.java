package jp.co.my.myplatform.news;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.myplatform.database.PLDatabase;

@ModelContainer
@Table(database = PLDatabase.class)
public class PLNewsSiteModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;
	@ForeignKey
	ForeignKeyContainer<PLNewsGroupModel> groupForeign;

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

	public void associateGroup(PLNewsGroupModel model) {
		this.groupForeign = FlowManager.getContainerAdapter(PLNewsGroupModel.class)
				.toForeignKeyContainer(model);
	}

	public ForeignKeyContainer<PLNewsGroupModel> getGroupForeign() {
		return groupForeign;
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
