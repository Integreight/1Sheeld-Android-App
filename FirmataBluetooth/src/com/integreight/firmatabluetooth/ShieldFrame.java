package com.integreight.firmatabluetooth;

import java.util.ArrayList;

public class ShieldFrame {
	public static final byte START_OF_FRAME=(byte) 0xFF;
	
	private byte shieldId;
	private byte instanceId;
	private byte functionId;
	private ArrayList<byte[]> arguments;

	public ShieldFrame(byte shieldId,byte instanceId,byte functionId) {
		// TODO Auto-generated constructor stub
		this.shieldId=shieldId;
		this.instanceId=instanceId;
		this.functionId=functionId;
		arguments=new ArrayList<byte[]>();
	}
	
	public byte getShieldId() {
		return shieldId;
	}

	public byte getInstanceId() {
		return instanceId;
	}

	public byte getFunctionId() {
		return functionId;
	}

	public ArrayList<byte[]> getArguments() {
		return arguments;
	}
	
	public byte[] getArgument(int n){
		if(n>=arguments.size())return null;
		return arguments.get(n);
	}
	
	public String getArgumentAsString(int n){
		if(n>=arguments.size())return null;
		return new String(arguments.get(n));
	}

	public void addArgument(byte[] argument) {
		arguments.add(argument);
	}
	
	public byte[] getAllFrameAsBytes(){
		int totalSizeOfArguments=0;
		for (byte[] argument : arguments) {
			totalSizeOfArguments+=argument.length;
		}
		int frameSize=5+arguments.size()+totalSizeOfArguments;
		byte[] data=new byte[frameSize];
		data[0]=START_OF_FRAME;
		data[1]=shieldId;
		data[2]=instanceId;
		data[3]=functionId;
		data[4]=(byte)arguments.size();
		
		for (int i = 0,j=5; i < arguments.size(); i++,j+=arguments.get(i).length+1) {
			data[j]=(byte)arguments.get(i).length;
			for (int k = 0; k < data[j]; k++) {
				data[j+k+1]=arguments.get(i)[k];
			}
		}
		return data;
	}

}
