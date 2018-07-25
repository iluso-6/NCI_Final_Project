package shay.example.com.dart_client.helper_classes;

/**
 * Created by Shay on 18/02/2018.
 */


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by Shay de Barra on 13,February,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class DownloadTrainData {
    // JSON_OBJECT={"trains":[{"Servertime":"2018-03-03T20:01:40.24","Traincode":"E829 ","Stationfullname":"Bray","Stationcode":"BRAY","Querytime":"20:01:40","Traindate":"03 Mar 2018","Origin":"Bray","Destination":"Malahide","Origintime":"20:10","Destinationtime":"21:20","Status":"No Information","Lastlocation":{},"Duein":"9","Late":"0","Exparrival":"00:00","Expdepart":"20:10","Scharrival":"00:00","Schdepart":"20:10","Direction":"Northbound","Traintype":"DART","Locationtype":"O"}]});
/*
JSONObject jb = new JSONObject("jsonstring");
JSONObject jb1 = jb.getJSONObject("response");
JSOnArray venues = jb1.getJSONArray("venues");
 */
    private static final String IE_DATA_URL = "http://shay.x10.mx/train/?station=%s";

    private static final String IE_DATA_URL_TESTING_ONE = "http://shay.x10.mx/train/dummy_one/";
    private static final String IE_DATA_URL_TESTING = "http://shay.x10.mx/train/dummy_data/";
    static JSONArray empty = new JSONArray();

    private static JSONObject getTrainJSON(String station) {
        try {

            URL url = new URL(String.format(IE_DATA_URL, station));
            Log.e(TAG, "getTrainJSON: " + url);
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
                // no train data available show message
                data.put("trains", empty);
                Log.e(TAG, "data.length() == 0: ");
            }

            JSONArray testForJSONArray = data.optJSONArray("trains");
            if (testForJSONArray == null) {
                /*  we have no array in this call .. just one JSONObject
                Create an array of one item and insert into the JSONObject return for post execute
                 */
                   Log.e(TAG, "testForJSONArray : null");
                   JSONArray rtn = new JSONArray();
                   rtn.put(data.getJSONObject("trains"));
                    data.put("trains", rtn);
                     return data;
            }



            return data;
        } catch (Exception e) {
            return null;
        }
    }


    public interface AsyncResponse {


        void processFinish(String traincode, String origin, String destination, String status, String direction, String scharrival, String exparrival, String duein, String late, String last_location);
    }

    public static class placeIdTask extends AsyncTask<String, Void, JSONObject> {
        public AsyncResponse delegate;//Call back interface
        MyCallbackListener listener;

        public placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interface through constructor
        }

        public void setCallbackListener(MyCallbackListener listener) {
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            try {

                getTrainJSON(params[0]);

                Log.e("jsonTrainData", "");


            } catch (Exception e) {

                Log.e("Error", "Cannot process JSON results", e);// FIX THIS - it will happen when there is no results from api i.e. 2AM
            }


            return getTrainJSON(params[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            try {
                int idx = 0;// iterate the inner objects of the array

                if (json == null) {
                    Log.e("getJSONArray", "");
                    return;
                }

                Log.e(TAG, "onPostExecute: ");
                int arrayLength = json.getJSONArray("trains").length();

                String traincode, origin, destination, train_type, direction, scharrival, exparrival, duein, late, last_location;

                while (idx < arrayLength) {

                    JSONObject result = json.getJSONArray("trains").getJSONObject(idx);

                    traincode = result.getString("Traincode");
                    origin = result.getString("Origin");
                    destination = result.getString("Destination");
                    train_type = result.getString("Traintype");
                    direction = result.getString("Direction");
                    scharrival = result.getString("Scharrival");
                    exparrival = result.getString("Exparrival");
                    duein = result.getString("Duein");
                    late = result.getString("Late");
                    last_location = result.getString("Lastlocation");

                    delegate.processFinish(traincode, origin, destination, train_type, direction, scharrival, exparrival, duein, late, last_location);
                    idx++;
                }

                if (listener != null) {
                    // we only set the interface listener in FirebaseCommon when needed
                    listener.myCallback(idx);
                }
                //    Log.e("ON POST", "FINISHED");
            } catch (JSONException e) {
                Log.e("ERROR", "Cannot process JSON results", e);
            }

            // asyncDownLoadCallback();
        }

        public interface MyCallbackListener {
            void myCallback(int obj);
        }

    }


}
