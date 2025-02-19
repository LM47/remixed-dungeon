package com.nyrds.pixeldungeon.support;

import android.view.View;
import android.webkit.WebView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.android.gms.ads.AdView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;

public class AdsUtils {

    static Map<AdsUtilsCommon.IBannerProvider, Integer> bannerFails = new HashMap<>();
    static Map<AdsUtilsCommon.IInterstitialProvider, Integer> interstitialFails = new HashMap<>();
    static Map<AdsUtilsCommon.IRewardVideoProvider, Integer> rewardVideoFails = new HashMap<>();

    static {
        bannerFails.put(new AdMobComboProvider(),-2);
        interstitialFails.put(new AdMobComboProvider(), -2);

        if(!Game.instance().checkOwnSignature()) {
            bannerFails.put(new AAdsComboProvider(), 0);
            interstitialFails.put(new AAdsComboProvider(), 0);
        }

        if(AppodealAdapter.usable()) {
            AppodealAdapter.init();
            bannerFails.put(new AppodealBannerProvider(), -1);
            interstitialFails.put(new AppodealInterstitialProvider(), -1);
        }
    }


    public static void initRewardVideo() {

        if(!rewardVideoFails.isEmpty()) {
            return;
        }

        if(AppodealAdapter.usable()) {
            AppodealRewardVideoProvider.init();
            rewardVideoFails.put(new AppodealRewardVideoProvider(), -1);
        }
        rewardVideoFails.put(new GoogleRewardVideoAds(), -2);
    }

    static int bannerIndex() {
        int childs = Game.instance().getLayout().getChildCount();
        for (int i = 0; i < childs; ++i) {
            View view = Game.instance().getLayout().getChildAt(i);
            if (view instanceof AdView || view instanceof WebView || view instanceof BannerView) {
                return i;
            }
        }
        return -1;
    }

    static void updateBanner(final View view) {
        Game.instance().runOnUiThread(() -> {

            int index = bannerIndex();
            if (index >= 0) {

                View adview = Game.instance().getLayout().getChildAt(index);
                if(adview == view) {
                    return;
                }

                if (adview instanceof BannerView) {
                    Appodeal.hide(Game.instance(), Appodeal.BANNER);
                }

                if(adview instanceof AdView) {
                    ((AdView)adview).destroy();
                }
                Game.instance().getLayout().removeViewAt(index);
            }

            try {
                Game.instance().getLayout().addView(view, 0);
            } catch (IllegalStateException e) {
                EventCollector.logException(e);
            }
        });
    }

    public static void removeTopBanner() {
        Game.instance().runOnUiThread(() -> {
            int index = bannerIndex();
            if (index >= 0) {

                View adview = Game.instance().getLayout().getChildAt(index);

                if (adview instanceof BannerView) {
                    Appodeal.hide(Game.instance(), Appodeal.BANNER);
                }
                if(adview instanceof AdView) {
                    ((AdView)adview).destroy();
                }

                Game.instance().getLayout().removeViewAt(index);
            }
        });
    }
}
