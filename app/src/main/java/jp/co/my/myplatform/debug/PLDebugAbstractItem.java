package jp.co.my.myplatform.debug;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class PLDebugAbstractItem {

	public PLDebugAbstractItem() {
		super();
	}

	public abstract View updateView(View view, ViewGroup parent);

	public View createCell(Context context, ViewGroup parent) {
		if (getResourceId() != -1) {
			return LayoutInflater.from(context).inflate(getResourceId(), parent, false);
		}

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setPadding(50, 0, 50, 0);
		linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT
		));
		return linearLayout;
	}

	protected int getResourceId() {
		return -1;
	}

	protected static void addViewToEqualInterval(View view, ViewGroup parent) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				0,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		params.weight = 1;
		parent.addView(view, params);
	}

	protected static void addPartitionToParent(ViewGroup parent) {
		View partition = new View(parent.getContext());
		partition.setBackgroundColor(Color.BLACK);
		ViewGroup.MarginLayoutParams margin = new LinearLayout.LayoutParams(
				1,
				LinearLayout.LayoutParams.MATCH_PARENT
		);
		margin.setMargins(30, 0, 30, 0);
		partition.setLayoutParams(margin);
		parent.addView(partition);
	}
}
