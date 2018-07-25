package shay.example.com.dart_client.services;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import shay.example.com.dart_client.BeginJourney;
import shay.example.com.dart_client.R;
import shay.example.com.dart_client.helper_classes.Dart;
import shay.example.com.dart_client.helper_classes.ImageUtility;
import shay.example.com.dart_client.helper_classes.PreferenceHelper;
import shay.example.com.dart_client.models.JourneyObj;
import shay.example.com.dart_client.models.Staff;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static shay.example.com.dart_client.BeginJourney.progressBar;

/**
 * Created by Shay de Barra on 04,April,2018
 * Email:  x16115864@student.ncirl.ie
 */
public class BackgroundService extends Service {

    private static final int NOTIFICATION_ID = 100;
    private static DatabaseReference origin_ref;
    private static DatabaseReference dest_ref;
    Context context;
    private String TAG = "BackgroundService";
    private FirebaseDatabase database;
    private NotificationTimer originTimer;
    private NotificationTimer destTimer;

    private Map<ValueEventListener, DatabaseReference> viewedListeners;
    private Map<ValueEventListener, DatabaseReference> locationListeners;

    // Base 64 encoded bitmap image of user ticket
    public static String convertBmpToString() {
        Bitmap bmp = ImageUtility.getImage();
        return ImageUtility.convertToString(bmp);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showForegroundNotification() {

        Intent intent = new Intent(getApplicationContext(), BeginJourney.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // more android OREO headaches channel required here as well
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHN = createChannel();
            notification = new Notification.Builder(getApplicationContext(), CHN)// the dreaded channel required
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("TAP is monitoring in the background")
                    .setSmallIcon(R.drawable.tap_small)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }else {

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("TAP is monitoring in the background")
                    .setSmallIcon(R.drawable.tap_small)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        startForeground(NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// The id of the channel.
        String id = "client_channel_01";

// The user  name of the channel.
        CharSequence name = getString(R.string.app_name);

// The user  description of the channel.
        String description = getString(R.string.channel_description);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(id, name,importance);

// Configure the notification channel.
        mChannel.setDescription(description);

        mChannel.enableLights(true);
// Sets the notification light color for notifications posted to this
// channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);

        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
        return id;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();

        String source_text = intent.getStringExtra("source_text");
        String destination_text = intent.getStringExtra("destination_text");
        JourneyObj journeyObj = (JourneyObj) intent.getSerializableExtra("journeyObj");
        beginTicketNotification(source_text, destination_text, journeyObj);
        showForegroundNotification();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        deleteNotificationData();
        // stop the service itself
        stopSelf();

    }

    public DatabaseReference getNotificationsNode() {
        database = FirebaseDatabase.getInstance();


        return database.getReference("Notifications");
    }

    public void beginTicketNotification(String origin_station, String dest_station, JourneyObj journeyObj) {

        // called once to store the viewed listeners references
        viewedListeners = null;
        viewedListeners = new HashMap<>();

        locationListeners = null;
        locationListeners = new HashMap<>();


        int origin_zone = Dart.getMyLocalZonedStations(origin_station);
        int dest_zone = Dart.getMyLocalZonedStations(dest_station);

        String departureText = "Departing from: " + origin_station;
        String arrivalText = "Arriving at: " + dest_station;
      //  UserSingletonXXX user = UserSingletonXXX.getInstance();
        String user_name = PreferenceHelper.getSharedPreferenceString(getApplicationContext(),"user_name","");
        journeyObj.setUser_name(user_name);

        String user_photo = PreferenceHelper.getSharedPreferenceString(getApplicationContext(),"user_photo","");
        journeyObj.setUser_url(user_photo);

        String user_phone = PreferenceHelper.getSharedPreferenceString(getApplicationContext(),"user_phone","");
        journeyObj.setUser_phone(user_phone);

        journeyObj.setTicket_image(convertBmpToString());

        // set the origin and destination titles in the object to be used in the master ClientActivity
        journeyObj.setOrigin(origin_station);
        journeyObj.setDestination(dest_station);


        journeyObj.setJourney_info(departureText);
        origin_ref = sendNotificationData(journeyObj, origin_zone, origin_station);

        long origin_time = Long.parseLong(String.valueOf(journeyObj.getOrg_duein()));// time in minutes
        originTimer = new NotificationTimer(origin_time);
        originTimer.setReferenceForDeletion(origin_ref);
        originTimer.start();

        // change the journey info before sending arriving Info
        journeyObj.setJourney_info(arrivalText);
        dest_ref = sendNotificationData(journeyObj, dest_zone, dest_station);

        long dest_time = Long.parseLong(String.valueOf(journeyObj.getDest_duein()));

        destTimer = new NotificationTimer(dest_time);
        destTimer.setReferenceForDeletion(dest_ref);
        destTimer.setJourneyIsCompleted(dest_ref);
        destTimer.start();

    }

    private DatabaseReference sendNotificationData(JourneyObj journeyObj, int zone, final String station) {

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

    /*****************          Method isInTheForeground()     **************
     //   https://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
     *    cesards  answered Nov 25 '16 at 12:03
     */

    private boolean isInTheForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    private ValueEventListener addListenerForLocationRequest(final DatabaseReference locationReference, String station) {
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
                        // set the staff member details required by the dialog to display
                        staffmember.setUrl(url);
                        staffmember.setName(name);
                        if (isInTheForeground()) {
                            Log.e(TAG, "isInTheForeground: TRUE");
                            BeginJourney.instance.startMyLocationService(locationReference, staffmember);
                        } else {
                            Log.e(TAG, "isInTheForeground: FALSE");
                            if (checkPermissions(context)) {
                                // we already have permission so no dialog will upset the application
                                BeginJourney.instance.startMyLocationService(locationReference, staffmember);
                            } else {
                                Log.e(TAG, "sendLocationNotificationRequest");
                                // we must have the ui in the foreground to obtain permissions
                                sendLocationNotificationRequest(locationReference, staffmember);
                            }
                        }
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
    private void sendLocationNotificationRequest(DatabaseReference locationReference, Staff staff_member) {

        NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String id = "id_location_request";
        // The user-visible name of the channel.
        CharSequence name = "TAP Client location";
        // The user-visible description of the channel.
        String description = "Notifications about location requests";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(mChannel);
        }


        Intent intent_rtn = new Intent(getApplicationContext(), BeginJourney.class);
        intent_rtn.putExtra("LOC_REQUEST_NOTIFICATION", true);

        String loc_reference = String.valueOf(locationReference);
        intent_rtn.putExtra("loc_reference", loc_reference);
        intent_rtn.putExtra("Staff", (Staff) staff_member);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 123, intent_rtn, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),"id_product")
                .setSmallIcon(R.drawable.tap_small) //your app icon
                .setBadgeIconType(R.drawable.tap_small)
                .setChannelId(id)
                .setContentTitle("Irish Rail Location Request")
                .setContentText("You have a location request pending")
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setNumber(1)
                .setColor(255)
                .setWhen(System.currentTimeMillis());
        notificationManager.notify(1, notificationBuilder.build());
    }


    private ValueEventListener addListenerForViewed(final DatabaseReference viewed, final String viewedName) {
        //    Log.e(TAG, "addListenerForViewed INIT " + viewed);
        return viewed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    // precautionary measure
                } else {
                    boolean value = (boolean) dataSnapshot.getValue();
                    if (value) {
                        if (isInTheForeground()) {
                            BeginJourney.instance.setCallbackImageViewed(viewedName);
                        } else {
                            sendNotificationViewed(viewedName);
                        }


                    }
                    Log.e(TAG, "addListenerForViewed onDataChange :  " + value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendNotificationViewed(String viewedName) {


            NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            String id = "id_ticket_view";
            // The user-visible name of the channel.
            CharSequence name = "TAP Client viewed ticket";
            // The user-visible description of the channel.
            String description = "Notifications about your ticket being viewed";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(id, name, importance);
                // Configure the notification channel.
                mChannel.setDescription(description);
                mChannel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                mChannel.setLightColor(Color.RED);
                notificationManager.createNotificationChannel(mChannel);
            }


            Intent intent_rtn = new Intent(getApplicationContext(), BeginJourney.class);
            intent_rtn.putExtra("VIEWED_NOTIFICATION", true);
            intent_rtn.putExtra("viewed_name", viewedName);


            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 123, intent_rtn, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),"id_product")
                    .setSmallIcon(R.drawable.tap_small) //your app icon
                    .setBadgeIconType(R.drawable.tap_small)
                    .setChannelId(id)
                    .setContentTitle("Ticked Viewed")
                    .setContentText("Your ticket has been viewed")
                    .setAutoCancel(true).setContentIntent(pendingIntent)
                    .setNumber(1)
                    .setColor(255)
                    .setWhen(System.currentTimeMillis());
            notificationManager.notify(1, notificationBuilder.build());




    }

    public void deleteNotificationData() {

        //   removeViewedListeners();
        DatabaseReference notifications = getNotificationsNode();
        //TODO remove event listener
        // remove the views nodes
        DatabaseReference viewsOrg = notifications.child("Views").child(origin_ref.getKey());
        viewsOrg.removeValue();
        DatabaseReference viewsDest = notifications.child("Views").child(dest_ref.getKey());
        viewsDest.removeValue();
        Log.e(TAG, "deleteNotificationData: " + origin_ref);
        Log.e(TAG, "deleteNotificationData: " + dest_ref);

        // remove the items nodes
        origin_ref.removeValue();
        dest_ref.removeValue();
        if (originTimer != null) {
            originTimer.cancel();
        }
        if (destTimer != null) {
            destTimer.cancel();
        }
        // there is no ticket anymore
        PreferenceHelper.setSharedPreferenceBoolean(this, "saved_state", false);
    }

    private void removeViewedListener(DatabaseReference dbReference) {

        for (Map.Entry<ValueEventListener, DatabaseReference> pairs : viewedListeners.entrySet()) {

            ValueEventListener listener = pairs.getKey();
            DatabaseReference databaseReference = pairs.getValue();
            if (databaseReference.equals(dbReference.child("viewed"))) {
                Log.e(TAG, databaseReference + " remove specific ViewedListener: " + listener);
                databaseReference.removeEventListener(listener);
            }
            //   Log.e(TAG, databaseReference+"!! (databaseReference.equals(dbReference)"+dbReference );
        }

    }

    // check permissions before starting location service otherwise will crash application if backgrounded (which it is here)
    public boolean checkPermissions(Context context) {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private class NotificationTimer extends CountDownTimer {
        private DatabaseReference notifications = getNotificationsNode();
        private DatabaseReference reference;
        private DatabaseReference final_reference;

        public NotificationTimer(long minutes) {
            super(minutes * 60 * 1000, 1000);
        }

        public void setReferenceForDeletion(DatabaseReference databaseReference) {
            this.reference = databaseReference;
        }

        public void setJourneyIsCompleted(DatabaseReference databaseReference) {
            this.final_reference = databaseReference;

        }

        @Override
        public void onTick(long millisUntilFinished) {
            //    Log.e("onTick", "Minutes Left: " + millisUntilFinished/1000/60);
        }

        @Override
        public void onFinish() {
            Log.e(TAG, "onFinish: " + this.reference);
            // remove the viewed listener and the the node
            DatabaseReference viewRef = notifications.child("Views").child(reference.getKey());
            removeViewedListener(viewRef);
            viewRef.removeValue();

            if (this.reference.equals(final_reference)) {// this is the destination completed .. so set the saved_state value to false
                PreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), "saved_state", false);
                Log.e(TAG, "NotificationTimer: saved_state:false");

                stopService(new Intent(BackgroundService.this, LocationService.class));
            }
            // remove the node value of origin/dest
            this.reference.removeValue();

        }
    }

}
