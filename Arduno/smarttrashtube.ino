#include <SoftwareSerial.h>
#include <Servo.h>

#define redpin A0
#define greenpin A1
#define bluepin A2
#define lowSensor 3
#define midSensor 4
#define topSensor 5

SoftwareSerial espSerial(7, 8);

Servo coverServo;
Servo shakerServo;

// 초음파 핀
const int cover_trigPin=10;
const int cover_echoPin=11;
const int volume_trigPin=12;
const int volume_echoPin=13;

// 인터럽트 버튼
const int buttonPin=2;

long cover_duration;
long volume_duration;
int cover_distance;
int volume_distance;
int cover_lastdistance=-1;
int last_trash_level=-1;
int last_cover_state=-1;
int commend=0; // 0 자동, 1 앱 수동, 2 버튼 강제개방, 3 버튼 초기화
bool buttonOverride=false;
int cover_state=0;
int cnt=0;
int totalHeight=10;
int shacking=0;
int trash_level=0;
int volume1=0;
int open_reason = 0; // 0=자동, 1=앱수동, 2=버튼

void setup() {
  Serial.begin(9600);
  espSerial.begin(9600);
  setup_cover();
  setup_volume();
  setup_shake();
  setup_interrupt();
}

void loop() {
  checkButton();

  if (commend == 2) {
    coverServo.write(0);
    shakerServo.write(80);
    cover_state=1;
    open_reason=2; // 추가: 버튼 강제 열기
    shacking=0;
    Serial.println("Button override: open and pause");
    commend=0;
  }

  if (buttonOverride) {
    delay(100);
    return;
  }

  if (commend == 3) {
    resetTrashCan();
    Serial.println("Button override: reset and resume");
  }

  cover();

  if (buttonOverride) {
    delay(100);
    return;
  }

  volume_level();
  volume();

  if ((trash_level != last_trash_level) || (cover_state != last_cover_state)) {
    sendData();
    if (cover_state == 0) {
      shake();
    }
    last_trash_level = trash_level;
    last_cover_state = cover_state;
  }
}

void sendData() {
  Serial.print("SEND : ");
  Serial.print(volume1);
  Serial.print(",");
  Serial.println(cover_state);
  Serial.print(",");
  Serial.println(open_reason);

  espSerial.print(volume1);
  espSerial.print(",");
  espSerial.println(cover_state);
  espSerial.print(",");
  espSerial.println(open_reason);
}

void resetTrashCan() {
  coverServo.write(90);
  shakerServo.write(80);
  cover_state=0;
  shacking=0;
  cnt=0;
  trash_level=0;
  cover_lastdistance=-1;
  last_trash_level=-1;
  last_cover_state=-1;
  commend=0;
}
