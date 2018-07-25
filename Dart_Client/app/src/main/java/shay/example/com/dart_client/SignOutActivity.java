package shay.example.com.dart_client;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static android.media.MediaPlayer.create;

/**
 * Created by Shay de Barra on 10,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

public class SignOutActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_out);

        mediaPlayer = create(this, R.raw.intro);
        ActionBar actionBar = getSupportActionBar();// get the parent in order to insert image
        assert actionBar != null;
        actionBar.setIcon(R.drawable.ie_logo);// display custom icon in toolbar

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);

        Button signOutBtn = findViewById(R.id.SignOutBtn);

        SignInActivity.mGoogleApiClient.connect();
        signOutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                SignInActivity.signOutFromGoogle();
                mediaPlayer.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                finish();
            }
        });
    }

}
