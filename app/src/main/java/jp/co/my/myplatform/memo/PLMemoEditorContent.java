package jp.co.my.myplatform.memo;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.view.LongClickRepeatAdapter;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLMemoEditorContent extends PLContentView {

	private static final String KEY_LAST_SCROLL_Y = "KEY_LAST_SCROLL_Y";

	private PLMemoEditText mEditText;
	private PLMemoReadWriter mReadWriter;
	private Button mBackButton, mForwardButton, mCloseButton;
	private ScrollView mScrollView;
	private int mPrevScrollY;

	public PLMemoEditorContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_memo_editor, this);
		mCloseButton = findViewById(R.id.close_button);
		mBackButton = findViewById(R.id.back_button);
		mForwardButton = findViewById(R.id.forward_button);
		mScrollView = findViewById(R.id.scroll_view);
		mEditText = findViewById(R.id.memo_edit);
		mEditText.setEditorContent(this);
		mEditText.setScrollView(mScrollView);

		LongClickRepeatAdapter.bless(mBackButton, mForwardButton, findViewById(R.id.delete_line_button),
				findViewById(R.id.line_down_button), findViewById(R.id.line_up_button));

		mReadWriter = new PLMemoReadWriter(mEditText);
		initEditTextEvent();
		initButtonEvent();

		mReadWriter.loadFirstMemo();

		SharedPreferences pref = MYLogUtil.getPreference();
		mPrevScrollY = pref.getInt(KEY_LAST_SCROLL_Y, 0);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// ??????????????????????????????????????????????????????????????????????????????
		(new Handler()).postDelayed(new Runnable() {
			@Override
			public void run() {
				mScrollView.setScrollY(mPrevScrollY);
			}
		}, 100);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mReadWriter.saveToFile();
		mPrevScrollY = mScrollView.getScrollY();

		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.putInt(KEY_LAST_SCROLL_Y, mPrevScrollY);
		editor.commit();
	}

	void updateButtons() {
		mBackButton.setEnabled(mEditText.hasBackText());
		mForwardButton.setEnabled(mEditText.hasForwardText());
	}

	private void initEditTextEvent() {
		mEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					return onBackKey();
				} else if (keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}
				return false;
			}
		});
		mEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					v.performClick(); // onTouch???????????????????????????
					scrollIfNeeded(event);
				}
				return false;
			}
		});
	}

	// onTouch ???????????????false???????????????????????????????????????
	@SuppressLint("ClickableViewAccessibility")
	private void initButtonEvent() {
		mScrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (mEditText.hasFocus()) {
					hideOtherViews();
				}
				return false;
			}
		});

		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.hideKeyboard();
				PLCoreService.getNavigationController().popView();
			}
		});
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.goBack();
			}
		});
		mForwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.goForward();
			}
		});
		findViewById(R.id.line_up_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.moveSelectionLine(false);
			}
		});
		findViewById(R.id.delete_line_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.deleteSelectionLine();
			}
		});
		findViewById(R.id.line_down_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.moveSelectionLine(true);
			}
		});
		findViewById(R.id.hide_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});
		findViewById(R.id.copy_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayClipboardList();
			}
		});

		addNavigationButton("??????", new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ???????????????????????????
				mReadWriter.saveToFile();
				displayFileList();
			}
		});
		addNavigationButton("INDEX", new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayIndexList();
			}
		});
		addNavigationButton("FILE", new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayFileOperationPopup();
			}
		});
	}

	private void displayFileList() {
		removeTopPopover();

		final File[] files = mReadWriter.memoFiles();
		ArrayList<String> nameList = new ArrayList<>();
		for (File file : files) {
			nameList.add(file.getName());
		}
		nameList.add("????????????");
		String[] titles = nameList.toArray(new String[nameList.size()]);
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLMemoEditorContent.this.removeTopPopover();
				mScrollView.setScrollY(0);

				if (files.length == position) {
					// ??????????????????
					mReadWriter.loadedMemo(null, true);
					mEditText.setText("");
					MYLogUtil.showToast("????????????");
					return;
				}

				File file = files[position];
				String name = file.getName();
				if (name.equals(mReadWriter.getCurrentName())) {
					MYLogUtil.showToast("????????????????????????");
				} else {
					mEditText.clearHistory();
					mReadWriter.loadFromFile(name);
					mReadWriter.loadedMemo(name, true);
				}
			}
		}).showPopover();
	}

	private void displayFileOperationPopup() {
		PLListPopover.showItems(new PLListPopover.PLListItem("????????????", new Runnable() {
			@Override
			public void run() {
				new PLTextFieldPopover(new PLTextFieldPopover.OnEnterListener() {
					@Override
					public boolean onEnter(View v, String text) {
						if (mReadWriter.renameCurrentFile(text)) {
							MYLogUtil.showToast("??????????????????");
						} else {
							MYLogUtil.showToast("?????????????????????????????????");
						}
						return true;
					}
				}).showPopover();
			}
		}), new PLListPopover.PLListItem("??????", new Runnable() {
			@Override
			public void run() {
				new PLConfirmationPopover("????????????", new PLConfirmationPopover.PLConfirmationListener() {
					@Override
					public void onClickButton(boolean isYes) {
						PLMemoEditorContent.this.removeTopPopover();
						if (mReadWriter.deleteCurrentFile()) {
							MYLogUtil.showToast("????????????");
						} else {
							MYLogUtil.showToast("???????????????????????????");
						}
						displayFileList();
					}
				}, null);
			}
		}), new PLListPopover.PLListItem("??????", new Runnable() {
			@Override
			public void run() {
				mReadWriter.saveToFile();
				MYLogUtil.showToast("??????");
			}
		}));
	}

	private void displayClipboardList() {
		hideOtherViews();
		PLListPopover.showItems(
				new PLListPopover.PLListItem("?????????", new Runnable() {
					@Override
					public void run() {
						copyToClipboard(false);
					}
				})
				, new PLListPopover.PLListItem("????????????", new Runnable() {
					@Override
					public void run() {
						copyToClipboard(true);
					}
				})
				, new PLListPopover.PLListItem("????????????", new Runnable() {
					@Override
					public void run() {
						ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
						if (null == clipboardManager) {
							return;
						}
						ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
						String text = item.getText().toString();
						int start = mEditText.getSelectionStart();
						int end = mEditText.getSelectionEnd();
						Editable editable = mEditText.getText();
						editable.replace(Math.min(start, end), Math.max(start, end), text);
					}
				})
		);
	}

	private void copyToClipboard(boolean needCut) {
		int start = mEditText.getSelectionStart();
		int end = mEditText.getSelectionEnd();
		Editable editable = mEditText.getText();
		String selectingString = editable.subSequence(Math.min(start, end), Math.max(start, end)).toString();
		if (selectingString.isEmpty()) {
			return;
		}
		ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		if (null == clipboardManager) {
			return;
		}
		clipboardManager.setPrimaryClip(ClipData.newPlainText("", selectingString));

		if (needCut) {
			editable.delete(Math.min(start, end), Math.max(start, end));
		}
	}

	private void displayIndexList() {
		removeTopPopover();

		final ArrayList<Integer> positions = new ArrayList<>();
		ArrayList<String> titleArray = new ArrayList<>();
		Pattern pattern = Pattern.compile("[ ???]*[??????][^\n]*");
		Matcher matcher = pattern.matcher(mEditText.getText());
		while (matcher.find()) {
			String title = matcher.group();
			titleArray.add(title);
			positions.add(matcher.end());
		}
		new PLListPopover(titleArray.toArray(new String[0]), new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				mEditText.scrollWithSelection(true, positions.get(i));
			}
		}).setBigWidth().setCellResource(R.layout.cell_oneline_title).showPopover();
	}

	private void hideOtherViews() {
		// ?????????????????????
		mEditText.clearFocus();
		mEditText.hideKeyboard();
	}

	// ????????????????????????????????????????????????????????????????????????????????????
	private void scrollIfNeeded(MotionEvent event) {
		int scrollY = mScrollView.getScrollY();
		float tapRelativeY = event.getY() - scrollY;
		int scrollHeight = mScrollView.getHeight();
		int showingHeight = (int)(scrollHeight * 0.4); // ???????????????????????????ScrollView???????????????
		if (tapRelativeY < showingHeight) {
			// ????????????????????????????????????
			return;
		}
		// ????????????????????????????????????????????????????????????????????????????????????
		final int nextScrollY = scrollY +  ((int)tapRelativeY - showingHeight);
		int lackHeight = (nextScrollY + scrollHeight) - mEditText.getHeight();
		if (lackHeight <= 0) {
			mScrollView.scrollTo(mScrollView.getScrollX(), nextScrollY);
		} else {
			// ???????????????????????????????????????????????????????????????????????????????????????
			int lineHeight = mEditText.getLineHeight();
			StringBuilder builder = new StringBuilder();
			for (; 0 < lackHeight ; lackHeight -= lineHeight) {
				builder.append("\n");
			}
			mEditText.append(builder.toString());
			// ?????????????????????????????????????????????scrollTo????????????????????????????????????????????????
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mScrollView.scrollTo(mScrollView.getScrollX(), nextScrollY);
				}
			}, 1);
		}
	}
}
