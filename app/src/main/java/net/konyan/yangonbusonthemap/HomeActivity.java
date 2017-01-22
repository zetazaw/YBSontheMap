package net.konyan.yangonbusonthemap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cocoahero.android.geojson.Feature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat;

import net.konyan.yangonbusonthemap.adapter.BusesAdapter;
import net.konyan.yangonbusonthemap.model.BusStop;
import net.konyan.yangonbusonthemap.util.RawUtil;

import org.json.JSONException;

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
        GoogleMap.OnMarkerClickListener {

    final String LOG_TAG = HomeActivity.class.getSimpleName();

    final int LANGUAGE_EN = 0x0ff;

    final float DEFAULT_ZOOM = 13.8f;
    final float DEFAULT_MIN_ZOOM = 11.5f;
    final float DEFAULT_MAX_ZOOM = 18;

    final LatLng YANGON = new LatLng(16.8661, 96.1951);

    private BottomSheetBehavior mBottomSheetBehavior;
    private View bottomSheet;
    private TextView tvBusName, tvRoadName;
    private RecyclerView busRecycler;

    private ProgressBar progressHome;

    private GoogleMap googleMap;

    private List<BusStop> allBusStops;
    private GeoJsonLayer layerLine;

    private Marker mapClickMarker;
    private List<Marker> selectedMarkers;
    private List<Marker> nearByMarkers;
    private List<Marker> busRouteMarkers;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initUi(savedInstanceState);
        ads();

    }

    //ads
    private void ads(){
        //App ID: ca-app-pub-3722160390007679~8865305547
        //Ad unit ID: ca-app-pub-3722160390007679/2818771947
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    //initialize
    private void initUi(Bundle saveState) {
        progressHome = (ProgressBar) findViewById(R.id.progress_home);

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


        if (saveState == null){
            getSupportFragmentManager()
                    .beginTransaction().add(R.id.content_home, YangonMapFragment.newInstance(null, null))
                    .commit();
        }

    }

    private void initBottomSheet() {

        bottomSheet = findViewById(R.id.sheet_bus_stop);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        tvBusName = (TextView) findViewById(R.id.tv_bus_stop_name);
        tvRoadName = (TextView) findViewById(R.id.tv_bus_stop_road);

        busRecycler = (RecyclerView) bottomSheet.findViewById(R.id.rc_buses);
        busRecycler.setHasFixedSize(true);
        busRecycler.setLayoutManager(new GridLayoutManager(this, 5));

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (busRouteMarkers != null || layerLine != null) {

            if (busRouteMarkers != null) {
                clearMarkers(busRouteMarkers, null);
                busRouteMarkers = null;
            }

            if (layerLine != null) {
                layerLine.removeLayerFromMap();
                layerLine = null;
            }


        } else if (nearByMarkers != null){
            clearMarkers(nearByMarkers, null);
            nearByMarkers = null;
        } else if (selectedMarkers != null ){
            clearMarkers(selectedMarkers, null);
            selectedMarkers = null;
        } else {
            if (mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    ////operate

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id){
            case R.id.nav_share:
                share(Intent.ACTION_SEND);
                break;
            case R.id.nav_rate:
                share(Intent.ACTION_VIEW);
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
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(YANGON, DEFAULT_ZOOM);
        googleMap.setMaxZoomPreference(DEFAULT_MAX_ZOOM);
        googleMap.setMinZoomPreference(DEFAULT_MIN_ZOOM);
        googleMap.animateCamera(cameraUpdate);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        this.googleMap = googleMap;

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

        if (checkPermission()) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (mapClickMarker != null) mapClickMarker.remove();

        String title = "Selected point!";
        mapClickMarker = googleMap.addMarker(setMarker(latLng, title, null,
                R.drawable.ic_user));

        showFoundBusStops(latLng.latitude, latLng.longitude, LANGUAGE_EN);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(mapClickMarker)){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            marker.showInfoWindow();
            showFoundBusStops(marker.getPosition().latitude, marker.getPosition().longitude, LANGUAGE_EN);
            return false;
        }

        if (selectedMarkers == null) selectedMarkers = new ArrayList<>();
        selectedMarkers.add(marker);

        if (nearByMarkers != null) clearMarkers(nearByMarkers, marker);
        if (busRouteMarkers != null) clearMarkers(busRouteMarkers, marker);


        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        marker.showInfoWindow();
        showBusStopInfoFromMarker(marker);
        return true;
    }

    //operation start
    //
    private boolean showFoundBusStops(double lat, double lng, int language) {
        //// TODO: 1/22/17 language switch with @language param
        Map<String, BusStop> found = searchBusStops(lat, lng);
        if (found == null) {
            //data require
            //show message
            return false;
        }
        if (found.size() < 1) {
            //no data found
            //show message
            return false;
        } else {
            if (nearByMarkers == null) {
                nearByMarkers = new ArrayList<>();
            }


            for (BusStop busStop : new ArrayList<>(found.values())) {
                Marker marker = googleMap.addMarker(
                        setMarker(
                                new LatLng(busStop.getLat(), busStop.getLng()),
                                busStop.getName_en(), busStop.getTownship_en(),
                                R.drawable.ic_action_bus_stop));
                marker.setTag(busStop);
                nearByMarkers.add(marker);

            }
            return true;
        }

    }

    private void showBusStopInfoFromMarker(final Marker marker) {
        BusStop busStop = (BusStop) marker.getTag();

        if (busStop == null) {
            return;
        }


        tvBusName.setText(String.format("%s (%s)", busStop.getName_en(), busStop.getTownship_en()));
        tvRoadName.setText(String.format("%s", busStop.getRoad_en()));

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

        if (layerLine != null) {
            Log.d(LOG_TAG, "more layer>>" + layerLine.toString());
            layerLine.removeLayerFromMap();
        }

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        try {

            int busName = busRoute.getProperties().getInt("svc_name");
            String busColor = busRoute.getProperties().getString("color");

            layerLine = new GeoJsonLayer(googleMap, busRoute.toJSON());
            layerLine.getDefaultLineStringStyle().setColor(Color.parseColor(busColor));
            layerLine.getDefaultLineStringStyle().setWidth(3);
            layerLine.addLayerToMap();

            findAndShowBusRoute(busName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_MIN_ZOOM + 1f));

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

                Marker marker = googleMap.addMarker(
                        setMarker(
                                new LatLng(busStop.getLat(), busStop.getLng()),
                                busStop.getName_en(), busStop.getTownship_en(),
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

    private Map<String, BusStop> searchBusStops(double lat, double lng) {
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
    * */
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

    private boolean checkPermission() {
        //if (ActivityCompat.checkSelfPermission(this,
        // android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        // && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        // != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        //  return;
        //}
        return (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED);
    }

    private void share(String action){
        final String MY_URL = "https://play.google.com/store/apps/details?id=net.konyan.yangonbusonthemap";
        Intent myShareIntent = new Intent(action);
        if (action.equals(Intent.ACTION_VIEW)){
            myShareIntent.setData(Uri.parse(MY_URL));
            startActivity(myShareIntent);
        }else {
            myShareIntent.setType("text/plain");
            myShareIntent.putExtra(Intent.EXTRA_TITLE, "Simple and useful application!");
            myShareIntent.putExtra(Intent.EXTRA_TEXT, MY_URL);
            startActivity(myShareIntent);
        }


    }

}
