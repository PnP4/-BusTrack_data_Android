package com.ucsc.pnp.bustrack_data;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by nrv on 9/18/16.
 */
public class NetSevrvice extends Service implements LocationListener {
    int numberOfTickets = 0;
    int fuellevel = 15000;
    int id = 10255;
    String regno = "255-2020";
    JSONArray locationaarray = new JSONArray();
    int numberoflocationdata = 0;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new NetSevrvice();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1, locationListener);

        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {


        if(numberoflocationdata==0){
            locationaarray=new JSONArray();
        }

        JSONObject templocdata=new JSONObject();
        try {
            templocdata.put("speed",location.getSpeed());
            templocdata.put("lat",location.getLatitude());
            templocdata.put("lon",location.getLongitude());
            locationaarray.put(templocdata);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        numberoflocationdata=numberoflocationdata+1;


        if(numberoflocationdata>0){
           JSONObject tosend=generatemessage(locationaarray);
            sendMessage(tosend);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public int generateTicketnumber(){
        Random randomgen=new Random();
        int newTicketnumber = randomgen.nextInt(3);
        return newTicketnumber;
    }

    public int generateWastedFueltnumber(){
        Random randomgen=new Random();
        int newTicketnumber = randomgen.nextInt(500);
        return newTicketnumber;
    }

    public JSONObject generatemessage(JSONArray locdata){
        numberOfTickets=numberOfTickets+generateTicketnumber();
        fuellevel=fuellevel-generateWastedFueltnumber();
        if(fuellevel<=0){
            fuellevel=15000;
        }

        JSONObject message=new JSONObject();

        try {
            message.put("id",id);
            message.put("regno",regno);
            message.put("nooftickets",numberOfTickets);
            message.put("time",getSystemtime());
            message.put("fuel",fuellevel);
            message.put("datamov",locdata);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;
    }

    public long getSystemtime(){
        return System.currentTimeMillis();
    }

    public void sendMessage(final JSONObject msg){
     /* Runnable runble=new Runnable() {
          @Override
          public void run() {
              flushToNetwork(msg.toString());
          }
      };

        Thread thread=new Thread(runble);
        thread.start();*/

        new SendNetwork().execute(msg.toString());

    }

    public void flushToNetwork(String msg) {

        Socket smtpSocket = null;
        DataOutputStream os = null;


        try {
            smtpSocket = new Socket("192.34.63.88", 8072);

            os = new DataOutputStream(smtpSocket.getOutputStream());
        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }
        if (smtpSocket != null && os != null) {
            try {
                os.writeBytes(msg);


                os.close();
                smtpSocket.close();
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }

    }

    public void postData(String json) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://138.197.30.34:5003");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("data", json));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SendNetwork extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... params) {
            postData(params[0]);
            return null;
        }
    }




}
