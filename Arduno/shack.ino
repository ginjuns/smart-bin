void setup_shake() {
  shakerServo.attach(6);
  shakerServo.write(75);
}

// void shake() {
//   if ((volume_distance > 0) && (volume_distance <= totalHeight)) {
//     int filledPercent = ((totalHeight - volume_distance) * 100) / totalHeight;

//     Serial.print("거리: ");
//     Serial.print(volume_distance);
//     Serial.print("cm | 현재 적재량: ");
//     Serial.print(filledPercent);
//     Serial.println("%");

//     if (filledPercent >= 70 && cnt == 1) {
//       shacking = 1;
//       Serial.println(">> [70% 적재] 최초 감지! 2번 흔듭니다.");
//       cnt++;
//       shakeTrashCan();
//     }
//     else if (filledPercent >= 40 && filledPercent < 70 && cnt == 0) {
//       shacking = 1;
//       Serial.println(">> [40% 적재] 최초 감지! 2번 흔듭니다.");
//       cnt++;
//       shakeTrashCan();
//     }

//     if (filledPercent < 20) {
//       Serial.println("휴지통이 [20% 이하] 입니다.");
//     }
//   }
//   delay(1000);
// }
void shake(){
  Serial.print("trash_level = ");
  Serial.print(trash_level);
  Serial.print("  cnt = ");
  Serial.println(cnt);

  if (trash_level == 2 && cnt <= 0) {
    shacking = 1;
    Serial.println(">> [50%] 흔들기");
    cnt++;
    shakeTrashCan();
  }
  else if (trash_level == 3 && (cnt >= 0 && cnt <=1)) {
    shacking = 1;
    Serial.println(">> [80%] 흔들기");
    cnt++;
    shakeTrashCan();
  }
  else if (trash_level == 0) {
    cnt = 0;
  }
  delay(1000);
}

void shakeTrashCan() {
  for (int i = 0; i < 2; i++) {
    shakerServo.write(55);
    if (smartDelay(400)) {
      shacking = 0;
      shakerServo.write(75);
      return;
    }

    shakerServo.write(75);

    if (smartDelay(400)) {
      shacking = 0;
      shakerServo.write(80);
      return;
    }
  }

  shakerServo.write(80);
  delay(500);
  Serial.println("흔들기 2회 완료. 센서 안정화 대기...");
  shacking = 0;
}
