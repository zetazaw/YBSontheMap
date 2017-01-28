package net.konyan.yangonbusonthemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat;

import net.konyan.yangonbusonthemap.adapter.BusesAdapter;
import net.konyan.yangonbusonthemap.model.BusStop;
import net.konyan.yangonbusonthemap.util.MyPref;
import net.konyan.yangonbusonthemap.util.ObservableUtil;
import net.konyan.yangonbusonthemap.util.Util;

import org.json.JSONException;
import org.rabbitconverter.rabbit.Rabbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraIdleListener,
        LocationListener {

    private final String LOG_TAG = HomeActivity.class.getSimpleName();

    public static final int LANGUAGE_EN = 0x0aa;
    public static final int LANGUAGE_MM = 0x0bb;
    public static final int LANGUAGE_ZG = 0x0cc;

    private int language;
    public static final String KEY_LANGUAGE = "language";

    //private final float BUS_STOP_FOUND_ZOOM = 14.5f;
    private final float BUS_STOP_VISIABLE_ZOOM = 14.5f;

    private final float DEFAULT_ZOOM = 13.8f;
    private final float BUS_ROUTE_ZOOM = 11.5f;

    private final LatLng YANGON = new LatLng(16.8661, 96.1951);

    private BottomSheetBehavior mBottomSheetBehavior;
    private View bottomSheet;
    private TextView tvBusName, tvRoadName;
    private RecyclerView busRecycler;

    private ProgressBar progressHome;

    private GoogleMap googleMap;

    private List<BusStop> allBusStops;

    private GeoJsonLayer layerLine;

    private Circle circle;
    //private Marker mapClickMarker;

    private List<Marker> selectedMarkers;
    private List<Marker> nearByMarkers;
    private List<Marker> busRouteMarkers;

    //private InterstitialAd mInterstitialAd;
    private LocationManager locationManager;

    private int currentSelectBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressHome = (ProgressBar) findViewById(R.id.progress_home);

        language = MyPref.getInt(KEY_LANGUAGE, LANGUAGE_EN);
        initData();

        initUi(savedInstanceState);
        initBanner();
    }

    //ads
    private void ads() {
        //App ID: ca-app-pub-3722160390007679~8865305547
        //Ad unit ID: ca-app-pub-3722160390007679/2818771947
        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build();
        mInterstitialAd.loadAd(adRequest);*/
    }

    public void initBanner() {
        AdView mAdView = (AdView) findViewById(R.id.adView_banner);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build();
        mAdView.loadAd(adRequest);
    }

    //initialize
    private void initUi(Bundle saveState) {

        ImageView ivMenu = (ImageView) findViewById(R.id.iv_action_menu);
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initBottomSheet();

        View view = navigationView.getHeaderView(0);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_language);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d(LOG_TAG, "" + i);
                switch (i) {
                    case R.id.nav_radio_lan_en:
                        language = LANGUAGE_EN;
                        MyPref.putInt(KEY_LANGUAGE, LANGUAGE_EN);
                        break;
                    case R.id.nav_radio_lan_mm:
                        language = LANGUAGE_MM;
                        MyPref.putInt(KEY_LANGUAGE, LANGUAGE_MM);
                        break;
                    case R.id.nav_radio_lan_zg:
                        language = LANGUAGE_ZG;
                        MyPref.putInt(KEY_LANGUAGE, LANGUAGE_ZG);
                        break;
                }

                if (nearByMarkers != null) {
                    clearMarkers(nearByMarkers, null);
                }
                if (busRouteMarkers != null) {
                    clearMarkers(busRouteMarkers, null);
                    currentSelectBus = -1;
                }
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        switch (language) {
            case LANGUAGE_EN:
                ((RadioButton) view.findViewById(R.id.nav_radio_lan_en)).setChecked(true);
                break;
            case LANGUAGE_MM:
                ((RadioButton) view.findViewById(R.id.nav_radio_lan_mm)).setChecked(true);
                break;
            case LANGUAGE_ZG:
                ((RadioButton) view.findViewById(R.id.nav_radio_lan_zg)).setChecked(true);
                break;
        }


        if (saveState == null) {
            getSupportFragmentManager()
                    .beginTransaction().add(R.id.content_home, YangonMapFragment.newInstance(null, null))
                    .commit();
        }

    }

    private void initData() {
        //init data
        ObservableUtil.getAllBusStops(this).subscribe(new Observer<List<BusStop>>() {
            @Override
            public void onSubscribe(Disposable d) {
                progressHome.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(List<BusStop> value) {
                progressHome.setVisibility(View.GONE);
                allBusStops = value;
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void initBottomSheet() {

        bottomSheet = findViewById(R.id.sheet_bus_stop);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        tvBusName = (TextView) findViewById(R.id.tv_bus_stop_name);
        tvRoadName = (TextView) findViewById(R.id.tv_bus_stop_road);

        busRecycler = (RecyclerView) bottomSheet.findViewById(R.id.rc_buses);
        busRecycler.setAdapter(null);
        busRecycler.setHasFixedSize(true);
        busRecycler.setLayoutManager(new GridLayoutManager(this, 5));

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (layerLine != null) {
            layerLine.removeLayerFromMap();
            layerLine = null;

        } else if (busRouteMarkers != null && !busRouteMarkers.isEmpty()
                || nearByMarkers != null && !nearByMarkers.isEmpty()
                ) {

            if (busRouteMarkers != null && !busRouteMarkers.isEmpty()) {
                clearMarkers(busRouteMarkers, null);
                currentSelectBus = -1;
            }


            if (nearByMarkers != null && !nearByMarkers.isEmpty()) {
                clearMarkers(nearByMarkers, null);
            }


            if (selectedMarkers != null && !selectedMarkers.isEmpty()) {
                clearMarkers(selectedMarkers, null);
            }


        } /*else if (selectedMarkers != null && !selectedMarkers.isEmpty()) {
            //clearMarkers(selectedMarkers, null);
        } */ else {
            /*if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }*/
            super.onBackPressed();
        }
    }

    ////operate

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id) {
            case R.id.nav_share:
                Util.share(this, Intent.ACTION_SEND);
                break;
            case R.id.nav_rate:
                Util.share(this, Intent.ACTION_VIEW);
                break;
            case R.id.nav_open_source:
                new EasyLicensesDialogCompat(this)
                        .setTitle("Licenses")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                break;
            case R.id.nav_yrta:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.ybs))
                        .setMessage(getString(R.string.ybs_licence_full))
                        .setPositiveButton(getString(android.R.string.ok), null)
                        .create()
                        .show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        item.setCheckable(false);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(this);

        this.googleMap = googleMap;

        //googleMap.setMaxZoomPreference(DEFAULT_MAX_ZOOM);
        //googleMap.setMinZoomPreference(BUS_ROUTE_ZOOM);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        checkPermission();


        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                initData();
                Log.d(LOG_TAG, "map loaded");
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            cameraUpdate(YANGON, DEFAULT_ZOOM);
        } else {
            float distance = distFrom(YANGON.latitude, YANGON.longitude, location.getLatitude(), location.getLongitude());
            Log.d(LOG_TAG, "from yangon >" + distance);
            cameraUpdate(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (nearByMarkers != null) clearMarkers(nearByMarkers, null);
        //if (mapClickMarker != null) mapClickMarker.remove();
        if (circle != null) circle.remove();

        String title = getString(R.string.select_point);

        /*mapClickMarker = googleMap.addMarker(setMarker(latLng, title, null,
                R.drawable.ic_user));*/

        circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(800)
                .strokeColor(Color.TRANSPARENT)
                .fillColor(0x220000FF)//0x10000000
                .strokeWidth(1)
        );

        boolean found = showFoundBusStops(latLng.latitude, latLng.longitude, language);
        Log.d(LOG_TAG, "first found-" + found);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        /*if (marker.equals(mapClickMarker)) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            marker.showInfoWindow();
            showFoundBusStops(marker.getPosition().latitude, marker.getPosition().longitude, language);
            return false;
        }*/

        if (selectedMarkers == null) {
            selectedMarkers = new ArrayList<>();
        }
        selectedMarkers.add(marker);


        //if (nearByMarkers != null) clearMarkers(nearByMarkers, marker);
        //if (busRouteMarkers != null) clearMarkers(busRouteMarkers, marker);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        marker.showInfoWindow();
        showBusStopInfoFromMarker(marker);
        return true;
    }

    //operation start
    //
    private boolean showFoundBusStops(double lat, double lng, int language) {


        //// TODO: 1/22/17 language switch with @language param
        String nobus = getString(R.string.message_no_bus_stop);

        if (language == LANGUAGE_MM) {
            nobus = getString(R.string.message_no_bus_stop_mm);
        } else if (language == LANGUAGE_ZG) {
            nobus = getString(R.string.message_no_bus_stop_mm);
            nobus = Rabbit.uni2zg(nobus);
        }


        List<BusStop> found = searchBusStops(lat, lng);
        if (found == null) {
            //data require
            //show message
            return false;
        }
        if (found.size() < 1) {
            //no data found
            //show message
            //mapClickMarker.setTitle(nobus);
            //mapClickMarker.showInfoWindow();
            Toast.makeText(this, nobus, Toast.LENGTH_SHORT).show();

            return false;
        } else {
            //if (mapClickMarker != null) mapClickMarker.remove();
            if (nearByMarkers == null) {
                nearByMarkers = new ArrayList<>();
            }


            Log.d(LOG_TAG, "start marker init");
            for (BusStop busStop : found) {

                String busStopName = busStop.getName_mm();
                //String townshipName = busStop.getTownship_mm();

                /*if (language == LANGUAGE_ZG) {

                    busStopName = Rabbit.uni2zg(busStopName);
                    //townshipName = Rabbit.uni2zg(townshipName);
                }*/

                if (language == LANGUAGE_EN) {
                    busStopName = busStop.getName_en();
                    //townshipName = busStop.getTownship_en();
                }

                Marker marker = googleMap.addMarker(
                        setMarker(
                                new LatLng(busStop.getLat(), busStop.getLng()),
                                busStopName, null, R.drawable.ic_action_bus_stop));
                marker.setTag(busStop);
                nearByMarkers.add(marker);

            }
            Log.d(LOG_TAG, "finish marker init");
            cameraUpdate(new LatLng(lat, lng), BUS_STOP_VISIABLE_ZOOM);
            return true;
        }

    }

    private void showBusStopInfoFromMarker(final Marker marker) {
        BusStop busStop = (BusStop) marker.getTag();

        if (busStop == null) {
            return;
        }

        String busName = busStop.getName_mm();
        String roadName = busStop.getRoad_mm();
        String townshipName = busStop.getTownship_mm();

        /*if (language == LANGUAGE_ZG) {
            busName = Rabbit.uni2zg(busName);
            roadName = Rabbit.uni2zg(roadName);
            townshipName = Rabbit.uni2zg(townshipName);
        }*/
        if (language == LANGUAGE_EN) {
            busName = busStop.getName_en();
            roadName = busStop.getRoad_en();
            townshipName = busStop.getTownship_en();

        }

        tvBusName.setText(String.format("%s (%s)", busName, townshipName));
        tvRoadName.setText(String.format("%s", roadName));

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        ObservableUtil.getRoutes(this, busStop.getBuses())
                .subscribe(new Observer<List<Feature>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Feature> value) {
                        if (value != null) {
                            initRecycler(value, marker);

                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    private void initRecycler(List<Feature> value, final Marker selectMarker) {

        busRecycler.setAdapter(new BusesAdapter(this, value,
                new BusesAdapter.BusItemClickListener() {
                    @Override
                    public void onItemClick(Feature bus) {

                        //if (searchMarker != null) searchMarker.remove();

                        //clearMarkers(nearByMarkers, selectMarker);

                        showRoute(bus);

                    }
                }));
    }

    private void showRoute(Feature busRoute) {
        //final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_bus_stop);

        if (layerLine != null) layerLine.removeLayerFromMap();

        if (busRouteMarkers != null) clearMarkers(busRouteMarkers, null);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        try {

            currentSelectBus = busRoute.getProperties().getInt("svc_name");
            String busColor = busRoute.getProperties().getString("color");

            layerLine = new GeoJsonLayer(googleMap, busRoute.toJSON());
            layerLine.getDefaultLineStringStyle().setColor(Color.parseColor(busColor));
            layerLine.getDefaultLineStringStyle().setWidth(3);
            layerLine.addLayerToMap();


        } catch (JSONException e) {
            e.printStackTrace();
        }

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(BUS_ROUTE_ZOOM + 1f));

    }

    private List<BusStop> findAndShowBusRoute(int bus_name) {
        if (allBusStops == null) return null;

        if (busRouteMarkers == null) {
            busRouteMarkers = new ArrayList<>();
        } else {
            clearMarkers(busRouteMarkers, null);
        }


        for (BusStop busStop : allBusStops) {
            if (busStop.getSvc_name() == bus_name) {

                String busStopName = busStop.getName_mm();

                if (language == LANGUAGE_EN) {
                    busStopName = busStop.getName_en();
                    //townshipName = busStop.getTownship_en();
                }

                Marker marker = googleMap.addMarker(
                        setMarker(
                                new LatLng(busStop.getLat(), busStop.getLng()),
                                busStopName, null,
                                R.drawable.ic_action_bus_stop));
                marker.setTag(busStop);
                busRouteMarkers.add(marker);
            }
        }

        return null;
    }




    /*
    * Util methods
    * ------------------------
    * */

    /*
    * #1 search bus-stops
    * */

    private List<BusStop> searchBusStops(double lat, double lng) {
        if (allBusStops == null) return null;

        List<BusStop> foundBusStopMap = new ArrayList<>();
        Log.d(LOG_TAG, "start search");
        //start search
        for (BusStop busStop : allBusStops) {

            //find distance from current selected point

            float dis = distFrom((float) lat, (float) lng, busStop.getLat(), busStop.getLng());

            //less estimate 1000 meter, add to found
            if (dis < 800) {
                foundBusStopMap.add(busStop);
            }
        }

        //return value is null, there is no data initialized.
        //return value size is less than 1, no data found
        return foundBusStopMap;
    }

    private Map<String, BusStop> searchBusStopMap(double lat, double lng) {
        if (allBusStops == null) return null;

        Map<String, BusStop> foundBusStopMap = new HashMap<>();

        //start search
        for (BusStop busStop : allBusStops) {

            //find distance from current selected point

            float dis = distFrom((float) lat, (float) lng, busStop.getLat(), busStop.getLng());

            //less estimate 1000 meter, add to found
            if (dis < 1000) {
                foundBusStopMap.put(busStop.getName_en(), busStop);
            }
        }

        //return value is null, there is no data initialized.
        //return value size is less than 1, no data found
        return foundBusStopMap;
    }

    /*
    * #2 search distance from bus stops
    * get from
    * http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    * answer by
    * http://stackoverflow.com/users/1542/espen-herseth-halvorsen
    * */
    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        return distFrom((float) lat1, (float) lng1, (float) lat2, (float) lng2);
    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private MarkerOptions setMarker(LatLng latLng, String title, String snipped, int pic) {
        return new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snipped)
                .flat(true).icon(BitmapDescriptorFactory.fromResource(pic));
    }

    private void clearMarkers(List<Marker> markers, Marker remain) {

        if (markers == null || markers.size() < 0) return;

        for (Marker marker : markers) {
            if (!marker.equals(remain)) {
                marker.remove();
            }
        }

        markers.clear();
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    12);

            return;

        }

    }


    private void cameraUpdate(LatLng latLng, float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.animateCamera(cameraUpdate);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //update map

        } else {

            //show message
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "loc change" + location.toString());
        float distFromYgn = distFrom(YANGON.latitude, YANGON.longitude,
                location.getLatitude(), location.getLongitude());
        Log.d(LOG_TAG, "dis fro ygn>" + distFromYgn);
        //showFoundBusStops(location.getLatitude(), location.getLongitude(), SELECTED_LANGUAGE);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(LOG_TAG, "loc status change" + s);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(LOG_TAG, "loc enable" + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(LOG_TAG, "loc disable" + s);
    }

    @Override
    public void onCameraIdle() {
        float zoom = googleMap.getCameraPosition().zoom;

        Log.d(LOG_TAG, "camera idle" + zoom);

        if (zoom >= BUS_STOP_VISIABLE_ZOOM && currentSelectBus != -1) {
            findAndShowBusRoute(currentSelectBus);
        } else {
            if (busRouteMarkers != null) clearMarkers(busRouteMarkers, null);
        }


    }
}
