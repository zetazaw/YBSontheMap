package net.konyan.yangonbusonthemap.model;
/*

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
*/

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by zeta on 1/17/17.
 */

public class BusStop {


    public static final String SVC_NAME = "svc_name";
    public static final String SEQUENCE = "sequence";
    public static final String STOP_ID = "stop_id";
    public static final String NAME_EN = "name_en";
    public static final String NAME_MM = "name_mm";
    public static final String ROAD_EN = "road_en";
    public static final String ROAD_MM = "road_mm";
    public static final String TOWNSHIP_EN = "township_en";
    public static final String TOWNSHIP_MM = "township_mm";
    public static final String LAT = "lat";
    public static final String LNG = "lng";

    @SerializedName("svc_name")
    private int svc_name;
    @SerializedName("sequence")
    private int sequence;
    @SerializedName("stop_id")
    private int stop_id;
    @SerializedName("name_en")
    private String name_en;
    @SerializedName("name_mm")
    private String name_mm;
    @SerializedName("road_en")
    private String road_en;
    @SerializedName("road_mm")
    private String road_mm;
    @SerializedName("township_en")
    private String township_en;
    @SerializedName("township_mm")
    private String township_mm;
    @SerializedName("lat")
    private float lat;
    @SerializedName("lng")
    private float lng;

    private List<Integer> buses;

    public List<Integer> getBuses() {
        return buses;
    }

    public BusStop setBuses(List<Integer> buses) {
        this.buses = buses;
        return this;
    }

    public BusStop() { }

    public BusStop setSvc_name(int svc_name) {
        this.svc_name = svc_name;
        return this;
    }

    public int getSvc_name() {
        return this.svc_name;
    }

    public BusStop setSequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public int getSequence() {
        return this.sequence;
    }

    public BusStop setStop_id(int stop_id) {
        this.stop_id = stop_id;
        return this;
    }

    public int getStop_id() {
        return this.stop_id;
    }

    public BusStop setName_en(String name_en) {
        this.name_en = name_en;
        return this;
    }

    public String getName_en() {
        return this.name_en;
    }

    public BusStop setName_mm(String name_mm) {
        this.name_mm = name_mm;
        return this;
    }

    public String getName_mm() {
        return this.name_mm;
    }

    public BusStop setRoad_en(String road_en) {
        this.road_en = road_en;
        return this;
    }

    public String getRoad_en() {
        return this.road_en;
    }

    public BusStop setRoad_mm(String road_mm) {
        this.road_mm = road_mm;
        return this;
    }

    public String getRoad_mm() {
        return this.road_mm;
    }

    public BusStop setTownship_en(String township_en) {
        this.township_en = township_en;
        return this;
    }

    public String getTownship_en() {
        return this.township_en;
    }

    public BusStop setTownship_mm(String township_mm) {
        this.township_mm = township_mm;
        return this;
    }

    public String getTownship_mm() {
        return this.township_mm;
    }

    public BusStop setLat(float lat) {
        this.lat = lat;
        return this;
    }

    public float getLat() {
        return this.lat;
    }

    public BusStop setLng(float lng) {
        this.lng = lng;
        return this;
    }

    public float getLng() {
        return this.lng;
    }

    //public LatLng getLatLng(){return new LatLng(this.lat, this.lng);}


}


