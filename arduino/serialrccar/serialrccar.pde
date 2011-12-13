const int rightPin = 8;
const int forwardPin = 9;
const int backPin = 10;
const int leftPin = 11;
const int ledPin = 13;

unsigned long previousTimeMs = 0;
int fwdPulseMs = 100;
int fwdPulseLowMs = 500;
int fwdPulseHighMs = 250;

int left = LOW;
int right = LOW;
int forward = LOW;
int back = LOW;
int error = LOW;
int forwardPulse = LOW;

void setup() {
  Serial.begin(9600);
  pinMode(forwardPin, OUTPUT);
  pinMode(backPin, OUTPUT);
  pinMode(leftPin, OUTPUT);
  pinMode(rightPin, OUTPUT);
  pinMode(ledPin, OUTPUT);
}

void loop() {
  
  unsigned long now = millis();

  if (now - previousTimeMs > fwdPulseMs) {
    if (forward == HIGH) {
      if (forwardPulse == HIGH) {
        forwardPulse = LOW;
        fwdPulseMs = fwdPulseLowMs;
      } else {
        forwardPulse = HIGH;
        fwdPulseMs = fwdPulseHighMs;
      }
      digitalWrite(forwardPin, forwardPulse);
    }
    previousTimeMs = now;
  }
  
  int incomingByte;

  if (Serial.available() > 0) {
    incomingByte = Serial.read();

    left = right = forward = back = LOW;
    if (incomingByte & 0x01) {
      left = HIGH;
    }
    if (incomingByte & 0x02) {
      right = HIGH;
    }
    if (incomingByte & 0x04) {
      forward = HIGH;
    }
    if (incomingByte & 0x08) {
      back = HIGH;
    }
   
    if ((left == HIGH && right == HIGH) ||
        (forward == HIGH && back == HIGH)) {
      left = LOW;
      right = LOW;
      forward = LOW;
      back = LOW;
      error = HIGH;
    }

    digitalWrite(leftPin, left);
    digitalWrite(rightPin, right);
    if (forward == LOW) {
      digitalWrite(forwardPin, forward);
    }
    digitalWrite(backPin, back);
    digitalWrite(ledPin, error);
  }
}
