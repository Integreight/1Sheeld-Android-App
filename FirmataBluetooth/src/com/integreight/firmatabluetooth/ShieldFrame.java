package com.integreight.firmatabluetooth;

import java.util.ArrayList;

public class ShieldFrame {
	public static final byte START_OF_FRAME=(byte) 0xFF;
	public static final byte END_OF_FRAME=(byte) 0x00;
	
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

}
