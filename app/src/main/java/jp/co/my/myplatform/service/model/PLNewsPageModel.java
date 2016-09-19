package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.Calendar;

@Table(database = PLDatabase.class)
public class PLNewsPageModel extends BaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;
	@ForeignKey
	ForeignKeyContainer<PLNewsGroupModel> groupForeign;
	@ForeignKey
	ForeignKeyContainer<PLNewsSiteModel> siteForeign;

	@Column
	private String title;
	@Column
	private String url;
	@Column
	private boolean alreadyRead;
	@Column
	private Calendar postedDate;		// RSSに記載された登録日

	public PLNewsPageModel() {
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

	public void associateSite(PLNewsSiteModel model) {
		this.siteForeign = FlowManager.getContainerAdapter(PLNewsSiteModel.class)
				.toForeignKeyContainer(model);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isAlreadyRead() {
		return alreadyRead;
	}

	public void setAlreadyRead(boolean alreadyRead) {
		this.alreadyRead = alreadyRead;
	}

	public Calendar getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(Calendar postedDate) {
		this.postedDate = postedDate;
	}
}
