package com.ucsc.pnp.bustrack_data;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

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


        if(numberoflocationdata!=20){
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
      Runnable runble=new Runnable() {
          @Override
          public void run() {
              flushToNetwork(msg.toString());
          }
      };

        Thread thread=new Thread(runble);
        thread.start();

    }

    public void flushToNetwork(String msg) {

        Socket smtpSocket = null;
        DataOutputStream os = null;
        final String QUEUE_NAME ="filterdata";

        try {
            Log.e("PPPP","step1");
            ConnectionFactory factory = new ConnectionFactory();
            Log.e("PPPP","step2");
            try {
                factory.setHost("10.0.2.2");
                //factory.setPort(15672);
            } catch(Exception e1){
                e1.printStackTrace();
            }
            Log.e("PPPP","step3");
            Connection connection = factory.newConnection();
            Log.e("PPPP","step4");
            Channel channel = connection.createChannel();
            Log.e("PPPP","step5");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            //System.out.println(" [x] Sent '" + message + "'");

        } catch(IOException | TimeoutException e){
            System.out.println("Problem with Connecting");
            e.printStackTrace();
        }

    /*    try {
            smtpSocket = new Socket("10.22.127.2", 8072);

            os = new DataOutputStream(smtpSocket.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

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
*/
    }
}
