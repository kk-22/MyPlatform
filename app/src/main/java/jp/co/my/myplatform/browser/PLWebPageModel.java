package jp.co.my.myplatform.browser;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;
import java.util.Objects;

import jp.co.my.myplatform.database.PLDatabase;

@Table(database = PLDatabase.class)
public class PLWebPageModel extends BaseModel {

	public static int TAB_NO_NONE					= -1;
	public static int TAB_NO_CURRENT				= 0; // Deprecated
	public static int BOOKMARK_DIRECTORY_NO_NONE	= -1;
	public static int BOOKMARK_DIRECTORY_NO_ROOT	= 0;

	@PrimaryKey(autoincrement = true)
	private long id;

	@Column
	private String title;
	@Column
	private String url;
	@Column
	private String userAgent;
	@Column
	private boolean isEnableScript;
	@Column
	private Calendar lastUpdateDate;

	// タブの番号
	// -1		タブではない
	// 0以上	該当するタブNo
	@Column
	private int tabNo;
	// ブックマークの階層
	// -1		ブックマークではない
	//  0		ルートディレクトリ
	//  1以上	該当するディレクリNo
	@Column
	private int bookmarkDirectoryNo;

	public PLWebPageModel() {
		super();
	}

	// ブックマークの保存時に使用
	public PLWebPageModel(String title, String url, int bookmarkDirectoryNo) {
		this(title, url);
		this.bookmarkDirectoryNo = bookmarkDirectoryNo;
	}

	// タブの保存時に使用
	public PLWebPageModel(String title, String url, Objects tabModel) {
		this(title, url);
		// TODO; set no from tabModel
		this.tabNo = TAB_NO_CURRENT;
	}

	private PLWebPageModel(String title, String url) {
		this();
		this.title = title;
		this.url = url;

		userAgent = "";
		isEnableScript = true;
		lastUpdateDate = Calendar.getInstance();
		tabNo = TAB_NO_NONE;
		bookmarkDirectoryNo = BOOKMARK_DIRECTORY_NO_NONE;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isEnableScript() {
		return isEnableScript;
	}

	public void setEnableScript(boolean enableScript) {
		isEnableScript = enableScript;
	}

	public Calendar getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Calendar lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public int getTabNo() {
		return tabNo;
	}

	public void setTabNo(int tabNo) {
		this.tabNo = tabNo;
	}

	public int getBookmarkDirectoryNo() {
		return bookmarkDirectoryNo;
	}

	public void setBookmarkDirectoryNo(int bookmarkDirectoryNo) {
		this.bookmarkDirectoryNo = bookmarkDirectoryNo;
	}
}
