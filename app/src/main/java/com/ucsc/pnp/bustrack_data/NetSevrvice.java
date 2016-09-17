package com.ucsc.pnp.bustrack_data;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by nrv on 9/18/16.
 */
public class NetSevrvice extends Service implements LocationListener{
    int numberOfTickets=0;
    int fuellevel=15000;
    int id=10255;
    String regno="255-2020";
    JSONArray locationaarray=new JSONArray();
    int numberoflocationdata=0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

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


        if(numberoflocationdata==20){
            generatemessage(locationaarray);
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

        return null;
    }

    public long getSystemtime(){
        return System.currentTimeMillis();
    }

}
