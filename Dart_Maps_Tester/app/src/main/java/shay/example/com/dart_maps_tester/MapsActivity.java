package shay.example.com.dart_maps_tester;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    DatabaseReference active_node;
    private GoogleMap mMap;
    private FirebaseDatabase database;
    private DatabaseReference ViewsNode;
    private boolean nodeExists;
    private List<Marker> markersList = new ArrayList<>();
    private List<LatLng> route_howth;
    private List<LatLng> route_malahide;

    Map<String, LatLng> station_value_map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        database = FirebaseDatabase.getInstance();
        ViewsNode = database.getReference("Notifications").child("Views");
        nodeExists = false;
    }


    private void setMapStyle() {
        if (mMap == null) {// this method being called on orient change before map is ready
            return;
        }
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));// custom json map style created

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMapStyle();

        drawMapPolyLines();
        getStationsDataForMap();
        getFirebaseActiveNode(ViewsNode);

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
          //      Log.e("My", "onMarkerClick");
                return false;
            }
        });


        LatLng pos = new LatLng(53.29637216139503, -6.165657564997674);
        CameraPosition INIT =
                new CameraPosition.Builder()
                        .target(pos)
                        .zoom(12F)
                        .bearing(290F) // orientation
                        .tilt(0F) // viewing angle
                        .build();
        // use GoogleMap mMap to move camera into position
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));

        setMarker(pos);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub
                //  Log.e("System out", "onMarkerDragStart..." + marker.getPosition().latitude + "..." + marker.getPosition().longitude);
                marker.setAlpha(0.5f);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub
         //       Log.e("System out", "onMarkerDragEnd..." + marker.getPosition().latitude + "..." + marker.getPosition().longitude);
                marker.setAlpha(1);
                String key = marker.getTitle();// the firebase reference is stored in the marker title
                //     updateFireBaseChildPos(key,marker.getPosition().latitude,marker.getPosition().longitude);// update firebase with the relevant new position

                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition())); // move cam to frame the view
                if (nodeExists) {
                    active_node.child("location").child("latitude").setValue(marker.getPosition().latitude);
                    active_node.child("location").child("longitude").setValue(marker.getPosition().longitude);
                    getMarkerPositionResult(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub
         //       Log.e("System out", "onMarkerDrag...");
            }
        });
    }
    public boolean getIsOnRailTrack(LatLng point) {
        double tolerance = 10.0;//  tolerance in meters
        boolean result_malahide = PolyUtil.isLocationOnPath(point, route_malahide, true,tolerance);
        boolean result_howth = PolyUtil.isLocationOnPath(point, route_howth, true,tolerance);
        Log.e("getIsOnRailTrack", "" );
        return result_howth || result_malahide;
    }

    private void getMarkerPositionResult(LatLng position) {
      boolean  result = getIsOnRailTrack(position);
        Log.e("getMarkerPositionResult", "getMarkerPositionResult: "+ result );

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


    private void setMarker(LatLng pos) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(pos)
                //   .title(dataSnapshot.getKey())
                .draggable(true);// draggable part required
        Marker marker = mMap.addMarker(markerOptions);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon));// show the custom markers on map


    }

    private DatabaseReference getFirebaseActiveNode(DatabaseReference ViewsNode) {
        final DatabaseReference[] reference = {null};
  //      Log.e("getFirebaseActiveNode", "getFirebaseActiveNode: " + ViewsNode);
        ViewsNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
         //       Log.e("DataSnapshot", "dataSnapshot: " + dataSnapshot);
                for (DataSnapshot childRef : dataSnapshot.getChildren()) {
            //        Log.e("DataSnapshot", "onDataChange: " + childRef);
                    if (childRef.child("locationRequested").exists()) {
                        boolean result = (boolean) childRef.child("locationRequested").getValue();
                        if (result) {
                            nodeExists = true;
                            childRef.child("locationResponse").getRef().setValue(true);
                         //   Log.e("locationRequested", "TRUE: ");
                            reference[0] = childRef.getRef();
                            active_node = childRef.getRef();
                            return;
                        } else {
                            nodeExists = false;
                        //    Log.e("locationRequested", "False: ");
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return reference[0];
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.e("onMapClick", "");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e("onMarkerClick", "");
        return false;
    }


    private void drawMapPolyLines() {
        String polyline_malahide = getString(R.string.junction_to_malahide);
        String polyline_howth = getString(R.string.greystones_to_howth);

        route_howth = PolyUtil.decode(polyline_howth);
        route_malahide = PolyUtil.decode(polyline_malahide);

        mMap.addPolyline(new PolylineOptions().addAll(route_howth).color(Color.GRAY));
        mMap.addPolyline(new PolylineOptions().addAll(route_malahide).color(Color.GRAY));
    }

    private void getStationsDataForMap() {

        // get the firebase stations list
        DatabaseReference station_list = database.getReference("Station_List");
        station_list.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    String name = (String) childSnap.child("name").getValue();
                    long zone_number = (long) childSnap.child("zone_number").getValue();
                    Double latitude = childSnap.child("latitude").getValue(Double.class);
                    Double longitude = childSnap.child("longitude").getValue(Double.class);

                    LatLng latLng = new LatLng(latitude, longitude);
                    drawMarker(name, latLng);
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void drawMarker(String name, LatLng latLng) {

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng));

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(name)));
        marker.setTag(name);
        marker.setVisible(true);
        station_value_map.put(name,latLng);
    }

    /*Code extract by Kamlesh Karwande modified by Shay de Barra from
     * Kamlesh Karwande
     * https://stackoverflow.com/questions/23572583/how-to-show-text-in-drawable-custom-marker
     * */
    private Bitmap createStoreMarker(String name) {
        View markerLayout = getLayoutInflater().inflate(R.layout.custom_marker, null);

        ImageView markerImage = markerLayout.findViewById(R.id.marker_image);
        TextView markerName = markerLayout.findViewById(R.id.marker_text);

            markerImage.setImageResource(R.drawable.station_icon);

        markerName.setText(name);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }


}
