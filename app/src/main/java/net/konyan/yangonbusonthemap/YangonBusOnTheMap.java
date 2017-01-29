package net.konyan.yangonbusonthemap;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

import net.konyan.yangonbusonthemap.util.MyPref;


/**
 * Created by zeta on 1/24/17.
 */

public class YangonBusOnTheMap  extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        MyPref.Builder.build(this);

        MobileAds.initialize(this, "ca-app-pub-3722160390007679/8865305547");
    }
}
