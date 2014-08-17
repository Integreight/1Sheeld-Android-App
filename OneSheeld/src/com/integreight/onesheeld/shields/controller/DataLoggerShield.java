package com.integreight.onesheeld.shields.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import android.app.Activity;
import android.os.Environment;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.shields.ControllerParent;

public class DataLoggerShield extends ControllerParent<DataLoggerShield> {
	private static final byte START_LOGGING = 0x02;
	private static final byte STOP_LOGGING = 0x03;
	private static final byte ADD = 0x04;
	ArrayList<String> headerList = new ArrayList<String>();
	ArrayList<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
	String fileName = null;
	String header[];

	public DataLoggerShield() {
		super();
	}

	public DataLoggerShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		super.setConnected(pins);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.DATA_LOGGER.getId()) {
			switch (frame.getFunctionId()) {
			case START_LOGGING:
				fileName = frame.getArgumentAsString(0);
				headerList = new ArrayList<String>();
				values = new ArrayList<Map<String, Object>>();
				break;
			case STOP_LOGGING:
				ICsvMapWriter mapWriter = null;
				try {
					File folder = new File(
							Environment.getExternalStorageDirectory()
									+ "/OneSheeld/DataLogger");
					if (!folder.exists()) {
						folder.mkdirs();
					}
					mapWriter = new CsvMapWriter(
							new FileWriter(
									Environment.getExternalStorageDirectory()
											+ "/OneSheeld/DataLogger/"
											+ (fileName == null
													|| fileName.length() == 0 ? new Date()
													.getTime() : fileName)
											+ ".csv"),
							CsvPreference.STANDARD_PREFERENCE);

					// assign a default value for married (if null), and write
					// numberOfKids as an empty column if null
					final CellProcessor[] processors = new CellProcessor[headerList
							.size()];
					for (int i = 0; i < processors.length; i++) {
						processors[i] = new Optional();
					}

					// write the header
					header = new String[headerList.size()];
					int i = 0;
					for (String headerItem : headerList) {
						header[i] = headerItem;
						i++;
					}
					mapWriter.writeHeader(header);

					// write the customer Maps
					for (Map<String, Object> value : values) {
						mapWriter.write(value, header, processors);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (mapWriter != null) {
						try {
							mapWriter.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						reset();
					}
				}
				break;
			case ADD:
				// System.out.println(frame.getArgumentAsString(0));
				String key = frame.getArgumentAsString(0);
				String value = frame.getArgumentAsString(1);
				if (!headerList.contains(key))
					headerList.add(key);
				Map<String, Object> valueMap = new HashMap<String, Object>();
				valueMap.put(key, value);
				values.add(valueMap);
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void reset() {
		if (values != null)
			values.clear();
		values = null;
		header = null;
		if (headerList != null)
			headerList.clear();
		headerList = null;
	}

}
