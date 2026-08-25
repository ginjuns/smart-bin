enum rainbow { red, green, blue };
void rb(int color) {
  switch (color) {
    case red:
      digitalWrite(redpin, HIGH);
      digitalWrite(greenpin, LOW);
      digitalWrite(bluepin, LOW);
      break;

    case green:
      digitalWrite(redpin, LOW);
      digitalWrite(greenpin, HIGH);
      digitalWrite(bluepin, LOW);
      break;

    case blue:
      digitalWrite(redpin, LOW);
      digitalWrite(greenpin, LOW);
      digitalWrite(bluepin, HIGH);
      break;
  }
}

void setup_volume() {
  pinMode(volume_trigPin, OUTPUT);
  pinMode(volume_echoPin, INPUT);
  pinMode(redpin, OUTPUT);
  pinMode(greenpin, OUTPUT);
  pinMode(bluepin, OUTPUT);
  pinMode(lowSensor, INPUT);
  pinMode(midSensor, INPUT);
  pinMode(topSensor, INPUT);
}

void volume() {
  if (shacking == 0) {
    volume_distance = readVolumeDistance();

    if (volume_distance != 999) {
      int filledPercent =((totalHeight - volume_distance) * 100) /totalHeight;

      Serial.print("거리 : ");
      Serial.print(volume_distance);
      Serial.print("cm | 적재량 : ");
      Serial.print(filledPercent);
      Serial.println("%");
    }
    delay(200);
  }

  if(trash_level == 0 || trash_level == 1){
    rb(blue);
    if(trash_level == 0){
      volume1=0;
    }
    else if(trash_level == 1){
      volume1=10;
    }
  }
  else if(trash_level == 2){
    rb(green);
    volume1=7;
  }
  else if(trash_level == 3) {
    rb(red);
    volume1=5;
  }
}

int readVolumeDistance() {
  int values[5];
  for (int i = 0; i < 5; i++) {
    digitalWrite(volume_trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(volume_trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(volume_trigPin, LOW);
    long duration = pulseIn(volume_echoPin, HIGH, 30000);
    if (duration == 0) {
      values[i] = 999;
    }
    else {
      values[i] = duration * 0.034 / 2;
    }
    delay(20);
  }

  for (int i = 0; i < 4; i++) {
    for (int j = i + 1; j < 5; j++) {
      if (values[i] > values[j]) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;
      }
    }
  }
  return values[2];
}

void volume_level(){
  if(digitalRead(topSensor)==HIGH){
    trash_level=3;
  }
  else if(digitalRead(midSensor)==HIGH){
    trash_level=2;
  }
  else if(digitalRead(lowSensor)==HIGH){
    trash_level=1;
  }
  else{
    trash_level=0;
  }
}
