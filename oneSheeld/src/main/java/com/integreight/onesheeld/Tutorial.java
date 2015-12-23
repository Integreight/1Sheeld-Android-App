package com.integreight.onesheeld;

import android.graphics.Color;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.integreight.onesheeld.adapters.TutorialPagerAdapter;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.viewpagerindicator.CirclePageIndicator;

public class Tutorial extends FragmentActivity{
    ViewPager pager;
    RelativeLayout logoCont;
    ImageView fadingLogo;
    OneSheeldButton skip;
    Boolean isAnimationFinished = false;

    @Override
    public void onBackPressed() {
        if (!isMenu && MainActivity.thisInstance != null) {
            MainActivity.thisInstance.finishManually();
        }
        finish();
        super.onBackPressed();
        if(!isMenu) {
           try{
               System.exit(0);
           }catch (Exception e){}
        }
    }

    @Override
    protected void onDestroy() {
        OneSheeldApplication app = (OneSheeldApplication) getApplication();
        app.setTutShownTimes(app.getTutShownTimes() + 1);
        super.onDestroy();
    }

    boolean isMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#CC000000"));
        }
        setContentView(R.layout.tutorial_popup);
        isMenu = getIntent().getExtras() != null
                && getIntent().getExtras().getBoolean("isMenu");
        pager = (ViewPager) findViewById(R.id.mpager);
        logoCont = (RelativeLayout) findViewById(R.id.upperLogoCont);
        fadingLogo = (ImageView) findViewById(R.id.fadingLogo);
        pager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(0);
        skip = (OneSheeldButton) findViewById(R.id.skip_tutorial);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(7);
            }
        });
        skip.setVisibility(View.INVISIBLE);
        CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(pager);
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position < 7 && isAnimationFinished)
                    skip.setVisibility(View.VISIBLE);
                else
                    skip.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mIndicator.setSnap(true);
        Animation anim = new AlphaAnimation(0, 1);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(2000);
        anim.setFillAfter(true);
//        anim.setRepeatCount(0);
//        anim.setRepeatMode(Animation.RESTART);
        anim.setFillEnabled(true);
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                logoCont.setVisibility(View.VISIBLE);
                fadingLogo.setVisibility(View.VISIBLE);
                skip.setVisibility(View.INVISIBLE);
                isAnimationFinished = false;
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
                        skip.setVisibility(View.VISIBLE);
                        isAnimationFinished = true;
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
