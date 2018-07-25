package shay.example.com.dart_client.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import java.util.List;

import shay.example.com.dart_client.R;


/**
 * Created by Shay de Barra on 01,April,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class LocationService extends Service {


    LocationRequest mLocationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    private String TAG = "LocationService";
    private DatabaseReference locRef;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            // get the last updated location
            updateFireBase(locationResult);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        String stringReference = intent.getStringExtra("DB_REF");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(stringReference);
        locRef = ref.getParent().getRef();// move up one level to add new attribute and not overwrite ..
        // request sent to client


        if (checkPermissions(context)) {
            startLocationUpdates(context);
            Log.e("startLocationUpdates", "onStartCommand");
            setApplicationResponse(true);
        }else{
            setApplicationResponse(false);
        }
        return START_REDELIVER_INTENT;
    }

    private void setApplicationResponse(boolean response) {
        locRef.child("locationResponse").setValue(response);
    }

    // Trigger new location updates at interval
    @SuppressLint({"MissingPermission", "RestrictedApi"})
    protected void startLocationUpdates(Context context) {
        //    Toast.makeText(context,"startLocationUpdates",Toast.LENGTH_LONG).show();
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        long UPDATE_INTERVAL = 30000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 10000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

    public boolean getIsOnDartLine(double latitude, double longitude) {
        double tolerance = 30.0;// double in meters
       // lat 53.3786  // lon -6.19131
        LatLng point = new LatLng(53.3786, -6.19131);
    //    LatLng point = new LatLng(latitude, longitude);
        String greystones_to_howth = getString(R.string.greystones_to_howth);
        String greystones_to_malahide = getString(R.string.greystones_to_malahide);

        List<LatLng> route_to_howth = PolyUtil.decode(greystones_to_howth);
        List<LatLng> route_to_malahide = PolyUtil.decode(greystones_to_malahide);

        boolean result_from_howth = PolyUtil.isLocationOnPath(point, route_to_howth, true,tolerance);
        boolean result_from_malahide = PolyUtil.isLocationOnPath(point, route_to_malahide, true,tolerance);

        return result_from_howth || result_from_malahide;
    }

    private void updateFireBase(LocationResult locationResult) {

        Location result = locationResult.getLastLocation();
        locRef.child("location").child("latitude").setValue(locationResult.getLastLocation().getLatitude());
        locRef.child("location").child("longitude").setValue(locationResult.getLastLocation().getLongitude());
        Log.e("onLocationResult", "updateFireBase " + result);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        // stop updating firebase by removing the updates to the callback ...
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        // stop the service itself
        stopSelf();

    }

    public boolean checkPermissions(Context context) {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


}