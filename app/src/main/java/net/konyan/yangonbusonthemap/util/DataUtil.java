package net.konyan.yangonbusonthemap.util;

import android.content.Context;
import android.util.Log;

import net.konyan.yangonbusonthemap.model.BusStop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zeta on 1/18/17.
 */

public class DataUtil {
    static final String LOG_TAG = DataUtil.class.getSimpleName();

    DataUtil() {
    }

    public static void sort(Context context) {
        //sortData(context);
        final List<BusStop> busStops = RawUtil.getStops(context, 0);
        findDu(busStops);
    }

    private static void sortData(Context context) {
        final List<BusStop> busStops = RawUtil.getStops(context, 0);
    }

    private static void sortData(List<BusStop> busStops){
        Collections.sort(busStops, new Comparator<BusStop>() {
            @Override
            public int compare(BusStop busStop, BusStop t1) {

                return busStop.getStop_id() - t1.getStop_id();
            }
        });

        for (BusStop busStop : busStops) {
            //Log.d(LOG_TAG, busStop.getSvc_name() + ">\t" + busStop.getStop_id());
            Log.d(LOG_TAG, "" + busStop.getStop_id());
        }
    }

    private static List<BusStop> removeDuplicates(List<BusStop> listWithDuplicates) {
        /* Set of all attributes seen so far */
        Set<BusStop> attributes = new HashSet<BusStop>();
        /* All confirmed duplicates go in here */
        List duplicates = new ArrayList<BusStop>();

        for (BusStop x : listWithDuplicates) {
            if (attributes.contains(x)) {
                duplicates.add(x);
            }
            attributes.add(x);
        }

        /* Clean list without any dups */
        listWithDuplicates.remove(duplicates);
        return listWithDuplicates;
    }

    private static List<BusStop> findDu(List<BusStop> listWithDuplicates){
        //List<BusStop> temp = listWithDuplicates;

        Map<Integer, BusStop> map = new HashMap<>();

        for (BusStop busStop : listWithDuplicates){
            map.put(busStop.getStop_id(), busStop);
        }



        List<BusStop> list =  new ArrayList<>(map.values());

        sortData(list);

        return list;
        /*for (BusStop busStop : map.values()){
            Log.d(LOG_TAG,">"+ busStop.getStop_id());
        }*/


    }
}
