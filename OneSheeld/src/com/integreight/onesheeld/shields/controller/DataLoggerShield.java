package com.integreight.onesheeld.shields.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
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
	private static final byte START_LOGGING = 0x01;
	// private static final byte START_LOGGING_WITH_NAME = 0x02;
	private static final byte STOP_LOGGING = 0x02;
	// private static final byte SET_FORMATE = 0x04;
	private static final byte ADD_FLOAT = 0x03;
	private static final byte ADD_STRING = 0x04;
	private static final byte LOG = 0x05;
	ArrayList<String> headerList = new ArrayList<String>();
	ArrayList<Map<String, String>> dataSet = new ArrayList<Map<String, String>>();
	String fileName = null;
	String header[];
	Map<String, String> rowData = new HashMap<String, String>();

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
				if (frame.getArguments().size() > 0)
					fileName = frame.getArgumentAsString(0);
				else
					fileName = null;
				headerList = new ArrayList<String>();
				dataSet = new ArrayList<Map<String, String>>();
				rowData = new HashMap<String, String>();
				break;
			// case START_LOGGING_WITH_NAME:
			// fileName = frame.getArgumentAsString(0);
			// headerList = new ArrayList<String>();
			// dataSet = new ArrayList<Map<String, Object>>();
			// rowData = new HashMap<String, Object>();
			// break;
			case STOP_LOGGING:
				ICsvMapWriter mapWriter = null;
				try {
					File folder = new File(
							Environment.getExternalStorageDirectory()
									+ "/OneSheeld");
					if (!folder.exists()) {
						folder.mkdirs();
					}
					folder = new File(Environment.getExternalStorageDirectory()
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
					for (Map<String, String> value : dataSet) {
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
						// reset();
					}
				}
				break;
			case ADD_STRING:
				String key = frame.getArgumentAsString(0);
				String value = new String(frame.getArgument(1));
				if (!headerList.contains(key))
					headerList.add(key);
				rowData.put(key, value);
				break;
			case ADD_FLOAT:
				String keyFloat = frame.getArgumentAsString(0);
				String valueFloat = ByteBuffer.wrap(frame.getArgument(1))
						.getFloat() + "";
				if (!headerList.contains(keyFloat))
					headerList.add(keyFloat);
				rowData.put(keyFloat, valueFloat);
				break;
			case LOG:
				HashMap<String, String> temp = new HashMap<String, String>();
				for (String head : headerList) {
					temp.put(head, rowData.get(head));
				}
				dataSet.add(new HashMap<String, String>(rowData));
				rowData = new HashMap<String, String>();
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void reset() {
		if (dataSet != null)
			dataSet.clear();
		dataSet = null;
		header = null;
		if (headerList != null)
			headerList.clear();
		headerList = null;
	}

}
