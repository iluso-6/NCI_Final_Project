package shay.example.com.dart_client;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import shay.example.com.dart_client.helper_classes.PreferenceHelper;
import shay.example.com.dart_client.helper_classes.Utilities;


import static android.media.MediaPlayer.create;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class MenuActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    Activity activity = MenuActivity.this;
    String permission_read_phone = android.Manifest.permission.READ_PHONE_STATE;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuContactNumber:
                showPhoneInputDialog();
                return true;
            case R.id.menuLocalStation:
                PreferenceHelper.setSharedPreferenceString(getApplicationContext(), "key_local_stat", null);
                startActivity(new Intent(this, StationsActivity.class));
                return true;
            case R.id.menuLogout:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void signOut() {
        SignInActivity.mGoogleApiClient.connect();
        MediaPlayer mediaPlayer = create(this, R.raw.intro);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                SignInActivity.signOutFromGoogle();
                finish();
            }
        });
        mediaPlayer.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image

        actionBar.setCustomView(R.layout.actionbar_menu_layout);// center logo layout xml


        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);

        Button map = findViewById(R.id.map_btn);

        String stored_phone_number = PreferenceHelper.getSharedPreferenceString(this, Utilities.CONST_USER_PHONE, null);
        boolean isEmpty = (TextUtils.isEmpty(stored_phone_number));
        getCustomerPhoneNum();
        if (!isEmpty) {
            if (stored_phone_number.length() > 8) {// we have a phone number
             //   user.setPhone_num(stored_phone_number);
             //   UserSingleton.getInstance().setPhone_num(stored_phone_number);
                PreferenceHelper.setSharedPreferenceString(this,"user_phone",stored_phone_number);
            } else {
                getCustomerPhoneNum();
            }
        }
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMap = new Intent(MenuActivity.this, MapsActivity.class);// select a new station
                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                startActivity(intentMap, animation);
            }
        });

        Button journey = findViewById(R.id.select);
        journey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentJourney = new Intent(MenuActivity.this, StationsActivity.class);// select a new station
                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                startActivity(intentJourney, animation);
            }
        });

        Button favourite_journeys = findViewById(R.id.favourite_btn);
        favourite_journeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentJourney = new Intent(MenuActivity.this, FavouriteJourneys.class);// select a new station
                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                startActivity(intentJourney, animation);
            }
        });

    }


    // this custom dialog will personalise the permissions request prior to calling them ie. you need permission to ...
    private void showPhoneInputDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.phone_dialog);
        dialog.setCancelable(false);
        Button cancel_btn = dialog.findViewById(R.id.cancel_action);
        Button ok_btn = dialog.findViewById(R.id.button_ok);
        String phone_num = PreferenceHelper.getSharedPreferenceString(dialog.getContext(), Utilities.CONST_USER_PHONE, null);
        final EditText phone_text = dialog.findViewById(R.id.phone_num_text);
        assert phone_num != null;
        if (phone_num.length() > 8) {
            phone_text.setText(phone_num);// put in existing number if we already have it
        }
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_number = String.valueOf(phone_text.getText());
                String result = phone_number.trim();
                PreferenceHelper.setSharedPreferenceString(dialog.getContext(), Utilities.CONST_USER_PHONE, result);
                Toast.makeText(dialog.getContext(), "Contact number successfully set", Toast.LENGTH_LONG).show();
                dialog.dismiss();

            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onBackPressed() {
        // clear all activit
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    private void getCustomerPhoneNum() {
        if (!checkPermission(permission_read_phone)) {
            Log.e("getCustomerPhoneNum", "getCustomerPhoneNum: " );
            showPhonePermissionsDialog();
        } else {

            Log.e("else", "getCustomerPhoneNum: "+   getPhone());

         //   user.setPhone_num(getPhone());/// flag this for review  xXXXXXXXXXXXXXXXX

        }
    }

    // returns the string phone number from system
    private String getPhone() {

        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, permission_read_phone) != PackageManager.PERMISSION_GRANTED) {
            return "Not permitted";
        }
    //    Log.e("", "getPhone: " + phoneMgr.getLine1Number());
        String phone_number = phoneMgr.getLine1Number() ==null ? "private" : phoneMgr.getLine1Number() ;
        return phone_number;
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Toast.makeText(activity, "Irish Rail may require your phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    String phone_num = getPhone();
               //     user.setPhone_num(phone_num);
                    PreferenceHelper.setSharedPreferenceString(getApplicationContext(), Utilities.CONST_USER_PHONE, phone_num);
                  // now proceed with whatever
                    showPhoneInputDialog();
                } else {
                    Toast.makeText(activity, "Permission Denied. Irish Rail cannot contact you", Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    private boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    // this custom dialog will personalise the permissions request prior to calling them ie. you need permission to ...
    private void showPhonePermissionsDialog() {


            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.phone_permissions_dialog);
            dialog.setTitle("Phone Permissions");
            dialog.setCancelable(false);
            Button ok = dialog.findViewById(R.id.button_ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    requestPermission(permission_read_phone);
                    // start the actual permissions request
                }
            });


            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        }

    }

