package com.example.hyoryeong.snapshotapi3;

/**
 * Created by Hyoryeong on 2018-02-12.
 */
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class loginActivity extends AppCompatActivity {

    private StorageReference mStorageRef;

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joinview);

        final DBHelper dbHelper = new DBHelper(getApplicationContext(), "CLIENTINFO.db", null, 1);
        final String TAG = "User";

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("message");

        // 테이블에 있는 모든 데이터 출력
        final TextView result = (TextView) findViewById(R.id.result);

        final EditText etDate = (EditText) findViewById(R.id.date);
        final EditText etName = (EditText) findViewById(R.id.name);
        final EditText etAge = (EditText) findViewById(R.id.age);
        final CheckBox etMan = (CheckBox) findViewById(R.id.gender_man);
        final CheckBox etWoman = (CheckBox) findViewById(R.id.gender_woman);


        ////////////////////////////////// Firebase에 이것저것 올려보기 //////////////////////////////////////////////////////////////////
        mStorageRef = FirebaseStorage.getInstance().getReference();


        Uri file = Uri.fromFile(new File("C:/Users/Hyoryeong/Desktop/학교/증명사진/abc.jpg"));
        final StorageReference riversRef = mStorageRef.child("images/rivers.jpg");

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.e("data", riversRef.getBucket());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });


        File localFile = null;
        try {
            localFile = File.createTempFile("생활안전지도", "xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                        Log.e("data", riversRef.getBucket());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
        //////////////////////////////////////////////////끝..안됨../////////////////////////////////////////////////////

        etMan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etWoman.setChecked(false);
            }}
        );

        etWoman.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etMan.setChecked(false);
            }}
        );


        // 날짜는 현재 날짜로 고정
        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        etDate.setText(simpleDateFormat.format(date));

        // DB에 데이터 추가
        Button insert = (Button) findViewById(R.id.insert);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = etDate.getText().toString();
                String name = etName.getText().toString();
                int age = Integer.parseInt(etAge.getText().toString());
                String gender=" ";
                if(etMan.isChecked()==true){
                    //etWoman.setChecked(false);
                    gender=etMan.getText().toString();
                }
                if(etWoman.isChecked()==true){
                    //etMan.setChecked(false);
                    gender=etWoman.getText().toString();
                }

                dbHelper.insert(date, name, age, gender);
                result.setText(dbHelper.getResult());
                Log.e(TAG, dbHelper.getResult());
                myRef.setValue(dbHelper.getResult());
            }
        });

        // DB에 있는 데이터 수정
        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                int age = Integer.parseInt(etAge.getText().toString());
                String gender=etMan.getText().toString();
                if(etMan.isChecked()==true){
                    //etWoman.setChecked(false);
                    gender=etMan.getText().toString();
                }
                if(etWoman.isChecked()==true){
                    //etMan.setChecked(false);
                    gender=etWoman.getText().toString();
                }

                dbHelper.update(name, age, gender);
                result.setText(dbHelper.getResult());
                Log.e(TAG, dbHelper.getResult());
                myRef.setValue(dbHelper.getResult());
            }
        });

        // DB에 있는 데이터 삭제
        Button delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();

                dbHelper.delete(name);
                result.setText(dbHelper.getResult());
                Log.e(TAG, dbHelper.getResult());
                myRef.setValue(dbHelper.getResult());
            }
        });

        // DB에 있는 데이터 조회
        Button select = (Button) findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText(dbHelper.getResult());
                Log.e(TAG, dbHelper.getResult());
            }
        });
    }

}
