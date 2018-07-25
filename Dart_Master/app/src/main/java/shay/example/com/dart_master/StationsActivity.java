package shay.example.com.dart_master;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import shay.example.com.dart_master.helpers.FirebaseHelper;
import shay.example.com.dart_master.helpers.PreferenceHelper;
import shay.example.com.dart_master.models.Stations;

import static shay.example.com.dart_master.SignInActivity.master;
/**
 * Created by Shay de Barra on 25,March,2018
 * Email:  x16115864@student.ncirl.ie
 */
public class StationsActivity extends AppCompatActivity {

    private static final String TAG = "StationsActivity";
    LinearLayoutManager linearLayoutManager;

    private DatabaseReference station_list;
    private RecyclerView recyclerView;
    public static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stations_recycler_view);

         activity = this;// reference to be used in a static context

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert switch
        //  actionBar.setCustomView(R.layout.actionbar_layout);
        actionBar.setIcon(R.drawable.ie_logo);// display custom icon in toolbar

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        final FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();


        station_list = database.getReference("Station_List");

        recyclerView = findViewById(R.id.mainRecyclerView);


        //   recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onBackPressed() {

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
                viewHolder.station_name = model.getName();
                viewHolder.station_list = getRef(position);

            }

        };

        recyclerView.setAdapter(adapter);

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
        private DatabaseReference station_list;
        Context mContext;
        private String station_name;


        public myViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext = itemView.getContext();
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ArrayList<String> userLoggedInDetails = new ArrayList<>();

                    userLoggedInDetails.add(station_list.getKey());
                    userLoggedInDetails.add(station_name);

                    // store the last key and station name under the logged in userID
                    PreferenceHelper.setSharedPreferenceStringArray(mContext,master.getMasterID(),userLoggedInDetails);


               //     PreferenceHelper.setSharedPreferenceString(mContext, Constants.CONST_STATION_NAME, station_name);
               //     PreferenceHelper.setSharedPreferenceString(mContext, Constants.CONST_STATION_KEY, station_list.getKey());


                    Log.e("station_list",""+station_list.getKey());
                    FirebaseHelper.setStaffingDetailsFireBase(station_list.getKey(),station_name);
                    Intent intent = new Intent(mContext, MainActivity.class);
                    Bundle animation = ActivityOptions.makeCustomAnimation(mContext.getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                    mContext.startActivity(intent, animation);

                    activity.finish();

                }
            });

        }



        public void setTitle(String title) {
            TextView data_title = mView.findViewById(R.id.cardTitle);
            data_title.setText(title);
        }


    }


}
