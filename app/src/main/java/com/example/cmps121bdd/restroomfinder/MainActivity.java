package com.example.cmps121bdd.restroomfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.example.cmps121bdd.restroomfinder.R.id.lookupLocation;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputLocation = findViewById(R.id.inputLocation);
        Button lookupLocation = findViewById(R.id.lookupLocation);

        String inputLocation_text = inputLocation.getText().toString();
        lookupLocation.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.lookupLocation:

                EditText inputLocation = findViewById(R.id.inputLocation);              //Reference to user's input text
                String inputLocation_text = inputLocation.getText().toString();         //Grabbing said text

                Intent intent1 = new Intent (this, MapsActivity.class);    //Creating intent to pass to MapActivity
                intent1.putExtra("location",inputLocation_text);                    //Adding user input to intent
                startActivity(intent1);                                                    //starting MapActivity with input
        }
    }
}
