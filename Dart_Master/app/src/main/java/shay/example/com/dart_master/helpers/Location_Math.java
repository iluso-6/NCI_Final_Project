package shay.example.com.dart_master.helpers;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

/**
 * Created by Shay de Barra on 11,April,2018
 * Email:  x16115864@student.ncirl.ie
 */
public class Location_Math {


    public static int calculateDifferentialDistance(double latitude, double longitude, double station_latitude, double station_longitude) {

        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, station_latitude, station_longitude, results);
        int distance = (int) results[0];// cast to clean number
        //  Log.e("Results", distance + "");
        return distance;
    }

    // check if point lies within dart line polyline
    public static boolean getIsOnRailTrack(LatLng point, String encodedRoute) {

        int tolerance = 20;
        List<LatLng> route = PolyUtil.decode(encodedRoute);
        boolean result = PolyUtil.isLocationOnPath(point, route, true,tolerance);
        Log.e("getIsOnRailTrack", "" + result);
        return result;
    }

}
