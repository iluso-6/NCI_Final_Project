package shay.example.com.dart_master.helpers;

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
 * Created by Shay de Barra on 08,February,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class FirebaseHelper {

    private static String[] number = {"Zero","One", "Two", "Three","Four"};

    public static String getEnglishNumber(long num) {
        int numero = (int) num;
        return number[numero];
    }

    private static String[][] zone = {{}, {"Greystones", "Bray", "Shankill"}, {"Killiney", "Dalkey", "Glenageary"},
            {"Sandycove", "Dun Laoghaire", "Salthill"}, {"Seapoint", "Blackrock", "Booterstown", "Sydney Parade"},
            {"Sandymount", "Lansdowne Road"}, {"Grand Canal Dock", "Dublin Pearse"}, {"Tara Street"}, {"Dublin Connolly"},
            {"Clontarf Road", "Killester"}, {"Harmonstown", "Raheny", "Kilbarrack"}, {"Howth Junction", "Bayside"}, {"Howth", "Sutton"},
            {"Clongriffin", "Portmarnock", "Malahide"}};


    private static int[] zone_color = {0, Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.GREEN};

    public static String[] getZone(String station) {
        for (int x = 0; x < zone.length; x++) {
            String subArray[] = zone[x];
            //         Log.e("Length of array " + x, " is " + subArray.length);
            for (int y = 0; y < subArray.length; y++) {
                String zone_station = subArray[y];
                if (zone_station.equals(station)) {
                    return zone[x];
                }
             //    Log.e("  Zone " + x, " is " + zone_station);

            }
        }
        return new String[0];
    }

    public static int getZoneNumber(String station) {
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

    public static void setStaffingDetailsFireBase(String key, final String name) {
  //      Log.e("", "setStaffingDetailsFireBase: " );
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Station_List");
   //     Log.e(TAG, "key: "+ key );
    //    Log.e(TAG, "name: "+ name);
      //  final String[] station_zone = getZone(name);
        final long station_zone_number = (long) getZoneNumber(name);
     //   Log.e(TAG, "station_zone_number: "+station_zone_number );
        DatabaseReference station = databaseReference.child(key).getRef();
        station.child("attending").child(master.getMasterID()).getRef().setValue(master.getUrl());// set the staffing details for selected station
        station.child("man_icon").setValue(true);// set the man icon here

         databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
              //      String station_name = (String) childSnap.child("name").getValue();
              //      Log.e(TAG, "childSnap: "+childSnap );
                    long num = (long) childSnap.child("zone_number").getValue();

                    if ((num == station_zone_number)) {
                        Log.e("setStaffingDetails", "zone true" );
                        childSnap.child("zone_active").getRef().setValue(true);//

                    }
              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public static void deleteStaffingDetailsFireBase(String key, final String name) {
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Station_List");

    //    final String[] station_zone = getZone(name);
        final long station_zone_number = (long) getZoneNumber(name);
        final List<String> station_zone_keys = new ArrayList<>();

        final DatabaseReference station = databaseReference.child(key).getRef();
        station.child("attending").child(master.getMasterID()).getRef().removeValue();// set the staffing details for selected station


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {


            public boolean manned;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){

                  //  String station_name = (String) childSnap.child("name").getValue();
                    long num = (long) childSnap.child("zone_number").getValue();
                    //   zone_active = false;
                    if ((num == station_zone_number)) {

                        String key = childSnap.getKey();
                        station_zone_keys.add(key);

                        childSnap.child("man_icon").getRef().setValue(false);// remove all the man icons
                        childSnap.child("zone_active").getRef().setValue(false);//
                        Log.e("Station"," name: "+childSnap.child("name").getValue());
                        if(childSnap.child("attending").exists()){
                            childSnap.child("man_icon").getRef().setValue(true);// set the man icon here
                            manned = true;
                            Log.e("EXISTS"," name:"+childSnap.child("name").getValue());

                        }
                    }

                }


                updateZoneActiveChanges(station_zone_keys, manned);
             //   Log.e("END of snap", "manned ");
            }


            //   updateZoneActiveChanges(station_zone_keys, flag);
            //   Log.e("station_zone_keys", "" + station_zone_keys);


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


    }


                // iterate on all the stations in the zone to set the active state true/false

    public static void updateZoneActiveChanges(List<String> station_zone_keys, Boolean flag) {
        Log.e("updateZoneActiveChanges","manned: "+flag);
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        for (int i = 0; i < station_zone_keys.size(); i++) {
            String key = station_zone_keys.get(i);
            DatabaseReference dataRef = firebaseDatabase.getReference("Station_List").child(key).getRef();

            dataRef.child("zone_active").setValue(flag);
        }


    }


    public static void populateFirebase() {
        FirebaseDatabase firebaseDatabase;
        firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference station_list = firebaseDatabase.getReference("Station_List");

        station_list.removeValue();
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

                    Double lat = Double.parseDouble(String.valueOf(childDataSnapshot.child("StationLatitude").getValue()));
                    station_list_child.child("latitude").setValue(lat);
                    Double lon = Double.parseDouble(String.valueOf(childDataSnapshot.child("StationLongitude").getValue()));
                    station_list_child.child("longitude").setValue(lon);
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