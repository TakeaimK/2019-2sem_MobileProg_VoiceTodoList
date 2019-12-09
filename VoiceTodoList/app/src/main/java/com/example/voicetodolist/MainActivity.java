package com.example.voicetodolist;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.speech.SpeechRecognizer.ERROR_AUDIO;
import static android.speech.SpeechRecognizer.ERROR_CLIENT;
import static android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
import static android.speech.SpeechRecognizer.ERROR_NETWORK;
import static android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT;
import static android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
import static android.speech.SpeechRecognizer.ERROR_SERVER;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;   //tts 엔진
    private ArrayList<String> items;    //todo 배열
    private ArrayAdapter<String> adapter;   //어댑터
    private ListView listView;  //리스트 뷰
    private Vibrator vibrator;  //진동

    private final String fileName = "items.list" ;//파일 저장 변수
    private final String logName = "log.list" ;//파일 저장 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("Welcome to VTL!");
        setContentView(R.layout.activity_main);
        ab.setBackgroundDrawable(new ColorDrawable(0xFF82B1FF));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("권한 요청");
            builder.setMessage("본 APP의 기능을 모두 사용하려면 녹음 권한이 필요합니다.");
            builder.setPositiveButton("승인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
                }
            });
            builder.setNegativeButton("거부", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        items = new ArrayList<String>();

        // 어댑터 생성
        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_multiple_choice_custom, items);


        // 어댑터 설정
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);



        //listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // 하나의 항목만 선택할 수 있도록 설정
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // 여러 항목을 선택할 수 있도록 설정


        //파일에서 데이터 로드
        loadItemsFromFile() ;
        adapter.notifyDataSetChanged();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(Menu.NONE, 2, Menu.NONE, "Log" + "");
        menu.add(Menu.NONE, 3, Menu.NONE, "Go to Blog");
        menu.add(Menu.NONE, 4, Menu.NONE, "About");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        switch (item.getItemId()){
            case 2:
                vibrator.vibrate(10); // 0.01초간 진동
                Intent myIntent2 = new Intent(getApplicationContext(), LogActivity.class);
                startActivity(myIntent2);
                return true;
            case 3:
                vibrator.vibrate(10); // 0.01초간 진동
                Intent myIntent3 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://takeaimk.tk"));
                startActivity(myIntent3);
                return true;
            case 4:
                vibrator.vibrate(10); // 0.01초간 진동
                Intent myIntent4 = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(myIntent4);
                return true;

        }
        return onOptionsItemSelected(item);
    }

/*
    public void voiceButton(View v){
        //TextView txt = findViewById(R.id.txt);
        inputVoice(listView);
    }
*/

    public void voiceButton(View v) {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params){
                    long[] pattern = {50, 50, 50, 50};
                    vibrator.vibrate(pattern, -1); // 0.1초간 진동
                    toast("말씀하세요...");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    long[] pattern = {50, 50};
                    vibrator.vibrate(pattern, -1); // 0.1초간 진동
                    toast("입력되었습니다.");
                }

                @Override
                public void onError(int error) {
                    //toast("오류 발생 : " + error);
                    vibrator.vibrate(500); // 0.5초간 진동
                    if(error == ERROR_NETWORK_TIMEOUT){
                        toast("네트워크에서 응답이 안 오네요..." , 1);
                    }
                    else if(error == ERROR_NETWORK){
                        toast("네트워크가 불안정해요! ㅠㅜ" , 2);
                    }
                    else if(error == ERROR_AUDIO){
                        toast("오디오에 문제가 있어요..." , 3);
                    }
                    else if(error == ERROR_SERVER){
                        toast("서버에 문제가 있어요..." , 4);
                    }
                    else if(error == ERROR_CLIENT){
                        toast("클라이언트에 문제가 있어요..." , 5);
                    }
                    else if(error == ERROR_SPEECH_TIMEOUT){
                        toast("시간이 초과되셨어요! ㅠㅜ\n다시 시도해 주세요..." , 6);
                    }
                    else if(error == ERROR_RECOGNIZER_BUSY){
                        toast("좀 많이 바빠서 지금은 힘들다네요.\n조금만 기다렸다가 재시도해 주세요.", 8);
                    }
                    else if(error == ERROR_INSUFFICIENT_PERMISSIONS){
                        toast("녹음 권한을 주셔야 쓸 수 있어요!\n앱 재실행 후 권한을 주세요.", 9);
                    }
                    else{
                        toast("저도 왜 이런지 알 수가 없네요...", 0);
                    }
                    stt.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    //txt.append("[나] "+result.get(0)+"\n");

                    SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일 HH시mm분ss초");
                    Date timed = new Date();
                    String time = format2.format(timed);
                    String writeString = (time + "\n" + result.get(0));
                    items.add(0,writeString);
                    adapter.notifyDataSetChanged();
                    replyAnswer(result.get(0));
                    saveItemsToFile();
                    saveItemsToLog(writeString);
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void replyAnswer(final String input){

        tts = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.KOREAN);

                    try{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ttsGreater21(input);
                        } else {
                            ttsUnder20(input);
                        }

                    } catch (Exception e) {
                        toast(e.toString());
                        //txt.append(e.toString());
                    }

                }

            }
        });


    }

    public void mOnClick(View v) {
        EditText ed = (EditText) findViewById(R.id.newitem);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        switch (v.getId()) {
            case R.id.btnAdd:                                 // ADD 버튼 클릭시
                vibrator.vibrate(50); // 0.1초간 진동
                String text = ed.getText().toString();          // EditText에 입력된 문자열값을 얻기
                if (!text.isEmpty()) {                          // 입력된 text 문자열이 비어있지 않으면
                    text = text.replaceAll(System.getProperty("line.separator"), " ");
                    SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일 HH시mm분ss초");
                    Date timed = new Date();
                    String time = format2.format(timed);
                    String writeString = (time + "\n" + text);
                    items.add(0, writeString);                           // items 리스트에 입력된 문자열 추가
                    replyAnswer(text);
                    saveItemsToFile();
                    saveItemsToLog(writeString);
                    ed.setText("");                             // EditText 입력란 초기화
                    adapter.notifyDataSetChanged();            // 리스트 목록 갱신
                }
                break;
            case R.id.btnDelete:                             // DELETE 버튼 클릭시
                vibrator.vibrate(100); // 0.1초간 진동
               /* int pos = listView.getCheckedItemPosition(); // 현재 선택된 항목의 첨자(위치값) 얻기
                if (pos != ListView.INVALID_POSITION) {     // 선택된 항목이 있으면
                    items.remove(pos);                        // items 리스트에서 해당 위치의 요소 제거
                    listView.clearChoices();                  // 선택 해제
                    adapter.notifyDataSetChanged();
                    // 어답터와 연결된 원본데이터의 값이 변경된을 알려 리스트뷰 목록 갱신
                }
                break;
                */

                SparseBooleanArray sbArray = listView.getCheckedItemPositions();
                // 선택된 아이템의 위치를 알려주는 배열 ex) {0=true, 3=true, 4=false, 6=true}
                Log.d("MainActivity", sbArray.toString());

                if(sbArray.size() != 0) {
                    for (int i = listView.getCount() -1; i>=0; i--) { // 목록의 역순으로 순회하면서 항목 제거
                        if (sbArray.get(i)) {
                            items.remove(i);
                        }
                    }
                    listView.clearChoices();
                    adapter.notifyDataSetChanged();
                }
                saveItemsToFile() ;
                break;
        }
    }

    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }


    private void saveItemsToFile() {    //save 함수
        File file = new File(getFilesDir(), fileName) ;
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

    private void saveItemsToLog(String input) {    //save 함수
        File file = new File(getFilesDir(), logName) ;
        FileWriter fw = null ;
        BufferedWriter bufwr = null ;

        try {
            // open file.
            fw = new FileWriter(file, true) ;
            bufwr = new BufferedWriter(fw) ;

            /*
            for (int i = listView.getCount() -1; i>=0; i--) { // 목록의 역순으로 순회하면서 항목 제거
                String str = items.get(i);
                bufwr.write(str) ;
                bufwr.newLine() ;
            }
            */

                bufwr.write(input) ;
                bufwr.newLine() ;

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

    private void loadItemsFromFile() {      //load함수
        File file = new File(getFilesDir(), fileName) ;
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





    //StyleableToast
    private void toast(String msg){
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        StyleableToast.makeText(this, msg, R.style.customToastStyle).show();
    }

    private void toast(String msg, int code){
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        StyleableToast.makeText(this, msg + "\nError Code : " + code, R.style.customErrorToastStyle).show();
    }


}
