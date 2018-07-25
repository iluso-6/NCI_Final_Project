package shay.example.com.dart_master;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import shay.example.com.dart_master.helpers.FirebaseHelper;
import shay.example.com.dart_master.helpers.PreferenceHelper;
import shay.example.com.dart_master.models.Master;

/**
 * Created by Shay de Barra on 02,January,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity Log:";

    public static GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static MediaPlayer mp;

    public static Master master;
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        // if being called by the onBackPressed in MainActivity
        if (getIntent().getBooleanExtra("EXIT", false)) {
            Log.e(TAG, "EXIT: " );

            finish();
        }

        SignInActivity.context = getApplicationContext();
        Log.e(TAG, "onCreate: ");
        mp = MediaPlayer.create(this, R.raw.accept_1);
        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image
        assert actionBar != null;
        actionBar.setIcon(R.drawable.ie_logo);// display custom icon in toolbar

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        // my google sign in button
        signInButton =  findViewById(R.id.googleSignInBtn);


      //  PreferenceHelper.setSharedPreferenceString(this,Constants.CONST_STATION_NAME,null);

        mAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    // user is already signed in
                    signInButton.setEnabled(false);

                   // changeAccountBtn.setVisibility(View.VISIBLE);

                   /* nextBtn.setVisibility(View.VISIBLE);
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeAccountBtn.setEnabled(false); // disable this for safety
                            // the user is requesting to proceeed
                            mp.start();
                        }
                    });*/
                    updateUserImageDetails();
                    mp.start();



                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {



                          //  String lastStationKey = PreferenceHelper.getSharedPreferenceString(getAppContext(), Constants.CONST_STATION_KEY, Constants.DEFAULT_STATION_KEY);
                          //  String lastStationName = PreferenceHelper.getSharedPreferenceString(getAppContext(), Constants.CONST_STATION_NAME, Constants.CONST_STATION_NAME);

                            ArrayList<String> userSettings;// = new ArrayList<>();

                            // Get the user last location for their ID
                            userSettings = PreferenceHelper.getSharedPreferenceStringArray(getAppContext(),master.getMasterID(),null);

                            Intent intentMain;
                            if(userSettings !=null) {
                                // Update Firebase with key , station name details
                                FirebaseHelper.setStaffingDetailsFireBase(userSettings.get(0),userSettings.get(1));// sign in the previous details
                                intentMain = new Intent(SignInActivity.this, MainActivity.class);// go to last station

                            } else{
                                intentMain = new Intent(SignInActivity.this, StationsActivity.class);// select a new station
                            }

                            //overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                            // slide animation between activities in Bundle - more reliable
                       //     populateFirebase();
                            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                            startActivity(intentMain, animation);
                            mp.release();

                            SignInActivity.this.finish();
                        }
                    });


                }
            }
        };




        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        //access the google play services
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(getApplicationContext(), "onConnectionFailed: " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                //  .addApi(LocationServices.API)
                .build();// build the sign in options dialog


        //  AppCommon inst = new AppCommon();
        //   inst.setClient(mGoogleApiClient);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();// start the sign in intent

                Log.e("onClick", "Sign in");
            }
        });

    }
    public static Context getAppContext() {
        return SignInActivity.context;
    }
// method to sign out accessible from other activities
    public static void signOutFromGoogle() {


     //   FirebaseHelper.removeAttendeeFirebase(getAppContext());// delete staff details in database
    //    String name = PreferenceHelper.getSharedPreferenceString(getAppContext(), Constants.CONST_STATION_NAME, Constants.DEFAULT_STATION_NAME);
    //    String lastStationKey = PreferenceHelper.getSharedPreferenceString(getAppContext(), Constants.CONST_STATION_KEY, Constants.DEFAULT_STATION_KEY);

        ArrayList<String> userDetails = PreferenceHelper.getSharedPreferenceStringArray(getAppContext(),master.getMasterID(),"");

        FirebaseHelper.deleteStaffingDetailsFireBase(userDetails.get(0),userDetails.get(1));
         mp = MediaPlayer.create(getAppContext(), R.raw.accept_1);
        FirebaseAuth.getInstance().signOut();// sign out of firebase
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(// sign out of Google
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.e(TAG, "Signed Out "+status );

                        Toast.makeText(SignInActivity.getAppContext(),"You have successfully signed out",Toast.LENGTH_LONG).show();

                    }
                });
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.e("result", "isSuccess");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.e("result", "Google Sign In failed");
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {


        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            updateUserImageDetails();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }


    private void updateUserImageDetails() {

        // get the user logged in details from Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String staffName = currentUser.getDisplayName();

        String name = (staffName!=null) ? staffName : "unknown"; // precautionary fallback
        String photoUrl = String.valueOf(currentUser.getPhotoUrl());

        currentUser.getIdToken(true);
        String userId = currentUser.getUid();// get the unique id set for the signed in user

        // update the master class
        master = new Master();
        master.setName(name);
        master.setUrl(photoUrl);
        master.setMasterID(userId);

        TextView title = findViewById(R.id.title_text);
        title.setText(name);

        ImageView userImg = findViewById(R.id.user_img);


        Picasso.with(getApplicationContext()).load(photoUrl).into(userImg); // a lovely image downloading and caching library for Android
        // clear preference data for testing
     //   PreferenceHelper.setSharedPreferenceStringArray(this,master.getMasterID(),null);
    }


}


