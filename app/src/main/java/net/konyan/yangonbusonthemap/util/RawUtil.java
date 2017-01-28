package net.konyan.yangonbusonthemap.util;

import android.content.Context;
import android.content.res.Resources;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.konyan.yangonbusonthemap.HomeActivity;
import net.konyan.yangonbusonthemap.R;
import net.konyan.yangonbusonthemap.model.BusStop;

import org.json.JSONException;
import org.rabbitconverter.rabbit.Rabbit;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by zeta on 1/17/17.
 */

public class RawUtil {

    private RawUtil() {
    }

    public static List<BusStop> getStops(Context context, int lan) {
        try {
            return readJson(context, lan);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GeoJSONObject getFeatureCollection(Context context, int rawId) throws IOException, JSONException {
        return readGeoJson(context, rawId);
    }

    private static List<BusStop> readJson(Context context, int lan) throws IOException {

        Resources res = context.getResources();
        InputStream is = res.openRawResource(R.raw.services_000115_buses_id_added);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        if (lan == HomeActivity.LANGUAGE_ZG){
            json = Rabbit.uni2zg(json);
        }

        Type listType = new TypeToken<List<BusStop>>() {
        }.getType();
        return new Gson().fromJson(json, listType);
    }

    private static GeoJSONObject readGeoJson(Context context, int rawId) throws IOException, JSONException {
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawId);
        return GeoJSON.parse(in_s);


    }

}
