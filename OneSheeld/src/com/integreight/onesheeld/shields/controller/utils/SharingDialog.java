package com.integreight.onesheeld.shields.controller.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.integreight.onesheeld.R;

public class SharingDialog extends Dialog {
	// private TmsMovie movie;
	final String PREFS_NAME = "pref";
	TwitterFactory factory;
	Twitter twitter;
	RequestToken requestToken;
	private final String CALLBACKURL = "oob";
	String authUrl;
	Activity activity;
	private String url;

	public SharingDialog(Activity activity, String imgURL) {
		super(activity, Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.);
		setCancelable(true);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		this.activity = activity;
		// this.movie = movie;
		this.url = imgURL;
		init();
	}

	void init() {
		// final EditText editText_message = (EditText)
		// findViewById(R.id.editText_shaingMsg);
		// final List<Theatre> theatres = movie.getTheatres();
		// Spinner timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
//		editText_message.setText(String.format(activity.getResources()
//				.getString(R.string.sharing_dialog_message), movie.getTitle()
//				.replaceAll(" ", ""),
//				theatres.get(0).getName().replaceAll(" ", ""), theatres.get(0)
//						.getStringTime()));
//		editText_message.setSelection(editText_message.length());
//		ArrayList<String> times = new ArrayList<String>();
//		for (Theatre th : theatres)
//			times.add(th.getStringTime());
//		timeSpinner.setAdapter(new PopupTimeSpinnerAdapter(activity, times));
//		timeSpinner
//				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//					@Override
//					public void onItemSelected(AdapterView<?> parent,
//							View view, int position, long id) {
//						editText_message.setText(String.format(
//								activity.getResources().getString(
//										R.string.sharing_dialog_message),
//								movie.getTitle().replaceAll(" ", ""),
//								theatres.get(position).getName()
//										.replaceAll(" ", ""),
//								theatres.get(position).getStringTime()));
//						editText_message.setSelection(editText_message.length());
//					}
//
//					@Override
//					public void onNothingSelected(AdapterView<?> parent) {
//						// To change body of implemented methods use File |
//						// Settings | File Templates.
//					}
//				});
//		View.OnClickListener shareClickListener = new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				share(editText_message.getText().toString().trim());
//			}
//		};
		View.OnClickListener tweetClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				factory = new TwitterFactory();
				twitter = new TwitterFactory().getInstance();
				twitter.setOAuthConsumer(activity.getResources().getString(R.string.twitter_consumer_key),
						activity.getResources().getString(R.string.twitter_consumer_secret));
				if (SocialAuthorization.FETCHED_ACCESS_TOKEN != null
						&& SocialAuthorization.FETCHED_SECRET_TOKEN != null) {
					AccessToken accestoken = new AccessToken(
							SocialAuthorization.FETCHED_ACCESS_TOKEN,
							SocialAuthorization.FETCHED_SECRET_TOKEN);
					twitter.setOAuthAccessToken(accestoken);
					tweet("Tweet");
				} else {
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							try {
								requestToken = twitter
										.getOAuthRequestToken(CALLBACKURL);
								authUrl = requestToken.getAuthenticationURL();
							} catch (TwitterException e) {

							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {

							final TwitterDialogListner listener = new TwitterDialogListner() {
								@Override
								public void onComplete() {
									if (SocialAuthorization.FETCHED_ACCESS_TOKEN != null
											&& SocialAuthorization.FETCHED_SECRET_TOKEN != null
											&& SocialAuthorization.TWITTER_USER_NAME != null) {
										SharedPreferences settings = activity
												.getSharedPreferences(
														PREFS_NAME, 0);
										SharedPreferences.Editor editor = settings
												.edit();
										editor.putString(
												"FETCHED_ACCESS_TOKEN",
												SocialAuthorization.FETCHED_ACCESS_TOKEN);
										editor.putString(
												"FETCHED_SECRET_TOKEN",
												SocialAuthorization.FETCHED_SECRET_TOKEN);
										editor.putString(
												"TWITTER_USER_NAME",
												SocialAuthorization.TWITTER_USER_NAME);
										editor.putString(
												"FETCHED_IMAGE_URL",
												SocialAuthorization.FETCHED_IMAGE_URL);
										// Commit the edits!
										editor.commit();
										tweet("Tweet");
									}

								}

								@Override
								public void onError(String error) {
									AlertDialog.Builder ab = new AlertDialog.Builder(
											activity);
									ab.setMessage(error);
									ab.setPositiveButton("OK",
											new OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													d.cancel();
												}
											});
									d = ab.create();
									d.show();
								}

								@Override
								public void onCancel() {

								}
							};
							new Handler().post(new Runnable() {

								@Override
								public void run() {
									TwitterDialog mDialog = new TwitterDialog(
											activity, authUrl, twitter,
											requestToken, listener);
									mDialog.show();
								}
							});

							super.onPostExecute(result);
						}

					}.execute(null, null);

				}

			}
		};
//		findViewById(R.id.btn_share).setOnClickListener(shareClickListener);
//		findViewById(R.id.btn_tweet).setOnClickListener(tweetClickListener);
	}

	Dialog d;

	void tweet(final String tweet) {
		if (tweet.length() <= 140 - activity.getResources()
				.getString(R.string.app_name).length()) {
			final ProgressDialog dial = new ProgressDialog(activity);
			dial.setMessage("Tweeting...");
			new AsyncTask<Void, Void, Void>() {
				protected void onPreExecute() {
					dial.show();
				}

				;

				@Override
				protected Void doInBackground(Void... params) {
					StatusUpdate st = new StatusUpdate(tweet
							+ activity.getResources().getString(
									R.string.app_name)
							+ " http://bit.ly/HUayMN");
					try {
						twitter.updateStatus(st);
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					return null;
				}

				protected void onPostExecute(Void result) {
					dial.dismiss();
				}

				;

			}.execute(null, null);
		} else {
			AlertDialog.Builder ab = new AlertDialog.Builder(activity);
			ab.setMessage("Tweet length must not exceed "
					+ (140 - activity.getResources()
							.getString(R.string.app_name).length())
					+ " characters!");
			ab.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					d.cancel();
				}
			});
			d = ab.create();
			d.show();
		}
	}

	void share(final String msg) {
		System.out.println(msg);
		if (Session.getActiveSession() == null
				|| Session.getActiveSession().isClosed()) {
			System.out.println(Session.getActiveSession());
			Session.openActiveSession(activity, true,
					new Session.StatusCallback() {

						@Override
						public void call(Session session, SessionState state,
								Exception exception) {
							System.out.println(session == null ? session
									: session.isOpened());
							if (session.isOpened()) {
								Session.setActiveSession(session);
								publishStory(msg);
							}

						}
					});
		} else {
			publishStory(msg);
		}
	}

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");

	private void publishStory(String msg) {
		final ProgressDialog dial = new ProgressDialog(activity);
		dial.setMessage("Sharing...");
		Session session = Session.getActiveSession();

		if (session != null) {
			dial.show();
			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						activity, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			Bundle postParams = new Bundle();
			postParams.putString("name", "title");
			postParams.putString("caption", "caption");
			postParams.putString("description", msg);
			postParams.putString("link", "http://bit.ly/HUayMN");
			postParams.putString("picture", url);

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					String postId = null;
					try {
						postId = graphResponse.getString("id");
					} catch (JSONException e) {
						Log.i("TAG", "JSON error " + e.getMessage());
					}
					// FacebookRequestError error = response.getError();
					// if (error != null) {
					// Toast.makeText(context, error.getErrorMessage(),
					// Toast.LENGTH_SHORT).show();
					// } else {
					// Toast.makeText(context, postId, Toast.LENGTH_LONG)
					// .show();
					// }
					dial.dismiss();
				}
			};

			Request request = new Request(session, "me/feed", postParams,
					HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
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
}
