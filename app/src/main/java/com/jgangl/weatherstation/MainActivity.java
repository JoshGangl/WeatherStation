package com.jgangl.weatherstation;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private TextView tempTextView;
    private TextView humidTextView;
    private TextView pressTextView;

    private double currentTemp;//Celsius
    private double currentHum;
    private double currentPress;

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
                    currentTemp = dataToCelsius(Integer.parseInt(dataSnapshot.getValue().toString()));

                    tempTextView.setText(String.valueOf(currentTemp));
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
                    humidTextView.setText(String.valueOf(currentHum));
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
                    pressTextView.setText(String.valueOf(pascalsToMercury(currentPress)));
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
