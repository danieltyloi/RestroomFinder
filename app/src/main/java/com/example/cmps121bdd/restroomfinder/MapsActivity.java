package com.example.cmps121bdd.restroomfinder;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.InfoWindowAdapter,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final boolean TODO = false;
    //TAG for Logs
    String TAG = "MAPACTIVITY";
    Marker prevAddedMarker = null;
    private static GoogleMap mMap;
    private Location mLastKnownLocation;
    private boolean mPermissionDenied = false;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (UCSC) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(36.9881, 122.0582);
    private static final int DEFAULT_ZOOM = 12;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mLocationPermissionGranted;
    private HttpURLConnection conn = null;
    static String API_Key = "AIzaSyAzwUcfSl7n2LkvecKKrw1cLnNmITbV97Y";
    String inputLocation;

    //BOTTOM SHEET VIEWS
    LinearLayout markerDet, addLocation;
    BottomSheetBehavior markerDetBehavior, addLocationBehavior;
    EditText addLocTitle;
    TextView mrkTitle;
    TextView mrkDet;
    FloatingActionButton nav;
    Button addLoc;
    Double lat, lng, curlat, curlng;
    CheckBox unisex, handicap, vendingMachine;
    //BOTTOM SHEET VIEWS

    GPSTracker gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the Map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent i = getIntent();
        if (i == null) {
            inputLocation = "ucsc";
        } else {
            inputLocation = i.getStringExtra("location");
        }

        // Get permission from the User to access their location.
        getLocationPermission();

        // ASK USER TO ENABLE GPS
        enableGPS();
        // ASK USER TO ENABLE GPS

        markerDet = findViewById(R.id.marker_det);
        markerDetBehavior = BottomSheetBehavior.from(markerDet);
        markerDetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mrkTitle = findViewById(R.id.btm_title);
        mrkTitle.setOnClickListener(this);
        mrkDet = findViewById(R.id.btm_detail);
        mrkDet.setOnClickListener(this);
        nav = findViewById(R.id.navigation);


        addLocation = findViewById(R.id.add_location);
        addLocationBehavior = BottomSheetBehavior.from(addLocation);
        addLocationBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        addLocTitle = findViewById(R.id.newMrk_title);
        addLocTitle.setOnClickListener(this);
        addLoc = findViewById(R.id.add);
        addLoc.setOnClickListener(this);
        unisex = findViewById(R.id.unisexBtn);
        handicap = findViewById(R.id.handicapBtn);
        vendingMachine = findViewById(R.id.vendingmachinBtn);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), API_Key);

        //PlacesClient placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String name = place.getName().toString();
                LatLng location = place.getLatLng();
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                geoLocate(name);
                displayUserRestrooms();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM)); //move camera to input location
                mMap.addMarker(new MarkerOptions().position(location).title(name)); //add marker

            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onError: " + status);
            }
        });

        gps = new GPSTracker(this);


    }
    @Override
    public void onBackPressed(){
        if(markerDetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            markerDetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else if(addLocationBehavior.getState()== BottomSheetBehavior.STATE_EXPANDED){
            addLocationBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else if(addLocationBehavior.getState()== BottomSheetBehavior.STATE_COLLAPSED){
            addLocationBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }else if(markerDetBehavior.getState()== BottomSheetBehavior.STATE_COLLAPSED){
            markerDetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }else{
            finish();
        }
    }


    public void performSearch(Address a) {
        StringBuilder b = new StringBuilder();
        b.append("https://maps.googleapis.com/maps/api/place/textsearch/json?");
        b.append("location=" + a.getLatitude() + "," + a.getLongitude());
        b.append("&query=restrooms");
        b.append("&key=" + API_Key);
        //MainActivity.textView.setText(b.toString());

        BackTask imageDownloader = new BackTask();
        imageDownloader.execute(b.toString());
    }

    public static void placeMarkers(String s) {
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(s);
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            // Extract the lat and lng and add markers there
            for (int i = 0; i < predsJsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) predsJsonArray.get(i);
                JSONObject jsonObject2 = (JSONObject) jsonObject.get("geometry");
                JSONObject jsonObject3 = (JSONObject) jsonObject2.get("location");

                LatLng location = new LatLng((Double) jsonObject3.get("lat"), (Double) jsonObject3.get("lng"));
                mMap.addMarker(new MarkerOptions().position(location).title("Restroom"));

            }
        } catch (JSONException e) {
            //Log.e(LOG_TAG, "Error processing JSON results", e);
        }
    }

    public void geoLocate(String userLocation) {
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        Log.d(MapsActivity.class.getSimpleName(), "inputLocation = " + userLocation);
        List<Address> list = new ArrayList<>(); // list of results when user types
        if (userLocation != null) {
            try {
                list = geocoder.getFromLocationName(userLocation, 1); // only get first result

            } catch (IOException e) {
                Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
            }
            if (list.size() > 0) {
                Address address = list.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude()); // get lat and lng of input location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM)); //move camera to input location
                mMap.addMarker(new MarkerOptions().position(location).title(userLocation.toUpperCase())); //add marker
                performSearch(address);
            }
        }
    }

    //Current location button on map
    @Override
    public boolean onMyLocationButtonClick() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return TODO;
        }
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    locateUser(location);
                }
            }
        });
        checkGPS();
        return false;
    }

    public void locateUser(Location location) {
        StringBuilder b = new StringBuilder();
        b.append("https://maps.googleapis.com/maps/api/place/textsearch/json?");
        b.append("location=" + location.getLatitude() + "," + location.getLongitude());
        b.append("&query=restrooms");
        b.append("&key=" + API_Key);
        //MainActivity.textView.setText(b.toString());

        BackTask imageDownloader = new BackTask();
        imageDownloader.execute(b.toString());
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        checkGPS();
    }
    //Current location button on map
    //------------------------------------------------------MAP STUFF------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "in onMapReady");
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(false);
        settings.setMyLocationButtonEnabled(false);
        geoLocate(inputLocation);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMapLongClickListener(this);
        enableMyLocation();
        mMap.setInfoWindowAdapter(new markerView(this));
        displayUserRestrooms();

    }
    @Override
    public void onMapClick(LatLng latLng) {
        if(prevAddedMarker!=null){
            prevAddedMarker.remove();
            addLocationBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        //Toast.makeText(this, "Map Clicked", Toast.LENGTH_LONG).show();
        markerDetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    @Override
    public void onCameraMove() {
        /*if(markerDetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            markerDetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }*/
    }
    @Override
    public void onMapLongClick(LatLng point) {
        if(prevAddedMarker!=null){
            prevAddedMarker.remove();
        }
        prevAddedMarker = mMap.addMarker(new MarkerOptions().position(point));
        lat = point.latitude;
        lng = point.longitude;
        CameraUpdate location_up = CameraUpdateFactory.newLatLngZoom(point,16);
        mMap.animateCamera(location_up);
        markerDetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        addLocationBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        addLocTitle.setText("");
        Toast.makeText(this, "prevAddedMarker clicked", Toast.LENGTH_SHORT).show();
    }

    public void addLocationDetails(){
        addLocationBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        Toast.makeText(this, "Adding location details", Toast.LENGTH_SHORT).show();
        String inputLocation = addLocTitle.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Locations");
        if (inputLocation.equals("")) {
            Toast.makeText(this, "Please input a name", Toast.LENGTH_SHORT).show();
        } else {
            myRef.child(inputLocation).child("Latitude").setValue(lat);
            myRef.child(inputLocation).child("Longitude").setValue(lng);
            if(unisex.isChecked()){
                myRef.child(inputLocation).child("Unisex").setValue(true);
            }
            else{
                myRef.child(inputLocation).child("Unisex").setValue(false);
            }
            if(handicap.isChecked()){
                myRef.child(inputLocation).child("Handicap").setValue(true);
            }
            else{
                myRef.child(inputLocation).child("Handicap").setValue(false);
            }
            if(vendingMachine.isChecked()){
                myRef.child(inputLocation).child("Vending Machine").setValue(true);
            }
            else{
                myRef.child(inputLocation).child("Vending Machine").setValue(false);
            }
            Toast.makeText(this, "Location Added!", Toast.LENGTH_LONG).show();

            prevAddedMarker=null;
        }
    }
    //------------------------------------------------------MAP STUFF---------------------------------------------------------------------
    //------------------------------------------------------MARKER STUFF------------------------------------------------------------------
    private void displayUserRestrooms() {
        FirebaseDatabase.getInstance().getReference("Locations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String restroomName = snapshot.getKey();
                            Double lat = (Double) snapshot.child("Latitude").getValue(); // only works with decimals
                            Double lng = (Double) snapshot.child("Longitude").getValue(); // only works with decimals
                            LatLng location = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions().position(location).title(restroomName));

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(prevAddedMarker)){
            Toast.makeText(this, "prevAddedMarker clicked", Toast.LENGTH_SHORT).show();
            markerDetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            addLocationBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }else{
            String mark_title = marker.getTitle();
            //Toast.makeText(this, "rand marker clicked", Toast.LENGTH_SHORT).show();
            addLocationBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            markerDetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mrkTitle.setText(mark_title);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
        return false;
    }
    public void add(View view){
        addLocationDetails();
    }
    @Override
    public View getInfoWindow(Marker marker) {
        //View MarkerView = findViewById(R.id.markerView);
        //return MarkerView;
        return null;
    }
    public void navigate(View view) {
        Toast.makeText(this, "navigation button clciked", Toast.LENGTH_SHORT).show();
    }
    public boolean location(View view) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return TODO;
        }
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    locateUser(location);
                }
            }
        });
        checkGPS();
        gps = new GPSTracker(this);
        curlat= gps.getLatitude(); // returns latitude
        curlng= gps.getLongitude();
        LatLng curloc = new LatLng(curlat, curlng);
        if((curlat != 0.0 && curlng != 0.0)){
            CameraUpdate location_up = CameraUpdateFactory.newLatLngZoom( curloc,DEFAULT_ZOOM);
            mMap.animateCamera(location_up);
        }
        return false;
    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.newMrk_title:
                Toast.makeText(this, "newMrk_title clicked", Toast.LENGTH_SHORT).show();
                markerDetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                addLocationBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);*/
            case R.id.btm_title:
                Toast.makeText(this, "title clicked", Toast.LENGTH_SHORT).show();
                addLocationBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                markerDetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            case R.id.btm_detail:
                //Toast.makeText(this, "newMrk_title clicked", Toast.LENGTH_SHORT).show();
                break;

        }
    }



    //------------------------------------------------------MARKER STUFF-------------------------------------------------------------------
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
    //PERMISSIONS STUFF FOR LOCATION----------------------------------------------------------
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }
    //Check if GPS is enabled, if not, enables
    public void enableGPS() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MapsActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }
    public void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableGPS();
        }
    }


    //Check if GPS is enabled, if not, enables
    //PERMISSIONS STUFF FOR LOCATION----------------------------------------------------------



}
