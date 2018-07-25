package shay.example.com.dart_client.helper_classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import shay.example.com.dart_client.R;

/**
 * Created by Shay de Barra on 09,February,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class Utilities {
    public static final String CONST_STATION_NAME = "station_name";
    public static final String CONST_STATION_KEY = "station_key";// firebase key for station i.e....  -L4HsoTwfL5tFP_aYAsb
    public static final String FIRST_POS_SELECTED = "last_pos";
    public static final String MY_LOCAL_STATION = "my_home_station";
    public static final String MY_LOCAL_POS = "my_local_station_position";
    public static final String CONST_USER_PHONE = "user_phone";
    public static final String TOTAL_FAVOURITE_JOURNEYS = "frequent_journeys";
    public static final String ACTIVITY_NAME = "activity";
    public static final String ACTIVITY_FAVOURITE = "activity_favourite";
    public static final String ACTIVITY_STATIONS = "activity_stations";
    public static final String CURSOR = "cursor";

    public static final int MAX_FAV_JOURNEYS = 5;
    public static final int MIN_FAV_JOURNEYS = 1;


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) { // we have connection .. now can we connect?
//https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out   Author:Daniel Mar 7 '14

            try {
                Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
                int returnVal = p1.waitFor();
                return (returnVal == 0);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }

    // this custom dialog will personalise the permissions request prior to calling them ie. you need permission to ...
    public static void showInfoDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.customTheme);
        builder.setTitle("TAP");
        builder.setMessage("This application requires an internet connection.\n");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setIcon(R.drawable.tap);
        builder.create().show();
    }


}

