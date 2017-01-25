package net.konyan.yangonbusonthemap.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import net.konyan.yangonbusonthemap.R;

/**
 * Created by zeta on 1/24/17.
 */

public class Util {
    private Util(){}


    public static void share(Context context, String action){
        final String MY_URL = context.getString(R.string.my_url);
        Intent myShareIntent = new Intent(action);
        if (action.equals(Intent.ACTION_VIEW)){
            myShareIntent.setData(Uri.parse(MY_URL));
            context.startActivity(myShareIntent);
        }else {
            myShareIntent.setType("text/plain");
            myShareIntent.putExtra(Intent.EXTRA_TITLE, "Simple and useful application!");
            myShareIntent.putExtra(Intent.EXTRA_TEXT, MY_URL);
            context.startActivity(myShareIntent);
        }


    }

}
