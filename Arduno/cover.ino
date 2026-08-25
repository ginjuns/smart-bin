void setup_cover() {
  pinMode(cover_trigPin, OUTPUT);
  pinMode(cover_echoPin, INPUT);
  coverServo.attach(9);
  coverServo.write(90);
  cover_state=0;
}

void cover() {
  if (espSerial.available()) {
    String angle=espSerial.readStringUntil('\n');
    angle.trim();

    if (angle == "SERVO_CLOSE") {
      espSerial.println("SERVO_CLOSE OK");
      Serial.println("SERVO_CLOSE OK");
      coverServo.write(90);
      cover_state=0;
      commend=0;
    }
    else if (angle == "SERVO_OPEN") {
      espSerial.println("SERVO_OPEN OK");
      Serial.println("SERVO_OPEN OK");
      coverServo.write(0);
      cover_state=1;
      open_reason=1; // 추가: 앱 수동 열기
      commend=1;
    }
  }

  if (commend == 0) {
    digitalWrite(cover_trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(cover_trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(cover_trigPin, LOW);

    cover_duration = pulseIn(cover_echoPin, HIGH, 30000);
    if (cover_duration == 0) {
      return;
    }

    cover_distance = cover_duration * 0.034 / 2;
    if (cover_distance != cover_lastdistance) {
      cover_lastdistance = cover_distance;
    }
    if (cover_distance <= 20) {
      if (trash_level == 3) {
        return;
      }
      if (cover_state == 0) {
        Serial.println("OPEN");
        coverServo.write(0);
        cover_state=1;
        open_reason=0; // 추가: 자동 열기

        if (smartDelay(5000)) {
          return;
        }
      }
    }
    else if (cover_state == 1) {
      Serial.println("CLOSE");
      coverServo.write(90);
      cover_state=0;
    }
    delay(200);
  }
}
