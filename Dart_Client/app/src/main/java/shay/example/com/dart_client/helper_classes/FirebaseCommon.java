package shay.example.com.dart_client.helper_classes;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import shay.example.com.dart_client.BeginJourney;
import shay.example.com.dart_client.models.JourneyObj;
import shay.example.com.dart_client.models.Staff;

import static shay.example.com.dart_client.BeginJourney.progressBar;

/**
 * Created by Shay de Barra on 15,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class FirebaseCommon {

    private static final String TAG = "FirebaseCommon";

    private static FirebaseDatabase database;

    private static DatabaseReference origin_ref;
    private static DatabaseReference dest_ref;

    //   private static ArrayList<ValueEventListener> viewedListeners;
    private static Map<ValueEventListener, DatabaseReference> viewedListeners;
    private static Map<ValueEventListener, DatabaseReference> locationListeners;

    public static DatabaseReference getStationList() {
        database = FirebaseDatabase.getInstance();

        return database.getReference("Station_List");

    }


    public static DatabaseReference getNotificationsNode() {
        database = FirebaseDatabase.getInstance();


        return database.getReference("Notifications");
    }



    private static DatabaseReference sendNotificationData(JourneyObj journeyObj, int zone, final String station) {

        DatabaseReference notifications = getNotificationsNode();
        DatabaseReference userRef = notifications.child("zone_" + zone).child(station).push().getRef();//child(user.getUserID());
        final DatabaseReference viewsRef = notifications.child("Views").child(userRef.getKey());
        //******* reference to the firebase location of viewed  *****////

        // set the initial viewed value to false and onComplete set a listener to listen for master changes
        viewsRef.child("viewed").setValue(false, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.e(TAG, "onComplete: " + databaseReference);

                ValueEventListener viewsListenerRef = addListenerForViewed(databaseReference, station);
                Log.e(TAG, "addListenerForViewed: " + databaseReference + " station: " + station);
                // store the key,value pair Listener, Database ref for later Listener removal
                viewedListeners.put(viewsListenerRef, databaseReference);

            }
        });


        String viewedRef = String.valueOf(viewsRef);
        journeyObj.setFirebaseRef(viewedRef);

        //initially the staff member will not have requested location
        viewsRef.child("locationRequested").setValue(false, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                ValueEventListener locationListenerRef = addListenerForLocationRequest(databaseReference, station);
                Log.e(TAG, "addListenerForLocationRequest: " + databaseReference + " station: " + station);
                locationListeners.put(locationListenerRef, databaseReference);
            }
        });

        ///// ****************** //////////////

        userRef.child("object").setValue(journeyObj);

        progressBar.setVisibility(View.INVISIBLE);

        return userRef;// return the reference for later ..


    }

    private static ValueEventListener addListenerForLocationRequest(final DatabaseReference locationReference, String station) {
        return locationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("LocationRequest", "onDataChange: ");
                if (dataSnapshot.getValue() == null) {
                    // precautionary measure
                } else {
                    boolean value = (boolean) dataSnapshot.getValue();

                    if (value) {

                        Log.e("LocationRequest", "onDataChange: " + value);


                        DatabaseReference ref = locationReference.getParent();
                        setStaffDetails(ref);

                    }

                }
            }

            private void setStaffDetails(DatabaseReference ref) {
                final Staff staffmember = new Staff();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        String url = dataSnapshot.child("requestByID").child("url").getValue(String.class);
                        String name = dataSnapshot.child("requestByID").child("name").getValue(String.class);
                        Log.e(TAG, "+requestByID " + url);
                        staffmember.setUrl(url);
                        staffmember.setName(name);
                        BeginJourney.instance.startMyLocationService(locationReference,staffmember);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private static ValueEventListener addListenerForViewed(final DatabaseReference viewed, final String viewedName) {
        //    Log.e(TAG, "addListenerForViewed INIT " + viewed);
        return viewed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    // precautionary measure
                } else {
                    boolean value = (boolean) dataSnapshot.getValue();
                    if (value) {
                        BeginJourney.instance.setCallbackImageViewed(viewedName);
                    }
                    Log.e(TAG, "addListenerForViewed onDataChange :  " + value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    // Base 64 encoded bitmap image of user ticket
    public static String convertBmpToString() {

        Bitmap bmp = ImageUtility.getImage();
        return ImageUtility.convertToString(bmp);
    }




    public static void drawStationsStatus(String origin, String destination) {
        final int origin_zone = Dart.getMyLocalZonedStations(origin);
        final int dest_zone = Dart.getMyLocalZonedStations(destination);

        // get the firebase stations list
        DatabaseReference station_list = FirebaseCommon.getStationList();
        station_list.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    String name = childSnap.child("name").getValue(String.class);
                    Boolean man = childSnap.child("man_icon").getValue(Boolean.class);

                    Long zone_number = childSnap.child("zone_number").getValue(Long.class);
                    //cast to int otherwise NullPointerException
                    int zone_num = zone_number != null ? zone_number.intValue() : 0;

                    // only the zoned stations
                    if (zone_num == dest_zone) {
                        Log.e("Destination", "Zone : " + zone_number + " Name: " + name);

                    } else if (zone_num == origin_zone) {
                        Log.e("Origin  ", "Zone : " + zone_number + " Name: " + name);
                    }

                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}