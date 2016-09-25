package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;

@ModelContainer
@Table(database = PLDatabase.class)
public class PLNewsGroupModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;

	@Column
	private String color;
	@Column
	private String title;
	@Column
	private int updateInterval;				// 自動更新間隔分
	@Column
	private boolean isAutoUpdate;			// フェッチ後の自動更新有無
	@Column
	private Calendar fetchedDate;			// 最終更新日
	@Column
	private Calendar readDate;				// 最終閲覧日

	private PLModelContainer<PLNewsSiteModel> siteContainer;
	private PLModelContainer<PLNewsPageModel> pageContainer;

	public PLNewsGroupModel() {
		super();
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public boolean isAutoUpdate() {
		return isAutoUpdate;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		isAutoUpdate = autoUpdate;
	}

	public Calendar getFetchedDate() {
		return fetchedDate;
	}

	public void setFetchedDate(Calendar fetchedDate) {
		this.fetchedDate = fetchedDate;
	}

	public Calendar getReadDate() {
		return readDate;
	}

	public void setReadDate(Calendar readDate) {
		this.readDate = readDate;
	}

	public PLModelContainer<PLNewsSiteModel> getSiteContainer() {
		if (siteContainer == null) {
			siteContainer = new PLModelContainer<>(SQLite.select()
					.from(PLNewsSiteModel.class)
					.where(PLNewsSiteModel_Table.groupForeign_no.eq(no)));
		}
		return siteContainer;
	}

	public PLModelContainer<PLNewsPageModel> getPageContainer() {
		if (pageContainer == null) {
			pageContainer = new PLModelContainer<>(SQLite.select()
					.from(PLNewsPageModel.class)
					.where(PLNewsPageModel_Table.groupForeign_no.eq(no)));
		}
		return pageContainer;
	}
}
