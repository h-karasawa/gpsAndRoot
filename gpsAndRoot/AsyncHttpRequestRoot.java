//-*-coding:utf-8-*-
//プログラム名：AsyncHttpRequestRoot.java
//機能　　　　：GoogleMapAPIにアクセスし、ルートを取得する
//引数　　　　：1(GoogleMap)
//戻り値　　　：null
//作成日　　　：2017/8/1
//更新日　　　：2017/7/3
//Copyright (c) 2017-2017 SSAvenue inc. All Rights Reserved.

package jp.co.ss_ave.gpsapp;

import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Haruna on 2017/08/01.
 */

public class AsyncHttpRequestRoot extends AsyncTask<String, Void, PolylineOptions[]> {
    private GoogleMap mMap;
    private  LatLng location = new LatLng(35.6261746,139.7236228);
    public static ArrayList<Double> elevations = new ArrayList<Double>();
    ArrayList<ArrayList<Double>> getElevations = new ArrayList<ArrayList<Double>>();
    private int id;


    public AsyncHttpRequestRoot(GoogleMap map) {
        // 呼び出し元のアクティビティもとい結果出力に必要なアイテム
        this.mMap = map;


    }
    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    @Override
    protected PolylineOptions[] doInBackground(String... locations) {
        //List<List<HashMap>> routes = null;
        String start;// = locations[0];
        String end;// = locations[1];

        end = "35.626174607330256,139.72362287342548";//五反田駅
        start = "35.62171435376273,139.71946109086275";//TOC
        start = locations[0]; //現在地
        LatLng gotanda = new LatLng(35.6261746,139.7236228) ;
        location = gotanda;
        LatLng toc = new LatLng(35.6217143,139.7194610);

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+ start + "&destination=" + end + "&mode=walking&alternatives=true" ;
        Log.v("jsonURL", url);
        //PolylineOptions polylineOptions = new PolylineOptions();
        PolylineOptions[] polylines= new PolylineOptions[3];
        PolylineOptions po = new PolylineOptions();//.add(toc, gotanda).color(Color.RED).width(3);
        polylines[0] = po.color(Color.RED).width(5);
        polylines[1] = po.color(Color.BLUE).width(4);
        polylines[2] = po.color(Color.GREEN).width(3);

        // httpリクエスト投げる処理を書く。
        try {
            InputStream is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            //String po = json.get("overview_polyline").toString();
            //String p = json.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");

            LatLng point = gotanda;

            int len = json.getJSONArray("routes").length();
            Log.v("lenInt", String.valueOf(len));
            polylines = new  PolylineOptions[len];
            for(int i = 0; i < len; i++){
                if (i == 0){
                    polylines[i] = new PolylineOptions().color(Color.RED).width(8);
                }else if (i == 1){
                    polylines[i] = new PolylineOptions().color(Color.BLUE).width(5);
                }else {
                    polylines[i] = new PolylineOptions().color(Color.GREEN).width(3);
                }
            }

            //int lenSteps1 = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").length();
            //Log.v("lenSteps1", String.valueOf(lenSteps1));

            for (int j = 0; j < len; j++){
                //MapsActivity.getRootId(j);

                Log.v("intJ", String.valueOf(j));
                MapsActivity.rootId = j;
                PolylineOptions polylineOptions = new PolylineOptions();
                int lenSteps = json.getJSONArray("routes").getJSONObject(j).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").length();
                Log.v("lenSteps", String.valueOf(lenSteps));

                for (int i = 0; i < lenSteps; i++) {
                    double startLat = json.getJSONArray("routes").getJSONObject(j).getJSONArray("legs").getJSONObject(0)
                            .getJSONArray("steps")
                            .getJSONObject(i)
                            .getJSONObject("start_location")
                            .getDouble("lat");

                    Log.v("startLat", String.valueOf(startLat));
                    double startLng = json.getJSONArray("routes").getJSONObject(j).getJSONArray("legs").getJSONObject(0)
                            .getJSONArray("steps")
                            .getJSONObject(i)
                            .getJSONObject("start_location")
                            .getDouble("lng");
                    double endLat = json.getJSONArray("routes").getJSONObject(j).getJSONArray("legs").getJSONObject(0)
                            .getJSONArray("steps")
                            .getJSONObject(i)
                            .getJSONObject("end_location")
                            .getDouble("lat");
                    double endLng = json.getJSONArray("routes").getJSONObject(j).getJSONArray("legs").getJSONObject(0)
                            .getJSONArray("steps")
                            .getJSONObject(i)
                            .getJSONObject("end_location")
                            .getDouble("lng");
                    point = new LatLng(startLat, startLng);
                    LatLng nextPoint = new LatLng(endLat, endLng);
                    polylines[j].add(point, nextPoint);

                    LatLng location = new LatLng(endLat, endLng);

                    MapsActivity.elevationMethod(mMap, location, j);

                }

            }

            return polylines;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return polylines;

    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }





    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(PolylineOptions[] polylineOptions) {
        //Log.v("polylineOptions", String.valueOf(polylineOptions));

        for (int i = 0; i < polylineOptions.length; i++) {
            PolylineOptions pos = polylineOptions[i];
            Log.v("pos", String.valueOf(pos));
            mMap.addPolyline(pos);
        }
        String position = "lat:" + location.latitude + " lng:"+ location.longitude;
        // 取得した結果を出力
        mMap.addMarker(new MarkerOptions().position(location).title("gotanda to toc").snippet(position));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

        MapsActivity.logElevation();

    }
}
