package jp.co.my.myplatform.service.wikipedia;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;

import jp.co.my.myplatform.service.model.PLDatabase;

@Table(database = PLDatabase.class)
public class PLWikipediaPageModel extends BaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;

	@Column
	private String title;
	@Column
	private String url;
	@Column
	private String html;
	@Column
	private Calendar registeredDate;	// 登録日時
	@Column
	private Calendar lastReadDate;		// 最終閲覧日時
	@Column
	private int scrollPosition;			// スクロール位置

	public PLWikipediaPageModel() {
		super();
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
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

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Calendar getRegisteredDate() {
		return registeredDate;
	}

	public void setRegisteredDate(Calendar registeredDate) {
		this.registeredDate = registeredDate;
	}

	public Calendar getLastReadDate() {
		return lastReadDate;
	}

	public void setLastReadDate(Calendar lastReadDate) {
		this.lastReadDate = lastReadDate;
	}

	public int getScrollPosition() {
		return scrollPosition;
	}

	public void setScrollPosition(int scrollPosition) {
		this.scrollPosition = scrollPosition;
	}
}
