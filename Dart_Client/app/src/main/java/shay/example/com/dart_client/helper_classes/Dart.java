package shay.example.com.dart_client.helper_classes;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;
/**
 * Created by Shay de Barra on 13,February,2018
 * Email:  x16115864@student.ncirl.ie
 */


public class Dart {// howth junction  = pos [] 25

    private static String[][] zones = {{},{"Greystones", "Bray", "Shankill"}, {"Killiney", "Dalkey", "Glenageary"},{
            "Sandycove", "Dun Laoghaire", "Salthill"}, {"Seapoint", "Blackrock", "Booterstown", "Sydney Parade"},
            {"Sandymount", "Lansdowne Road"}, {"Grand Canal Dock", "Dublin Pearse"}, {"Tara Street"},{ "Dublin Connolly"},
            {"Clontarf Road", "Killester"}, {"Harmonstown", "Raheny", "Kilbarrack"},{ "Howth Junction","Bayside"},{"Sutton", "Howth"},{"Clongriffin", "Portmarnock", "Malahide"}};

    public static String[] main_line = {"Greystones", "Bray", "Shankill", "Killiney", "Dalkey", "Glenageary",
            "Sandycove", "Dun Laoghaire", "Salthill", "Seapoint", "Blackrock", "Booterstown", "Sydney Parade",
            "Sandymount", "Lansdowne Road", "Grand Canal Dock", "Dublin Pearse", "Tara Street", "Dublin Connolly",
            "Clontarf Road", "Killester", "Harmonstown", "Raheny", "Kilbarrack", "Howth Junction"};

    private static String[] howth_line = {"Bayside", "Sutton", "Howth"};

    private static String[] malahide_line = {"Clongriffin", "Portmarnock", "Malahide"};

    private static String[] full_line_to_malahide = {"Greystones", "Bray", "Shankill", "Killiney", "Dalkey", "Glenageary",
            "Sandycove", "Dun Laoghaire", "Salthill", "Seapoint", "Blackrock", "Booterstown", "Sydney Parade",
            "Sandymount", "Lansdowne Road", "Grand Canal Dock", "Dublin Pearse", "Tara Street", "Dublin Connolly",
            "Clontarf Road", "Killester", "Harmonstown", "Raheny", "Kilbarrack", "Howth Junction", "Clongriffin", "Portmarnock", "Malahide"};

    public static String[] full_line_to_howth = {"Greystones", "Bray", "Shankill", "Killiney", "Dalkey", "Glenageary",
            "Sandycove", "Dun Laoghaire", "Salthill", "Seapoint", "Blackrock", "Booterstown", "Sydney Parade",
            "Sandymount", "Lansdowne Road", "Grand Canal Dock", "Dublin Pearse", "Tara Street", "Dublin Connolly",
            "Clontarf Road", "Killester", "Harmonstown", "Raheny", "Kilbarrack", "Howth Junction", "Bayside", "Sutton", "Howth"};

    public static String[] full_line_tour = {"Greystones", "Bray", "Shankill", "Killiney", "Dalkey", "Glenageary",
            "Sandycove", "Dun Laoghaire", "Salthill", "Seapoint", "Blackrock", "Booterstown", "Sydney Parade",
            "Sandymount", "Lansdowne Road", "Grand Canal Dock", "Dublin Pearse", "Tara Street", "Dublin Connolly",
            "Clontarf Road", "Killester", "Harmonstown", "Raheny", "Kilbarrack", "Howth Junction", "Bayside", "Sutton", "Howth","Howth Junction", "Clongriffin", "Portmarnock", "Malahide"};

    private static String[] mainTrainStations = {"Greystones", "Bray","Dun Laoghaire","Dublin Connolly","Howth", "Malahide"};
    // Convert String Array to List
    public static List<String> main_line_list = Arrays.asList(main_line);
    private static List<String> howth_line_list = Arrays.asList(howth_line);
    private static List<String> malahide_line_list = Arrays.asList(malahide_line);

    private static List<String> full_line_list_malahide = Arrays.asList(full_line_to_malahide);
    public static List<String> full_line_list_tour = Arrays.asList(full_line_tour);

    public static List<String> mainStationNames = Arrays.asList(mainTrainStations);

    // allow for either fork (ONLY 1) to belong to the main line
    public static boolean getValidJourney(String source, String destination) {
        // Convert String Array to List

        if ((howth_line_list.contains(source) || howth_line_list.contains(destination)) && (malahide_line_list.contains(source) || malahide_line_list.contains(destination))) {
            return false;
        }
        return true;
    }


    public static int getMyLocalZonedStations(String local_station) {

        for(int i=0; i<zones.length; i++) {
            for(int j=0; j<zones[i].length; j++) {
                String temp = zones[i][j];
                if(temp.equalsIgnoreCase(local_station)) {
                    Log.e(TAG, "ZonedStations: "+i );
                    return i;
                }
            }
        }
        return 0;
    }

    /* returns 3 stations Strings[] with local station in the middle
    public static String[] getMyLocalStations(String local_station) {
        String[] rtn;

        if (full_line_list_malahide.contains(local_station)) {
            int idx = full_line_list_malahide.indexOf(local_station);
            if (idx == full_line_to_malahide.length-1) {
                idx = (full_line_to_malahide.length-1) - 1;
            } else if (idx == 0) {
                idx = 1;
            }
            Log.e(TAG, "getMyLocalStations: " + idx);
            rtn = new String[]{full_line_to_malahide[idx - 1], full_line_to_malahide[idx], full_line_to_malahide[idx + 1]};
        } else {
            int idx = full_line_list_howth.indexOf(local_station);
            if (idx == full_line_to_howth.length-1) {
                idx = (full_line_to_howth.length-1) - 1;
            } else if (idx == 0) {
                idx = 1;
            }
            rtn = new String[]{full_line_to_howth[idx - 1], full_line_to_howth[idx], full_line_to_howth[idx + 1]};
        }

        return rtn;
    }*/

    public static boolean getIsOnRailTrack(String encodedRoute) {

        List<LatLng> route = PolyUtil.decode(encodedRoute);
        LatLng point = new LatLng(53.2661, -6.14245);
        boolean result = PolyUtil.isLocationOnPath(point, route, true);
        Log.e("getIsOnRailTrack", "" + result);
        return result;
    }



    // return the position in the list
    public static int getPos(String station) {


        if (main_line_list.contains(station)) {
            return main_line_list.indexOf(station);

        } else if (howth_line_list.contains(station)) {
            return (26 + howth_line_list.indexOf(station));// add the index on to the main line

        } else if (malahide_line_list.contains(station)) {
            return (26 + malahide_line_list.indexOf(station));
        }

        return 0;
    }

    // find out whether North or South bound or if it a possible journey
    public static String getDirection(String source, String destination) {

        boolean validJourney = getValidJourney(source, destination);
        if (!validJourney) {
            return "Invalid Journey Selected";
        }

        int source_int = getPos(source);
        int destination_int = getPos(destination);
        int number = destination_int - source_int;

        if (number < 0) {
            // negative it's a Southbound
            return "Southbound";
        } else {
            // it's a positive Northbound
            return "Northbound";
        }

    }

    private int calculateDifferentialDistance(LatLng start, LatLng end) {

        float[] results = new float[1];
        double startLatitude = start.latitude;// initial start center of map
        double startLongitude = start.longitude;
        double endLatitude = end.latitude;// new center of map after move
        double endLongitude = end.longitude;
        Location.distanceBetween(startLatitude, startLongitude,
                endLatitude, endLongitude, results);
        int distance = (int) results[0];// cast to clean number
        //  Log.e("Results", distance + "");
        return distance;
    }
}
