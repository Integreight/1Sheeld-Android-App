package com.integreight.onesheeld.utils;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class ValidationPopup extends Dialog {
	private ValidationAction[] actions;
	private String title, msg;
	private MainActivity activity;

	public ValidationPopup(MainActivity activity, String title, String msg,
			ValidationAction... actions) {
		super(activity, android.R.style.Theme_Translucent_NoTitleBar);
		this.msg = msg;
		this.actions = actions;
		this.title = title;
		this.activity = activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.validation_popup);
		setCancelable(false);
		((OneShieldTextView) findViewById(R.id.title)).setText(title);
		((OneShieldTextView) findViewById(R.id.msg)).setText(msg);
		LinearLayout actionsCont = (LinearLayout) findViewById(R.id.actionsContainer);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, (int) (40 * activity
						.getResources().getDisplayMetrics().density + .5f));
		params.weight = 1;
		for (int i = 0; i < actions.length; i++) {
			final int x = i;
			final OneShieldButton btn = new OneShieldButton(activity);
			btn.setLayoutParams(params);
			btn.setGravity(Gravity.CENTER);
			btn.setSingleLine(true);
			btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
			btn.setText(actions[i].actionTitle);
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					actions[x].onClick.onClick(btn);
					if (actions[x].cancelAfterAction)
						cancel();
				}
			});
			actionsCont.addView(btn);
		}
		super.onCreate(savedInstanceState);
	}

	public static class ValidationAction {
		public String actionTitle;
		public View.OnClickListener onClick;
		public boolean cancelAfterAction = false;

		public ValidationAction() {
			// TODO Auto-generated constructor stub
		}

		public ValidationAction(String actionTitle,
				android.view.View.OnClickListener onClick,
				boolean cancelAfterAction) {
			super();
			this.actionTitle = actionTitle;
			this.onClick = onClick;
			this.cancelAfterAction = cancelAfterAction;
		}

	}
}
