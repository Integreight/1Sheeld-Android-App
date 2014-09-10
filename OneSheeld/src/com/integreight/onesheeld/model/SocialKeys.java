package com.integreight.onesheeld.model;

public class SocialKeys {
	public String facebookID;
	public SocialKey twitter = new SocialKey(), foursquare = new SocialKey();

	public static class SocialKey {
		public String id, secret;

		public SocialKey() {
			// TODO Auto-generated constructor stub
		}

	}
}
