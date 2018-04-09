package com.example.hyoryeong.snapshotapi3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hyoryeong on 2018-02-12.
 */

public class StartActivity extends AppCompatActivity {

    Button Authbutton;

    //db
    SharedPreferences pref;

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startview);

        //로그인 페이지 호출 버튼
        //프로바이더 추가 가능 - 시간남으면 하기
        Authbutton = (Button) findViewById(R.id.authbutton);
        Authbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                // Create and launch sign-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }
        });
    }

    //로그인 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        pref= getSharedPreferences("auth", MODE_PRIVATE);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("Auth", "Signed in");

                //사용자 정보 저장 여부 확인 -> 존재시 main, 미존재시 userinfo로
                if ((pref.getInt("X", 0) + pref.getInt("Y", 0)) == 0) {
                    Toast.makeText(getApplicationContext(),"사용자 정보가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(StartActivity.this, UserinfoActivity.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),"사용자 정보가 존재합니다.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    //startActivity(new Intent(StartActivity.this, NMapViewer.class));
                    finish();
                }
            } else {
                //로그인 미성공시 스타트로
                // Sign in failed, check response for error code
                // ...
                Log.d("Auth", "Sign in failed");
                startActivity(new Intent(StartActivity.this, StartActivity.class));
                finish();
                //move to start activity
            }
        }
    }
}

