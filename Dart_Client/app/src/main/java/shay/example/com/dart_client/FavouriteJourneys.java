package shay.example.com.dart_client;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.List;

import shay.example.com.dart_client.helper_classes.FavSQLiteStorage;
import shay.example.com.dart_client.helper_classes.Utilities;
import shay.example.com.dart_client.models.Favourite;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class FavouriteJourneys extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout parent;
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_journeys);

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image
        actionBar.setCustomView(R.layout.actionbar_layout);// center logo layout xml
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
      //  setSharedPreferenceInt(this, TOTAL_FAVOURITE_JOURNEYS, 1); testing
        mInflater = LayoutInflater.from(this);
        parent = findViewById(R.id.parent);

        FavSQLiteStorage db = new FavSQLiteStorage(FavouriteJourneys.this);
        List<Favourite> favouriteList = db.getAllMyFavourites();

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 60;

        for (Favourite item : favouriteList) {
            Log.e("ITEM", "onCreate: "+ item );

            View btn = mInflater.inflate(R.layout.button_overlay, null, false);
            btn.setLayoutParams(layoutParams);

            TextView origin = btn.findViewById(R.id.firstTextView);
            TextView dest = btn.findViewById(R.id.secondTextView);

            origin.setText(item.getOrigin_name());
            dest.setText(item.getDest_name());
            btn.setOnClickListener(this);

            parent.addView(btn);
        }

     //   int number_of_saved_journeys = getSharedPreferenceInt(this, Utilities.TOTAL_FAVOURITE_JOURNEYS, MIN_FAV_JOURNEYS);

        /*
        for (int i = MIN_FAV_JOURNEYS; i < number_of_saved_journeys; i++) {

            String key = String.valueOf(i);
            ArrayList<String> fav = getSharedPreferenceStringArray(this, key, "");

            View btn = mInflater.inflate(R.layout.button_overlay, null, false);
            btn.setLayoutParams(layoutParams);

            TextView origin = btn.findViewById(R.id.firstTextView);
            TextView dest = btn.findViewById(R.id.secondTextView);

            origin.setText(fav.get(0));
            dest.setText(fav.get(1));
            btn.setOnClickListener(this);

            parent.addView(btn);
        }
*/
    }

    @Override
    public void onClick(View v) {
        // get the relevant text fields
        TextView origin = v.findViewById(R.id.firstTextView);
        TextView dest = v.findViewById(R.id.secondTextView);
        String originText = origin.getText() + "";
        String destText = dest.getText() + "";

        Intent intentMain = new Intent(FavouriteJourneys.this, JourneySelectionActivity.class);// select a new station
        intentMain.putExtra("source_text", originText);// will use this in next activity to get station timetable
        intentMain.putExtra("destination_text", destText);
        intentMain.putExtra(Utilities.ACTIVITY_NAME,Utilities.ACTIVITY_FAVOURITE);

        Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
        startActivity(intentMain, animation);
        Log.e("Origin: "+originText, "Destination: "+destText );
    }


    private void gotoPreviousActivity(){
        Intent intent = new Intent(FavouriteJourneys.this, MenuActivity.class);
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
}
