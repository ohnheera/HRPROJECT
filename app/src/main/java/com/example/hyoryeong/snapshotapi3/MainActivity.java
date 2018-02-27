package com.example.hyoryeong.snapshotapi3;

import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_LOCATION = 1;
    TextView activi;
    TextView locat;
    TextView place;
    TextView weath;
    TextView headphone;
    Button sensordata;
    Button mapbutton;
    Button deletebutton;//탈퇴버튼
    Button Logoutbutton;//로그아웃버튼
    Button Editbutton;//정보수정버튼

    private static final String TAG = "Awareness";
    Timer timer;
    private GoogleApiClient client;

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Activity");
    DatabaseReference myRef2 = database.getReference("Headphones");
    DatabaseReference myRef3 = database.getReference("Location");
    DatabaseReference myRef4 = database.getReference("Places");
    DatabaseReference myRef5 = database.getReference("Weather");

    //사용자 정보 db 변수
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //사용자 정보 db
        pref= getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activi = (TextView) findViewById(R.id.activity);
        locat = (TextView) findViewById(R.id.location);
        place = (TextView) findViewById(R.id.place);
        weath = (TextView) findViewById(R.id.weather);
        headphone = (TextView) findViewById(R.id.headphone);
        sensordata=(Button) findViewById(R.id.sensorbutton);
        mapbutton=(Button) findViewById(R.id.mapbutton);
        //탈퇴 버튼
        deletebutton=(Button) findViewById(R.id.deletebutton);
        //로그아웃 버튼
        Logoutbutton = (Button) findViewById(R.id.logoutbutton);
        //정보수정 버튼
        Editbutton= (Button)findViewById(R.id.editbutton);


        //정보수정버튼 리스너
        Editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB 삭제
                editor.clear();
                editor.commit();
                startActivity(new Intent(MainActivity.this, UserinfoActivity.class));
            }
        });

        //탈퇴버튼 리스너
        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB 삭제
                editor.clear();
                editor.commit();
                //사용자 정보 삭제
                AuthUI.getInstance()
                        .delete(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Auth","user info deleted");
                                startActivity(new Intent(MainActivity.this,StartActivity.class));
                            }
                        });
            }
        });

        //로그아웃 버튼 리스너
        Logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Auth", "Signed out");
                                startActivity(new Intent(MainActivity.this, StartActivity.class));
                            }
                        });
            }
        });

        client = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Awareness.API)
                .build();
        client.connect();

        timer=new Timer();

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                getSnapShot();
            }
        };

        timer.schedule(timertask,1000,1000);

        sensordata.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SensorActivity.class));
            }
        });

        mapbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NMapViewer.class));
            }
        });
    }

    public void getSnapShot(){
        Awareness.SnapshotApi.getDetectedActivity(client)
                .setResultCallback(new ResultCallback<DetectedActivityResult>() {

                    @Override
                    public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                        if (!detectedActivityResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get the current activity.");
                            myRef.setValue("X");
                            return;
                        }
                        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                        DetectedActivity probableActivity = ar.getMostProbableActivity();
                        Log.i(TAG, probableActivity.toString());
                        activi.setText(probableActivity.toString());
                        myRef.setValue(probableActivity.toString());
                    }
                });

        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_LOCATION
            );
            return;
        }

        Awareness.SnapshotApi.getLocation(client)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        if (!locationResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get location.");
                            return;
                        }
                        Location location = locationResult.getLocation();
                        Log.i(TAG, "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                        locat.setText("Location: "+ location.getLatitude()+" , "+location.getLongitude());
                        myRef3.setValue(location.getLatitude()+" , "+location.getLongitude());
                    }
                });

        Awareness.SnapshotApi.getHeadphoneState(client)
                .setResultCallback(new ResultCallback<HeadphoneStateResult>() {
                    @Override
                    public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                        if (!headphoneStateResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get headphone state.");
                            return;
                        }
                        HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                        if (headphoneState.getState() == HeadphoneState.PLUGGED_IN) {
                            Log.i(TAG, "Headphones are plugged in.\n");
                            headphone.setText("Headphones are plugged in");
                            myRef2.setValue("Plugged in");
                        } else {
                            Log.i(TAG, "Headphones are NOT plugged in.\n");
                            headphone.setText("Headphones are NOT plugged in");
                            myRef2.setValue("Not Plugged in");
                        }

                    }
                });

        Awareness.SnapshotApi.getPlaces(client)
                .setResultCallback(new ResultCallback<PlacesResult>() {
                    @Override
                    public void onResult(@NonNull PlacesResult placesResult) {
                        if (!placesResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get places.");
                            return;
                        }
                        List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                        String places="Top 5 places: ";
                        // Show the top 5 possible location results.
                        if (placeLikelihoodList != null) {
                            for (int i = 0; i < 5 && i < placeLikelihoodList.size(); i++) {
                                PlaceLikelihood p = placeLikelihoodList.get(i);
                                Log.i(TAG, p.getPlace().getName().toString() + ", likelihood: " + p.getLikelihood());
                                places+=p.getPlace().getName().toString()+"  ";
                            }
                            place.setText(places);
                            myRef4.setValue(places);
                        } else {
                            Log.e(TAG, "Place is null.");
                        }
                    }
                });

        Awareness.SnapshotApi.getWeather(client)
                .setResultCallback(new ResultCallback<WeatherResult>() {
                    @Override
                    public void onResult(@NonNull WeatherResult weatherResult) {
                        if (!weatherResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get weather.");
                            return;
                        }
                        Weather weather = weatherResult.getWeather();
                        Log.i(TAG, "Weather: " + weather);
                        weath.setText("Weather: "+ weatherResult.getWeather().toString());
                        myRef5.setValue(weatherResult.getWeather().toString());
                    }
                });

    }
}

