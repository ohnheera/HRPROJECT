package com.example.hyoryeong.snapshotapi3;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.ImageButton;
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

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final int MY_PERMISSION_LOCATION = 1;
    private SensorManager mSensorManager;

    TextView activi;
    TextView locat;
    TextView place;
    TextView weath;
    TextView headphone;
    ImageButton mapbutton;
    Button deletebutton;//탈퇴버튼
    Button Logoutbutton;//로그아웃버튼
    Button Editbutton;//정보수정버튼
    TextView sensordata;


    String accelometer="Acc:";
    String light="Lux:";
    String magnetic="Mag:";
    String gyroscope="Gyro:";
    String geomagnetic="Geo_Mag:";
    String gravity="Gravity:";

    private static final String TAG = "Awareness";
    Timer timer;
    private GoogleApiClient client;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Activity");
    DatabaseReference myRef2 = database.getReference("Headphones");
    DatabaseReference myRef3 = database.getReference("Location");
    DatabaseReference myRef4 = database.getReference("Places");
    DatabaseReference myRef5 = database.getReference("Weather");

    DatabaseReference Accel=database.getReference("Accelometer");
    DatabaseReference Light=database.getReference("Light");
    DatabaseReference Magnet=database.getReference("Magnetic field");
    DatabaseReference Gyro=database.getReference("Gyroscope");
    DatabaseReference Grav=database.getReference("Gravity");

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
        mapbutton=(ImageButton) findViewById(R.id.mapbutton);
        //탈퇴 버튼
        deletebutton=(Button) findViewById(R.id.deletebutton);
        //로그아웃 버튼
        Logoutbutton = (Button) findViewById(R.id.logoutbutton);
        //정보수정 버튼
        Editbutton= (Button)findViewById(R.id.editbutton);
        //센서 데이터 텍스트뷰
        sensordata=(TextView) findViewById(R.id.sensordata);


        //정보수정버튼 리스너
        Editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB 삭제
                editor.clear();
                editor.commit();
                startActivity(new Intent(MainActivity.this, UserinfoActivity.class));
                finish();
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
                                finish();
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
                                finish();
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

        /*sensordata.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SensorActivity.class));
            }
        });*/

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mapbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NMapViewer.class));
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        Sensor sens=event.sensor;
        if(sens.getType()==Sensor.TYPE_ACCELEROMETER){
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.
            final float alpha = (float) 0.8;

            // Isolate the force of gravity with the low-pass filter.
            double [] gravity={0,0,0};
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            double [] linear_acceleration={0,0,0};
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            accelometer="<"+linear_acceleration[0]+","+linear_acceleration[1]+","+linear_acceleration[2]+"> ";
            Log.e("Accelometer", accelometer);
            Accel.setValue(accelometer);
        }
        else if(sens.getType()==Sensor.TYPE_LIGHT){
            float lux=event.values[0];
            light=lux+" ";
            Log.e("Light", light);
            Light.setValue(light);
        }
        else if(sens.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            double[] mag={0,0,0};
            mag[0]=event.values[0];
            mag[1]=event.values[1];
            mag[2]=event.values[2];
            magnetic="<"+mag[0]+","+mag[1]+","+mag[2]+"> ";
            Log.e("Magnetic",magnetic);
            Magnet.setValue(magnetic);
        }
        else if(sens.getType()==Sensor.TYPE_GYROSCOPE){
            // This time step's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the time step
                // in order to get a delta rotation from this sample over the time step
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) sin(thetaOverTwo);
                float cosThetaOverTwo = (float) cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
            // User code should concatenate the delta rotation we computed with the current rotation
            // in order to get the updated rotation.
            gyroscope="<";
            for(int i=0;i<4;i++) {
                //rotationCurrent[i] = rotationCurrent[i] * deltaRotationMatrix[i];
                gyroscope+=deltaRotationVector[i]+",";
            }
            gyroscope+="> ";
            Log.e("Gyroscope",gyroscope);
            Gyro.setValue(gyroscope);
        }
        else if(sens.getType()==Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
            float [] geomag={0,0,0};
            geomag[0]=event.values[0];
            geomag[1]=event.values[1];
            geomag[2]=event.values[2];

            geomagnetic+="<"+geomag[0]+","+geomag[1]+","+geomag[2]+"> ";
            Log.e("GeoMagnetic:",geomagnetic);
        }
        else if(sens.getType()==Sensor.TYPE_GRAVITY){
            float [] grav={0,0,0};
            grav[0]=event.values[0];
            grav[1]=event.values[1];
            grav[2]=event.values[2];

            gravity="<"+grav[0]+","+grav[1]+","+grav[2]+"> ";
            Log.e("Gravity:", gravity);
            Grav.setValue(gravity);
        }

        sensordata.setText(accelometer+","+Light+","+gravity+","+gyroscope+","+magnetic);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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

