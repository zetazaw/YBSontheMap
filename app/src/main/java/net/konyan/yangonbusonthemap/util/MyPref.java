package net.konyan.yangonbusonthemap.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zeta on 1/26/17.
 */

public final class MyPref {

    private static SharedPreferences mPref;

    private MyPref(){}

    private static void setPref(Context context){
        mPref = context.getSharedPreferences("", Context.MODE_PRIVATE);
    }

    public static void putInt(final String key, final int value) {
        if (mPref == null) throw new RuntimeException("must init first ");

        final SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(final String key, final int defValue) {
        return mPref.getInt(key, defValue);
    }

    public static class Builder{
        private Builder(){}
        public static void build(Context context){
            MyPref.setPref(context);
        }
    }


}
