package com.example.babywatchpro;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static Boolean receivedData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mainActivity = this;
        registerWithNotificationHubs();
        FirebaseService.createChannelAndHandleNotifications(getApplicationContext());
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    public void handleNewMeasurement(final String measurements) {
        runOnUiThread(() -> {
            TextView temperatureTv = findViewById(R.id.temperature_value);
            TextView humidityTv = findViewById(R.id.humidity_value);
            TextView pressureTv = findViewById(R.id.pressure_value);
            TextView altitudeTv = findViewById(R.id.altitude_value);
            TextView lastMeasurementTv = findViewById(R.id.last_measurement);

            if (measurements.startsWith("Temperature")) {
                String temperature = measurements.split(":")[1].substring(1, 6);
                String humidity = measurements.split(",")[1].split(":")[1].substring(1, 6);
                String pressure = measurements.split(",")[2].split(":")[1].substring(1, 6);
                if(pressure.endsWith(".")) pressure=pressure.substring(0,3);
                String altitude = measurements.split(",")[3].split(":")[1].substring(1, 4);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  dd MMM yyyy", Locale.ENGLISH);
                Date when = new Date(System.currentTimeMillis());
                String measurement = sdf.format(when);

                temperatureTv.setText(String.format("%s °C", temperature));
                humidityTv.setText(String.format("%s %%", humidity));
                pressureTv.setText(String.format("%s hPa", pressure));
                altitudeTv.setText(String.format("%s m", altitude));
                lastMeasurementTv.setText(String.format("Last measurement time: %s", measurement));
            }
        });
    }
}