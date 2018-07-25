package shay.example.com.dart_master;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import shay.example.com.dart_master.helpers.PreferenceHelper;
import shay.example.com.dart_master.map_directions_routes.DownloadMapRoutesTask;
import shay.example.com.dart_master.models.JourneyObj;

import static shay.example.com.dart_master.SignInActivity.getAppContext;
import static shay.example.com.dart_master.SignInActivity.master;
import static shay.example.com.dart_master.helpers.Location_Math.getIsOnRailTrack;

/**
 * Created by Shay de Barra on 01,April,2018
 * Email:  x16115864@student.ncirl.ie
 */


public class ClientActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 987;
    public static String thisStationName;

    private String phoneNo;
    private JourneyObj journeyObj;// forward reference for object we get passed in intent
    private TextView responseText;
    public GoogleMap mMap;
    private String origin;
    private String destination;
    private ImageButton requestLocBtn;
    private String unique_journey_identifier;
    private Marker dest_marker;
    private Marker user_marker;
    private Marker origin_marker;
    private Circle circle;
    private ValueAnimator valueAnimator;
    private GoogleMap.CancelableCallback cameraComplete;
    public static ClientActivity instance;


    private Marker drawStationMarker(String name, LatLng latLng) {

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng));

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(createStationMarker(name)));
        marker.setTag(name);
        return marker;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));// custom json map style created

        drawMapPolyLines();


    }

    private void drawMapPolyLines() {
        String polyline_malahide = getString(R.string.junction_to_malahide);
        String polyline_howth = getString(R.string.greystones_to_howth);
        // PolyUtil.decode will return a list of the LatLng objects
        List<LatLng> route_howth = PolyUtil.decode(polyline_howth);
        List<LatLng> route_malahide = PolyUtil.decode(polyline_malahide);

        Polyline poly_howth = mMap.addPolyline(new PolylineOptions().addAll(route_howth).color(Color.GRAY));
        Polyline poly_malahide = mMap.addPolyline(new PolylineOptions().addAll(route_malahide).color(Color.GRAY));
    }


    /*Code extract by Kamlesh Karwande modified by Shay de Barra from
     * Kamlesh Karwande
     * https://stackoverflow.com/questions/23572583/how-to-show-text-in-drawable-custom-marker
     * */
    private Bitmap createStationMarker(String name) {
        View markerLayout = getLayoutInflater().inflate(R.layout.custom_marker, null);

        ImageView markerImage = markerLayout.findViewById(R.id.marker_image);
        TextView markerName = markerLayout.findViewById(R.id.marker_text);
        markerImage.setImageResource(R.drawable.map_icon_sm);
        markerName.setText(name);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        instance = this;

        ArrayList<String> stationName = PreferenceHelper.getSharedPreferenceStringArray(getAppContext(), master.getMasterID(), "");
        thisStationName = stationName.get(1);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

  /*      ActionBar actionBar = getSupportActionBar();// get the parent in order to insert custom logo and station name
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.client_actionbar_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        }*/

        requestLocBtn = findViewById(R.id.requestLocBtn);

        // get the object passed from the Main Activity click event
        Intent intent = getIntent();
        // public class JourneyObj implements Serializable
        journeyObj = (JourneyObj) intent.getSerializableExtra("obj");
        //    setActivityTitle(journeyObj);
        setClientDetails(journeyObj);
        setTrainDetails(journeyObj);

        // assign the phone number to the button for calling
        ImageView phone = findViewById(R.id.phoneIcon);
        phone.setTag(journeyObj.getUser_phone());
        phone.setOnClickListener(this);

        responseText = findViewById(R.id.requestLocText);


        // identifier valid for this journey only and not other journeys or users
        unique_journey_identifier = journeyObj.getFirebaseRef();
        // get the stored button state if this was previously open
        final boolean buttonSelected = PreferenceHelper.getSharedPreferenceBoolean(this, unique_journey_identifier, false);
        // set the visual on the button
        requestLocBtn.setSelected(buttonSelected);

        // check if the request has been previously sent
        String response = PreferenceHelper.getSharedPreferenceString(this, unique_journey_identifier + "responseString", "Location Request: not requested");
        if (response.equalsIgnoreCase("Location Request: accepted")) {
            requestLocBtn.setVisibility(View.GONE);
        }
        // set the stored value if there is one
        responseText.setText(response);

        requestLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!buttonSelected) {
                    requestLocBtn.setSelected(true);//button selected image
                    String responseString = "Location Request: requested";
                    responseText.setText(responseString);
                    PreferenceHelper.setSharedPreferenceString(getApplicationContext(), unique_journey_identifier + "responseString", responseString);

                    // remember the button state for jumping in/out activity
                    PreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), unique_journey_identifier, true);
                    String firebaseRef = journeyObj.getFirebaseRef();

                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseRef);
                    // request sent to client
                    ref.child("locationRequested").setValue(true);
                    ref.child("requestByID").setValue(master);
                    // set  a child listener for a response from the application to update the UI of the request state
                    ref.child("locationResponse").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                // do nothing no response yet
                            } else {
                                //     boolean response = (boolean)dataSnapshot.getValue();
                                updateLocationResponseValues();
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {// the button has previously been pressed so update values in UI
                    //  updateLocationResponseValues();
                }
            }
        });
    }

    private void updateLocationResponseValues() {
        Log.e("updateResponseValues", "updateLocationResponseValues: ");
        String firebaseRef = journeyObj.getFirebaseRef();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseRef);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // can be null if the ticket is cancelled by the user when the master is open
                Object exists = dataSnapshot.child("locationResponse").getValue();

                if (exists != null) {

                    boolean granted = (boolean) dataSnapshot.child("locationResponse").getValue();
                    PreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), "response_state", granted);
                    if (granted) {
                        String responseString = "Location Request: accepted";
                        responseText.setText(responseString);
                        PreferenceHelper.setSharedPreferenceString(getApplicationContext(), unique_journey_identifier + "responseString", responseString);
                        requestLocBtn.setVisibility(View.GONE);
                    } else {
                        String responseString = "Location Request: denied";
                        responseText.setText(responseString);
                        PreferenceHelper.setSharedPreferenceString(getApplicationContext(), unique_journey_identifier + "responseString", responseString);
                    }

                    if (granted) {
                        DataSnapshot location = dataSnapshot.child("location");
                        if (location.hasChildren()) {
                            // precautionary measure ... initially the value could be null
                            Object hasValue = dataSnapshot.child("location").child("longitude").getValue();
                            if (hasValue != null) {

                                double lat = (double) dataSnapshot.child("location").child("latitude").getValue();
                                double lon = (double) dataSnapshot.child("location").child("longitude").getValue();
                                //     Double lat = Double.parseDouble(latitude);
                                //     Double lon = Double.parseDouble(longitude);
                                //   Log.e("(location).getValue();", "onDataChange: " + dataSnapshot.child("location").child("longitude").getValue());

                                drawUserLocation(lat, lon);
                            } else {
                                Log.e("(location).getValue()", "NULL");
                            }
                        }
                    }
                } else {
                    Log.e("Exist = null;", "onDataChange: ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void drawUserLocation(final double my_lat, final double my_lon) {
// get the latitude and longitude for this station location and the destination station
        getMyFirebaseStationLocation(new SimpleCallback<LatLng[]>() {

            @Override
            public void callback(LatLng[] data) {
                Log.e("drawUserLocation", "callback: ");
                if (origin_marker != null) {
                    origin_marker.remove();
                }
                if (dest_marker != null) {
                    dest_marker.remove();
                }
                origin_marker = drawStationMarker(origin, data[0]);
                dest_marker = drawStationMarker(destination, data[1]);


                LatLng my_pos = new LatLng(my_lat, my_lon);
                drawUserMarker(my_pos);



                    // show possible route they will travel upon
                    drawRouteToOriginStation(my_pos,data[0]);


                animateCameraToPos(data, my_pos);
            }


        });


    }

    private void drawRouteToOriginStation(LatLng my_pos, LatLng target) {

        /// first check to see if the client is on the train (polyline)
        String greystones_to_howth = getString(R.string.greystones_to_howth);
        String howth_junction_to_malahide = getString(R.string.junction_to_malahide);
        // true if on the line
        boolean howth_result = getIsOnRailTrack(my_pos, greystones_to_howth);
        boolean malahide_result = getIsOnRailTrack(my_pos, howth_junction_to_malahide);

        if(howth_result || malahide_result){
            Log.e("Client is on the train", "drawRouteToOriginStation: " );
            return;
        }
        DownloadMapRoutesTask downloadMapRoutesTask = new DownloadMapRoutesTask();
        String url = getDirectionsUrl(my_pos, target);
        // Start downloading json data from Google Directions API
        downloadMapRoutesTask.execute(url);
        Log.e("URL", "drawRouteToOriginStation: "+url );
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
// &key=YOUR_API_KEY
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = getString(R.string.google_maps_key);
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=" + key ;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }
    /**  ********************* */

    private void drawUserMarker(LatLng my_pos) {
        // Radius of the circle
        final int radius = 100;
        final float stroke_width = 12.0f;
        int blue = getResources().getColor(R.color.blue_circle);
        int radius_blue = getResources().getColor(R.color.light_circle);
        if (circle != null) {
            valueAnimator.pause();
            circle.remove();
            circle = mMap.addCircle(new CircleOptions()
                    .center(my_pos)
                    .radius(radius)
                    .strokeWidth(stroke_width)
                    .strokeColor(radius_blue)
                    .fillColor(blue));
            valueAnimator.resume();
        } else {

            // draw the circle for the first time
            circle = mMap.addCircle(new CircleOptions()
                    .center(my_pos)
                    .radius(radius)
                    .strokeWidth(stroke_width)
                    .strokeColor(radius_blue)
                    .fillColor(blue));


            valueAnimator = new ValueAnimator();
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setIntValues(0, 400);
            valueAnimator.setDuration(3000);
            valueAnimator.setEvaluator(new IntEvaluator());
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    circle.setRadius(animatedFraction * radius * 2);
                    circle.setStrokeWidth(animatedFraction*stroke_width*4);
                }
            });

            valueAnimator.start();

        }

    }


    private void animateCameraToPos(LatLng[] data, LatLng my_pos) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        // add the marker positions to be included in the camera bounds
        builder.include(data[0]);
        builder.include(data[1]);
        builder.include(my_pos);
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);


    }


    // get the lat lang of this station
    private void getMyFirebaseStationLocation(@NonNull final SimpleCallback<LatLng[]> finishedCallback) {
        final LatLng stationsLocArray[] = new LatLng[2];
        origin = journeyObj.getOrigin();
        destination = journeyObj.getDestination();

        // get the firebase stations list
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference station_list = database.getReference("Station_List");

        station_list.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    String name = childSnap.child("name").getValue(String.class);

                    Log.e("FirebaseStations", "Name : " + name);
                    // only the origin and destination location required
                    if (name.equalsIgnoreCase(origin)) {
                        Log.e("FirebaseStations", "Name : " + name);

                        double latitude = (double) childSnap.child("latitude").getValue();

                        double longitude = (double) childSnap.child("longitude").getValue();

                        LatLng org_latlng = new LatLng(latitude, longitude);
                        stationsLocArray[0] = org_latlng;


                    }


                    if (name.equalsIgnoreCase(destination)) {
                        Log.e("FirebaseStations", "Name : " + name);

                        double latitude = (double) childSnap.child("latitude").getValue();

                        double longitude = (double) childSnap.child("longitude").getValue();

                        LatLng dest_latlng = new LatLng(latitude, longitude);
                        stationsLocArray[1] = dest_latlng;


                    }
                }
                Log.e("finishedcallback", "(stationsLocArray) : ");
                finishedCallback.callback(stationsLocArray);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private void setTrainDetails(JourneyObj journeyObj) {
        String infosource = journeyObj.getJourney_info();
        String split[] = infosource.split(" ");
        if (split[0].trim().equalsIgnoreCase("Departing")) {
            // departure details from object
            populateTable(journeyObj.getOrg_traincode(), journeyObj.getOrg_train_type(), journeyObj.getOrg_direction(), journeyObj.getOrg_scharrival(), journeyObj.getOrg_exparrival(), journeyObj.getOrg_origin(), journeyObj.getOrg_destination());
        } else {
            // arriving details from object
            populateTable(journeyObj.getDest_traincode(), journeyObj.getDest_train_type(), journeyObj.getDest_direction(), journeyObj.getDest_scharrival(), journeyObj.getDest_exparrival(), journeyObj.getDest_origin(), journeyObj.getDest_destination());

        }

    }

    private void populateTable(String t_id, String t_type, String t_dir, String t_schArr, String t_expArr, String t_origin, String t_dest) {
        TextView id = findViewById(R.id.train_id);
        TextView type = findViewById(R.id.type);
        TextView dir = findViewById(R.id.direction);
        TextView schArr = findViewById(R.id.sch_arr);
        TextView expArr = findViewById(R.id.exp_arr);
        TextView origin = findViewById(R.id.origin);
        TextView dest = findViewById(R.id.destination);

        id.setText(t_id);
        type.setText(t_type);
        dir.setText(t_dir);
        schArr.setText(t_schArr);
        expArr.setText(t_expArr);
        origin.setText(t_origin);
        dest.setText(t_dest);
    }


    private void setClientDetails(JourneyObj journeyObj) {
        ImageView userImg = findViewById(R.id.userImage);
        Picasso.with(getApplicationContext()).load(journeyObj.getUser_url()).into(userImg);

        TextView name = findViewById(R.id.user_name);
        name.setText(journeyObj.getUser_name());

        TextView info = findViewById(R.id.info_text);
        info.setText(journeyObj.getJourney_info());
    }

    // ignore this .. we need the added space
    private void setActivityTitle(JourneyObj journeyObj) {
        TextView title = findViewById(R.id.title_text);
        title.setText(journeyObj.getDest_traincode());
        ImageView train = findViewById(R.id.train);
        if (journeyObj.getDest_train_type().equalsIgnoreCase("DART")) {
            train.setImageResource(R.drawable.dart);
        } else {
            train.setImageResource(R.drawable.ie_sm);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
        startActivity(intent, animation);

        ClientActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        String phone_num = (String) v.getTag();
        callPhone(phone_num);
        Log.e("phone call", "onClick: " + phone_num);

    }

    public void callPhone(String phone) {
        phoneNo = phone;
        int checkPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(// show the default request dialog
                    this,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);// value for permissions callback .. onRequestPermissionsResult
        } else {
            // make the call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNo));
            startActivity(callIntent);
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // go ahead and make call after user set the dialog permission
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneNo));
                    try {
                        startActivity(callIntent);
                    } catch (Exception e) {

                    }
                } else {
                    // enough said ..
                    Toast.makeText(getApplicationContext(),
                            "Permission denied to make a phone call by the user", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLocationResponseValues();
        Log.e("updateLocResponseVal();", "onResume: ");
    }
}
