package shay.example.com.dart_client;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import shay.example.com.dart_client.helper_classes.Dart;
import shay.example.com.dart_client.helper_classes.FirebaseCommon;
import shay.example.com.dart_client.helper_classes.GetAllCurrentTrains;
import shay.example.com.dart_client.helper_classes.PreferenceHelper;
import shay.example.com.dart_client.helper_classes.Stations;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int PERIOD = 10000;// check live train data every 10 seconds
    public static ArrayList<Circle> trainArray = new ArrayList<>();
    private final String[] none = {""};// nothing required in spinner except image dropdown
    public Timer timer;
    Stations stations;

    List<LatLng> howth_points_list;
    Marker marker;
    private LatLng myLatLng;
    private GoogleMap mMap;
    private String my_local_station;// saved value in sharedPrefs
    private List<Marker> markersList = new ArrayList<>();
    private ListIterator<Marker> iterator;
    private boolean clearTrainMarkers = false;
    private boolean aerialView = false;
    private List<Marker> tourList;
    private Polyline poly_howth;
    private RelativeLayout top;
    private ImageButton infoBtn;

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();        // initialise a Timer for train movements Async download
        startAsyncLiveTrainTimer();
        Log.e("onResume", "timer = new Timer(); ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        // get the saved local station name for map ref
        my_local_station = PreferenceHelper.getSharedPreferenceString(getApplicationContext(), "key_local_stat", "");


        ActionBar actionBar = getSupportActionBar();// get the parent in order to map buttons

        assert actionBar != null;
        actionBar.setCustomView(R.layout.map_bar_layout);// map buttons with back (up) arrow layout xml


        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        infoBtn = findViewById(R.id.info_btn);
        top = findViewById(R.id.top_layout);

        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag("toggle");
                toggleOverlay(v);
            }
        });
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag("toggle");
                toggleOverlay(v);

            }
        });
        RadioGroup rg = findViewById(R.id.radio_group);// two radio buttons for Map View changes

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(RadioGroup group, final int checkedId) {
                CameraPosition cameraPosition = null;
                LatLngBounds init_bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

                myLatLng = ((myLatLng == null) ? init_bounds.getCenter() : myLatLng);
                int radius = 200;

                if (checkedId == R.id.radio1) {// local station orthographic view
                    Log.d("chk", "First");
                    aerialView = false;
                    cameraPosition = new CameraPosition.Builder()
                            .target(myLatLng)      // Sets the map to the users local station
                            .zoom(14)                   // Sets the zoom
                            .bearing(-65)                // -90 = west, 90 = east
                            .tilt(45)
                            .build();

                } else if (checkedId == R.id.radio2) {// aerial view
                    Log.d("Aerial View", "Second");
                    aerialView = true;
                    for (Marker marker : markersList) {
                        if (Dart.mainStationNames.contains(marker.getTag()) || (marker.getTag().equals(my_local_station))) {
                            // do Nothing
                        } else {
                            marker.setVisible(false);
                            Log.e("Marker", "Name" + marker.getTag());
                        }
                    }


                    cameraPosition = new CameraPosition.Builder()
                            .target(myLatLng)
                            .zoom(10)
                            .bearing(0)
                            .tilt(0)
                            .build();
                } else if (checkedId == R.id.radio3) {
                    Log.d("333333333333333333", "Third Radio");
                    initiliseTrainJourney();// animated tour ... begin

                }
                if (aerialView) {
                    radius = 600;
                }
                for (Circle circle : trainArray) {
                    circle.setRadius(radius);
                }
                if (checkedId == R.id.radio3) {
                } else {
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            if (checkedId == R.id.radio1) {
                                for (Marker marker : markersList) {
                                    marker.setVisible(true);
                                }
                            }

                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
            }

        });

    }

    private void toggleOverlay(View v) {

        if (v.getTag().equals("toggle")) {// only the info button and image allowed
            int vis = top.getVisibility();

            if (vis == View.GONE) {
                top.setVisibility(View.VISIBLE);
            } else {
                top.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        setMapStyle();

        // get the stations from firebase and set local station reference
        getStationsDataForMap(my_local_station);

        drawMapPolyLines();

        //    startAsyncLiveTrainTimer();

    }

    private void startAsyncLiveTrainTimer() {

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                try {

                    clearTrainMarkers = true;
                    Log.e("beginDataDownload()", "beginLiveTrainDataDownload()");
                    beginLiveTrainDataDownload();// get the live trains currently running every "PERIOD" seconds

                } catch (Exception e) {
                    Log.e("Exception e", "  Error;" + e);
                    // TODO: handle exception
                }

            }
        }, 0, PERIOD);

    }

    private void getStationsDataForMap(final String local_station) {
        final int zone = Dart.getMyLocalZonedStations(local_station);

        // get the firebase stations list
        DatabaseReference station_list = FirebaseCommon.getStationList();
        station_list.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    String name = (String) childSnap.child("name").getValue();
                    long zone_number = (long) childSnap.child("zone_number").getValue();
                    Double latitude = childSnap.child("latitude").getValue(Double.class);
                    Double longitude = childSnap.child("longitude").getValue(Double.class);

                    stations = new Stations();
                    stations.setLat(latitude);
                    stations.setLon(longitude);
                    stations.setName(name);
                    //   stations_list.add(stations);
                    Log.e("zone: " + zone, "zone_number: " + zone_number);
                    // only the zoned stations
                    if (zone == zone_number) {
                        //    Log.e("", "Zone : " + zone + " Name: " + name);


                        assert name != null;
                        if (name.equalsIgnoreCase(local_station)) {

                            myLatLng = new LatLng(latitude, longitude);
                            //       Log.e("", "(name.equalsIgnoreCase(local_station)): " + myLatLng);

                        }
                    }
                    LatLng latLng = new LatLng(latitude, longitude);
                    drawMarker(name, latLng);
                }

                setCameraPosition(myLatLng);
                for(Marker marker: markersList) {
                    Log.e("MARKER", "onDataChange: " + marker.getTitle());
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void removeTrains() {
        Log.e("removeTrains", "removeTrains");
        for (Circle circle : trainArray) {
            //        Log.e("BEFORE", "  array Size;" + trainArray.size());
            circle.remove();
            //        Log.e("AFTER", "  array Size;" + trainArray.size() + " : ");
        }
        trainArray = null;
        trainArray = new ArrayList<>();
        clearTrainMarkers = false;// ensure this method gets called only once
    }
    // END OF Kamlesh Karwande

    private void drawTrain(String direction, LatLng latLng, String status) {
        if (clearTrainMarkers && trainArray != null) {
            removeTrains();
        }
        int terminated = (Color.GRAY);
        int direction_north = Color.rgb(30, 136, 229);
        int direction_south = Color.rgb(251, 140, 2);
        int trainColor;
        int radius = 200;
        if (aerialView) {
            radius = 600;
        }
        if (!status.equalsIgnoreCase("R")) {
            trainColor = terminated;
        } else if (direction.equalsIgnoreCase(getString(R.string.northbound))) {
            trainColor = direction_north;
        } else {
            trainColor = direction_south;
        }
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.WHITE)
                .strokeWidth(7)
                .zIndex(1.5f)
                .fillColor(trainColor));// orange
        trainArray.add(circle);

    }

    private void drawMarker(String name, LatLng latLng) {

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng));

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(createStationMarker(name)));
        marker.setTag(name);
        marker.setVisible(false);
        markersList.add(marker);
    }

    /*Code extract by Kamlesh Karwande modified by Shay de Barra from
    * Kamlesh Karwande
    * https://stackoverflow.com/questions/23572583/how-to-show-text-in-drawable-custom-marker
    * */
    private Bitmap createStationMarker(String name) {
        View markerLayout = getLayoutInflater().inflate(R.layout.custom_marker, null);

        ImageView markerImage = markerLayout.findViewById(R.id.marker_image);
        TextView markerName = markerLayout.findViewById(R.id.marker_text);
        if (name.equalsIgnoreCase(my_local_station)) {
            markerImage.setImageResource(R.drawable.map_icon_home);
            markerImage.setColorFilter(Color.MAGENTA);
        } else {
            markerImage.setImageResource(R.drawable.map_icon);

        }
        markerName.setText(name);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }

    private void drawMapPolyLines() {
        String polyline_malahide = getString(R.string.junction_to_malahide);
        String polyline_howth = getString(R.string.greystones_to_howth);

        List<LatLng> route_howth = PolyUtil.decode(polyline_howth);
        List<LatLng> route_malahide = PolyUtil.decode(polyline_malahide);

        poly_howth = mMap.addPolyline(new PolylineOptions().addAll(route_howth).color(Color.GRAY));
        Polyline poly_malahide = mMap.addPolyline(new PolylineOptions().addAll(route_malahide).color(Color.GRAY));
    }

    private void setCameraPosition(LatLng loc) {

 /*       Log.e("", "setCameraPosition: " + loc);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkerArray) {
            //    double longitude = marker.getPosition().longitude
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        */
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLatLng)      // Sets the center of the map
                .zoom(14)                   // Sets the zoom
                .bearing(-65)                // -90 = west, 90 = east
                .tilt(45)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                for (Marker marker : markersList) {

                    marker.setVisible(true);

                }
            }

            @Override
            public void onCancel() {

            }
        });
        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 12));

    }

    private void setMapStyle() {
        if (mMap == null) {// this method being called on orient change before map is ready
            return;
        }
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));// custom json map style created

    }

    private void createOrderedTourList() {
        tourList = new ArrayList<>();
        List<String> full_line_to_howth = Dart.full_line_list_tour;
        for (String stat : full_line_to_howth) {
            for (Marker marker : markersList) {
                if (marker.getTag().equals(stat)) {
                    tourList.add(marker);
                }
            }
        }
    }

    private void initiliseTrainJourney() {
        mMap.getUiSettings().setAllGesturesEnabled(false);// disable any gestures that would interfere with camera movement
        howth_points_list = poly_howth.getPoints();
        createOrderedTourList();
        for (Marker marker : markersList) {
            marker.setVisible(false);
        }
        iterator = tourList.listIterator();
        //    Collections.reverse(markersList);
        animateTrainJourney();
    }

    private void animateTrainJourney() {


        if (iterator.hasNext()) {
            aerialView = false;
            Marker currStat = iterator.next();
            if (marker != null) {
                marker.remove();
            }
            Marker nextStat = currStat;//fallback for last station
            currStat.setVisible(true);

            LatLng currLatLng = new LatLng(currStat.getPosition().latitude, currStat.getPosition().longitude);

            int index = tourList.indexOf(currStat);
            if (index < tourList.size() - 1) {
                nextStat = tourList.get(index + 1);
            }
            nextStat.setVisible(true);
            LatLng nextLatLng = new LatLng(nextStat.getPosition().latitude, nextStat.getPosition().longitude);
            marker = mMap.addMarker(new MarkerOptions()
                    .position(currLatLng));
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.target);
            marker.setIcon(icon);
            float zoomVal = mMap.getCameraPosition().zoom >= 15 ? mMap.getCameraPosition().zoom : 15;
            float bearing = bearingBetweenLatLngs(mMap.getCameraPosition().target, nextLatLng);
            int dist = (int) (distanceBetweenLatLngs(mMap.getCameraPosition().target, nextLatLng) / 1.8);

            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(currLatLng)
                            .tilt(45)
                            .bearing(bearing)
                            .zoom(zoomVal)
                            .build();

            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    dist,
                    new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            animateTrainJourney();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Finished Tour", Toast.LENGTH_LONG).show();
            mMap.getUiSettings().setAllGesturesEnabled(true);// restore touch gestures
        }
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("nothing");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);
        return beginL.bearingTo(endL);
    }

    private float distanceBetweenLatLngs(LatLng begin, LatLng end) {
        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);
        return beginL.distanceTo(endL);
    }

    // back button in toolbar for previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Intent intent = new Intent(MapsActivity.this, MenuActivity.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intent, animation);
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    //get live data on all dart trains currently running
    private void beginLiveTrainDataDownload() {

        GetAllCurrentTrains.placeIdTask asyncTask = new GetAllCurrentTrains.placeIdTask(new GetAllCurrentTrains.AsyncResponse() {
            @Override
            public void processFinish(String latitude, String longitude, String traincode, String status, String message, String direction) {

                //    Log.e("", "processFinish: " + latitude + " : " + longitude + " : " + traincode + " : " + status + " : " + message + " : " + direction);
                Double lat = Double.parseDouble(latitude);
                Double lon = Double.parseDouble(longitude);
                LatLng temp = new LatLng(lat, lon);
                drawTrain(direction, temp, status);
                //     Log.e("dir",""+direction);

            }


        });


        asyncTask.execute();

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        Log.e("onPause", "  timer.cancel();");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.e("onDestroy", "  timer.cancel();");
    }
}
