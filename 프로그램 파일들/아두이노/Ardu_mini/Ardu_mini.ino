#include<SoftwareSerial.h>
SoftwareSerial arduinoSerial (2, 3); // 2,3번 핀
#include <SPI.h>    // 10, 11, 12 ,13번 핀

char msg[50];
String dataIn = "";
int cspin = 10; //arduino nano
int turn_pin1 = 4;
int turn_pin2 = 5;
int turn_pin3 = 6;
int turn_pin4 = 7;
int state_pin1 = 8;
int state_pin2 = 9;
int pin = A6; //나머지 0.5바퀴 남았다는 입력 핀

int start_pin = A1;
int stop_pin = A2;
int turn_up_pin = A3;
int turn_down_pin = A4;
int turn_reset_pin = A5;


int pin1_state = 0;
int pin2_state = 0;
int pin3_state = 0;
int pin4_state = 0;
int pin5_state = 0;
int pin5_state_old = 0;
int pin6_state = 0;
int old_f = 0;

float torque_voltage = 500;
float speed_voltage = 500;
int turns_value = 1;
int turns_value_pre = 1;

int Wiper0 = 127;     // Wiper register
int Wiper0_pre = 0; // Previous Wiper register

void turns_set();
void check_state();
void clear_msg();
void break_msg();
void resister_set();

void setup() {
  Serial.begin(115200);
  arduinoSerial.begin(38400);
  pinMode(turn_pin1, INPUT);
  pinMode(turn_pin2, INPUT);
  pinMode(turn_pin3, INPUT);
  pinMode(turn_pin4, INPUT);
  pinMode(state_pin1, INPUT);
  pinMode(state_pin2, INPUT);
  pinMode(start_pin, OUTPUT);
  pinMode(stop_pin, OUTPUT);
  pinMode(turn_up_pin, OUTPUT);
  pinMode(turn_down_pin, OUTPUT);
  pinMode(turn_reset_pin, OUTPUT);
  digitalWrite(start_pin, LOW);
  digitalWrite(stop_pin, LOW);
  digitalWrite(turn_up_pin, LOW);
  digitalWrite(turn_down_pin, LOW);
  digitalWrite(turn_reset_pin, LOW);

  pinMode(cspin, OUTPUT);    // Pin 24 OUTPUT, Slave Select(SS)
  digitalWrite(cspin, HIGH); // SS High
  SPI.begin();            // Initialize SPI(Default: 4MHz, Mode 0)
  Serial.println("start am");
  arduinoSerial.print("hello wemos");
}
void loop() {
  if (Serial.available()) {
    int k = 0;
    while (Serial.available()) {
      char myChar = (char)Serial.read();
      msg[k] = myChar;
      k++;
      delay(5);
    }
    arduinoSerial.write(msg);
    Serial.println("send ");
    clear_msg();
  }

  if (arduinoSerial.available()) {
    int k = 0;
    while (arduinoSerial.available()) {
      char myChar = (char)arduinoSerial.read();
      msg[k] = myChar;
      dataIn += myChar;
      k++;
      delay(5);
    }
    Serial.print("message : ");
    Serial.println(msg);
    break_msg();
    clear_msg();
  }
  check_state();
  delay(10);
}


void clear_msg() {
  char buf[50] = "";
  for (int k = 0; k < 50; k++) {
    msg[k] = buf[k] ;
  }
}


void break_msg() {

  if (dataIn.startsWith("sp")) {
    dataIn = dataIn.substring(2);
    speed_voltage = dataIn.toInt();
    Serial.print("Speed set : ");
    Serial.print(speed_voltage / 100);
    Serial.println();
  }
  else if (dataIn.startsWith("tq")) {
    dataIn = dataIn.substring(2);
    torque_voltage = dataIn.toInt();
    Wiper0 = map(torque_voltage, 0, 1000, 0, 255);
    Serial.print("Torque set : ");
    Serial.print(torque_voltage / 100);
    Serial.println();
    resister_set();
  }
  else if (dataIn.startsWith("ts")) {
    dataIn = dataIn.substring(2);
    turns_value = dataIn.toInt();
    //    turns_value=map(turns_value,0,1000,1,17);
    Serial.print("Turns set : (now)");
    Serial.print(turns_value);
    Serial.print(" vs (pre)");
    Serial.print(turns_value_pre);
    Serial.println();
    turns_set();
  }
  else if (dataIn.startsWith("reset")) {
    turns_value = 1;
    digitalWrite(turn_reset_pin, HIGH);
    Serial.print("Turns reset : ");
    Serial.print(turns_value);
    Serial.println();
    delay(100);
    digitalWrite(turn_reset_pin, LOW);
  }
  else if (dataIn.startsWith("go")) {
    digitalWrite(start_pin, HIGH);
    Serial.print("Go");
    Serial.println();
    delay(100);
    digitalWrite(start_pin, LOW);
  }
  else if (dataIn.startsWith("no")) {
    digitalWrite(stop_pin, HIGH);
    Serial.print("Stop");
    Serial.println();
    delay(100);
    digitalWrite(stop_pin, LOW);
  }
  dataIn = "";
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


void turns_set() {
  if (turns_value_pre < turns_value) {
    int steps = turns_value - turns_value_pre;
    Serial.print("Turns step up : ");
    Serial.println(steps);
    for (int i = 0; i < steps; i++) {
      digitalWrite(turn_up_pin, HIGH);
      delay(200);
      digitalWrite(turn_up_pin, LOW);
      delay(200);
    }
    turns_value_pre = turns_value;
  } else if (turns_value_pre > turns_value) {
    int steps = turns_value_pre - turns_value;
    Serial.print("Turns step down : ");
    Serial.println(steps);
    for (int i = 0; i < steps; i++) {
      digitalWrite(turn_down_pin, HIGH);
      delay(200);
      digitalWrite(turn_down_pin, LOW);
      delay(200);
    }
    turns_value_pre = turns_value;
  }
}

void check_state() {

  pin1_state = digitalRead(turn_pin1);
  pin2_state = digitalRead(turn_pin2);
  pin3_state = digitalRead(turn_pin3);
  pin4_state = digitalRead(turn_pin4);
  pin5_state = digitalRead(state_pin1);
  pin6_state = digitalRead(state_pin2);

int  f = analogRead(A6);
if(f>800){
  f=LOW;
  }else{
    f=HIGH;
    }
  if (f == HIGH && old_f == LOW) {
    digitalWrite(cspin, LOW);
    SPI.transfer(0x00);
    SPI.transfer(50);
    digitalWrite(cspin, HIGH);
    Serial.println("last 1");
  } else if (old_f == HIGH && f == LOW) { 
    digitalWrite(cspin, LOW);
    SPI.transfer(0x00);   
    SPI.transfer(Wiper0);  
    digitalWrite(cspin, HIGH);
    Serial.print("Resister step set : ");
    Serial.println(Wiper0);
    Serial.println();
    Serial.println("end");
  }
//  Serial.print(pin5_state);
//  Serial.println(pin5_state_old);
if(pin5_state==HIGH && pin5_state_old == LOW){
  if(pin6_state==LOW){
  Serial.println("normal");
  }
  if(pin6_state==HIGH){
    Serial.println("error");
    }  
  }
old_f = f;   //old f값
pin5_state_old = pin5_state;
}
