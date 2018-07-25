package shay.example.com.dart_client.helper_classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import static shay.example.com.dart_client.helper_classes.Utilities.CURSOR;
import static shay.example.com.dart_client.helper_classes.Utilities.MAX_FAV_JOURNEYS;
import static shay.example.com.dart_client.helper_classes.Utilities.MIN_FAV_JOURNEYS;
import static shay.example.com.dart_client.helper_classes.Utilities.TOTAL_FAVOURITE_JOURNEYS;


/**
 * Created by Shay de Barra on 17,April,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class PreferenceHelper {
    private final static String SETTINGS = "settings";


    public static void setSharedPreferenceString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static void setSharedPreferenceInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    public static void setSharedPreferenceBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    public static String getSharedPreferenceString(Context context, String key, String defValue) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        return settings.getString(key, defValue);
    }


    public static int getSharedPreferenceInt(Context context, String key, int defValue) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        return settings.getInt(key, defValue);
    }

    public static boolean getSharedPreferenceBoolean(Context context, String key, boolean defValue) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        return settings.getBoolean(key, defValue);
    }

    // Shared Preference Array

    private static String convertToString(ArrayList<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list) {
            sb.append(delim);
            sb.append(s);
            ;
            delim = ",";
        }
        return sb.toString();
    }

    private static ArrayList<String> convertToArray(String string) {
        if (string == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>(Arrays.asList(string.split(",")));
        return list;
    }


    public static void setSharedPreferenceStringArray(Context context, String key, ArrayList<String> list) {
        String value = convertToString(list);
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static ArrayList<String> getSharedPreferenceStringArray(Context context, String key, String defValue) {

        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        return convertToArray(settings.getString(key, defValue));
    }


    public static void storeJourneySelection(Context context, String source_text, String destination_text) {
        int number_of_saved_journeys = getSharedPreferenceInt(context, TOTAL_FAVOURITE_JOURNEYS, MIN_FAV_JOURNEYS);
        //   Log.e("Stored", "number_of_saved_journeys: " + number_of_saved_journeys);

        // get the last cursor position ... then write over the beginning
        int cursor = getSharedPreferenceInt(context, CURSOR, MIN_FAV_JOURNEYS);
        cursor = number_of_saved_journeys < MAX_FAV_JOURNEYS ? number_of_saved_journeys : cursor;

        ArrayList<String> journeyDetails = new ArrayList<>();
        journeyDetails.add(source_text);
        journeyDetails.add(destination_text);
        String key = String.valueOf(cursor);
        Log.e("CURSOR", "cursor: " + cursor);
        setSharedPreferenceStringArray(context, key, journeyDetails);
        //   Log.e("BEFORE", "cursor: " + cursor);

        // we store a maximum of 5 journeys
        int total = number_of_saved_journeys < MAX_FAV_JOURNEYS ? number_of_saved_journeys + 1 : MAX_FAV_JOURNEYS;
        Log.e("Total", "total: " + number_of_saved_journeys);
        cursor = cursor < MAX_FAV_JOURNEYS ? cursor + 1 : MIN_FAV_JOURNEYS;

        setSharedPreferenceInt(context, TOTAL_FAVOURITE_JOURNEYS, total);
        setSharedPreferenceInt(context, CURSOR, cursor);
    }

    public static void clearJourneySelection(Context context) {
        int cursor = getSharedPreferenceInt(context, CURSOR, MIN_FAV_JOURNEYS);
      //  Log.e("clearJourneySelection", "BEFORE: " + cursor);
        /// if its at 1 then the last entry was the max else we just deduct 1
        cursor--;
        if (cursor < MIN_FAV_JOURNEYS) {
            cursor = MIN_FAV_JOURNEYS;
        }
   //     Log.e("clearJourneySelection", "AFTER: " + cursor);
        setSharedPreferenceInt(context, CURSOR, cursor);

    }
}

