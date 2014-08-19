package com.integreight.onesheeld.shields.fragments;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.TerminalShield;
import com.integreight.onesheeld.shields.controller.TerminalShield.PrintedLine;
import com.integreight.onesheeld.shields.controller.TerminalShield.TerminalHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.customviews.OneSheeldEditText;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class TerminalFragment extends ShieldFragmentParent<TerminalFragment> {
	private OneSheeldTextView output;
	private OneSheeldEditText inputField;
	private OneSheeldButton send;
	private boolean endedWithNewLine = false;

	public static String getTimeAsString() {
		return DateFormat.format("MM/dd/yyyy hh:mm:ss", new java.util.Date())
				.toString();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.terminal_shield_fragment_layout,
				container, false);
		// selectedEnMth = 0;
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
				((TerminalShield) getApplication().getRunningShields().get(
						getControllerTag())).printedLines.add(new PrintedLine(
						(!endedWithNewLine ? "\n" : "") + getTimeAsString()
								+ " [TX] " + "- ", inputField.getText()
								.toString(), true));
				output.append(((!endedWithNewLine ? "\n" : "")
						+ getTimeAsString() + " [TX] " + "- "
						+ getEncodedString(inputField.getText().toString()) + "\n"));
				final int scrollAmount = output.getLayout().getLineTop(
						output.getLineCount())
						- output.getHeight();
				// if there is no need to scroll, scrollAmount will be <=0
				if (scrollAmount > 0)
					output.scrollTo(
							0,
							scrollAmount
									+ ((int) (0 * getResources()
											.getDisplayMetrics().density + .5f)));
				else
					output.scrollTo(0, 0);

				endedWithNewLine = true;
				inputField.setText("");
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputField.getWindowToken(), 0);
			}
		});
		int i = 0;
		for (final int id : ((TerminalShield) getApplication()
				.getRunningShields().get(getControllerTag())).encodingMths) {
			final int x = i;
			v.findViewById(id).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View vi) {
					if (((TerminalShield) getApplication().getRunningShields()
							.get(getControllerTag())).selectedEnMth != x) {
						v.findViewById(
								((TerminalShield) getApplication()
										.getRunningShields().get(
												getControllerTag())).encodingMths[((TerminalShield) getApplication()
										.getRunningShields().get(
												getControllerTag())).selectedEnMth])
								.setBackgroundColor(
										getResources()
												.getColor(
														R.color.arduino_conn_resetAll_bg));
						v.findViewById(
								((TerminalShield) getApplication()
										.getRunningShields().get(
												getControllerTag())).encodingMths[x])
								.setBackgroundColor(
										getResources().getColor(
												R.color.arduinoPinsSelector));
						((TerminalShield) getApplication().getRunningShields()
								.get(getControllerTag())).selectedEnMth = x;
						output.setText("");
						for (PrintedLine line : ((TerminalShield) getApplication()
								.getRunningShields().get(getControllerTag())).printedLines) {
							output.append(line.date
									+ getEncodedString(line.print)
									+ (line.isEndedWithNewLine ? "\n" : ""));
						}
						final int scrollAmount = output.getLayout().getLineTop(
								output.getLineCount())
								- output.getHeight();
						// if there is no need to scroll, scrollAmount will
						// be <=0
						if (scrollAmount > 0)
							output.scrollTo(
									0,
									scrollAmount
											+ ((int) (0 * getResources()
													.getDisplayMetrics().density + .5f)));
						else
							output.scrollTo(0, 0);
					}
				}
			});
			i++;
		}
		return v;
	}

	private String getEncodedString(String toBeEncoded) {
		String out = "";
		switch (((TerminalShield) getApplication().getRunningShields().get(
				getControllerTag())).selectedEnMth) {
		case 0:
			out = toBeEncoded;
			break;
		case 1:
			try {
				byte[] en = toBeEncoded.getBytes("US-ASCII");
				for (byte b : en) {
					out += b;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 2:
			byte[] bytes = toBeEncoded.getBytes();
			StringBuilder binary = new StringBuilder();
			for (byte b : bytes) {
				int val = b;
				for (int i = 0; i < 8; i++) {
					binary.append((val & 128) == 0 ? 0 : 1);
					val <<= 1;
				}
				binary.append(' ');
			}
			out = binary.toString();
			break;
		case 3:
			char[] chars = toBeEncoded.toCharArray();

			StringBuffer hex = new StringBuffer();
			for (int i = 0; i < chars.length; i++) {
				hex.append(Integer.toHexString((int) chars[i]));
			}
			out = hex.toString().toUpperCase();
			break;
		default:
			break;
		}
		return out;
	}

	@Override
	public void onStart() {
		if ((TerminalShield) getApplication().getRunningShields().get(
				getControllerTag()) == null) {
			getApplication().getRunningShields().put(getControllerTag(),
					new TerminalShield(activity, getControllerTag()));
		}
		v.findViewById(
				((TerminalShield) getApplication().getRunningShields().get(
						getControllerTag())).encodingMths[0])
				.setBackgroundColor(
						getResources().getColor(
								R.color.arduino_conn_resetAll_bg));
		v.findViewById(
				((TerminalShield) getApplication().getRunningShields().get(
						getControllerTag())).encodingMths[((TerminalShield) getApplication()
						.getRunningShields().get(getControllerTag())).selectedEnMth])
				.setBackgroundColor(
						getResources().getColor(R.color.arduinoPinsSelector));
		((TerminalShield) getApplication().getRunningShields().get(
				getControllerTag())).setEventHandler(terminalHandler);
		if (((TerminalShield) getApplication().getRunningShields().get(
				getControllerTag())).printedLines == null)
			((TerminalShield) getApplication().getRunningShields().get(
					getControllerTag())).printedLines = new ArrayList<TerminalShield.PrintedLine>();
		output.setText("");
		for (PrintedLine line : ((TerminalShield) getApplication()
				.getRunningShields().get(getControllerTag())).printedLines) {
			output.append(line.date + getEncodedString(line.print)
					+ (line.isEndedWithNewLine ? "\n" : ""));
			endedWithNewLine = line.isEndedWithNewLine;
		}
		if (output.getLayout() != null) {
			final int scrollAmount = output.getLayout().getLineTop(
					output.getLineCount())
					- output.getHeight();
			// if there is no need to scroll, scrollAmount will
			// be <=0
			if (scrollAmount > 0)
				output.scrollTo(0,
						scrollAmount
								+ ((int) (0 * getResources()
										.getDisplayMetrics().density + .5f)));
			else
				output.scrollTo(0, 0);
		}
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
	public void onDestroy() {
		// if (((TerminalShield) getApplication().getRunningShields().get(
		// getControllerTag())).printedLines != null)
		// ((TerminalShield) getApplication().getRunningShields().get(
		// getControllerTag())).printedLines.clear();
		// ((TerminalShield) getApplication().getRunningShields().get(
		// getControllerTag())).printedLines = null;
		v = null;
		super.onDestroy();
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
							String date = output.length() == 0
									|| endedWithNewLine ? getTimeAsString()
									+ " [RX] - " : "";
							boolean isEndedWithNewLine = outputTxt.length() > 0
									&& outputTxt.charAt(outputTxt.length() - 1) == '\n';
							// ((TerminalShield) getApplication()
							// .getRunningShields()
							// .get(getControllerTag())).printedLines.add(new
							// TerminalShield.PrintedLine(
							// date, outputTxt.substring(
							// 0,
							// isEndedWithNewLine ? outputTxt
							// .length() - 1 : outputTxt
							// .length()),
							// isEndedWithNewLine));
							output.append(date
									+ getEncodedString(outputTxt.substring(
											0,
											isEndedWithNewLine ? outputTxt
													.length() - 1 : outputTxt
													.length()))
									+ (isEndedWithNewLine ? "\n" : ""));
							if (outputTxt.length() > 0)
								endedWithNewLine = outputTxt.charAt(outputTxt
										.length() - 1) == '\n';
							final int scrollAmount = output.getLayout()
									.getLineTop(output.getLineCount())
									- output.getHeight();
							// if there is no need to scroll, scrollAmount will
							// be <=0
							if (scrollAmount > 0)
								output.scrollTo(
										0,
										scrollAmount
												+ ((int) (0 * getResources()
														.getDisplayMetrics().density + .5f)));
							else
								output.scrollTo(0, 0);
						}
					}
				});
		}
	};

}
