package shay.example.com.dart_client;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import shay.example.com.dart_client.helper_classes.Dart;
import shay.example.com.dart_client.helper_classes.DownloadTrainData;
import shay.example.com.dart_client.helper_classes.ImageUtility;
import shay.example.com.dart_client.models.OriginTrain;

public class JourneySelectionActivity_org extends AppCompatActivity {
    private CardView card_view;
    Context ctx;
    int count = 0;
    private OriginTrain originTrain;
    private String my_direction;
    public static OriginTrain selectedObject;
    private ImageView go_btn;

    private View.OnClickListener handleTouch = new View.OnClickListener() {


        @Override
        public void onClick(View v) {

            OriginTrain data = ((OriginTrain) v.getTag());//  get the stored object being clicked upon
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
        card_view = findViewById(R.id.main_card);

        ctx = getApplicationContext();
        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image

        assert actionBar != null;
        actionBar.setCustomView(R.layout.actionbar_layout);// center logo with back arrow layout xml


        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);


        // get the bundled string extras, station origin + destination values
        Intent intent = getIntent();
        final String source_text = intent.getStringExtra("source_text");
        final String destination_text = intent.getStringExtra("destination_text");
        // for testing
        //   source_text = "Dalkey";
        //   destination_text = "Malahide";
        my_direction = Dart.getDirection(source_text, destination_text);
        // get the current times for the origin station
        beginAsyncDownLoad(source_text);
        final TextView origin = findViewById(R.id.originText);
        TextView dest = findViewById(R.id.destText);
        origin.setText(source_text);
        dest.setText(destination_text);

        // go button to accept journey selection
        go_btn = findViewById(R.id.go_btn);
        go_btn.setVisibility(View.VISIBLE);

        go_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();
                String hour = String.valueOf(today.hour);
                String min = String.valueOf(today.minute);
                if(min.length()==1){
                    min = "0" + String.valueOf(today.minute);
                }
                String current_time = ( hour + " : "+min);
                String issued = getString(R.string.issued_at)+current_time;
                TextView issuedField = findViewById(R.id.current);
                issuedField.setText(issued);

                Intent intent = new Intent(JourneySelectionActivity_org.this, BeginJourney.class);
                intent.putExtra("source_text", source_text);// will use this in next activity to get station timetable
                intent.putExtra("destination_text", destination_text);

                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                go_btn.setVisibility(View.INVISIBLE);


                Bitmap bitmap = loadBitmapFromView(card_view);

               ImageUtility.setImage(bitmap);// save the image

                startActivity(intent, animation);

            }
        });

    }

    // populate the cardView data with clicked on object (originTrain)
    private void populateCard(OriginTrain data) {
        TextView trainId = findViewById(R.id.train_id);
        trainId.setText(data.getTraincode());
        String scheduleText = getString(R.string.sch_arr) + data.getScharrival();
        TextView schArr = findViewById(R.id.schArr);
        schArr.setText(scheduleText);
        String expText = getString(R.string.exp_arr) + data.getExparrival();
        TextView expArr = findViewById(R.id.expArr);
        expArr.setText(expText);
        String currentLocation = ((data.getLast_location().equals("{}")) ? getString(R.string.no_further_info) : data.getLast_location());// tenary operators for a cleaner output
        TextView current = findViewById(R.id.current);
        current.setText(currentLocation);
        String originTxt = getString(R.string.origin) + data.getOrigin();
        TextView origin = findViewById(R.id.origin);
        origin.setText(originTxt);
        String destTxt = getString(R.string.dest) + data.getDestination();
        TextView dest = findViewById(R.id.destination);
        dest.setText(destTxt);
        String dueTxt = getString(R.string.due) + data.getDuein();
        TextView due = findViewById(R.id.dueIn);
        due.setText(dueTxt);

        ImageView imageView = findViewById(R.id.logo);
        if (data.getTrain_type().equalsIgnoreCase("DART")) {
            Picasso.with(getCtx()).load(R.drawable.dart).into(imageView);
        } else {
            Picasso.with(getCtx()).load(R.drawable.ie_sm).into(imageView);
        }
    }




    private void beginAsyncDownLoad(String station) {

        DownloadTrainData.placeIdTask asyncTask = new DownloadTrainData.placeIdTask(new DownloadTrainData.AsyncResponse() {
            @Override
            public void processFinish(String traincode, String origin, String destination, String status, String direction, String scharrival, String exparrival, String duein, String late, String last_location) {
                originTrain = new OriginTrain(traincode, origin, destination, status, direction, scharrival, exparrival, duein, late, last_location);

                drawTimetableView(originTrain);
                selectedObject = originTrain;
            }


        });


        asyncTask.execute(station);

    }

    private void drawTimetableView(OriginTrain originTrain) {
        {
            //  Log.e("ALL Status",""+status);
            if (my_direction.equalsIgnoreCase(originTrain.getDirection())) {
                //  Log.e("drawTimetableView ", "Late: " + originTrain.getOrg_late() + " : dir " + direction + " : dest: " + destination + " CODE: " + traincode);
                populateCard(originTrain);
                //     Log.e("status",""+status);

                // get reference to the main table and header from xml
                TableLayout tableLayout = findViewById(R.id.table_content);

                // Create the table row dynamically (dependant on JSON object length)
                TableRow table_row = new TableRow(this);
                table_row.setTag(originTrain);// pass the object to the click method for populating the cardView
                table_row.setOnClickListener(handleTouch);
                if (count % 2 != 0) table_row.setBackgroundColor(Color.GRAY);
                table_row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                // Create a TextView to info
                TextView exp_label = new TextView(this);
                exp_label.setText(originTrain.getExparrival());
                exp_label.setPadding(25, 25, 25, 25);
                exp_label.setTextColor(Color.WHITE);
                table_row.addView(exp_label);

                TextView due_label = new TextView(this);
                due_label.setText(originTrain.getDuein());
                due_label.setTextColor(Color.WHITE);
                table_row.addView(due_label);


                TextView lateT = new TextView(this);
                lateT.setText(originTrain.getLate());
                lateT.setTextColor(Color.WHITE);
                table_row.addView(lateT);


                TextView destined = new TextView(this);
                destined.setText(originTrain.getDestination());
                destined.setTextColor(Color.WHITE);
                table_row.addView(destined);
// finally add this to the table row
                tableLayout.addView(table_row, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
                count++;

            }


        }
    }

    // back button in toolbar for previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Intent intent = new Intent(JourneySelectionActivity_org.this, StationsActivity.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in_left, R.transition.slide_out_left).toBundle();
            startActivity(intent, animation);
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
    @Override
    public void onBackPressed() {

    }
    @Override
    protected void onResume() {
        Log.e("", "onResume: " );
        super.onResume();
    }
}