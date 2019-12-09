package com.example.voicetodolist;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogActivity extends AppCompatActivity {

    private ArrayList<String> items;    //todo 배열
    private ArrayAdapter<String> adapter;   //어댑터
    private ListView listView;  //리스트 뷰
    private Vibrator vibrator;  //진동

    private final String logName = "log.list" ;//파일 저장 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("VTL's Log");
        setContentView(R.layout.activity_log);
        ab.setBackgroundDrawable(new ColorDrawable(0xFF82B1FF));
        items = new ArrayList<String>();

        // 어댑터 생성
        adapter = new ArrayAdapter<String>(LogActivity.this, R.layout.simple_list_item_1_custom, items);

        // 어댑터 설정
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        //listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // 하나의 항목만 선택할 수 있도록 설정
        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // 여러 항목을 선택할 수 있도록 설정


        //파일에서 데이터 로드
        loadItemsFromLog() ;
        adapter.notifyDataSetChanged();
    }


    private void loadItemsFromLog() {      //load함수
        File file = new File(getFilesDir(), logName) ;
        FileReader fr = null ;
        BufferedReader bufrd = null ;
        String str, str2, str3 ;

        if (file.exists()) {
            try {
                // open file.
                fr = new FileReader(file) ;
                bufrd = new BufferedReader(fr) ;

                while ((str = bufrd.readLine()) != null) {
                    str2 = bufrd.readLine();
                    str3 = (str + "\n" + str2);
                    items.add(str3) ;
                }

                bufrd.close() ;
                fr.close() ;
            } catch (Exception e) {
                e.printStackTrace() ;
            }
        }
    }

    private void saveItemsToLog() {    //save 함수
        File file = new File(getFilesDir(), logName) ;
        FileWriter fw = null ;
        BufferedWriter bufwr = null ;

        try {
            // open file.
            fw = new FileWriter(file) ;
            bufwr = new BufferedWriter(fw) ;

             /*
            for (int i = listView.getCount() -1; i>=0; i--) { // 목록의 역순으로 순회하면서 항목 제거
                String str = items.get(i);
                bufwr.write(str) ;
                bufwr.newLine() ;
            }
            */

            for (String str : items) {
                bufwr.write(str) ;
                bufwr.newLine() ;
            }

            // write data to the file.
            bufwr.flush() ;

        } catch (Exception e) {
            e.printStackTrace() ;
        }

        try {
            // close file.
            if (bufwr != null) {
                bufwr.close();
            }

            if (fw != null) {
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace() ;
        }
    }


    public void mOnClick(View v) {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        long[] pattern = {500, 100, 500, 100, 500, 100}; // 0.5초 진동, 0.1초 대기
        vibrator.vibrate(pattern, -1); // 패턴 수행
        builder.setTitle("Warning!");
        builder.setMessage("[경고] 로그가 모두 삭제됩니다! \n삭제 후 복구하실 수 없습니다.");


        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
                if(listView.getCount() != 0) {
                    for (int i = listView.getCount() -1; i>=0; i--) { // 목록의 역순으로 순회하면서 항목 제거
                        items.remove(i);
                    }
                }
                saveItemsToLog();
                finish();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
            }
        });
        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
            }
        });

        dialog.show();



    }



}
