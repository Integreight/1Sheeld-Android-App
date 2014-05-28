package com.integreight.onesheeld;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.integreight.onesheeld.adapters.TutorialPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

public class TutorialPopup extends FragmentActivity {
	ViewPager pager;
	RelativeLayout logoCont;
	ImageView fadingLogo;

	@Override
	public void onBackPressed() {
		MainActivity.thisInstance.finish();
		finish();
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		OneSheeldApplication app = (OneSheeldApplication) getApplication();
		app.setTutShownTimes(app.getTutShownTimes() + 1);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.tutorial_popup);
		pager = (ViewPager) findViewById(R.id.mpager);
		logoCont = (RelativeLayout) findViewById(R.id.upperLogoCont);
		fadingLogo = (ImageView) findViewById(R.id.fadingLogo);
		pager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));
		pager.setCurrentItem(0);
		CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(pager);
		mIndicator.setSnap(true);
		Animation anim = new AlphaAnimation(0, 1);
		anim.setDuration(2000);
		anim.setFillAfter(true);
		anim.setFillEnabled(true);
		anim.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				logoCont.setVisibility(View.VISIBLE);
				fadingLogo.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				logoCont.postDelayed(new Runnable() {

					@Override
					public void run() {
						logoCont.setVisibility(View.GONE);
					}
				}, 500);
			}
		});
		fadingLogo.startAnimation(anim);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
}
