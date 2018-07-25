package shay.example.com.dart_client;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import shay.example.com.dart_client.helper_classes.Dart;
import shay.example.com.dart_client.helper_classes.DownloadTrainData;
import shay.example.com.dart_client.helper_classes.ImageUtility;
import shay.example.com.dart_client.helper_classes.Utilities;
import shay.example.com.dart_client.models.DestTrain;
import shay.example.com.dart_client.models.JourneyObj;
import shay.example.com.dart_client.models.OriginTrain;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class JourneySelectionActivity extends AppCompatActivity {

    private String callingActivity;
    public static JourneyObj selectedObject;
    public OriginTrain originTrain;
    public DestTrain destTrain;
    Context ctx;
    int count = 0;
    // used to compare trains at both stations to find a common list of trains used to create JourneyObj (merged class of both attributes)
    List<OriginTrain> originList = new ArrayList<>();
    List<DestTrain> destList = new ArrayList<>();

    private CardView card_view;
    private String my_direction;
    private ImageView go_btn;
    private ProgressBar progressBar;
    private View.OnClickListener handleTouch = new View.OnClickListener() {


        @Override
        public void onClick(View v) {

            JourneyObj data = ((JourneyObj) v.getTag());//  get the stored object being clicked upon
            //   Log.e("data", "onClick: "+data );
            populateCard(data);
            selectedObject = data;
        }
    };


    // grab a screen shot (bitmap) of the CardView originTrain for posterity
    public static Bitmap loadBitmapFromView(View v) {

        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }


    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journey_selection_activity);
        card_view = findViewById(R.id.main_card_final);

        ctx = getApplicationContext();
        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image

        assert actionBar != null;
        actionBar.setCustomView(R.layout.actionbar_layout);// center logo with back arrow layout xml


        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);

        // get the bundled string extras, station origin + destination values
        Intent intent = getIntent();
        final String source_text = intent.getStringExtra("source_text");
        final String destination_text = intent.getStringExtra("destination_text");
        callingActivity = intent.getStringExtra(Utilities.ACTIVITY_NAME);


        my_direction = Dart.getDirection(source_text, destination_text);
        // get the current times for the origin station and latterly the destination both Async Tasks with call back
        beginOriginAsyncDownLoad(source_text, destination_text);
        final TextView origin = card_view.findViewById(R.id.originTicket);
        TextView dest = card_view.findViewById(R.id.destTicket);
        origin.setText(source_text);
        dest.setText(destination_text);

        // go button to accept journey selection
        go_btn = findViewById(R.id.go_btn);
     


        go_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));

                String hour = String.valueOf((c.get(Calendar.HOUR)) + 12);// 24 hour clock
                String min = String.valueOf(c.get(Calendar.MINUTE));
                if (min.length() == 1) {
                    min = "0" + String.valueOf(c.get(Calendar.MINUTE));
                }


                String current_time = (hour + " : " + min);
                String issued = getString(R.string.issued_at) + current_time;
                TextView issuedField = findViewById(R.id.current);
                issuedField.setText(issued);

                Intent intent = new Intent(JourneySelectionActivity.this, BeginJourney.class);
                intent.putExtra("source_text", source_text);// will use this in next activity to get station timetable
                intent.putExtra("destination_text", destination_text);
                // return option to the calling activity in the next ...
                intent.putExtra(Utilities.ACTIVITY_NAME,callingActivity);

                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                go_btn.setVisibility(View.INVISIBLE);


                Bitmap bitmap = loadBitmapFromView(card_view);
                ImageUtility.setImage(bitmap);// save the image

                startActivity(intent, animation);

            }
        });

    }

    // populate the cardView data with clicked on object ( journeyObj )
    private void populateCard(JourneyObj data) {
        selectedObject = data;
        TextView trainId = findViewById(R.id.train_id);
        trainId.setText(data.getOrg_traincode());
        String scheduleText = getString(R.string.sch_arr) + data.getOrg_scharrival();
        TextView schArr = findViewById(R.id.schArr);
        schArr.setText(scheduleText);
        String expText = getString(R.string.exp_arr) + data.getOrg_exparrival();
        TextView expArr = findViewById(R.id.expArr);
        expArr.setText(expText);
        String currentLocation = ((data.getOrg_last_location().equals("{}")) ? getString(R.string.no_further_info) : data.getOrg_last_location());// tenary operators for a cleaner output
        TextView current = findViewById(R.id.current);
        current.setText(currentLocation);
        String originTxt = getString(R.string.origin) + data.getOrg_origin();
        TextView origin = findViewById(R.id.origin);
        origin.setText(originTxt);
        String destTxt = getString(R.string.dest) + data.getOrg_destination();
        TextView dest = findViewById(R.id.destination);
        dest.setText(destTxt);
        String dueTxt = getString(R.string.due) + data.getOrg_duein();
        TextView due = findViewById(R.id.dueIn);
        due.setText(dueTxt);
        String lateTxt = getString(R.string.late) + data.getOrg_late();
        TextView late = findViewById(R.id.late);
        late.setText(lateTxt);
        String dest_scheduleText = getString(R.string.sch_dest) + data.getDest_scharrival();
        TextView dest_schArr = findViewById(R.id.dest_schArr);
        dest_schArr.setText(dest_scheduleText);
        String dest_expText = getString(R.string.exp_dest) + data.getDest_exparrival();
        TextView dest_expArr = findViewById(R.id.dest_expArr);
        dest_expArr.setText(dest_expText);
        ImageView imageView = findViewById(R.id.logo);
        if (data.getOrg_train_type().equalsIgnoreCase("DART")) {
            Picasso.with(getCtx()).load(R.drawable.dart).into(imageView);
        } else {
            Picasso.with(getCtx()).load(R.drawable.ie_sm).into(imageView);
        }
    }


    private void beginOriginAsyncDownLoad(String origin_station, final String dest_station) {

        DownloadTrainData.placeIdTask asyncTask = new DownloadTrainData.placeIdTask(new DownloadTrainData.AsyncResponse() {
            @Override
            public void processFinish(String traincode, String origin, String destination, String status, String direction, String scharrival, String exparrival, String duein, String late, String last_location) {
                originTrain = new OriginTrain(traincode, origin, destination, status, direction, scharrival, exparrival, duein, late, last_location);
                originList.add(originTrain);
            }


        });

        // callbackOnPermissionsResult interface when finished with origin trains download
        asyncTask.setCallbackListener(new DownloadTrainData.placeIdTask.MyCallbackListener() {
            @Override
            public void myCallback(int arraylength) {
                Log.e("OriginAsyncDownLoad", " arraylength : " + arraylength);
                if (arraylength > 0) {
                    beginDestinationAsyncDownLoad(dest_station);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    TextView info = findViewById(R.id.no_train_info);
                    info.setVisibility(View.VISIBLE);
                    Log.e("NO TRAINS INFORMATION", "onUpdate: ");

                }

            }


        });

        asyncTask.execute(origin_station);

    }

    private void beginDestinationAsyncDownLoad(String dest_station) {

        DownloadTrainData.placeIdTask asyncTask = new DownloadTrainData.placeIdTask(new DownloadTrainData.AsyncResponse() {
            @Override
            public void processFinish(String traincode, String origin, String destination, String status, String direction, String scharrival, String exparrival, String duein, String late, String last_location) {
                destTrain = new DestTrain(traincode, origin, destination, status, direction, scharrival, exparrival, duein, late, last_location);
                destList.add(destTrain);
            }


        });

        // callbackOnPermissionsResult interface when finished all downloads
        asyncTask.setCallbackListener(new DownloadTrainData.placeIdTask.MyCallbackListener() {
            @Override
            public void myCallback(int obj) {
                Log.e("DestinationDownLoad", " arraylength : " + obj);
                getMatchingTrains();
                progressBar.setVisibility(View.INVISIBLE);
                go_btn.setVisibility(View.VISIBLE);
            }


        });

        asyncTask.execute(dest_station);

    }

    // match both objects and create a merged "journeyObj" of common attributes
    private void getMatchingTrains() {

        for (OriginTrain originTrain : originList) {
            for (DestTrain destTrain : destList) {
                if (originTrain.getTraincode().equalsIgnoreCase(destTrain.getTraincode())) {
                    Log.e("match" + originTrain.getScharrival(), "getMatchingTrains: " + destTrain.getScharrival());

                    JourneyObj journeyObj =
                            new JourneyObj(originTrain.getTraincode(), originTrain.getOrigin(), originTrain.getDestination(), originTrain.getTrain_type(), originTrain.getDirection(), originTrain.getScharrival(), originTrain.getExparrival(), originTrain.getDuein(), originTrain.getLate(), originTrain.getLast_location(), destTrain.getTraincode(), destTrain.getOrigin(), destTrain.getDestination(), destTrain.getTrain_type(), destTrain.getDirection(), destTrain.getScharrival(), destTrain.getExparrival(), destTrain.getDuein(), destTrain.getLate(), destTrain.getLast_location());
                    drawTimetableView(journeyObj);
                }

            }

        }

    }

    private void drawTimetableView(JourneyObj journeyObj) {
        {
            int white = getResources().getColor(R.color.off_white);
            int light_grey = getResources().getColor(R.color.light_grey);
            //  Log.e("ALL Status",""+status);
            if (my_direction.equalsIgnoreCase(journeyObj.getOrg_direction())) {
                //  Log.e("drawTimetableView ", "Late: " + originTrain.getOrg_late() + " : dir " + direction + " : dest: " + destination + " CODE: " + traincode);
                populateCard(journeyObj);
                //     Log.e("status",""+status);

                // get reference to the main table and header from xml
                TableLayout tableLayout = findViewById(R.id.table_content);

                // Create the table row dynamically (dependant on JSON object length)
                TableRow table_row = new TableRow(this);
                table_row.setTag(journeyObj);// pass the object to the click method for populating the cardView
                table_row.setOnClickListener(handleTouch);
                if (count % 2 != 0) table_row.setBackgroundColor(light_grey);
                table_row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                // Create a TextView to info
                TextView exp_label = new TextView(this);
                exp_label.setText(journeyObj.getOrg_exparrival());
                exp_label.setPadding(25, 25, 55, 25);
                exp_label.setTextColor(white);
                table_row.addView(exp_label);

                TextView due_label = new TextView(this);
                due_label.setText(journeyObj.getOrg_duein());
                due_label.setTextColor(white);
                table_row.addView(due_label);


                TextView lateT = new TextView(this);
                lateT.setText(journeyObj.getOrg_late());
                lateT.setTextColor(white);
                table_row.addView(lateT);


                TextView destined = new TextView(this);

                destined.setText(journeyObj.getOrg_destination());
                destined.setTextColor(white);
                table_row.addView(destined);
// finally add this to the table row
                tableLayout.addView(table_row, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.MATCH_PARENT));
                count++;

            }else{
                Log.e("my_direction "+my_direction, "drawTimetableView: "+journeyObj.getOrg_direction() );
            }


        }
    }

    private void gotoPreviousActivity() {
        if(Objects.equals(callingActivity, Utilities.ACTIVITY_FAVOURITE)){
            Intent intent = new Intent(JourneySelectionActivity.this, FavouriteJourneys.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intent, animation);
            finish();
        }else {
            Intent intent = new Intent(JourneySelectionActivity.this, StationsActivity.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intent, animation);
            finish();
        }
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
    protected void onResume() {
        Log.e("", "onResume: ");
        super.onResume();
    }
}