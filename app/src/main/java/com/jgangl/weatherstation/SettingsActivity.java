package com.jgangl.weatherstation;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "com.example.android.hellosharedprefs";

    // Key for current value
    private final String DISPLAY_FAHRENHEIT_KEY = "displayFahrenheit";
    private final String DISPLAY_PASCALS_KEY = "displayPascals";

    private final String TEMP_HIGH_KEY = "tempHigh";
    private final String TEMP_LOW_KEY = "tempLow";

    private boolean displayFahrenheit;
    private boolean displayPascals;

    private int tempLow;
    private int tempHigh;

    EditText tempHighEditText;
    EditText tempLowEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPreferences = getSharedPreferences(
                sharedPrefFile, MODE_PRIVATE);

        displayFahrenheit = mPreferences.getBoolean(DISPLAY_FAHRENHEIT_KEY, displayFahrenheit);
        displayPascals = mPreferences.getBoolean(DISPLAY_PASCALS_KEY, displayPascals);

        tempHigh = mPreferences.getInt(TEMP_HIGH_KEY, tempHigh);
        tempLow = mPreferences.getInt(TEMP_LOW_KEY, tempLow);

        Switch tempSettingSwitch = findViewById(R.id.tempSettingSwitch);
        Switch pressSettingSwitch = findViewById(R.id.pressSettingSwitch);
        tempHighEditText = findViewById(R.id.tempHighSettingEditText);
        tempLowEditText = findViewById(R.id.tempLowSettingEditText);

        tempSettingSwitch.setChecked(displayFahrenheit);
        pressSettingSwitch.setChecked(displayPascals);
        tempHighEditText.setText(Integer.toString(tempHigh));
        tempLowEditText.setText(Integer.toString(tempLow));

        tempSettingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                //Check if switch is off
                if(isChecked){
                    //Switch to celsius
                    displayFahrenheit = true;
                }
                else{
                    displayFahrenheit = false;
                }
            }
        });

        pressSettingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                //Check if switch is off
                if(isChecked){
                    //Switch to celsius
                    displayPascals = true;
                }
                else{
                    displayPascals = false;
                }
            }
        });

    }

    /**
     * Saves the instance state if the activity is restarted (for example,
     * on device rotation.) Here you save the values for the count and the
     * background color.
     *
     * @param outState The state data.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String tempHighText = tempHighEditText.getText().toString();
        tempHigh = Integer.parseInt(tempHighText);

        String tempLowText = tempLowEditText.getText().toString();
        tempLow = Integer.parseInt(tempLowText);

        outState.putBoolean(DISPLAY_FAHRENHEIT_KEY, displayFahrenheit);
        outState.putBoolean(DISPLAY_PASCALS_KEY, displayPascals);
        outState.putInt(TEMP_HIGH_KEY, tempHigh);
        outState.putInt(TEMP_LOW_KEY, tempLow);
    }

    @Override
    protected void onPause(){
        super.onPause();

        String tempHighText = tempHighEditText.getText().toString();
        tempHigh = Integer.parseInt(tempHighText);

        String tempLowText = tempLowEditText.getText().toString();
        tempLow = Integer.parseInt(tempLowText);

        SharedPreferences.Editor preferencesEditor = mPreferences.edit();

        preferencesEditor.putBoolean(DISPLAY_FAHRENHEIT_KEY, displayFahrenheit);
        preferencesEditor.putBoolean(DISPLAY_PASCALS_KEY, displayPascals);
        preferencesEditor.putInt(TEMP_HIGH_KEY, tempHigh);
        preferencesEditor.putInt(TEMP_LOW_KEY, tempLow);

        preferencesEditor.apply();
    }

}
