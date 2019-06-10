package com.jgangl.weatherstation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private TextView tempTextView;
    private TextView humidTextView;
    private TextView pressTextView;

    private double currentTemp;//Celsius
    private double currentHum;
    private double currentPress;

    private double highTempThreshold;
    private double lowTempThreshold;

    DecimalFormat precision = new DecimalFormat("0.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTextView = findViewById(R.id.tempTextView);
        humidTextView = findViewById(R.id.humidTextView);
        pressTextView = findViewById(R.id.pressTextView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ValueEventListener temperatureListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.getValue() != null){
                    //Temperature comes as a 4 digit # ("4650" = 46.50 degrees Celsius)
                    currentTemp = celsiusToFahrenheit(dataToCelsius(Integer.parseInt(dataSnapshot.getValue().toString())));
                    tempTextView.setText(precision.format(currentTemp));

                    if(currentTemp >= highTempThreshold){
                        sendNotification();
                    }
                    else if(currentTemp <= lowTempThreshold){
                        sendNotification();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Firebase Error", "loadPost:onCancelled", databaseError.toException());
            }
        };
        ValueEventListener humidityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.getValue() != null){
                    currentHum = dataToHumidity(Integer.parseInt(dataSnapshot.getValue().toString()));
                    humidTextView.setText(precision.format(currentHum));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Firebase Error", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        ValueEventListener pressureListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.getValue() != null){
                    currentPress = dataToPascals(Integer.parseInt(dataSnapshot.getValue().toString()));
                    pressTextView.setText(precision.format(pascalsToMercury(currentPress)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Firebase Error", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        mDatabase.child("Temperature").addValueEventListener(temperatureListener);
        mDatabase.child("Humidity").addValueEventListener(humidityListener);
        mDatabase.child("Pressure").addValueEventListener(pressureListener);

        createNotificationChannel();

        sendNotification();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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


    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private static final String ACTION_UPDATE_NOTIFICATION =
            "com.jgangl.notifyme.ACTION_UPDATE_NOTIFICATION";

    private NotificationManager mNotifyManager;

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Mascot Notification", NotificationManager
                    .IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public void sendNotification() {
        //Intent updateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);
        //PendingIntent updatePendingIntent = PendingIntent.getBroadcast
        //        (this, NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        //notifyBuilder.addAction(R.drawable.ic_update, "Update Notification", updatePendingIntent);

        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

    }

    private NotificationCompat.Builder getNotificationBuilder(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("Temperature Alert")
                .setContentText("Temperature High")
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return notifyBuilder;
    }

    //Convert Celsius to Fahrenheit
    //Returns Temperature in Fahrenheit
    private double celsiusToFahrenheit(double celsius){
        return (celsius * 1.8) + 32;
    }

    //Returns Temperature in Celsius
    private double dataToCelsius(int data){
        return data / 100.0;
    }

    //Returns Pressure in Pa (Pascals)
    private double dataToPascals(int data){
        return data / 256.0;
    }

    //Convert Pa to inHg
    //Returns Pressure in inHg
    private double pascalsToMercury(double pascals){
        return pascals / 3386.389;
    }

    //Returns %RH (Relative Humidity)
    private double dataToHumidity(int data){
        return data / 1024.0;
    }
}
