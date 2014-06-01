package com.integreight.onesheeld.shields.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.EventHandler;
import com.integreight.onesheeld.Log;

public class FacebookShield extends ControllerParent<FacebookShield> {
	private static FacebookEventHandler eventHandler;
	private String lastPost;
	private Fragment fragment;
	private static final byte UPDATE_STATUS_METHOD_ID = (byte) 0x01;

	static final String PREF_KEY_FACEBOOK_USERNAME = "FacebookName";

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private boolean pendingPublishReauthorization = false;

	private static SharedPreferences mSharedPreferences;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	public String getLastPost() {
		return lastPost;
	}

	public FacebookShield() {
		super();
	}

	@Override
	public ControllerParent<FacebookShield> setTag(String tag) {
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
		if (session == null) {
			if (session == null) {
				session = new Session(activity);
			}
			Session.setActiveSession(session);
		}

		session.addCallback(statusCallback);
		return super.setTag(tag);
	}

	public FacebookShield(Activity activity, String tag, Fragment fragment,
			Bundle savedInstanceState) {
		super(activity, tag);
		mSharedPreferences = activity.getApplicationContext()
				.getSharedPreferences("com.integreight.onesheeld",
						Context.MODE_PRIVATE);
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(activity, null,
						statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(activity);
			}
			Session.setActiveSession(session);
		}

		session.addCallback(statusCallback);
		this.fragment = fragment;
	}

	public void setShieldFragment(Fragment fragment) {
		this.fragment = fragment;
		CommitInstanceTotable();
	}

	public void setFacebookEventHandler(FacebookEventHandler eventHandler) {
		FacebookShield.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface FacebookEventHandler extends EventHandler {
		void onRecievePost(String post);

		void onFacebookLoggedIn();

		void onFacebookError(String error);
	}

	public void loginToFacebook() {
		Session session = Session.getActiveSession();
		if (session == null) {
			Session.openActiveSession(activity, fragment, true, statusCallback);
		} else if (!session.isOpened()) {
			session.openForRead(new Session.OpenRequest(fragment)
					.setCallback(statusCallback));
		}
		CommitInstanceTotable();
	}

	public void logoutFromFacebook() {

		if (Session.getActiveSession() != null
				&& !Session.getActiveSession().isClosed()) {
			// Session.getActiveSession().close();
			Session.getActiveSession().closeAndClearTokenInformation();
		} else {
			Session ses = new Session(activity);
			Session.setActiveSession(ses);
			ses.closeAndClearTokenInformation();
		}
		Session.setActiveSession(null);
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_FACEBOOK_USERNAME);
		e.commit();
		CommitInstanceTotable();
	}

	// @Override
	// public void onUartReceive(byte[] data) {
	// TODO Auto-generated method stub
	// if (data.length < 2)
	// return;
	// byte command = data[0];
	// byte methodId = data[1];
	// int n = data.length - 2;
	// byte[] newArray = new byte[n];
	// System.arraycopy(data, 2, newArray, 0, n);
	// if (command == FACEBOOK_COMMAND) {
	// String post = new String(newArray);
	// lastPost = post;
	// if (isFacebookLoggedInAlready())
	// if (methodId == UPDATE_STATUS_METHOD_ID)
	// publishStory(post);
	//
	// // }
	// super.onUartReceive(data);
	// }

	public String getUsername() {
		// TODO Auto-generated method stub
		return mSharedPreferences.getString(PREF_KEY_FACEBOOK_USERNAME, "");
	}

	public boolean isFacebookLoggedInAlready() {
		// TODO Auto-generated method stub
		if (Session.getActiveSession() != null)
			return Session.getActiveSession().isOpened()
					&& getUsername().length() > 0;
		else
			return false;
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(final Session session, SessionState state,
				Exception exception) {
			if (exception != null && eventHandler != null) {
				exception.printStackTrace();
				logoutFromFacebook();
				eventHandler.onFacebookError(exception.getMessage());
			}
			if (session.isOpened()) {

				if (!pendingPublishReauthorization) {
					if (!isSubsetOf(PERMISSIONS, session.getPermissions())) {
						pendingPublishReauthorization = true;
						Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
								fragment, PERMISSIONS);
						if (newPermissionsRequest != null)
							session.requestNewPublishPermissions(newPermissionsRequest);
						// else {
						// if (eventHandler != null)
						// eventHandler
						// .onFacebookError("Kindly, reset you facebook app or update it!");
						// }
					}
				}

				if ((pendingPublishReauthorization && state
						.equals(SessionState.OPENED_TOKEN_UPDATED))
						|| isSubsetOf(PERMISSIONS, session.getPermissions())) {
					pendingPublishReauthorization = false;
					Request.newMeRequest(session,
							new Request.GraphUserCallback() {

								// callback after Graph API response with user
								// object
								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									if (user != null) {

										Editor e = mSharedPreferences.edit();
										e.putString(PREF_KEY_FACEBOOK_USERNAME,
												user.getName());
										e.commit();
										if (eventHandler != null)
											eventHandler.onFacebookLoggedIn();
									}

								}
							}).executeAsync();
				}
				// make request to the /me API

			}
		}
	}

	private boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	private void publishStory(final String message) {
		Session session = Session.getActiveSession();

		if (session != null) {

			Bundle postParams = new Bundle();
			postParams.putString("message", message);
			// postParams.putString("name", "Facebook SDK for Android");
			// postParams.putString("caption",
			// "Build great social apps and get more installs.");
			// postParams
			// .putString(
			// "description",
			// "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
			// postParams.putString("link",
			// "https://developers.facebook.com/android");
			// postParams
			// .putString("picture",
			// "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					FacebookRequestError error = response.getError();
					if (error != null) {
						if (eventHandler != null) {
							Log.sysOut("$#$#$ " + error);
							eventHandler.onFacebookError(error
									.getErrorMessage());
						}
						return;
					}
					// String postId = null;
					// try {
					// JSONObject graphResponse = response.getGraphObject()
					// .getInnerJSONObject();
					// postId = graphResponse.getString("id");
					if (eventHandler != null)
						eventHandler.onRecievePost(message);
					// } catch (Exception e) {
					// //Log.i("", "JSON error " + e.getMessage());
					// if(eventHandler!=null)eventHandler.onFacebookError(e.getMessage());
					// }

				}
			};

			Request request = new Request(session, "me/feed", postParams,
					HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		// TODO Auto-generated method stub
		if (frame.getShieldId() == UIShield.FACEBOOK_SHIELD.getId()) {
			lastPost = frame.getArgumentAsString(0);
			if (isFacebookLoggedInAlready())
				if (frame.getFunctionId() == UPDATE_STATUS_METHOD_ID)
					if (ConnectionDetector
							.isConnectingToInternet(getApplication()
									.getApplicationContext()))
						publishStory(lastPost);
					else
						Toast.makeText(
								getApplication().getApplicationContext(),
								"Please check your Internet connection and try again.",
								Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void reset() {

	}
}
