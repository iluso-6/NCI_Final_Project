package shay.example.com.dart_client;

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

import shay.example.com.dart_client.helper_classes.PreferenceHelper;
import shay.example.com.dart_client.models.User;

import static shay.example.com.dart_client.helper_classes.Utilities.isNetworkAvailable;
import static shay.example.com.dart_client.helper_classes.Utilities.showInfoDialog;

/**
 * Created by Shay de Barra on 06,January,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class SignInActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity Log:";
    public static GoogleApiClient mGoogleApiClient;
    public User user;// user Model Class
    private static MediaPlayer mp;
    private static Context context;
    public boolean online;
    private SignInButton signInButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static void signOutFromGoogle() {


        FirebaseAuth.getInstance().signOut();// sign out of firebase
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(// sign out of Google
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.e(TAG, "Signed Out " + status);

                        //   Toast.makeText(SignInActivity.getAppContext(),"You have successfully signed out",Toast.LENGTH_LONG).show();

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        // if being called by the onBackPressed in MenuActivity
        if (getIntent().getBooleanExtra("EXIT", false)) {
            Log.e(TAG, "EXIT: ");
            finish();
            return;
        }
        context =this;
        // check to to if we have an ongoing journey "Ticket" in progress
        boolean active_ticket =  PreferenceHelper.getSharedPreferenceBoolean(this, "saved_state", false);
    //    PreferenceHelper.setSharedPreferenceBoolean(this, "saved_state", false);
        if (active_ticket) {
            Intent intentBegin = new Intent(this,BeginJourney.class);
            Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
            startActivity(intentBegin, animation);

            SignInActivity.this.finish();
        }
        //    Log.e(TAG, "onCreate: "+Dart.getOrg_direction( "Clongriffin","Portmarnock"));
        mp = MediaPlayer.create(this, R.raw.intro);
        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image
        assert actionBar != null;
        actionBar.setIcon(R.drawable.ie_logo);// display custom icon in toolbar

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        // my google sign in button
        signInButton = findViewById(R.id.googleSignInBtn);
        // clear preference data for testing
        //  PreferenceHelper.setSharedPreferenceString(this,Utilities.CONST_STATION_NAME,null);

        // Check connectivity before launching
        online = isNetworkAvailable(this);

        Log.e(TAG, "isNetworkAvailable: " + online);
        if (!online) {
            showInfoDialog(this);
            signInButton.setEnabled(false);
            return;
        }
        signInButton.setEnabled(true);
        mAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    // user is already signed in
                    signInButton.setEnabled(false);

                    updateUserImageDetails();
                    mp.start();  // begin next activity


                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //     PreferenceHelper.setSharedPreferenceString(getApplicationContext(), Utilities.MY_LOCAL_STATION, null);
                            String local_station = PreferenceHelper.getSharedPreferenceString(getApplicationContext(), "key_local_stat", null);
                            if (local_station != null) {
                                Intent intentMain = new Intent(SignInActivity.this, MenuActivity.class);// select a new station
                                intentMain.putExtra("User",user);
                                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();
                                startActivity(intentMain, animation);
                                mp.release();

                                SignInActivity.this.finish();
                            } else {
                                // first time sign in set your local station
                                Intent intentStationList = new Intent(SignInActivity.this, StationsActivity.class);// select a new station
                                Bundle animation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.transition.slide_in, R.transition.slide_out).toBundle();

                                startActivity(intentStationList, animation);
                                mp.release();

                                SignInActivity.this.finish();
                            }
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

    @Override
    protected void onStart() {
        super.onStart();
        if (online) {
            mAuth.addAuthStateListener(mAuthListener);
        }
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

                            mp.start();  // begin next activity

                            //      updateUserImageDetails();
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

        Log.e("updateUserImageDetails", "updateUserImageDetails: ");
        // get the user logged in details from Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();

            String name = (userName != null) ? userName : ""; // precautionary fallback
            String photoUrl = String.valueOf(currentUser.getPhotoUrl());

            currentUser.getIdToken(true);
            String userId = currentUser.getUid();// get the unique id set for the signed in user


            TextView title = findViewById(R.id.title_text);
            title.setText(name);

            ImageView userImg = findViewById(R.id.user_img);


            Picasso.with(getApplicationContext()).load(photoUrl).into(userImg); // a lovely image downloading and caching library for Android

         /*   if (user == null) {
                user = new User();
            }
            Log.e(TAG, "currentUser.getPhoneNumber();: " + currentUser.getProviderData());
            user.setName(userName);
            user.setPhotoUrl(photoUrl);
            user.setUserID(userId);
*/
            Log.e(TAG, "SIGNIN: " );
            PreferenceHelper.setSharedPreferenceString(context,"user_name",userName);
            PreferenceHelper.setSharedPreferenceString(context,"user_photo",photoUrl);
        //    UserSingletonXXX.getInstance().setName(userName);
        //    UserSingletonXXX.getInstance().setPhotoUrl(photoUrl);
        //    UserSingletonXXX.getInstance().setUserID(userId);
        }
    }


}


