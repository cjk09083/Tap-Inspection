#include <ESP8266WiFiMulti.h>
#include <ESP8266HTTPClient.h>
#include <PubSubClient.h>
#include<SoftwareSerial.h>
#include <stdlib.h>
#include <SPI.h>

SoftwareSerial arduinoSerial(5, 4); // D1, D2

WiFiClient wificlient;
WiFiClient client2;
ESP8266WiFiMulti WiFiMulti;
PubSubClient client(wificlient);

// 변수선언
int state = LOW;
int moter_stop = LOW;
int steady_state1 = LOW;
int steady_state2 = LOW;
char msg[50];
int result_state = 0;
String dataIn = "";
int wifi_timeout = 0;
int cspin = 15; //wemos mini d1 r2


//mqtt
const char* mqtt_server = "postman.cloudmqtt.com";    // port 17181
const char* outTopic = "TaboutTopic";
const char* inTopic = "TabinTopic";
const char* mqtt_username = "mzxzkerr";
const char* mqtt_password = "6ePJPdo2Nkr6";
const char* pubdata = "";


//세팅값
String id = "1" ;       // 디바이스 id

#define ssid "olleh_WiFi_9DF0" //wifi id
#define pass "0000004810"      //wifi pass
#define ssid2 "D109-1" //wifi id
#define pass2 "113333555555"      //wifi pass

float torque_voltage = 500;
float speed_voltage = 500;
int turns_value = 1;

int Wiper0 = 127;     // Wiper register
int Wiper0_pre = 0; // Previous Wiper register

// 함수들
void resister_set();
void clear_msg();
void setup_wifi();
void Http_post();
void Http_get();
void wifi_check();
void break_msg();
void Http_post();
void callback(char* topic, byte* payload, unsigned int length);
void reconnect();

/////////////////////////////////////////
//*       셋업 & 루프                   *//
//*                                   *//
/////////////////////////////////////////

void setup() {
  Serial.begin(115200);
  arduinoSerial.begin(38400);
  setup_wifi();

  client.setServer(mqtt_server, 17181);
  client.setCallback(callback);
  //    pinMode(0,OUTPUT);
  //    pinMode(2,OUTPUT);
  //    pinMode(14,INPUT);
  //    pinMode(12,INPUT);
  //    pinMode(13,OUTPUT);
  pinMode(cspin, OUTPUT);    // Pin 24 OUTPUT, Slave Select(SS)
  digitalWrite(cspin, HIGH); // SS High
  SPI.begin();            // Initialize SPI(Default: 4MHz, Mode 0)
  wifi_check();        //Wifi 상태 체크 함수
  Serial.println("start wm");
  resister_set();

}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  if (arduinoSerial.available()) {
    int k = 0;
    dataIn = "";
    while (arduinoSerial.available()) {
      char myChar = (char)arduinoSerial.read();
      //msg[k]=myChar;
      dataIn += myChar;
      k++;
      delay(5);
    }
    //Serial.print("message : ");
    //Serial.print(msg);
    Serial.print("message : ");
    Serial.println(dataIn);
    break_msg(dataIn);
    //clear_msg();
  }


  if (Serial.available()) {
    int k = 0;
    dataIn = "";
    while (Serial.available()) {
      char myChar = (char)Serial.read();
      //msg[k]=myChar;
      dataIn += myChar;
      k++;
      delay(5);
    }
    //Serial.print("message : ");
    //Serial.print(msg);
    Serial.print("message : ");
    Serial.println(dataIn);
    break_msg(dataIn);
    //clear_msg();
  }




  delay(1000);
}


/////////////////////////////////////////
//*       사용자 함수                   *//
//*                                   *//
/////////////////////////////////////////

void clear_msg() {
  char buf[50] = "";
  for (int k = 0; k < 50; k++) {
    msg[k] = buf[k] ;
  }
}
void break_msg(String msg) {
  String torque_data;
  String speed_data;
  String turns_data;
  int speed_index = msg.indexOf(",");
  int torque_index = dataIn.indexOf(",", speed_index + 1);

  if (msg.length() > 4) {
    if (msg.startsWith("p")) {
      arduinoSerial.print("ok");
      result_state = 1;
      speed_data = msg.substring(1, speed_index);
      torque_data = msg.substring(speed_index + 1, torque_index);
      turns_data = msg.substring(torque_index + 1, msg.length());
      torque_voltage = torque_data.toInt();
      turns_value = turns_data.toInt();
      Serial.println("State : pass");
      Serial.print("Speed : ");
      Serial.println(speed_voltage);
      Serial.print("Torque : ");
      Serial.println(torque_voltage);
      Serial.print("Turns  : ");
      Serial.println(turns_value);
      Http_post();
      pubdata = "p";
      //strcpy(msg,pubdata.c_str()));
      client.publish(outTopic, pubdata);
      clear_msg();
    } else if (msg.startsWith("r")) {
      arduinoSerial.print("ok");
      result_state = 2;
      speed_data = msg.substring(1, speed_index);
      torque_data = msg.substring(speed_index + 1, torque_index);
      turns_data = msg.substring(torque_index + 1, msg.length());
      torque_voltage = torque_data.toInt();
      turns_value = turns_data.toInt();
      Serial.println("State : reject");
      Serial.print("Speed : ");
      Serial.println(speed_voltage);
      Serial.print("Torque : ");
      Serial.println(torque_voltage);
      Serial.print("Turns  : ");
      Serial.println(turns_value);
      Http_post();
      pubdata = "r";
      // strcpy(msg,pubdata.c_str());
      client.publish(outTopic, pubdata);
      clear_msg();
    } else if (dataIn.startsWith("hello")) {
      Serial.print("now Torque : ");
      Serial.println(torque_voltage);
      Serial.print("now Turns  : ");
      Serial.println(turns_value);
      delay(50);
      String data2 = "tq" + (String) torque_voltage;
      arduinoSerial.print(data2);
      delay(50);
      String data1 = "ts" + (String) turns_value;
      arduinoSerial.print(data1);     
      Serial.println("preset send");
   }
  }
}


void setup_wifi() {
  WiFi.disconnect();
  // Serial.setDebugOutput(true);
  Serial.println();
  Serial.println("[SETUP] Wifi Connecting ");
  //Serial.println(ssid2);
  WiFi.mode(WIFI_STA);              // Station 모드로 설정
  //wifi_scan();
  WiFiMulti.addAP(ssid, pass);      // Wifi 공유기 추가 및 자동연결
  // WiFi.begin(ssid2, pass2);
  WiFiMulti.addAP(ssid2, pass2);      // 공유기2 추가 및 자동연결

  Serial.print("[SETUP] WAIT ");
  while (WiFiMulti.run() != WL_CONNECTED) {
    delay(500); Serial.print("."); wifi_timeout++;
    if (wifi_timeout == 40) {
      wifi_timeout = 0;
      break;
    }
  }
  Serial.println();
}


void wifi_check() {
  //WiFiMulti.run();
  Serial.print("[SETUP] Wifi status : ");
  Serial.print(WiFi.status());
  Serial.print(" => ");
  if ((WiFi.status() == WL_CONNECTED)) {
    Serial.println("WL_CONNECTED");
  }
  else if ((WiFi.status() == WL_IDLE_STATUS)) {
    Serial.println("WL_IDLE_STATUS");
  }
  else if ((WiFi.status() == WL_CONNECTION_LOST)) {
    Serial.println("WL_CONNECTION_LOST");
  }
  else if ((WiFi.status() == WL_NO_SSID_AVAIL)) {
    Serial.println("WL_NO_SSID_AVAIL");
  }
  else if ((WiFi.status() == WL_CONNECT_FAILED)) {
    Serial.println("WL_CONNECT_FAILED");
  }
  else if ((WiFi.status() == WL_DISCONNECTED)) {
    Serial.println("WL_DISCONNECTED");
    if (!WiFi.getAutoConnect()) {
      WiFi.reconnect();
      Serial.print("[Reconnect] WAIT ");
      while (WiFi.status() != WL_CONNECTED) {
        delay(500); Serial.print(".");
        wifi_timeout++;
        if (wifi_timeout == 200) {
          wifi_timeout = 0;
          break;
        }
      }
      Serial.println();
    }
  }
  else {
    Serial.println("WL_ERROR");
  }
  Serial.print("[SETUP] WifiMulti status : ");
  Serial.print(WiFiMulti.run());
  Serial.print(" => ");
  if ((WiFiMulti.run() == WL_CONNECTED)) {
    Serial.println("WL_CONNECTED");
  }
  else if ((WiFiMulti.run() == WL_IDLE_STATUS)) {
    Serial.println("WL_IDLE_STATUS");
  }
  else if ((WiFiMulti.run() == WL_CONNECTION_LOST)) {
    Serial.println("WL_CONNECTION_LOST");
  }
  else if ((WiFiMulti.run() == WL_NO_SSID_AVAIL)) {
    Serial.println("WL_NO_SSID_AVAIL");
  }
  else if ((WiFiMulti.run() == WL_CONNECT_FAILED)) {
    Serial.println("WL_CONNECT_FAILED");
  }
  else if ((WiFiMulti.run() == WL_DISCONNECTED)) {
    Serial.println("WL_DISCONNECTED");
  }
  else {
    Serial.println("WL_ERROR");
  }
  Serial.print("[SETUP] WifiAutoConnect : ");
  Serial.print(WiFi.getAutoConnect());
  Serial.print(" => ");
  if (WiFi.getAutoConnect()) {
    Serial.println("True");
  }
  else {
    Serial.println("False");
  }
  Serial.print("[SETUP] Connected to ");        //연결된 wifi ID
  Serial.println(WiFi.SSID());
  Serial.print("[SETUP] localIP address:\t");   //할당된 IP 주소
  Serial.println(WiFi.localIP());

}

void Http_post() {
  HTTPClient http;           //  WiFiClient client;
  http.setTimeout(3000);
  Serial.print("[HTTP] begin... Connected to ");
  Serial.println(WiFi.SSID());
  if (http.begin(client2, "http://cjk09083.cafe24.com/tab/insert.php")) {  // Wifi를 통한 HTTP 통신
    http.addHeader("Content-Type", "application/x-www-form-urlencoded");
    Serial.print("[HTTP] POST... ");
    //          // start connection and send HTTP header
    //String post_content = "data="+String(WiFi.SSID());
    String post_content = "device_id=" + String(id) + "&state=" + String(result_state)
                          + "&speed=" + String(speed_voltage) + "&torque=" + String(torque_voltage) + "&turns=" + String(turns_value); //전달할 데이터
    int httpCode = http.POST(post_content); //Http 통신. Code가 200이면 정상, 음수면 실패
    Serial.println(post_content);
    // httpCode will be negative on error
    if (httpCode > 0) {
      // HTTP header has been send and Server response header has been handled
      Serial.printf("[HTTP] POST... success, code : %d\n", httpCode);
      String payload = http.getString();
      Serial.println(payload);
    } else {
      Serial.printf("[HTTP] POST... failed, error : %s\n", http.errorToString(httpCode).c_str());
    }
    http.end();
  } else {
    Serial.printf("[HTTP} Unable to connect\n");
  }
}

void Http_get() {
   HTTPClient http;
    Serial.print("[HTTP] begin...\n");
    if (http.begin(client2, "http://cjk09083.cafe24.com/tab/get_rate_data.php?device_id="+id)) {  // HTTP
      Serial.print("[HTTP] GET...\n");
      // start connection and send HTTP header
      int httpCode = http.GET();
      // httpCode will be negative on error
      if (httpCode > 0) {
        // HTTP header has been send and Server response header has been handled
        Serial.printf("[HTTP] GET... code: %d\n", httpCode);
        // file found at server
        if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_MOVED_PERMANENTLY) {
          String payload = http.getString();
          Serial.print("payload : ");
          Serial.println(payload);
          int total_index= payload.indexOf('&');
          int reject_index= payload.indexOf('&',total_index+1);
          int rate_index= payload.indexOf('&', reject_index+1);
          int speed_index= payload.indexOf('&', rate_index+1);
          int torque_index= payload.indexOf('&', speed_index+1);
          int turns_index= payload.indexOf('&', torque_index+1);
          speed_voltage=payload.substring(rate_index+1,speed_index).toInt();
          torque_voltage=payload.substring(speed_index+1,torque_index).toInt();
          turns_value=payload.substring(torque_index+1,turns_index).toInt();

          Serial.print("Speed : ");
          Serial.println(speed_voltage);
          Serial.print("Torque : ");
          Serial.println(torque_voltage);
          Serial.print("Turns  : ");
          Serial.println(turns_value);

          Wiper0 = map(speed_voltage, 0, 1000, 0, 255);
          Serial.print("Speed set(wemos) : ");
          Serial.print(speed_voltage / 100);
          Serial.println();
          resister_set();
          delay(50);
          String data2 = "tq" + (String) torque_voltage;
          arduinoSerial.print(data2);
          delay(50);
          String data1 = "ts" + (String) turns_value;
          arduinoSerial.print(data1);     
          Serial.println("preset send");
          }
      } else {
        Serial.printf("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
      }
      http.end();
    } else {
      Serial.printf("[HTTP} Unable to connect\n");
    }
}


void callback(char* topic, byte* payload, unsigned int length) {
  Serial.println();
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  dataIn = "";
  for (int i = 0; i < length; i++) {
    dataIn += (char)payload[i];
  }
  Serial.print(dataIn);
  Serial.println();
  if (dataIn.startsWith("sp")) {
    dataIn = dataIn.substring(2);
    speed_voltage = dataIn.toInt();
    Wiper0 = map(speed_voltage, 0, 1000, 0, 255);
    Serial.print("Speed set(wemos) : ");
    Serial.print(speed_voltage / 100);
    Serial.println();
    resister_set();
  }
  else if (dataIn.startsWith("tq")) {
    arduinoSerial.print(dataIn);
    dataIn = dataIn.substring(2);
    torque_voltage = dataIn.toInt();
    Serial.print("Torque set(nano) : ");
    Serial.print(torque_voltage / 100);
    Serial.println();
  }
  else if (dataIn.startsWith("ts")) {
    arduinoSerial.print(dataIn);
    dataIn=dataIn.substring(2);
    turns_value = dataIn.toInt();
    Serial.print("Turns set : ");
    Serial.print(turns_value);
    Serial.println();

  }
  else if (dataIn.startsWith("go")) {
    arduinoSerial.print(dataIn);

    Serial.print("Go");
    Serial.println();
    pubdata = "s";
    // strcpy(msg,pubdata.c_str());
    client.publish(outTopic, pubdata);
    clear_msg();
  }
  else if (dataIn.startsWith("no")) {
    arduinoSerial.print(dataIn);

    Serial.print("Stop");
    Serial.println();
  }

  
  dataIn = "";
}

void reconnect() {
  char idbuf[50];
  String geneid;
  char testsend[75];
  for (int k = 0; k <= 32; k++)
  {
    geneid += random(1, 9);
  }
  geneid.toCharArray(idbuf, geneid.length());
  sprintf(testsend, "TAB Mqtt : %s", idbuf);
  const char* mqttid = testsend;

  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect(mqttid, mqtt_username, mqtt_password)) {
      Serial.print("connected with id : ");
      Serial.println(idbuf);
      // Once connected, publish an announcement...
      client.publish(outTopic, mqttid);
      client.publish("test_connection", mqttid);
      // ... and resubscribe
      client.subscribe(inTopic);
      Http_get();
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      wifi_check();
      delay(5000);
    }
  }
}



void resister_set() {

  if (Wiper0 != Wiper0_pre) // If Wiper value is changed
  {
    //SPI communication
    digitalWrite(cspin, LOW);
    SPI.transfer(0x00);    // Write Data to Wiper0 register(address: 0x0)
    SPI.transfer(Wiper0);  // Value of Wiper0 register
    digitalWrite(cspin, HIGH);
    Serial.print("Resister step set : ");
    Serial.println(Wiper0);
    Serial.println();
    Wiper0_pre = Wiper0;
  }

}
