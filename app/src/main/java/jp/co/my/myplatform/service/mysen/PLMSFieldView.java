package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.ArrayList;

import jp.co.my.myplatform.R;


public class PLMSFieldView extends FrameLayout {
	static final int MAX_X = 6;
	static final int MAX_Y = 8;

	private ArrayList<PLMSLandView> mLandArray;

	public PLMSFieldView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_field, this);
		mLandArray = new ArrayList<>();
		loadField();

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				resizeChildViews();
			}
		});
	}

	public PLMSFieldView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSFieldView(Context context) {
		this(context, null);
	}

	private void loadField() {
		LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_linear);

		LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, 0, 1);
		LinearLayout.LayoutParams landParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		for (int y = 0; y < MAX_Y; y++) {
			LinearLayout horizontalLayout = new LinearLayout(getContext());
			verticalLayout.addView(horizontalLayout);

			Space leftSpace = new Space(getContext());
			horizontalLayout.addView(leftSpace, spaceParams);
			for (int x = 0; x < MAX_X; x++) {
				PLMSLandView landView = new PLMSLandView(getContext());
				horizontalLayout.addView(landView, landParams);
				mLandArray.add(landView);
			}
			Space rightSpace = new Space(getContext());
			horizontalLayout.addView(rightSpace, spaceParams);
		}
	}

	private void resizeChildViews() {
		int width = getWidth() / MAX_X;
		int height = getHeight() / MAX_Y;
		if (width < height) {
			height = width;
		} else {
			width = height;
		}

		for (PLMSLandView landView : mLandArray) {
			ViewGroup.LayoutParams params = landView.getLayoutParams();
			params.width = width;
			params.height = height;
			landView.setLayoutParams(params);
		}
	}
}
