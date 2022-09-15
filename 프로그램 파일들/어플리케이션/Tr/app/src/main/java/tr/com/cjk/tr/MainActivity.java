package tr.com.cjk.tr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * Created by user on 2018-08-08.
 */
public class MainActivity extends Activity {
    private String resentData ="";
    private String temp ="";
    private String id="1";
    private int Speed=0;
    private int Torque=0;
    private int Turns=0;

    private String mqttcontent="";
    private String subscribeTopic[]={"TaboutTopic","TestoutTopic2"};
    private String publishTopic="TabinTopic";
    public static MqttClient mqttClient;
    private int mqttstate=0;
    private int fromint=0;
    private EditText speedET;
    private EditText torqueET;
    private EditText turnsET;

    private EditText idET;
    private TextView rejectTv;
    private TextView speedTV;
    private TextView turnsTv;
    private TextView torqueTV;
    private TextView TimeTv;
    private TextView totalTv;
    private TextView rejectperTv;
    private TextView mqttTV;
    private SeekBar speedSB;
    private SeekBar torqueSB;
    private SeekBar turnsSB;
    private ImageView startlampOff;
    private ImageView passlampOff;
    private ImageView rejectlampOff;
    public static Activity AActivity;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    private int recyclestate=0;

    phpDown task;
    private String TAG="카페24";

    static public String[] data_buf = new String[100];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        <item name="windowNoTitle">true</item> //타이틀바 없애기

        AActivity=MainActivity.this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        idET = (EditText)findViewById(R.id.txtText1);
        speedTV = (TextView) findViewById(R.id.sptv);
        torqueTV = (TextView) findViewById(R.id.tqtv);
        turnsTv = (TextView) findViewById(R.id.tstv);
        totalTv = (TextView) findViewById(R.id.totaltv);
        rejectTv = (TextView) findViewById(R.id.badcounttv);
        rejectperTv = (TextView) findViewById(R.id.badpertv);
        speedET = (EditText) findViewById(R.id.spET);
        torqueET = (EditText) findViewById(R.id.tqET);
        turnsET = (EditText) findViewById(R.id.tsET);
        speedSB = (SeekBar) findViewById(R.id.spseekBar);
        torqueSB = (SeekBar) findViewById(R.id.tqseekBar);
        turnsSB = (SeekBar) findViewById(R.id.tsseekBar);
        TimeTv = (TextView) findViewById(R.id.updatetime);
        mqttTV = (TextView) findViewById(R.id.mqttmessage);
        startlampOff = (ImageView) findViewById(R.id.startImgoff);
        passlampOff = (ImageView) findViewById(R.id.passImgoff);
        rejectlampOff = (ImageView) findViewById(R.id.rejectImgoff);

        SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); //SharedPreferences 선언
        id=pref.getString("id","1");  //최근 설정한 ID 읽어오기 (기본값 1)
        Log.d(TAG+": 현재 id =",""+id);

        task = new phpDown();
        task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);
        Intent intent = getIntent();
        fromint = intent.getIntExtra("from",0);

        try {
            connectMqtt();
            Log.d("카페24", "MQTT 연결");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("카페24", "MqttConnect Error");
        }

        Log.d("카페24","setup 완료 ");

        speedSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //손을 뗏을때
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //터치 시작시
            }

            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                //드래그 중일때
                speedET.setText("" + progress);
            }
        });
        torqueSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //손을 뗏을때
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //터치 시작시
            }

            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                //드래그 중일때
                torqueET.setText("" + progress);
            }
        });
        turnsSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //손을 뗏을때
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //터치 시작시
            }

            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                //드래그 중일때
                turnsET.setText("" + (progress+1));
            }
        });
    }

    //자료확인 버튼 클릭
    public void mOnMiddle(View v){
//        if(!idET.getText().toString().isEmpty()){
//            id=idET.getText().toString();}
//        Log.d("카페24","자료확인버튼 클릭 + id : " +id);
//        Intent backintent = new Intent(
//                getApplicationContext(),//현재제어권자
//                RecyclerActivity.class); // 이동할 컴포넌트
//        backintent.putExtra("ID",id);
//        startActivity(backintent); // 백그라운드 서비스
    }



    //새로고침 버튼 클릭
    public void mOnupdate(View v){
        if(!idET.getText().toString().isEmpty()){
            id=idET.getText().toString();}

        Log.d("카페24","새로고침버튼 클릭 + id : " +id);
        task = new phpDown();
        task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);


        SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); // 선언
        SharedPreferences.Editor editor = pref.edit();// editor에 put 하기
        editor.putString("id",id); //id라는 key값으로 id 데이터를 저장한다.
        editor.commit(); //완료한다.
        //////////////////////////// SharedPreferences 데이터 저장 완료
    }

    // 속도 전송 버튼 클릭
    public void onSpeed(View v){
        if(!speedET.getText().toString().isEmpty()){
            Speed=Integer.valueOf(speedET.getText().toString());}
        Log.d("카페24","속도전송 버튼 클릭 + Speed : " +Speed);
        speedSB.setProgress(Speed);
        try {
            MqttMessage message = new MqttMessage(("sp"+speedET.getText().toString()).getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 이 곳에 UI작업을 한다
                    Toast.makeText(MainActivity.this, "속도 설정 : "+Speed, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // 토크 전송 버튼 클릭
    public void onTorque(View v){
        if(!torqueET.getText().toString().isEmpty()){
            Torque=Integer.valueOf(torqueET.getText().toString());}
        Log.d("카페24","토크전송 버튼 클릭 + Torque : " +Torque);
        torqueSB.setProgress(Torque);
        try {
            MqttMessage message = new MqttMessage(("tq"+torqueET.getText().toString()).getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 이 곳에 UI작업을 한다
                    Toast.makeText(MainActivity.this, "토크 설정 : "+Torque, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // 횟수 전송 버튼 클릭
    public void onTurns(View v){
        if(!turnsET.getText().toString().isEmpty()){
            Turns=Integer.valueOf(turnsET.getText().toString());}
        Log.d("카페24","회전수전송 버튼 클릭 + Turns : " +Turns);
        turnsSB.setProgress(Turns-1);
        try {
            MqttMessage message = new MqttMessage(("ts"+turnsET.getText().toString()).getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 이 곳에 UI작업을 한다
                    Toast.makeText(MainActivity.this, "회전수 설정 : "+Turns, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // 동작 버튼 클릭
    public void onGo(View v){
        try {
            MqttMessage message = new MqttMessage(("go").getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 이 곳에 UI작업을 한다
                    Toast.makeText(MainActivity.this, "검사 시작", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 정지 버튼 클릭
    public void onStop(View v){
        try {
            MqttMessage message = new MqttMessage(("no").getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 이 곳에 UI작업을 한다
                    Toast.makeText(MainActivity.this, "검사 중지", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();

            finish();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();

        }
    }


    private void setData(){
        if(recyclestate==1){

            StringTokenizer stok = new StringTokenizer(resentData, "&", false);
            String total = stok.nextToken();
            String reject = stok.nextToken();
            String rate = stok.nextToken();
            String speed = stok.nextToken();
            String torque = stok.nextToken();
            String turns = stok.nextToken();
            String mtime = "데이터 업데이트 :" + stok.nextToken();
            String percent = rate+"%";
            Speed=Integer.valueOf(speed);
            Torque=Integer.valueOf(torque);
            Turns=Integer.valueOf(turns);


            speedTV.setText(speed);
            torqueTV.setText(torque);
            turnsTv.setText(turns);
            speedET.setText(speed);
            torqueET.setText(torque);
            turnsET.setText(turns);
            speedSB.setProgress(Speed);
            torqueSB.setProgress(Torque);
            turnsSB.setProgress(Turns-1);

            totalTv.setText(total);
            rejectTv.setText(reject);
            rejectperTv.setText(percent);

            TimeTv.setText(mtime);
            Log.d("카페24 최종 아이템 : ",mtime);

            Toast.makeText(this, "새로고침 완료 (ID : "+id+")", Toast.LENGTH_LONG).show();}
        else if(recyclestate==0){
            Toast.makeText(this, "ID : "+id+" 데이터가 없습니다", Toast.LENGTH_LONG).show();}



    }

    private class phpDown extends AsyncTask<String, Integer,String> {
        @Override
        protected String doInBackground(String... urls) {
            int i =0;
            Log.d("카페24 정보 읽어오기 : ","");

            StringBuilder jsonHtml = new StringBuilder();
            try{
                // 연결 url 설정
                URL url = new URL(urls[0]);
                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // 연결되었으면.
                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    data_buf = new String[200];
                    // 연결되었음 코드가 리턴되면.
                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for(;;){
                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                            data_buf[i] = br.readLine();
                            if(data_buf[i] == null) break;
                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                            Log.d("카페24 DB아이템 : "+i, data_buf[i]);
                            resentData=data_buf[i];
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }


            return jsonHtml.toString();

        }

        protected void onPostExecute(String str){
            if(resentData.length()>15){   recyclestate=1;}
            else{ recyclestate=0;  }
            setData();
            //Intent mainActivityIntent = new Intent(MainActivity.this, MainActivity.class);
            //startActivity(mainActivityIntent);

        }

    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////MQTT관련 함수//////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////



    private void connectMqtt() throws Exception{
        mqttClient = new MqttClient("tcp://postman.cloudmqtt.com:17181", MqttClient.generateClientId(),null);
        MqttConnectOptions options = new MqttConnectOptions();
        //options.setCleanSession(true);
        options.setUserName("mzxzkerr");
        options.setPassword("6ePJPdo2Nkr6".toCharArray());
        mqttClient.connect(options);
        mqttClient.subscribe(subscribeTopic);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG,"Mqtt ReConnect");
                try{connectMqtt();}catch(Exception e){Log.d(TAG,"MqttReConnect Error");}
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                mqttcontent=new String(message.getPayload());
                Log.d(TAG,"messageArrived topic ["+topic+"], message : "+mqttcontent);
                if(topic.equals("TaboutTopic")){
                    //mqttTV.setText(mqttcontent);
                    setImage(); //UI변경


                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void setImage (){
        Log.d(TAG,"카페24 탭 ["+mqttcontent+"] 검사?");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 이 곳에 UI작업을 한다

        if(mqttcontent.startsWith("s")){
            Log.d(TAG,"카페24 탭 ["+mqttcontent+"] 검사 시작");
            startlampOff.setImageResource(R.drawable.yellowcircle);
            passlampOff.setImageResource(R.drawable.voidcircle);
            rejectlampOff.setImageResource(R.drawable.voidcircle);
        }else if(mqttcontent.startsWith("p")){
            Log.d(TAG,"카페24 탭 ["+mqttcontent+"] 검사 통과");
            startlampOff.setImageResource(R.drawable.voidcircle);
            passlampOff.setImageResource(R.drawable.greencircle);
            rejectlampOff.setImageResource(R.drawable.voidcircle);

            if(!idET.getText().toString().isEmpty()){
                id=idET.getText().toString();}
                Log.d("카페24","새로고침버튼 클릭 + id : " +id);
            task = new phpDown();
            task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);
            SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); // 선언
            SharedPreferences.Editor editor = pref.edit();// editor에 put 하기
            editor.putString("id",id); //id라는 key값으로 id 데이터를 저장한다.
            editor.apply(); //완료한다.
        }else if(mqttcontent.startsWith("r")){
            Log.d(TAG,"카페24 탭 ["+mqttcontent+"] 검사 불량");
            startlampOff.setImageResource(R.drawable.voidcircle);
            passlampOff.setImageResource(R.drawable.voidcircle);
            rejectlampOff.setImageResource(R.drawable.redcircle);

            if(!idET.getText().toString().isEmpty()){
                id=idET.getText().toString();}
            Log.d("카페24","새로고침버튼 클릭 + id : " +id);
            task = new phpDown();
            task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);
            SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); // 선언
            SharedPreferences.Editor editor = pref.edit();// editor에 put 하기
            editor.putString("id",id); //id라는 key값으로 id 데이터를 저장한다.
            editor.apply(); //완료한다.
        } else{

        }

            }
        });

    }
}


