volatile unsigned long lastInterruptTime = 0;
volatile bool buttonInterruptPending = false;

void setup_interrupt() {
  pinMode(buttonPin, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(buttonPin), buttonISR, FALLING);
}

void buttonISR() {
  buttonInterruptPending = true;
}

void checkButton() {
  static bool lastButtonState = true;
  bool buttonState = digitalRead(buttonPin);

  if (buttonInterruptPending || (lastButtonState == true && buttonState == false)) {
    buttonInterruptPending = false;
    handleButtonPress();
  }
  lastButtonState = buttonState;
}

void handleButtonPress() {
  unsigned long interruptTime = millis();

  if (interruptTime - lastInterruptTime < 250) {
    return;
  }
  lastInterruptTime = interruptTime;
  if (!buttonOverride) {
    buttonOverride = true;
    commend = 2;
  }
  else {
    buttonOverride = false;
    commend = 3;
  }
}

bool smartDelay(unsigned long ms) {
  unsigned long start = millis();

  while (millis() - start < ms) {
    checkButton();
    if (buttonOverride || commend == 2) {
      return true;
    }
    delay(10);
  }
  return false;
}
