package shay.example.com.dart_client;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import shay.example.com.dart_client.helper_classes.Dart;
import shay.example.com.dart_client.helper_classes.FirebaseCommon;
import shay.example.com.dart_client.helper_classes.PreferenceHelper;
import shay.example.com.dart_client.models.Stations;

import static shay.example.com.dart_client.helper_classes.Utilities.ACTIVITY_NAME;
import static shay.example.com.dart_client.helper_classes.Utilities.ACTIVITY_STATIONS;
import static shay.example.com.dart_client.helper_classes.Utilities.FIRST_POS_SELECTED;
import static shay.example.com.dart_client.helper_classes.Utilities.MY_LOCAL_POS;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class StationsActivity extends AppCompatActivity {

    private static final String TAG = "StationsActivity";
    private static boolean home_station_status;
    private static boolean source_selected;
    private static boolean destination_selected;
    private static String source_text;
    private static String destination_text;
    LinearLayoutManager linearLayoutManager;
    private String my_home_station;
    // forward refs for selecting source and destination
    private TextView from;
    private TextView to;
    private ImageButton from_btn;
    private ImageButton to_btn;
    private ImageButton exit_btn;
    private DatabaseReference station_list;
    private RecyclerView recyclerView;

    //    public static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image

        assert actionBar != null;
        actionBar.setCustomView(R.layout.stations_actionbar_layout);// center logo with back arrow layout xml


        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);


        setContentView(R.layout.activity_expand_toolbar);
        //  myLocal =  PreferenceHelper.getSharedPreferenceString(this, MY_LOCAL_STATION, "Bray");
        my_home_station = PreferenceHelper.getSharedPreferenceString(this, "key_local_stat", null);
        if (my_home_station == null) {
            home_station_status = false;// first time user, set home station
            Toast.makeText(this, "Select your local station", Toast.LENGTH_LONG).show();
        } else {
            home_station_status = true;
        }

        from = findViewById(R.id.from_text);
        to = findViewById(R.id.to_text);
        from_btn = findViewById(R.id.from_btn);
        to_btn = findViewById(R.id.to_btn);

        exit_btn = findViewById(R.id.exit_btn);
        resetJourneySelection();// clear the text selection to none for every time onCreate() is called

        ImageButton home_button = findViewById(R.id.home_button);
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int offset = 3;
               int my_position = PreferenceHelper.getSharedPreferenceInt(getApplicationContext(), MY_LOCAL_POS,0);
                LinearLayoutManager layoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
                int totalVisibleItems = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();
                int centeredItemPosition = totalVisibleItems / 2;

                recyclerView.setScrollY(centeredItemPosition );
                recyclerView.smoothScrollToPosition(my_position+offset);

            }
        });
        from_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: FROM");
                to.setVisibility(View.VISIBLE);
                from_btn.setVisibility(View.INVISIBLE);
                source_selected = true;

            }
        });

        to_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: TO");


                to_btn.setVisibility(View.INVISIBLE);
                destination_selected = true;
                startMainActivity();
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from.setVisibility(View.GONE);
                exit_btn.setVisibility(View.GONE);
                Intent intent = new Intent(StationsActivity.this, MenuActivity.class);
                intent.putExtra(ACTIVITY_NAME, ACTIVITY_STATIONS);
                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
                startActivity(intent, animation);
                finish();
            }
        });


        station_list = FirebaseCommon.getStationList();

        recyclerView = findViewById(R.id.mainRecyclerView);



        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);


    }

    private void startMainActivity() {
        // method to check for silly choices i.e. Bayside => Clongriffen
        boolean valid = Dart.getValidJourney(source_text, destination_text);
        if (valid) {


            Intent intentMain = new Intent(StationsActivity.this, JourneySelectionActivity.class);// select a new station
            intentMain.putExtra("source_text", source_text);// will use this in next activity to get station timetable
            intentMain.putExtra("destination_text", destination_text);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
            startActivity(intentMain, animation);
        } else {
            Toast.makeText(getApplicationContext(), "Not a valid journey", Toast.LENGTH_LONG).show();
            resetJourneySelection();
        }

    }

    private void resetJourneySelection() {
        // default reset values for onCreate and error selection
        source_selected = false;
        destination_selected = false;
        to.setVisibility(View.GONE);
        to_btn.setVisibility(View.GONE);
        from.setVisibility(View.GONE);
        from_btn.setVisibility(View.GONE);

    }

    private void gotoPreviousActivity() {
        Intent intent = new Intent(StationsActivity.this, MenuActivity.class);
        Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
        startActivity(intent, animation);
        finish();

    }

    // back button in toolbar for previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            gotoPreviousActivity();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        gotoPreviousActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Stations, myViewHolder> adapter = new FirebaseRecyclerAdapter<Stations, myViewHolder>(


                Stations.class,
                R.layout.station_card,
                myViewHolder.class,
                station_list
        ) {
            @Override
            protected void populateViewHolder(myViewHolder viewHolder, Stations model, int position) {


                viewHolder.setTitle(model.getName());
                viewHolder.setZone(model.getZone_active());
                viewHolder.setManIcon(model.getMan_icon());
                viewHolder.setZoneColor(model.getZone_color());
                viewHolder.station_name = model.getName();

                // pass these values in order to access them
                viewHolder.pos = position;
                viewHolder.from = from;
                viewHolder.to = to;
                viewHolder.from_btn = from_btn;
                viewHolder.to_btn = to_btn;
                viewHolder.exit_btn = exit_btn;
                viewHolder.view_home_station = my_home_station;


            }

        };

        recyclerView.setAdapter(adapter);
        recyclerView.startLayoutAnimation();

        // get the last recycler position and scroll to it // default fallback is 0
        int position = PreferenceHelper.getSharedPreferenceInt(this, FIRST_POS_SELECTED, 0);
        //     if(position==999){
        //        position = PreferenceHelper.getSharedPreferenceInt(this, MY_LOCAL_POS,0);
        //    }
        ///  Log.e("position",""+position);

        recyclerView.smoothScrollToPosition(position);

        /* listen for when recyclerView has finished loading
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                recyclerView.scrollToPosition(20);

            }
        });
*/

    }

    public static class myViewHolder extends RecyclerView.ViewHolder {


        private final View mView;
        //  private DatabaseReference station_list;
        Context mContext;
        private String station_name;
        private int pos;
        private String view_home_station;
        private TextView from;
        private ImageButton from_btn;
        private ImageButton exit_btn;
        private TextView to;
        private ImageButton to_btn;

        public myViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    //     Log.e("last_position",""+last_position);


                    setSelectedStation(station_name, pos);


                    //   Intent intent = new Intent(mContext, JourneySelectionActivity.class);
                    //   Bundle animation = ActivityOptions.makeCustomAnimation(mContext.getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                    //   mContext.startActivity(intent, animation);

                    //  activity.finish();

                }
            });

        }

        // provides the functionality of the dropdown selection From: To:
        private void setSelectedStation(String station_name, int pos) {
            //       linearLayoutManager.offsetChildrenVertical(100);
            if (!home_station_status) {// must have home station set up on initial launch
                Log.e(TAG, "no staion : " + station_name);
                setLocalStation(station_name, pos);
                return;
            }
            if (!source_selected) {// get the origin first
                source_text = station_name;
                String fromTxt = mContext.getString(R.string.from) + source_text;
                from.setText(fromTxt);
                from.setVisibility(View.VISIBLE);
                from_btn.setVisibility(View.VISIBLE);
                PreferenceHelper.setSharedPreferenceInt(mContext, FIRST_POS_SELECTED, pos);// store the origin station position
            } else if (!destination_selected) {// toggle like behaviour on selecting
                destination_text = station_name;
                String destTxt = mContext.getString(R.string.to) + destination_text;
                to.setText(destTxt);
                to.setVisibility(View.VISIBLE);
                to_btn.setVisibility(View.VISIBLE);
            }
        }

        private void setLocalStation(String station_name, int pos) {
            PreferenceHelper.setSharedPreferenceString(mContext, "key_local_stat", station_name);
            PreferenceHelper.setSharedPreferenceInt(mContext, FIRST_POS_SELECTED, pos);
            PreferenceHelper.setSharedPreferenceInt(mContext, MY_LOCAL_POS, pos);
            Toast.makeText(mContext, "Click above to confirm: " + station_name, Toast.LENGTH_LONG).show();
            String locStationTxt = "Local Station: " + station_name;
            from.setText(locStationTxt);
            from.setVisibility(View.VISIBLE);
            exit_btn.setVisibility(View.VISIBLE);
        }


        ///////////////// setters for the model data

        public void setTitle(String title) {
            if (title.equalsIgnoreCase(view_home_station)) {
                ImageView trainImg = mView.findViewById(R.id.stationImg);
                Picasso.with(mContext).load(R.drawable.home).into(trainImg);
            }else{
                ImageView trainImg = mView.findViewById(R.id.stationImg);
                Picasso.with(mContext).load(R.drawable.dart).into(trainImg);
            }
            TextView data_title = mView.findViewById(R.id.titleText);
            data_title.setText(title);
        }

        public void setManIcon(Boolean visible) {
            ImageView man = mView.findViewById(R.id.zone_man);
            if (visible) {
                man.setVisibility(View.VISIBLE);
            } else {
                man.setVisibility(View.INVISIBLE);
            }
        }

        public void setZone(Boolean state) {
            ImageView zone_active_img = mView.findViewById(R.id.zone_active);
            if (state) {
                zone_active_img.setVisibility(View.VISIBLE);

            } else {
                zone_active_img.setVisibility(View.INVISIBLE);
            }
        }

        public void setZoneColor(int clr) {
            ImageView zone_active_img = mView.findViewById(R.id.zone_active);
            zone_active_img.setColorFilter(clr);
        }


    }


}
