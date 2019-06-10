package com.jgangl.weatherstation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
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
    private TextView tempSymbolTextView;
    private TextView pressSymbolTextView;

    private double currentTemp;//Celsius
    private double currentHum;
    private double currentPress;

    private int highTempThreshold;
    private int lowTempThreshold;

    private boolean displayFahrenheit;
    private boolean displayPascals;

    private String tempExtreme;

    DecimalFormat precision = new DecimalFormat("0.0");

    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "com.example.android.hellosharedprefs";

    // Key for current value
    private final String DISPLAY_FAHRENHEIT_KEY = "displayFahrenheit";
    private final String DISPLAY_PASCALS_KEY = "displayPascals";

    private final String TEMP_HIGH_KEY = "tempHigh";
    private final String TEMP_LOW_KEY = "tempLow";

    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    private NotificationManager mNotifyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);

        displayFahrenheit = mPreferences.getBoolean(DISPLAY_FAHRENHEIT_KEY, displayFahrenheit);
        displayPascals = mPreferences.getBoolean(DISPLAY_PASCALS_KEY, displayPascals);
        highTempThreshold = mPreferences.getInt(TEMP_HIGH_KEY, highTempThreshold);
        lowTempThreshold = mPreferences.getInt(TEMP_LOW_KEY, lowTempThreshold);

        tempTextView = findViewById(R.id.tempTextView);
        humidTextView = findViewById(R.id.humidTextView);
        pressTextView = findViewById(R.id.pressTextView);
        tempSymbolTextView = findViewById(R.id.tempTextViewSymbol);
        pressSymbolTextView = findViewById(R.id.pressTextViewSymbol);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ValueEventListener temperatureListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.getValue() != null){
                    //Temperature comes as a 4 digit # ("4650" = 46.50 degrees Celsius)
                    currentTemp = celsiusToFahrenheit(dataToCelsius(Integer.parseInt(dataSnapshot.getValue().toString())));

                    if(displayFahrenheit){//Fahrenheit
                        tempTextView.setText(precision.format(currentTemp));
                        tempSymbolTextView.setText(getString(R.string.tempFahrenheitSymbol));
                    }
                    else{//Celsius
                        tempTextView.setText(precision.format(fahrenheitToCelsius(currentTemp)));
                        tempSymbolTextView.setText(getString(R.string.tempCelsiusSymbol));
                    }

                    if(currentTemp >= highTempThreshold){
                        tempExtreme = "high";
                        sendNotification();
                    }
                    else if(currentTemp <= lowTempThreshold){
                        tempExtreme = "low";
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

                    if(displayPascals){//Pascals
                        pressTextView.setText(precision.format(currentPress));
                        pressTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                        pressSymbolTextView.setText(getString(R.string.pressPascalsSymbol));
                    }
                    else{//inches of mercury
                        pressTextView.setText(precision.format(pascalsToMercury(currentPress)));
                        pressTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
                        pressSymbolTextView.setText(getString(R.string.pressMercurySymbol));
                    }
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
            Intent settingsIntent = new Intent(this,
                    SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "WeatherStation Notification", NotificationManager
                    .IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Weather station");

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public void sendNotification() {
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("Temperature Alert")
                .setContentText("Temperature " + tempExtreme)
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.ic_whatshot_24dp)
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

    //Convert Fahrenheit to Celsius
    private double fahrenheitToCelsius(double fahrenheit){
        return (fahrenheit - 32) * 5/9;
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
