package jp.co.my.myplatform.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.List;

import jp.co.my.myplatform.content.PLHomeContent;
import jp.co.my.myplatform.core.PLCoreService;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class PLNavigationBarView extends LinearLayout {

	private static final int MAX_NUMBER_OF_CHILD = 5; // 戻るボタンとスペースを含む最大数

	private Space mSpace;
	private Button mBackButton;

	public PLNavigationBarView(Context context) {
		this(context, null);
	}
	public PLNavigationBarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public PLNavigationBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mBackButton = new Button(getContext());
		mBackButton.setText("BACK");
		mBackButton.setTextSize(10);
		addView(mBackButton, getButtonLayoutParams());
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PLCoreService.getNavigationController().getCurrentView().canGoBackContent()) {
					PLCoreService.getNavigationController().popView();
				}
			}
		});
		mBackButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				PLCoreService.getNavigationController().pushView(PLHomeContent.class);
				return true;
			}
		});

		mSpace = new Space(getContext());
		addView(mSpace);
		updateSpaceWeight();
	}

	public void updateSubLayoutParams() {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (mSpace != child) {
				child.setLayoutParams(getButtonLayoutParams());
			}
		}
	}

	// 古いボタンを削除して一括追加
	public void resetButtons(List<Button> buttonList) {
		// 戻るボタンと mSpace 以外を取り除く
		removeViews(1, getChildCount() - 2);
		if (buttonList != null) {
			for (Button button : buttonList) {
				addButton(button);
			}
		}
		updateSpaceWeight();
	}

	// ボタンを1つだけ追加
	public void showButton(Button button) {
		addButton(button);
		updateSpaceWeight();
	}

	public void setBackEnable(boolean enabled) {
		mBackButton.setEnabled(enabled);
	}

	private void addButton(Button button) {
		// スペースの前に追加
		int childCount = getChildCount();
		if (MAX_NUMBER_OF_CHILD <= childCount) {
			return;
		}
		addView(button, childCount - 1, getButtonLayoutParams());
	}

	private void updateSpaceWeight() {
		LayoutParams params = (LayoutParams) mSpace.getLayoutParams();
		params.weight = MAX_NUMBER_OF_CHILD - getChildCount() + 1;
	}

	private LinearLayout.LayoutParams getButtonLayoutParams() {
		LinearLayout.LayoutParams params;
		if (PLCoreService.getCoreService().isPortrait()) {
			params = new LinearLayout.LayoutParams(0, MATCH_PARENT);
		} else {
			params = new LinearLayout.LayoutParams(MATCH_PARENT, 0);
		}
		params.weight = 1;
		return params;
	}
}
