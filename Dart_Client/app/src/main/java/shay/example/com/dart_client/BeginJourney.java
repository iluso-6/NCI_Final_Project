package shay.example.com.dart_client;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import shay.example.com.dart_client.helper_classes.Dart;
import shay.example.com.dart_client.helper_classes.FavSQLiteStorage;
import shay.example.com.dart_client.helper_classes.FirebaseCommon;
import shay.example.com.dart_client.helper_classes.ImageUtility;
import shay.example.com.dart_client.helper_classes.PreferenceHelper;
import shay.example.com.dart_client.helper_classes.Utilities;
import shay.example.com.dart_client.models.Favourite;
import shay.example.com.dart_client.models.Info_destObj;
import shay.example.com.dart_client.models.Info_originObj;
import shay.example.com.dart_client.models.JourneyObj;
import shay.example.com.dart_client.models.Staff;
import shay.example.com.dart_client.services.BackgroundService;
import shay.example.com.dart_client.services.LocationService;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class BeginJourney extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 4711;
    private static final String TAG = "BeginJourney";
    public static ProgressBar progressBar;
    public static BeginJourney instance;// static reference for outer class access to non static methods
    private static String source_text;
    private static String destination_text;
    public boolean appIsBackgrounded;
    boolean saved_state;// global access for class
    TextView origin_status;
    TextView dest_status;
    private String savedBitmap;
    private JourneyObj selectedObj = JourneySelectionActivity.selectedObject;
    private String callingActivity;
    // foreword refs
    private DatabaseReference locationReference;
    private Staff staff_member;

    // @SuppressLint("SetTextI18n")
    public void setCallbackImageViewed(String callback) {

        if (callback.equalsIgnoreCase(source_text)) {
            ImageView org_viewed = findViewById(R.id.org_viewed);
            org_viewed.setImageResource(R.drawable.eye);
        } else if (callback.equalsIgnoreCase(destination_text)) {
            ImageView dest_viewed = findViewById(R.id.dest_viewed);
            dest_viewed.setImageResource(R.drawable.eye);
        }
    }


    public void onSavedState() {
        //save state boolean for Sign In to skip to this activity
        PreferenceHelper.setSharedPreferenceBoolean(this, "saved_state", true);
        //save the important values in the current state
        PreferenceHelper.setSharedPreferenceString(this, "source_text", source_text);
        PreferenceHelper.setSharedPreferenceString(this, "destination_text", destination_text);
        PreferenceHelper.setSharedPreferenceString(this, "ticket", savedBitmap);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved_state = PreferenceHelper.getSharedPreferenceBoolean(this, "saved_state", false);
        Bitmap bitmap;
        if (saved_state) {
            source_text = PreferenceHelper.getSharedPreferenceString(this, "source_text", "");
            destination_text = PreferenceHelper.getSharedPreferenceString(this, "destination_text", "");
            savedBitmap = PreferenceHelper.getSharedPreferenceString(this, "ticket", "");
            bitmap = ImageUtility.convertToBitmap(savedBitmap);
            Log.e(TAG, "saved_state: ");
        } else {
            // first time creating a ticket
            Intent intent = getIntent();
            source_text = intent.getStringExtra("source_text");
            destination_text = intent.getStringExtra("destination_text");
            bitmap = ImageUtility.getImage();
            callingActivity = intent.getStringExtra(Utilities.ACTIVITY_NAME);
        }

        instance = this;

        /* Check if the user clicked upon a notification location request ,if so - start the location service request
         which can only be started when the application is in the foreground
         */
        boolean notification_request_intent = getIntent().getBooleanExtra("LOC_REQUEST_NOTIFICATION", false);
        boolean notification_viewed_intent = getIntent().getBooleanExtra("VIEWED_NOTIFICATION", false);

        if (notification_request_intent) {
            Log.e(TAG, "LOC_REQUEST_NOTIFICATION: ");
            String loc_reference = getIntent().getStringExtra("loc_reference");
            Log.e(TAG, "LOC_REQUEST_NOTIFICATION: " + loc_reference);
            DatabaseReference location_reference = FirebaseDatabase.getInstance().getReferenceFromUrl(loc_reference);
            Staff staff_member = (Staff) getIntent().getSerializableExtra("Staff");
            startMyLocationService(location_reference, staff_member);
        }



        setContentView(R.layout.begin_journey);

        // leave this here after the layout file has been loaded ..
        if(notification_viewed_intent){
            String viewed_name = getIntent().getStringExtra("viewed_name");
            setCallbackImageViewed(viewed_name);
        }

        progressBar = findViewById(R.id.progressBarBegin);


        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;


        if (saved_state) {

            actionBar.setCustomView(R.layout.actionbar_menu_layout);// center logo
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);

            progressBar.setVisibility(View.INVISIBLE);
        } else {

            actionBar.setCustomView(R.layout.actionbar_layout);// center logo with back arrow layout xml
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);

            // progress bar will be cancelled after sending notification
            progressBar.setVisibility(View.VISIBLE);
            //   FirebaseCommon.beginTicketNotification(source_text, destination_text, selectedObj);
            startBackgroundService(source_text, destination_text, selectedObj);
        }

        Button close_btn = findViewById(R.id.close_btn);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSavedState();// custom saved state for ongoing ticket
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                // tell the activity to sign us out
                intent.putExtra("EXIT", true);
                startActivity(intent);

            }
        });

        Button cancel_btn = findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackWarningDialog();
            }
        });
        // get the live firebase data for the selected stations and zones to display to the user
        getInfoBoxData(source_text, destination_text);


        ImageView ticket = findViewById(R.id.ticket_bitmap);

        // draw the ticket image

        ticket.setImageBitmap(bitmap);

        // saved for later
        savedBitmap = ImageUtility.convertToString(bitmap);

        final ToggleButton favouriteButton = findViewById(R.id.favouriteButton);
        // check the database to see if the ticket is already a favourite
        FavSQLiteStorage db = new FavSQLiteStorage(BeginJourney.this);
        boolean result = db.checkMyFavourite(source_text, destination_text);
        // set the button accordingly
        favouriteButton.setChecked(result);

        favouriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    favouriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
                    //    storeJourneySelection(getApplicationContext(), source_text, destination_text);
                    FavSQLiteStorage db = new FavSQLiteStorage(BeginJourney.this);
                    Favourite favourite = new Favourite(source_text, destination_text);
                    db.addFavourite(favourite);
                    Toast.makeText(getApplicationContext(), "Added to favourites", Toast.LENGTH_LONG).show();

                } else {
                    favouriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
                    //     clearJourneySelection(getApplicationContext());
                    FavSQLiteStorage db = new FavSQLiteStorage(BeginJourney.this);
                    Favourite favourite = new Favourite(source_text, destination_text);
                    db.deleteMyFavourite(favourite);
                    Toast.makeText(getApplicationContext(), "Deleted from favourites", Toast.LENGTH_LONG).show();

                }
            }
        });


    }

    public void getInfoBoxData(final String origin, final String destination) {
        final int origin_zone = Dart.getMyLocalZonedStations(origin);
        final int dest_zone = Dart.getMyLocalZonedStations(destination);
        //   View view = layoutInflater.inflate(R.layout.begin_item, null);
        // get the firebase stations list

        DatabaseReference station_list = FirebaseCommon.getStationList();
        station_list.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<Info_originObj> originList = new ArrayList<>();
                final List<Info_destObj> destList = new ArrayList<>();

                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    String name = childSnap.child("name").getValue(String.class);
                    Boolean man = childSnap.child("man_icon").getValue(Boolean.class);
                    Boolean zone_active = childSnap.child("zone_active").getValue(Boolean.class);
                    Long colorLong = childSnap.child("zone_color").getValue(Long.class);

                    Long zone_number = childSnap.child("zone_number").getValue(Long.class);

                    //cast to int otherwise NullPointerException
                    int zone_num = zone_number != null ? zone_number.intValue() : 0;
                    int color = colorLong != null ? colorLong.intValue() : 0;


                    if (zone_num == origin_zone) {
                        Info_originObj info_originObj = new Info_originObj(name, man, zone_active, color);
                        originList.add(info_originObj);

                    }
                    // leave like this not "else if" in cases where both are in the same zone ...
                    if (zone_num == dest_zone) {
                        Info_destObj info_destObj = new Info_destObj(name, man, zone_active, color);
                        destList.add(info_destObj);
                    }


                }

                drawInfoBoxData(originList, destList, origin, destination);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void drawInfoBoxData(List<Info_originObj> originList, List<Info_destObj> destList, String origin, String destination) {

        final LinearLayout linearLayout = findViewById(R.id.linear_layout_insert);
        //   LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)linearLayout.getLayoutParams();
        //    params.setMargins(0,0,0,0);

        // clear the layout from/if previous children and dynamically insert new ones
        linearLayout.removeAllViewsInLayout();
        final LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // get the largest size list and work back from there ...
        int infoBoxSize = originList.size() > destList.size() ? originList.size() : destList.size();

        int origin_zone_number = Dart.getMyLocalZonedStations(origin);
        int dest_zone_number = Dart.getMyLocalZonedStations(destination);

        TextView org_zone = findViewById(R.id.origin_zone);
        String org_text = getString(R.string.zone) + origin_zone_number;
        org_zone.setText(org_text);

        String dest_text = getString(R.string.zone) + dest_zone_number;
        TextView dest_zone = findViewById(R.id.dest_zone);
        dest_zone.setText(dest_text);
        Log.e("drawInfoBoxData", "infoBoxSize: " + infoBoxSize + " originList.size(): " + originList.size() + " destList.size(): " + destList.size());
        for (int i = 0; i < infoBoxSize; i++) {
            // layout a view for the max amount and fill accordingly
            View view = layoutInflater.inflate(R.layout.begin_item, null);
            TextView txtOrigin = view.findViewById(R.id.txt_origin);
            TextView txtDest = view.findViewById(R.id.txt_dest);
            ImageView org_man = view.findViewById(R.id.org_man);
            ImageView dest_man = view.findViewById(R.id.dest_man);
            ImageView zone_org_circle = view.findViewById(R.id.zone_org_circle);
            ImageView zone_dest_circle = view.findViewById(R.id.zone_dest_circle);

            if (i < originList.size()) {
                Info_originObj originObj = originList.get(i);
                txtOrigin.setText(originObj.getOrigin());
                if (!originObj.getOrigin().equalsIgnoreCase(origin)) {
                    txtOrigin.setAlpha(0.35f);
                } else {
                    // change the alpha to highlight the origin
                    txtOrigin.setAlpha(1.0f);
                }
                if (originObj.getMan_icon()) {
                    org_man.setVisibility(View.VISIBLE);
                } else {
                    org_man.setVisibility(View.INVISIBLE);
                }
                if (originObj.getZone_active()) {
                    zone_org_circle.setVisibility(View.VISIBLE);
                    zone_org_circle.setColorFilter(originObj.getColor());
                } else {
                    zone_org_circle.setVisibility(View.INVISIBLE);
                }

            }
            if (i < destList.size()) {
                Info_destObj destObj = destList.get(i);
                txtDest.setText(destObj.getDestination());
                if (!destObj.getDestination().equalsIgnoreCase(destination)) {
                    txtDest.setAlpha(0.35f);
                } else {
                    // change the alpha to highlight the origin
                    txtDest.setAlpha(1.0f);
                }
                if (destObj.getMan_icon()) {
                    dest_man.setVisibility(View.VISIBLE);
                } else {
                    dest_man.setVisibility(View.INVISIBLE);
                }
                if (destObj.getZone_active()) {
                    zone_dest_circle.setVisibility(View.VISIBLE);
                    zone_dest_circle.setColorFilter(destObj.getColor());
                } else {
                    zone_dest_circle.setVisibility(View.INVISIBLE);
                }
            }
            linearLayout.addView(view);

        }


    }


    private void showBackWarningDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.warning_dialog);
        dialog.setCancelable(true);
        Button no = dialog.findViewById(R.id.button_no);
        Button yes = dialog.findViewById(R.id.button_yes);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                gotoPreviousActivity();

            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }



    private void gotoPreviousActivity() {
        clearNotifications();
        stopMyLocationService();
        if (saved_state) {
            PreferenceHelper.setSharedPreferenceBoolean(this, "saved_state", false);
        }
        if (Objects.equals(callingActivity, Utilities.ACTIVITY_FAVOURITE)) {
            Intent intent = new Intent(BeginJourney.this, FavouriteJourneys.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intent, animation);
            finish();
        } else {
            Intent intent = new Intent(BeginJourney.this, StationsActivity.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intent, animation);
            finish();
        }

        stopBackgroundService();

    }

    private void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
            Log.e(TAG, "clearNotifications: " );
        }
    }

    // back button in toolbar for previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            showBackWarningDialog();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        showBackWarningDialog();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // save the instance state here
        appIsBackgrounded = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        // load the saved instance state here
        appIsBackgrounded = false;
    }




    /*
    ////////////  startBackgroundService for notification deletion timers  ///////////////
    */


    private void startBackgroundService(String source_text, String destination_text, JourneyObj journeyObj) {
        Intent intent = new Intent(this, BackgroundService.class);
        intent.putExtra("source_text", source_text);
        intent.putExtra("destination_text", destination_text);
        intent.putExtra("journeyObj", (Serializable) journeyObj);
        startService(intent);
    }

    private void stopBackgroundService() {
        Log.e(TAG, "stopBackgroundService: " );
        Intent intent = new Intent(this, BackgroundService.class);
        stopService(intent);
    }

    /*
    ///////////////////////////      Location Service Permissions onward ...     //////////////////////////////////
    */


    public void startMyLocationService(DatabaseReference locReference, Staff staffmember) {
        locationReference = locReference;
        staff_member = staffmember;

        Log.e(TAG, "startMyLocationService: " + staffmember.getName());
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startLocationService(locationReference);
            setFireBaseResponse(true);
        }
    }

    public void stopMyLocationService() {
        stopService(new Intent(this, LocationService.class));
    }


    private void setFireBaseResponse(boolean response){
  /// sets the  locationResponse: true/false in the firebase response for the master to display
       DatabaseReference locationRef = locationReference.getParent().getRef();
       locationRef.child("locationResponse").setValue(response);
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // launch the default permissions dialog
    private void startLocationPermissionRequest() {

        ActivityCompat.requestPermissions(BeginJourney.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // this custom dialog will personalise the permissions request prior to calling them ie. your boss needs permission to ...
    private void showCustomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.permissions_dialog);
        dialog.setTitle("Location Permissions");
        dialog.setCancelable(false);
        Button ok = dialog.findViewById(R.id.button_ok);
        ImageView photo_iv = dialog.findViewById(R.id.photoID);
        String url = staff_member.getUrl();
        Picasso.with(getApplicationContext()).load(url).into(photo_iv);
        TextView name = dialog.findViewById(R.id.empl_name);
        String staff_name = staff_member.getName();
        name.setText(staff_name);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                startLocationPermissionRequest();// start the actual permissions request
            }
        });


        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.show();
        dialog.getWindow().setAttributes(layoutParams);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.e(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            showCustomDialog();
                        }
                    });

        } else {
            Log.e(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            showCustomDialog();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                startLocationService(locationReference);
            } else {
                setFireBaseResponse(false);
                Log.e(TAG, " ** Permission denied. ** ");
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void startLocationService(DatabaseReference locationReference) {
        // change the ref to string for intent extra ...
        String locRef = String.valueOf(locationReference);
        Intent intent = new Intent(BeginJourney.this, LocationService.class);
        intent.putExtra("DB_REF", locRef);
        startService(intent);
        Log.e("startLocationService", "startLocationService");
    }


}

