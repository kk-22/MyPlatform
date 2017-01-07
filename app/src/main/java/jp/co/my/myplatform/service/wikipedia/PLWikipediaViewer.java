package jp.co.my.myplatform.service.wikipedia;

import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;
import jp.co.my.myplatform.service.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.popover.PLActionListPopover;
import jp.co.my.myplatform.service.popover.PLListPopover;

public class PLWikipediaViewer extends PLContentView
		implements PLWikipediaHtmlEncoder.PLWikipediaEncodeListener, PLActionListPopover.PLActionListListener<PLWikipediaPageModel> {

	private TextView mTextView;
	private TextView mLoadingText;
	private PLWikipediaPageModel mCurrentPageModel;
	private PLWikipediaHtmlEncoder mEncoder;
	private ArrayList<PLWikipediaPageModel> mPageList;

	private Handler mMainHandler;

	public PLWikipediaViewer() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_wikipedia_viewer, this);
		mTextView = (TextView) findViewById(R.id.html_text);
		mLoadingText = (TextView) findViewById(R.id.loading_text);
		mMainHandler = new Handler();
		mEncoder = new PLWikipediaHtmlEncoder(this);
		mPageList = new ArrayList<>();

		initClickEvent();
		loadFirstPage();
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		mEncoder.cancel();
		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
	}

	private void loadFirstPage() {
		PLModelContainer<PLWikipediaPageModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLWikipediaPageModel.class)
				.orderBy(PLWikipediaPageModel_Table.lastReadDate, false));
		container.loadList(new PLModelContainer.PLOnModelLoadThreadListener<PLWikipediaPageModel>() {
			@Override
			public void onLoad(List<PLWikipediaPageModel> modelLists) {
				if (modelLists.size() > 0) {
					mPageList.addAll(modelLists);
					loadPageModel(modelLists.get(0));
					return;
				}
				String url = "https://ja.wikipedia.org/wiki/%E7%B5%90%E5%9F%8E%E7%A7%80%E5%BA%B7";
				requestPage(url);
			}
		}, null);
	}

	private void requestPage(final String url) {
		showIndicator();
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(final String response) {
				PLWikipediaPageModel pageModel = new PLWikipediaPageModel();
				try {
					String title = url.replaceFirst(".*/", "");
					String decodeTitle = URLDecoder.decode(title, "UTF8");
					pageModel.setTitle(decodeTitle);
				} catch (UnsupportedEncodingException e) {
					MYLogUtil.showExceptionToast(e);
					pageModel.setTitle("UnsupportedEncodingException error");
				}
				pageModel.setUrl(url);
				pageModel.setHtml(response);
				pageModel.setRegisteredDate(Calendar.getInstance());
				pageModel.save();
				mPageList.add(pageModel);

				loadPageModel(pageModel);
			}
		};
		Response.ErrorListener error = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MYLogUtil.outputErrorLog("Fetch page error " + error.toString());
				hideIndicator();
			}
		};
		StringRequest request = new StringRequest(url, listener, error);
		PLVolleyHelper volleyHelper = PLCoreService.getVolleyHelper();
		volleyHelper.addRequest(request, this.getClass());
	}

	private void loadPageModel(final PLWikipediaPageModel pageModel) {
		mCurrentPageModel = pageModel;
		new Thread(new Runnable() {
			@Override
			public void run() {
				mEncoder.encodeHtml(pageModel);
			}
		}).start();
	}

	private void initClickEvent() {
		findViewById(R.id.list_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<String> titleArray = new ArrayList<>();
				List<PLWikipediaPageModel> modelList = new ArrayList<>(mPageList);
				for (PLWikipediaPageModel model : modelList) {
					titleArray.add(model.getTitle());
				}
				new PLActionListPopover<>(titleArray, modelList, PLWikipediaViewer.this).showPopover(new PLRelativeLayoutController(v));
			}
		});
		findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] titles = {"削除"};
				new PLListPopover(titles, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						removePageModel(mCurrentPageModel);
						PLWikipediaViewer.this.removeTopPopover();
					}
				}).showPopover();
			}
		});
	}

	private void removePageModel(PLWikipediaPageModel pageModel) {
		mPageList.remove(pageModel);
		pageModel.delete();
	}

	private void showIndicator() {
		MYOtherUtil.runOnUiThread(getContext(), mMainHandler, new Runnable() {
			@Override
			public void run() {
				mLoadingText.setVisibility(VISIBLE);
			}
		});
	}

	private void hideIndicator() {
		mLoadingText.setVisibility(GONE);
	}

	// PLWikipediaEncodeListener
	@Override
	public void finishedEncode(SpannableStringBuilder strBuilder) {
		mTextView.setText(strBuilder);
		mTextView.setMovementMethod(LinkMovementMethod.getInstance());
		hideIndicator();
	}

	@Override
	public void onClickLink(String linkUrl) {
		// 保存済みでないかチェック
		String fullUrl = "https://ja.wikipedia.org" + linkUrl;
		for (PLWikipediaPageModel model : mPageList) {
			if (model.getUrl().equals(fullUrl)) {
				MYLogUtil.showToast("既に登録済み " +model.getTitle());
				return;
			}
		}
		requestPage(fullUrl);
	}

	// PLActionListListener of page list
	@Override
	public void onItemClick(PLWikipediaPageModel object, PLActionListPopover listPopover) {
		loadPageModel(object);
		listPopover.removeFromContentView();
	}

	@Override
	public void onActionClick(final PLWikipediaPageModel object, final PLActionListPopover<PLWikipediaPageModel> listPopover, View buttonView) {
		String[] titles = {"削除"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MYLogUtil.showToast("ページ削除：" +object.getTitle());
				removePageModel(object);
				listPopover.removeObject(object);
				// アクションPopoverだけ削除してリストは残す
				PLWikipediaViewer.this.removeTopPopover();
			}
		}).showPopover(new PLRelativeLayoutController(buttonView));
	}
}
