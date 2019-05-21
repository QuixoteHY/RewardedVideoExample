package com.google.ads.rewardedvideoexample;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/**
 * Main Activity. Inflates main activity xml and implements RewardedVideoAdListener.
 */
public class MainActivity extends Activity implements RewardedVideoAdListener {
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
    private static final long COUNTER_TIME = 10;
    private static final int GAME_OVER_REWARD = 1;

    private int coinCount;
    private TextView coinCountText;
    private CountDownTimer countDownTimer;
    private boolean gameOver;
    private boolean gamePaused;
    private RewardedVideoAd rewardedVideoAd;
    private Button retryButton;
    private Button showVideoButton;
    private long timeRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, APP_ID);

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        // Create the "retry" button, which tries to show an interstitial between game plays.
        retryButton = findViewById(R.id.retry_button);
        retryButton.setVisibility(View.INVISIBLE);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        // Create the "show" button, which shows a rewarded video if one is loaded.
        showVideoButton = findViewById(R.id.show_video_button);
        showVideoButton.setVisibility(View.INVISIBLE);
        showVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRewardedVideo();
            }
        });

        // Display current coin count to user.
        coinCountText = findViewById(R.id.coin_count_text);
        coinCount = 0;
        coinCountText.setText("Coins: " + coinCount);

        startGame();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseGame();
        rewardedVideoAd.pause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!gameOver && gamePaused) {
            resumeGame();
        }
        rewardedVideoAd.resume(this);
    }

    private void pauseGame() {
        countDownTimer.cancel();
        gamePaused = true;
    }

    private void resumeGame() {
        createTimer(timeRemaining);
        gamePaused = false;
    }

    private void loadRewardedVideoAd() {
        if (!rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.loadAd(AD_UNIT_ID, new AdRequest.Builder().build());
        }
    }

    private void addCoins(int coins) {
        coinCount += coins;
        coinCountText.setText("Coins: " + coinCount);
    }

    private void startGame() {
        // Hide the retry button, load the ad, and start the timer.
        retryButton.setVisibility(View.INVISIBLE);
        showVideoButton.setVisibility(View.INVISIBLE);
        loadRewardedVideoAd();
        createTimer(COUNTER_TIME);
        gamePaused = false;
        gameOver = false;
    }

    // Create the game timer, which counts down to the end of the level
    // and shows the "retry" button.
    private void createTimer(long time) {
        final TextView textView = findViewById(R.id.timer);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(time * 1000, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                timeRemaining = ((millisUnitFinished / 1000) + 1);
                textView.setText("seconds remaining: " + timeRemaining);
            }

            @Override
            public void onFinish() {
                if (rewardedVideoAd.isLoaded()) {
                    showVideoButton.setVisibility(View.VISIBLE);
                }
                textView.setText("You Lose!");
                addCoins(GAME_OVER_REWARD);
                retryButton.setVisibility(View.VISIBLE);
                gameOver = true;
            }
        };
        countDownTimer.start();
    }

    private void showRewardedVideo() {
        showVideoButton.setVisibility(View.INVISIBLE);
        if (rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.show();
        }
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        // Preload the next video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(this,
                String.format(" onRewarded! currency: %s amount: %d", reward.getType(),
                        reward.getAmount()),
                Toast.LENGTH_SHORT).show();
        addCoins(reward.getAmount());
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }
}

/*
curl
-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Google Nexus 5X - 6.0.0 - API 23 - 1080x1920 Build/MRA58K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/44.0.2403.119 Mobile Safari/537.36 (Mobile; afma-sdk-a-v14300000.14300000.0)'
-H 'Cookie: id=22a0234cebbf00e7||t=1558418680|et=730|cs=002213fd4806198d3d016b72ba'
-H 'Host: googleads.g.doubleclick.net'
--compressed
'
https://googleads.g.doubleclick.net/mads/gma
?carrier=310260&riv=5&_activity_context=true
&format=interstitial_mb&gl=US&seq_num=1&u_sd=2.625
&ms=CoACYYHHKOC8KsLvr2yUSj0v9Pl1lf1UOEW51brDIi1QnH4tO4d0YWMlphfcubD7kBUheVglNPjfkKxYqhzZ3Lcn_2SRFT9J8oVD4XeaFTmEDlQZBvtmjHAjkF63oP507zcAG8z-YSRRe52lt1Jlhg3HuRkAys1ck30F5OE3Q0f8mGKtXVIOPrZAeBr94Ppt7EGC9RiPKxFQI2V1JoS5ZfmxHHN0p6UKy-ufBnyyUm8qb2qxu74lkCzIIiWCWME3E7zsa5PE2_AK2m9zjEaIDlSXxRPqnquPHi_9YaoAiBPT5AHnm4BezNxr0OYUuGawquZrSuajPx0Wp9aIFGN_qgvJCAqAAlSF9UVGKrx9MMrCDx8Nal_O0aG4E0iqlRxEgavZQ3nOB6AsCRLLxiFyfitbNSffMu-folZ9eqQfeGkI2enNkumdKpJYq-3j4Qy4G36VMPquGfhuqA1RjDMj3cD3NafFjRdtyexpa2mWK5XIuNatH3IoD5gMMtbGdG_PRIf7u8hDuETORx9zc9co6sQpIFUBIVo4LwTTgT9H1VcaWM5HZYVjn6aIJpa9ImbhSlnSXk_zA2cOD8Kq3U9PbqJBaiLuaoWdBuLqy8al0yLuh1cKa20F26Ce5LquoeHVm9hg_b84BcLXhfNIJBuR1DzJk-TzCHvntowGyda1UqzrNRNkuyMSEBcWhCpQ3GML1Xl1Ky4GL80
&target_api=26&hl=en&scroll_index=-1&platform=Genymotion&submodel=Google%20Nexus%205X%20-%206.0.0%20-%20API%2023%20-%201080x1920
&rm=2&android_app_muted=false&request_id=42fc8f6d-a202-4272-ac36-778500e67e16&am=0&is_latchsky=false&disable_ml=false
&js=afma-sdk-a-v14300000.14300000.0&session_id=17540460047819683673&coh=1&sp=0&android_app_volume=1&render_in_browser=false
&android_num_video_cache_tasks=0&_c_csdk_npa_o=false&guci=0.0.0.0.0.0.0.0&rbv=1&cap=m&u_w=412&u_h=684
&msid=com.google.ads.rewardedvideoexample&app_name=1.android.com.google.ads.rewardedvideoexample
&_package_name=com.google.ads.rewardedvideoexample&an=1.android.com.google.ads.rewardedvideoexample
&net=wi&u_audio=1&u_so=p&preqs_in_session=0&support_transparent_background=true&preqs=0&time_in_session=0&output=html
&region=mobile_app&u_tz=-240&client_sdk=1&ex=1&client=ca-app-pub-3940256099942544&slotname=5224354917&adtest=on&gsb=wi
&local_service=true&lite=false&num_ads=1&vpt=8&vfmt=18&vst=0&sdkv=o.14300000.14300000.0&sdmax=0&dmax=1&sdki=3c4d
&caps=inlineVideo_interactiveVideo_mraid1_mraid2_mraid3_sdkVideo_th_autoplay_mediation_scroll_av_transparentBackground_sdkAdmobApiForAds_di_sfv_dinm_dim_nav_navc_dinmo_ipdof_gls_xSeconds
&bisch=true&blev=1&swdr=false&cans=5&canm=false&heap_free=235124&heap_max=100663296&heap_total=3241092
&wv_count=1&blockAutoClicks=true&dv=0&_newBundle=true&includeDoritos=true&tcar=32
&jsv=sdk_20190107_RC02-production-sdk_20190513_RC01&urll=2288
'
 */
