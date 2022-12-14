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
    private String TAG="??????24";

    static public String[] data_buf = new String[100];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        <item name="windowNoTitle">true</item> //???????????? ?????????

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

        SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); //SharedPreferences ??????
        id=pref.getString("id","1");  //?????? ????????? ID ???????????? (????????? 1)
        Log.d(TAG+": ?????? id =",""+id);

        task = new phpDown();
        task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);
        Intent intent = getIntent();
        fromint = intent.getIntExtra("from",0);

        try {
            connectMqtt();
            Log.d("??????24", "MQTT ??????");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("??????24", "MqttConnect Error");
        }

        Log.d("??????24","setup ?????? ");

        speedSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //?????? ?????????
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //?????? ?????????
            }

            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                //????????? ?????????
                speedET.setText("" + progress);
            }
        });
        torqueSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //?????? ?????????
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //?????? ?????????
            }

            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                //????????? ?????????
                torqueET.setText("" + progress);
            }
        });
        turnsSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                //?????? ?????????
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //?????? ?????????
            }

            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                //????????? ?????????
                turnsET.setText("" + (progress+1));
            }
        });
    }

    //???????????? ?????? ??????
    public void mOnMiddle(View v){
//        if(!idET.getText().toString().isEmpty()){
//            id=idET.getText().toString();}
//        Log.d("??????24","?????????????????? ?????? + id : " +id);
//        Intent backintent = new Intent(
//                getApplicationContext(),//??????????????????
//                RecyclerActivity.class); // ????????? ????????????
//        backintent.putExtra("ID",id);
//        startActivity(backintent); // ??????????????? ?????????
    }



    //???????????? ?????? ??????
    public void mOnupdate(View v){
        if(!idET.getText().toString().isEmpty()){
            id=idET.getText().toString();}

        Log.d("??????24","?????????????????? ?????? + id : " +id);
        task = new phpDown();
        task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);


        SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); // ??????
        SharedPreferences.Editor editor = pref.edit();// editor??? put ??????
        editor.putString("id",id); //id?????? key????????? id ???????????? ????????????.
        editor.commit(); //????????????.
        //////////////////////////// SharedPreferences ????????? ?????? ??????
    }

    // ?????? ?????? ?????? ??????
    public void onSpeed(View v){
        if(!speedET.getText().toString().isEmpty()){
            Speed=Integer.valueOf(speedET.getText().toString());}
        Log.d("??????24","???????????? ?????? ?????? + Speed : " +Speed);
        speedSB.setProgress(Speed);
        try {
            MqttMessage message = new MqttMessage(("sp"+speedET.getText().toString()).getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ??? ?????? UI????????? ??????
                    Toast.makeText(MainActivity.this, "?????? ?????? : "+Speed, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // ?????? ?????? ?????? ??????
    public void onTorque(View v){
        if(!torqueET.getText().toString().isEmpty()){
            Torque=Integer.valueOf(torqueET.getText().toString());}
        Log.d("??????24","???????????? ?????? ?????? + Torque : " +Torque);
        torqueSB.setProgress(Torque);
        try {
            MqttMessage message = new MqttMessage(("tq"+torqueET.getText().toString()).getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ??? ?????? UI????????? ??????
                    Toast.makeText(MainActivity.this, "?????? ?????? : "+Torque, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // ?????? ?????? ?????? ??????
    public void onTurns(View v){
        if(!turnsET.getText().toString().isEmpty()){
            Turns=Integer.valueOf(turnsET.getText().toString());}
        Log.d("??????24","??????????????? ?????? ?????? + Turns : " +Turns);
        turnsSB.setProgress(Turns-1);
        try {
            MqttMessage message = new MqttMessage(("ts"+turnsET.getText().toString()).getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ??? ?????? UI????????? ??????
                    Toast.makeText(MainActivity.this, "????????? ?????? : "+Turns, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // ?????? ?????? ??????
    public void onGo(View v){
        try {
            MqttMessage message = new MqttMessage(("go").getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ??? ?????? UI????????? ??????
                    Toast.makeText(MainActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // ?????? ?????? ??????
    public void onStop(View v){
        try {
            MqttMessage message = new MqttMessage(("no").getBytes());
            mqttClient.publish(publishTopic,message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ??? ?????? UI????????? ??????
                    Toast.makeText(MainActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "???????????? ????????? ????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();

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
            String mtime = "????????? ???????????? :" + stok.nextToken();
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
            Log.d("??????24 ?????? ????????? : ",mtime);

            Toast.makeText(this, "???????????? ?????? (ID : "+id+")", Toast.LENGTH_LONG).show();}
        else if(recyclestate==0){
            Toast.makeText(this, "ID : "+id+" ???????????? ????????????", Toast.LENGTH_LONG).show();}



    }

    private class phpDown extends AsyncTask<String, Integer,String> {
        @Override
        protected String doInBackground(String... urls) {
            int i =0;
            Log.d("??????24 ?????? ???????????? : ","");

            StringBuilder jsonHtml = new StringBuilder();
            try{
                // ?????? url ??????
                URL url = new URL(urls[0]);
                // ????????? ?????? ??????
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // ??????????????????.
                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    data_buf = new String[200];
                    // ??????????????? ????????? ????????????.
                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for(;;){
                            // ????????? ???????????? ???????????? ??????????????? ?????? ??????.
                            data_buf[i] = br.readLine();
                            if(data_buf[i] == null) break;
                            // ????????? ????????? ????????? jsonHtml??? ????????????
                            Log.d("??????24 DB????????? : "+i, data_buf[i]);
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
    ////////////////////////////////////////MQTT?????? ??????//////////////////////////////////////////////////////
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
                    setImage(); //UI??????


                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void setImage (){
        Log.d(TAG,"??????24 ??? ["+mqttcontent+"] ???????");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // ??? ?????? UI????????? ??????

        if(mqttcontent.startsWith("s")){
            Log.d(TAG,"??????24 ??? ["+mqttcontent+"] ?????? ??????");
            startlampOff.setImageResource(R.drawable.yellowcircle);
            passlampOff.setImageResource(R.drawable.voidcircle);
            rejectlampOff.setImageResource(R.drawable.voidcircle);
        }else if(mqttcontent.startsWith("p")){
            Log.d(TAG,"??????24 ??? ["+mqttcontent+"] ?????? ??????");
            startlampOff.setImageResource(R.drawable.voidcircle);
            passlampOff.setImageResource(R.drawable.greencircle);
            rejectlampOff.setImageResource(R.drawable.voidcircle);

            if(!idET.getText().toString().isEmpty()){
                id=idET.getText().toString();}
                Log.d("??????24","?????????????????? ?????? + id : " +id);
            task = new phpDown();
            task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);
            SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); // ??????
            SharedPreferences.Editor editor = pref.edit();// editor??? put ??????
            editor.putString("id",id); //id?????? key????????? id ???????????? ????????????.
            editor.apply(); //????????????.
        }else if(mqttcontent.startsWith("r")){
            Log.d(TAG,"??????24 ??? ["+mqttcontent+"] ?????? ??????");
            startlampOff.setImageResource(R.drawable.voidcircle);
            passlampOff.setImageResource(R.drawable.voidcircle);
            rejectlampOff.setImageResource(R.drawable.redcircle);

            if(!idET.getText().toString().isEmpty()){
                id=idET.getText().toString();}
            Log.d("??????24","?????????????????? ?????? + id : " +id);
            task = new phpDown();
            task.execute("http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id);
            SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE); // ??????
            SharedPreferences.Editor editor = pref.edit();// editor??? put ??????
            editor.putString("id",id); //id?????? key????????? id ???????????? ????????????.
            editor.apply(); //????????????.
        } else{

        }

            }
        });

    }
}


