package shay.example.com.dart_master.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shay.example.com.dart_master.MainActivity;
import shay.example.com.dart_master.R;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

/**
 * Created by Shay de Barra on 26,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class FirebaseService extends Service {


    private static final String CHANNEL_ID = "FB_CHN";
    public static boolean isRunning = false;

    public boolean intitialData = true;
    private String TAG = "FirebaseService";
    private Context context;
    private ValueEventListener my_zone_listener;
    private DatabaseReference myZone;
    private boolean notification_sent = false;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        myZone.removeEventListener(my_zone_listener);
        isRunning = false;
        stopSelf();
    }
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;

    private void showForegroundNotification() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // more android OREO headaches channel required here as well
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHN = createChannel();
            notification = new Notification.Builder(getApplicationContext(), CHN)// the dreaded channel required
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("TAP Staff is monitoring in the background")
                    .setSmallIcon(R.drawable.tap_small)
                    .setWhen(System.currentTimeMillis())
                    .setChannelId(CHN)
                    .build();
        }else {

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("TAP Staff is monitoring in the background")
                    .setSmallIcon(R.drawable.tap_small)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// The id of the channel.
        String id = "staff_channel_01";

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
        isRunning = true;
        intitialData = true;
        String zone_reference = intent.getStringExtra("ZONE_REF");
        Log.e(TAG, "ZONE: " + zone_reference);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        myZone = db.getReference("Notifications").child(zone_reference);
        myZone.addValueEventListener(my_zone_listener);
        // show a foreground message to keep the service alive in OREO API 26+ (Oreo kills background services when the phone sleeps)
        showForegroundNotification();
        return START_STICKY;
    }
    private boolean isInTheForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        my_zone_listener = new ValueEventListener() {// listening to master switch state


            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (intitialData) {// triggered once when connected
                    intitialData = false;

                    //       sendConnectedCallBackBroadcast();
                    Log.e(TAG, "First time Connection ");
                } else {
                    // ensure notification is called only once

        if(isInTheForeground()){
            // do nothing we are looking at the screen
        }else {
            sendNotification();
            notification_sent = true;
        }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Value: " + databaseError);

            }


        };

    }

    private void sendNotification() {

        Log.e(TAG, "sendNotification: " );
        NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String id = "id_client_monitoring";
        // The user-visible name of the channel.
        CharSequence name = "Notifications of client changes";
        // The user-visible description of the channel.
        String description = "Notifications of changes in my allocated zone";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.O" );
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }


        Intent intent_rtn = new Intent(getApplicationContext(), MainActivity.class);

        // the custom notification view
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        remoteViews.setImageViewResource(R.id.notif_icon, R.drawable.tap_white);
        remoteViews.setImageViewResource(R.id.notif_ticket, R.drawable.ticket_sm);
        remoteViews.setTextViewText(R.id.notification_text, getText(R.string.notification_text));


        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 456, intent_rtn, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),"id_product")
                .setSmallIcon(R.drawable.tap_small) //your app icon
                .setBadgeIconType(R.drawable.tap_small)
                .setChannelId(id)
                .setContentTitle("Zone Updates")
                .setContentText("You have client changes for review")
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setNumber(1)
                .setCustomContentView(remoteViews)
                .setColor(255)
                .setWhen(System.currentTimeMillis());
        notificationManager.notify(1, notificationBuilder.build());




    }

    private void sendNotificationXXX() {
        if (notification_sent) {
            return;
        }
        Log.e(TAG, "sendNotification");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "client_channel_id";
            CharSequence channelName = "Dart Client";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            if (notificationManager != null) {//precautionary measure
                notificationManager.createNotificationChannel(notificationChannel);
            }else{
                Log.e(TAG, "notificationManager is null" );
            }

        }

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        remoteViews.setImageViewResource(R.id.notif_icon, R.drawable.tap_white);
        remoteViews.setImageViewResource(R.id.notif_ticket, R.drawable.ticket_sm);
        remoteViews.setTextViewText(R.id.notification_text, getText(R.string.notification_text));

        Intent notification_intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notification_intent, 0);

        Notification customNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.tap_small)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        int notification_id = (int) System.currentTimeMillis();

        assert notificationManager != null;
        notificationManager.notify(notification_id, customNotification);

    }


}
