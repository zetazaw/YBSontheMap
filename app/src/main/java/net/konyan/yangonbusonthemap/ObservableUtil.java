package net.konyan.yangonbusonthemap;

import android.content.Context;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.google.android.gms.maps.model.LatLng;

import net.konyan.yangonbusonthemap.model.BusStop;
import net.konyan.yangonbusonthemap.util.RawUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zeta on 1/22/17.
 */

public class ObservableUtil {


    private ObservableUtil() {
    }


    public static Observable<List<BusStop>> getAllBusStops(Context context) {
        return getBusStops(context, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public static Observable<List<Feature>> getRoutes(Context context, List<Integer> busList) {
        return routeObservable(context, busList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }


    private static Observable<List<BusStop>> getBusStops(final Context context, LatLng filter) {
        return Observable.create(new ObservableOnSubscribe<List<BusStop>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BusStop>> e) throws Exception {
                e.onNext(RawUtil.getStops(context));
            }
        });
    }

    private static Observable<List<Feature>> routeObservable(final Context context, final List<Integer> busList) {
        return Observable.create(new ObservableOnSubscribe<List<Feature>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Feature>> e) throws Exception {
                try {
                    FeatureCollection result =
                            (FeatureCollection)
                                    RawUtil.getFeatureCollection(context, R.raw.services_000115);

                    Map<Integer, Feature> featureMap = new HashMap<Integer, Feature>();

                    for (Feature i : result.getFeatures())
                        featureMap.put(i.getProperties().getInt("svc_name"), i);

                    List<Feature> filterList = new ArrayList<Feature>();

                    for (int bus : busList) {
                        filterList.add(featureMap.get(bus));
                    }

                    e.onNext(filterList);

                } catch (IOException ioe) {
                    e.onError(ioe);
                } catch (JSONException exception) {
                    e.onError(exception);
                }
            }
        });
    }


}
