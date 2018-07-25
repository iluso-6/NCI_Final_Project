package shay.example.com.dart_master.helpers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static shay.example.com.dart_master.SignInActivity.master;

/**
 * Created by Shay on 08/02/2018.
 */

public class FirebaseHelper_org {
static Context ctx;
    static String[][] zone = {{}, {"Greystones", "Bray", "Shankill"}, {"Killiney", "Dalkey", "Glenageary"},
            {"Sandycove", "Dun Laoghaire", "Salthill"}, {"Seapoint", "Blackrock", "Booterstown", "Sydney Parade"},
            {"Sandymount", "Lansdowne Road"}, {"Grand Canal Dock", "Dublin Pearse"}, {"Tara Street"}, {"Dublin Connolly"},
            {"Clontarf Road", "Killester"}, {"Harmonstown", "Raheny", "Kilbarrack"}, {"Howth Junction", "Bayside"}, {"Howth", "Sutton"},
            {"Clongriffin", "Portmarnock", "Malahide"}};
    static int[] zone_color = {0,Color.RED,Color.YELLOW,Color.GREEN,Color.BLUE,Color.MAGENTA,Color.RED,Color.YELLOW,Color.GREEN,Color.BLUE,Color.MAGENTA,Color.RED,Color.YELLOW,Color.GREEN};

    private static String[] getZone(String station) {
        for (int x = 0; x < zone.length; x++) {
            String subArray[] = zone[x];
            //         Log.e("Length of array " + x, " is " + subArray.length);
            for (int y = 0; y < subArray.length; y++) {
                String zone_station = subArray[y];
                if (zone_station.equals(station)) {
                    return zone[x];
                }
                // Log.e("  Zone " + x, " is " + zone_station);

            }
        }
        return new String[0];
    }

    private static int getZoneNumber(String station) {
        for (int x = 0; x < zone.length; x++) {
            String subArray[] = zone[x];
            //         Log.e("Length of array " + x, " is " + subArray.length);
            for (int y = 0; y < subArray.length; y++) {
                String zone_station = subArray[y];
                if (zone_station.equals(station)) {
                    return x;
                }
                // Log.e("  Zone " + x, " is " + zone_station);

            }
        }

        return 0;
    }
    public static void deleteStaffingDetailsFireBase(final String name) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Station_List");

        DatabaseReference current  = databaseReference.child(master.getMasterID());
        current.child("attending").removeValue();
        current.child("man_icon").setValue(false);

        final String[] station_zone = getZone(name);
        final long station_zone_number = (long) getZoneNumber(name);
        final List<String> station_zone_keys = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {


                    String name = (String) childDataSnapshot.child("name").getValue();
                    Log.e("name", "onDataChange: "+ name );




                    // updateZoneActiveChanges(station_zone_keys, zone_active);

                }
            }


            //   updateZoneActiveChanges(station_zone_keys, flag);
            //   Log.e("station_zone_keys", "" + station_zone_keys);


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


    }
    private static int getZoneColor(String station) {
        for (int x = 0; x < zone.length; x++) {
            String subArray[] = zone[x];
            //         Log.e("Length of array " + x, " is " + subArray.length);
            for (int y = 0; y < subArray.length; y++) {
                String zone_station = subArray[y];
                if (zone_station.equals(station)) {
                    return zone_color[x];
                }
                // Log.e("  Zone " + x, " is " + zone_station);

            }
        }


        return 0;
    }

    public static void setStaffingDetailsFireBase(final String name) {
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Station_List");

        final String[] station_zone = getZone(name);
        final int station_zone_number = getZoneNumber(name);
        //   Log.e("iterateFireBase", "" + station_zone[0]);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String station_name = (String) childDataSnapshot.child("name").getValue();
                    //  push the stations in the same zone to firebase with an "manned" status
                    for (int x = 0; x < station_zone.length; x++) {
                        if (station_name.equalsIgnoreCase(station_zone[x])) {
                            if (station_name.equalsIgnoreCase(name)) {//  set the url where the staff member[s] are stationed
                                Log.e("", "SELECTED STATION " + name);
                                childDataSnapshot.child("attending").child(master.getMasterID()).getRef().setValue(master.getUrl());// set the staffing details for selected station
                                childDataSnapshot.getRef().child("man_icon").setValue(true);// set the man icon here
                            }
                            //  Log.e("", "ZONED STATION " + station_name); // set the zone to true
                            childDataSnapshot.child("zone_active").getRef().setValue(true);// set the zone for selected station to be active
                        //    childDataSnapshot.child("zone_number").getRef().setValue(getZoneNumber(name));// set the zone number
                       //     childDataSnapshot.child("zone_color").getRef().setValue(getZoneColor(name));// set the zone color

                        }
                    }

                    //   Log.e( "onDataChange: ",""+childDataSnapshot.child("name").getValue() );

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    // iterate on all the stations in the zone to set the active state true/false
    private static void updateZoneChanges(List<String> station_zone_keys, Boolean flag) {
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        for (int i = 0; i < station_zone_keys.size(); i++) {
            String key = station_zone_keys.get(i);
            DatabaseReference dataRef = firebaseDatabase.getReference("Station_List").child(key);

            dataRef.child("zone_active").setValue(flag);
        }


    }


    public static void populateFirebase() {
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference station_list = firebaseDatabase.getReference("Station_List");

        DatabaseReference stations = firebaseDatabase.getReference("Stations");

        stations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //  Log.e(TAG, "onDataChange: "+ dataSnapshot.getValue() );


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    DatabaseReference station_list_child = station_list.push();
                    String name = (String) childDataSnapshot.child("StationDesc").getValue();
                    station_list_child.child("name").setValue(childDataSnapshot.child("StationDesc").getValue());
                    station_list_child.child("code").setValue(childDataSnapshot.child("StationCode").getValue());
                    station_list_child.child("id").setValue(childDataSnapshot.child("StationId").getValue());
                    station_list_child.child("latitude").setValue(childDataSnapshot.child("StationLatitude").getValue());
                    station_list_child.child("longitude").setValue(childDataSnapshot.child("StationLongitude").getValue());
                    station_list_child.child("zone_number").getRef().setValue(getZoneNumber(name));// set the zone number
                    station_list_child.child("zone_color").getRef().setValue(getZoneColor(name));// set the zone color
                    //  Log.e(TAG, "" + childDataSnapshot.child("StationDesc").getValue());

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
/*
* StationCode:
"MHIDE"
StationDesc:
"Malahide"
StationId:
"112"
StationLatitude:
"53.4509"
StationLongitude:
"-6.15649"
1
     private DatabaseReference stations;
 used to initially populate firebase ... keep here for further reference

        stations = database.getReference("Stations");

        stations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //  Log.e(TAG, "onDataChange: "+ dataSnapshot.getValue() );


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {


                    DatabaseReference station_list_child = station_list.push();
                    station_list_child.child("name").setValue(childDataSnapshot.child("StationDesc").getValue());
                    station_list_child.child("code").setValue(childDataSnapshot.child("StationCode").getValue());
                    station_list_child.child("id").setValue(childDataSnapshot.child("StationId").getValue());
                    station_list_child.child("latitude").setValue(childDataSnapshot.child("StationLatitude").getValue());
                    station_list_child.child("longitude").setValue(childDataSnapshot.child("StationLongitude").getValue());
                    Log.e(TAG, "" + childDataSnapshot.child("StationDesc").getValue());

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
*/