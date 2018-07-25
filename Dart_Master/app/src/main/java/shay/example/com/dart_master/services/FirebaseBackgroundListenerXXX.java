package shay.example.com.dart_master.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shay.example.com.dart_master.MainActivity;
import shay.example.com.dart_master.R;


/**
 * Created by Shay on 25/03/2018.
 */

public class FirebaseBackgroundListenerXXX extends Service {

    private static final String CHANNEL_ID = "FB_CHN";
    String TAG = "FirebaseBackgroundListenerXXX";

    Context context;
    private boolean isConnected = false;

    private NotificationManager notificationManager;
    private RemoteViews remoteViews;
    private DatabaseReference myZone;

    ValueEventListener my_zone_listener = new ValueEventListener() {// listening to master switch state
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if (!isConnected) {// triggered once when connected
                isConnected = true;
                sendConnectedCallBackBroadcast();
                Log.e(TAG, "First time Connection callback ");
            } else {
                // we are already connected and data has changed ...
                Log.e(TAG, "onDataChange Firebase ");
                sendNotification();
            }


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Value: " + databaseError);

        }


    };


    private void sendConnectedCallBackBroadcast() {
        Intent intent = new Intent("firebase_is_connected"); // the filter used when registering the receiver
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        String zone_reference = intent.getStringExtra("ZONE_REF");
        Log.e(TAG, "ZONE: " + zone_reference);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        myZone = db.getReference("Notifications").child(zone_reference);
        myZone.addValueEventListener(my_zone_listener);

        return START_STICKY;
    }

    private void sendNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        remoteViews.setImageViewResource(R.id.notif_icon, R.drawable.tap_white);
        remoteViews.setImageViewResource(R.id.notif_ticket, R.drawable.ticket_sm);
        remoteViews.setTextViewText(R.id.notification_text, getText(R.string.notification_text));

        Intent notification_intent = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notification_intent, 0);

        Notification customNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.tap_small)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setContentIntent(pendingIntent)
                .build();

        int notification_id = (int) System.currentTimeMillis();
        notificationManager.notify(notification_id, customNotification);
        myZone.removeEventListener(my_zone_listener);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: " );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " );
    }

}
