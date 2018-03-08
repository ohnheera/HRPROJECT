package com.example.hyoryeong.snapshotapi3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

/*
    사용자 정보 입력받는 액티비티
    사용자 정보 입력 변수와 동일하게 저장
 */
public class UserinfoActivity extends AppCompatActivity {

    Button store;
    Spinner age;
    CheckBox women;
    CheckBox men;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    TextView ageview;

    String[] items = {"6세 이하","7 ~ 12세","13 ~ 15세","16 ~ 20세","21 ~ 30세", "31 ~ 40세","41 ~ 50세","51 ~ 60세", "61세 이상"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //db에 저장
        pref= getSharedPreferences("auth", MODE_PRIVATE);
        editor = pref.edit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        //variables
        store = (Button) findViewById(R.id.store);
        age = (Spinner) findViewById(R.id.spinner);
        women = (CheckBox) findViewById(R.id.woman);
        men = (CheckBox) findViewById(R.id.man);
        ageview = (TextView) findViewById(R.id.ageview);

        //이미 저장된 정보를 표시
        //성별 정보
        if(pref.getInt("X",0)==1){
            women.setChecked(true);
        }
        else if(pref.getInt("Y",0)==1){
            men.setChecked(true);
        }
        //나이 정보
        //"6세 이하","7 ~ 12세","13 ~ 15세","16 ~ 20세","21 ~ 30세", "31 ~ 40세","41 ~ 50세","51 ~ 60세", "61세 이상"
        //    a          b          c           d           e            f           g          h           i
        if(pref.getInt("a",0)==1) ageview.setText("6세 이하");
        else if(pref.getInt("b",0)==1) ageview.setText("7 ~ 12세");
        else if(pref.getInt("c",0)==1) ageview.setText("13 ~ 15세");
        else if(pref.getInt("d",0)==1) ageview.setText("16 ~ 20세");
        else if(pref.getInt("e",0)==1) ageview.setText("21 ~ 30세");
        else if(pref.getInt("f",0)==1) ageview.setText("31 ~ 40세");
        else if(pref.getInt("g",0)==1) ageview.setText("41 ~ 50세");
        else if(pref.getInt("h",0)==1) ageview.setText("51 ~ 60세");
        else if(pref.getInt("i",0)==1) ageview.setText("61세 이상");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age.setAdapter(adapter);

        //DB 삭제
        editor.clear();
        //사용자 정보 삭제

        //여성 or 남성
        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(women.isChecked()){
                    if(men.isChecked()){
                        //오류: 성별 둘다 체크 -> 다시 설정하도록
                        Toast.makeText(UserinfoActivity.this, "  여성과 남성 중\n하나만 선택해 주세요.",Toast.LENGTH_LONG).show();
                     }
                    else{
                        //여자만 체크 -> 나이 확인 후 메인화면으로
                        editor.putInt("X",1);
                        setage(age.getSelectedItem().toString());
                        editor.commit(); //완료한다.
                        startActivity(new Intent(UserinfoActivity.this,MainActivity.class));
                        finish();
                    }
                }
                else if(men.isChecked()){
                    //남자만 체크 -> 나이 확인 후 메인화면으로
                    editor.putInt("Y",1);
                    setage(age.getSelectedItem().toString());
                    editor.commit(); //완료한다.
                    startActivity(new Intent(UserinfoActivity.this,MainActivity.class));
                    finish();
                }
                else{
                    //성별을 체크하지 않았을 때 -> 다시 설정하도록
                    Toast.makeText(UserinfoActivity.this, "성별을 선택해 주세요.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //나이를 db에 저장
    //"6세 이하","7 ~ 12세","13 ~ 15세","16 ~ 20세","21 ~ 30세", "31 ~ 40세","41 ~ 50세","51 ~ 60세", "61세 이상"
    //    a          b          c           d           e            f           g          h           i
    private void setage(String age) {
        if (age.equals("6세 이하")) editor.putInt("a",1);
        else if (age.equals("7 ~ 12세")) editor.putInt("b",1);
        else if (age.equals("13 ~ 15세")) editor.putInt("c",1);
        else if (age.equals("16 ~ 20세")) editor.putInt("d",1);
        else if (age.equals("21 ~ 30세")) editor.putInt("e",1);
        else if (age.equals("31 ~ 40세")) editor.putInt("f",1);
        else if (age.equals("41 ~ 50세")) editor.putInt("g",1);
        else if (age.equals("51 ~ 60세")) editor.putInt("h",1);
        else if (age.equals("61세 이상")) editor.putInt("i",1);
    }
}
