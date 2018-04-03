package com.example.hyoryeong.snapshotapi3;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        startLoading();
    }

    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    Intent intent = new Intent(getBaseContext(), NMapViewer.class);
                    startActivity(intent);
                    finish();
                } else {
                    // No user is signed in
                    Intent intent = new Intent(getBaseContext(), StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

}