package shay.example.com.dart_client.helper_classes;

/**
 * Created by Shay on 18/03/2017.
 */


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * Created by Shay de Barra on 13,February,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class GetAllCurrentTrains {

    private static final String IE_DATA_URL = "http://shay.x10.mx/train/by_dart/";

    private static JSONObject getTrainJSON(String[] strings) {
        try {

            URL url = new URL(IE_DATA_URL);

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);//StringBuffer json = new StringBuffer(1024);//
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            if (data.length() == 0) {
                // no train data available throw message
                String TAG = "DownloadTrainData";
                Log.e(TAG, "NO DATA: " + data.length());
                JSONObject empty = null;
                return empty;
            }
            return data;
        } catch (Exception e) {
            Log.e("XXXXXXXXXX", "getTrainJSON: "+e );
            return null;
        }
    }


    public interface AsyncResponse {


        void processFinish(String latitude, String longitude, String traincode, String status, String message, String direction);
    }

    public static class placeIdTask extends AsyncTask<String, Void, JSONObject> {

        public AsyncResponse delegate = null;//Call back interface

        public placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interface through constructor
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            JSONObject jsonTrainData = null;
            try {
                jsonTrainData = getTrainJSON(strings);
            } catch (Exception e) {
                Log.e("Error", "Cannot process JSON results", e);// FIX THIS - it will happen when there is no results from api i.e. 2AM
            }


            return jsonTrainData;
        }


        @Override
        protected void onPostExecute(JSONObject json) {


            try {
                int idx = 0;// iterate the inner objects of the array
                if (json == null) {// returns null after 24:00 i.e. no trains
                    Log.e("RETURN", "");
                    return;
                }
                int arrayLength = json.getJSONArray("trains").length();
                Log.e("arrayLength", "" + arrayLength);
                String latitude, longitude, traincode, status, message, direction;

                while (idx < arrayLength) {

                    JSONObject result = json.getJSONArray("trains").getJSONObject(idx);

                    latitude = result.getString("TrainLatitude");
                    longitude = result.getString("TrainLongitude");
                    traincode = result.getString("TrainCode");
                    status = result.getString("TrainStatus");

                    message = result.getString("PublicMessage");
                    direction = result.getString("Direction");
                    delegate.processFinish(latitude, longitude, traincode, status, message, direction);
                    idx++;
                }
                Log.e("ON POST", "");

            } catch (JSONException e) {
                Log.e("ERROR", "Cannot process JSON results", e);
            }


        }
    }


}
