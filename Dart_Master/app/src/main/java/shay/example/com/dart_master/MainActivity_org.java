package shay.example.com.dart_master;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import shay.example.com.dart_master.helpers.FirebaseHelper;
import shay.example.com.dart_master.helpers.PreferenceHelper;
import shay.example.com.dart_master.models.JourneyObj;

import static shay.example.com.dart_master.SignInActivity.getAppContext;
import static shay.example.com.dart_master.SignInActivity.master;
import static shay.example.com.dart_master.helpers.FirebaseHelper.getZone;


public class MainActivity_org extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "MainActivity";
    private static final String TAG_LIST = "LIST";
    private static final String TAG_REFRESH = "REFRESH";
    private static final String TAG_LOG_OUT = "TAG_LOG_OUT";
    public static FloatingActionMenu actionMenu;
    public final String CONST_STATION_KEY = "station_key";
    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference DB_Ref;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<JourneyObj>> listDataHashMap;
    private ArrayList<String> userSavedStationName;
    private int groupPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // String name = PreferenceHelper.getSharedPreferenceString(getApplicationContext(), Constants.CONST_STATION_NAME, "Sutton");

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert custom logo and station name
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        }
        TextView textView = findViewById(R.id.bar_text);

        userSavedStationName = PreferenceHelper.getSharedPreferenceStringArray(getAppContext(), master.getMasterID(), "");
        textView.setText(userSavedStationName.get(1));// station name

        //  Log.e("KEY"+userSavedStationName.get(0), "NAME"+userSavedStationName.get(1));


        // -------------------------  FAB Button  -----------------------

        ImageView fbIcon = new ImageView(this); // Create an fbIcon;

        fbIcon.setImageResource(R.drawable.fbsettings); // load the settings Icon


        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(fbIcon)
                .build();
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        ImageView itemIcon1 = new ImageView(this);
        itemIcon1.setImageResource(R.drawable.logout);
        SubActionButton button1 = itemBuilder.setContentView(itemIcon1).build();
        button1.setTag(TAG_LOG_OUT);

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.list);
        SubActionButton button2 = itemBuilder.setContentView(itemIcon2).build();
        button2.setTag(TAG_LIST);

        ImageView itemIcon3 = new ImageView(this);
        itemIcon3.setImageResource(R.drawable.fbrefresh);
        SubActionButton button3 = itemBuilder.setContentView(itemIcon3).build();
        button3.setTag(TAG_REFRESH);


        actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .attachTo(actionButton)
                .build();

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        // -------------------------- END OF FAB Button  -----------------------
        // Expandable List View
//https://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/


        // get the listview
        expListView = findViewById(R.id.expListView);

        setUpDataStructures();
        addFirebaseData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataHashMap);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {


                String header_name = listDataHeader.get(groupPosition);
                boolean withChild = listAdapter.getChildrenCount(groupPosition) > 0;
                if (withChild) {
                    for (int i = 0; i < listDataHashMap.get(header_name).size(); i++) {


                        JourneyObj stored_object = listDataHashMap.get(header_name).get(i);

                        if (stored_object == null || stored_object.getFirebaseRef() == null) {
                            // do nothing .. object has just been removed
                            Log.e(TAG, "stored_object NULL");

                        } else {
                            String firebaseRef = stored_object.getFirebaseRef();

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseRef);
                            ref.child("viewed").setValue(true);// we have expanded the view and seen the object
                            Log.e(TAG, "stored_object true");
                        }
                    }
                }
                Toast.makeText(getApplicationContext(),
                        header_name + " Expanded " + listAdapter.getChildrenCount(groupPosition),
                        Toast.LENGTH_SHORT).show();


            }
        });
        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                JourneyObj journeyObj = listDataHashMap.get(listDataHeader.get(groupPosition)).get(childPosition);

                Intent intent = new Intent(getApplicationContext(), ClientActivity.class);
                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                intent.putExtra("obj", journeyObj);
                startActivity(intent, animation);

                MainActivity_org.this.finish();
                return false;
            }
        });


    }

    private void setUpDataStructures(){
        String myStaionName = userSavedStationName.get(1);// my train station where i'm working
        String[] localZonedStations = getZone(myStaionName);// all stations in my zone

        listDataHeader = new ArrayList<>();
        listDataHeader.addAll(Arrays.asList(localZonedStations));

        listDataHashMap = new HashMap<>();

        List<JourneyObj> empty = new ArrayList<>();

        for (String name : listDataHeader) {
            listDataHashMap.put(name, empty);
        }
    }

    private void addFirebaseData() {


        int zone_num = FirebaseHelper.getZoneNumber(userSavedStationName.get(1));
        //   Log.e("zone_num",""+zone_num);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        String zone = "zone_" + zone_num;
        DatabaseReference myZone = database.getReference("Notifications").child(zone);

        myZone.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot stations) {
                Log.e(TAG, "onDataChange Snapshot: " );

                    // clear the data in the HashMap for renewal
                    List<JourneyObj> empty = new ArrayList<>();
                    for (String name : listDataHeader) {
                        listDataHashMap.put(name, empty);
                    }

                for (DataSnapshot station : stations.getChildren()) {

                    String stationName = station.getKey();
                    Log.e(TAG, "onDataChange stationName: " + stationName);
                    List<JourneyObj> data = new ArrayList<>();// new list for every object
                    for (DataSnapshot members : station.getChildren()) {

                        Log.e(TAG, " onDataChange members: empty: "+stationName);
                        JourneyObj journeyObj = members.child("object").getValue(JourneyObj.class);
                        if (journeyObj == null) {
                            Log.e(TAG, " onDataChange Object: null");
                        } else {
                            Log.e(TAG, "onDataChange add Object: " + journeyObj);
                            data.add(journeyObj);
                            listDataHashMap.put(stationName, data);
                        }

                    }

                }
                listAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public void onClick(View v) {
        if (actionMenu.isOpen()) {
            actionMenu.close(true);
        }
        if (v.getTag().equals(TAG_LIST)) {
            Intent intentList = new Intent(MainActivity_org.this, StationsActivity.class);

            //  String lastStationName = PreferenceHelper.getSharedPreferenceString(MainActivity.this, Constants.CONST_STATION_NAME, "Sutton");
            //   String lastStationKey = PreferenceHelper.getSharedPreferenceString(MainActivity.this, Constants.CONST_STATION_KEY, "-L5BhieXdWdqhApQnq6h");

            FirebaseHelper.deleteStaffingDetailsFireBase(userSavedStationName.get(0), userSavedStationName.get(1));

            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intentList, animation);
            MainActivity_org.this.finish();


        } else if (v.getTag().equals(TAG_REFRESH)) {

            Intent intent = new Intent(this, MainActivity_org.class);
            startActivity(intent);
            this.finish();
        } else if (v.getTag().equals(TAG_LOG_OUT)) {
            Intent intentSignOut = new Intent(MainActivity_org.this, SignOutActivity.class);

            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
            startActivity(intentSignOut, animation);
            MainActivity_org.this.finish();

        }

    }

}