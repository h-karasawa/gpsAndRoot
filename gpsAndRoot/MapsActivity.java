//-*-coding:utf-8-*-
//プログラム名：MapsActivity.java
//機能　　　　：メイン
//引数　　　　：null
//戻り値　　　：null
//作成日　　　：2017/7/28
//更新日　　　：2017/8/3
//Copyright (c) 2017-2017 SSAvenue inc. All Rights Reserved.

package jp.co.ss_ave.gpsapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    GoogleMap mMap;
    private Marker mBrisbane;
    LatLng location;
    private double locationLatitude;
    private double locationLongitude;
    private AsyncHttpRequest request;
    ArrayList<Double> elevations = new ArrayList<Double>();
    public static int rootId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());


    }

    //REALMに標高とルートIDを挿入
    public static void insertObject(final double elevation, final int finalRootId){
        //int rootId = 1;

        Log.v("finalRootId", String.valueOf(finalRootId));


        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                SampleObject obj = realm.createObject(SampleObject.class);
                obj.id = finalRootId;
                obj.locationElevation= elevation;
            }
        });

        realm.close();



    }

    //REALM参照、LOGに書き込み
    public static void logElevation(){



        Realm realm = Realm.getDefaultInstance();

        for (int i = 0; i <= rootId; i++){
            RealmQuery<SampleObject> query = realm.where(SampleObject.class);
            query.equalTo("id", i);
            RealmResults<SampleObject> result = query.findAll();
            //RealmResults<SampleObject> result = realm.where(SampleObject.class).findAll();
            Log.v("realmResult", String.valueOf(result));
            if (result.size() > 0){
                SampleObject so = result.first();
                int realmid = so.id;
                double realmele = so.locationElevation;
                Log.v("realm", realmid + ":" + realmele);
            }else {
                Log.v("realm", "non");
            }

        }

        realm.close();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        location = new LatLng(35.68, 139.76);

        mMap.addMarker(new MarkerOptions().position(location).title("Tokyo"));
        // camera 移動
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));





        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        //LocationManagerの取得
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        //GPSから現在地の情報を取得
        Location myLocate = locationManager.getLastKnownLocation("gps");

        double mylat = myLocate.getLatitude();
        double mylng = myLocate.getLongitude();
        String myPosition = mylat + "," + mylng;

        LatLng gotanda = new LatLng(35.6261746,139.7236228) ;
        LatLng toc = new LatLng(35.6217143,139.7194610);

        LatLng local[] = {gotanda, toc};

        String ll = "35.6217143,139.7194610";
        AsyncHttpRequestRoot root = new AsyncHttpRequestRoot(mMap);
        root.execute(myPosition);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng tapLocation) {
                // tapされた位置の緯度経度
                location = new LatLng(tapLocation.latitude, tapLocation.longitude);

                locationLatitude = tapLocation.latitude;
                locationLongitude = tapLocation.longitude;


                elevationMethod(mMap, location, 0);

            }
        });
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


    //標高算出クラスへ
    public static void elevationMethod(GoogleMap mMap, LatLng location, int root){
        double elevation = 0;
        double longitude = location.longitude;
        double latitude = location.latitude;

        AsyncHttpRequest task = new AsyncHttpRequest(mMap, location, root);
        task.execute(location);

    }

}

