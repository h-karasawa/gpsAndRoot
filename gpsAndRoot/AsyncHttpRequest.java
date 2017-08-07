//-*-coding:utf-8-*-
//プログラム名：AsyncHttpRequest.java
//機能　　　　：座標を用いて国土地理院のAPIにアクセスし、標高を取得する
//引数　　　　：2(GoogleMap, LatLng)
//戻り値　　　：null
//作成日　　　：2017/8/1
//更新日　　　：2017/7/3
//Copyright (c) 2017-2017 SSAvenue inc. All Rights Reserved.

package jp.co.ss_ave.gpsapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import io.realm.Realm;

/**
 * Created by Haruna on 2017/08/01.
 */

//座標を用いて国土地理院のAPIにアクセスし、標高を取得する

public class AsyncHttpRequest extends AsyncTask<LatLng, Void, Double> {
    private GoogleMap mMap;
    private  LatLng location;
    private int rootid;


    public AsyncHttpRequest(GoogleMap map, LatLng mlocation, int id) {

        // 呼び出し元のアクティビティもとい結果出力に必要なアイテム
        this.mMap = map;
        this.location = mlocation;
        this.rootid = id;

    }

    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    @Override
    protected Double doInBackground(LatLng... locations) {
        LatLng location = locations[0];
        double longitude = location.longitude;
        double latitude = location.latitude;
        //標高を代入する変数
        double elevation = 0;


        // httpリクエスト投げる処理を書く。
        try {
            InputStream is = new URL("http://cyberjapandata2.gsi.go.jp/general/dem/scripts/getelevation.php?lon=" + longitude + "&lat=" + latitude + "&outtype=JSON").openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            String strElevation = json.getString("elevation").toString();
            Log.v("strEle:", strElevation);
            elevation = new Double(strElevation).doubleValue();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String result = "ele:" + elevation;

        return elevation;

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
    protected void onPostExecute(Double result) {
        String position = "lat:" + location.latitude + " lng:"+ location.longitude;
        // 取得した結果を出力
        //mMap.addMarker(new MarkerOptions().position(location).title(result).snippet(position));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

        Log.v("rootele", String.valueOf(result) + ":" +rootid);

        //AsyncHttpRequestRoot.elevationArray(result);

        //MapsActivity.SampleObject(result);
        MapsActivity.insertObject(result, rootid);


        /*
        String end = location.latitude + "," + location.longitude;
        AsyncHttpRequestRoot root = new AsyncHttpRequestRoot(mMap);
        root.execute(end);*/

    }
}
