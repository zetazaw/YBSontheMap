package net.konyan.yangonbusonthemap;

import android.app.Application;

import net.konyan.yangonbusonthemap.util.MyPref;


/**
 * Created by zeta on 1/24/17.
 */

public class YangonBusOnTheMap  extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        /*CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/mmrCensusv5_.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());*/

        new MyPref.Builder().build(this);
    }
}
