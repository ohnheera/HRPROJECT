package com.example.hyoryeong.snapshotapi3;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by Hyoryeong on 2018-01-31.
 */

public class SensorActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;

    String accelometer="Acc:";
    String light="Lux:";
    String magnetic="Mag:";
    String gyroscope="Gyro:";
    String geomagnetic="Geo_Mag:";
    String gravity="Gravity:";

    FirebaseDatabase database= FirebaseDatabase.getInstance();
    DatabaseReference Accel=database.getReference("Accelometer");
    DatabaseReference Light=database.getReference("Light");
    DatabaseReference Magnet=database.getReference("Magnetic field");
    DatabaseReference Gyro=database.getReference("Gyroscope");
    DatabaseReference Grav=database.getReference("Gravity");


    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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

}
