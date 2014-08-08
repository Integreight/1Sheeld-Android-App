package com.integreight.onesheeld.shields.fragments;

import java.util.Date;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.TerminalShield;
import com.integreight.onesheeld.shields.controller.TerminalShield.TerminalHandler;
import com.integreight.onesheeld.utils.ShieldFragmentParent;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.customviews.OneSheeldEditText;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class TerminalFragment extends ShieldFragmentParent<TerminalFragment> {
	View v;
	private OneSheeldTextView output;
	private OneSheeldEditText inputField;
	private OneSheeldButton send;
	private boolean endedWithNewLine = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.terminal_shield_fragment_layout,
				container, false);
		output = (OneSheeldTextView) v.findViewById(R.id.terminalOutput);
		inputField = (OneSheeldEditText) v.findViewById(R.id.terminalInput);
		send = (OneSheeldButton) v.findViewById(R.id.send);
		output.setMovementMethod(new ScrollingMovementMethod());
		send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				((TerminalShield) getApplication().getRunningShields().get(
						getControllerTag())).input(inputField.getText()
						.toString());
				output.append((!endedWithNewLine ? "\n" : "")
						+ new Date().toString() + " - "
						+ inputField.getText().toString() + "\n");
				final int scrollAmount = output.getLayout().getLineTop(
						output.getLineCount())
						- output.getHeight();
				// if there is no need to scroll, scrollAmount will be <=0
				if (scrollAmount > 0)
					output.scrollTo(
							0,
							scrollAmount
									+ ((int) (30 * getResources()
											.getDisplayMetrics().density + .5f)));
				else
					output.scrollTo(0, 0);

				endedWithNewLine = true;
				inputField.setText("");
			}
		});
		return v;
	}

	@Override
	public void onStart() {
		if ((TerminalShield) getApplication().getRunningShields().get(
				getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new TerminalShield(activity, getControllerTag()));
		}
		((TerminalShield) getApplication().getRunningShields().get(
				getControllerTag())).setEventHandler(terminalHandler);
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void doOnServiceConnected() {
	}

	TerminalHandler terminalHandler = new TerminalHandler() {

		@Override
		public void onPrint(final String outputTxt) {
			if (canChangeUI())
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						if (output != null) {
							output.append(outputTxt);
							if (outputTxt.length() > 0)
								endedWithNewLine = outputTxt.charAt(outputTxt
										.length() - 1) == '\n';
						}
					}
				});
		}
	};

}
