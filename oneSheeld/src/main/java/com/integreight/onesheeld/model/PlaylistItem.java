package com.integreight.onesheeld.model;

public class PlaylistItem {
	public int id;
	public String name, path;
	public boolean isSelected = false;

	public PlaylistItem() {
		// TODO Auto-generated constructor stub
	}

	public PlaylistItem(int id, String name, String path) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
	}

	public PlaylistItem(String name, String path) {
		super();
		this.name = name;
		this.path = path;
	}

}
