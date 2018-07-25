package shay.example.com.dart_master.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Shay de Barra on 17,April,2018
 * Email:  x16115864@student.ncirl.ie
 */


public class PreferenceHelper {
    private final static String SETTINGS = "settings";

    // https://stackoverflow.com/questions/7057845/save-arraylist-to-sharedpreferences  Contributor: SKT

    private static String convertToString(ArrayList<String> list) {
        if(list==null){return null;}
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list)
        {
            sb.append(delim);
            sb.append(s);;
            delim = ",";
        }
        return sb.toString();
    }

    private static ArrayList<String> convertToArray(String string) {
        if(string==null){return null;}
        ArrayList<String> list = new ArrayList<>(Arrays.asList(string.split(",")));
        return list;
    }

   // END  Contributor: SKT

    public static void setSharedPreferenceString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
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
        ArrayList<String> list = convertToArray (settings.getString(key, defValue));
        return list;
    }

    static void setSharedPreferenceInt(Context context, String key, int value) {
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


    static int getSharedPreferenceInt(Context context, String key, int defValue) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        return settings.getInt(key, defValue);
    }

    public static boolean getSharedPreferenceBoolean(Context context, String key, boolean defValue) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        return settings.getBoolean(key, defValue);
    }
}

